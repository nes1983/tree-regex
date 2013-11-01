package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;

@SuppressWarnings("javadoc")
public final class IntegrationTest {
  TDFAInterpreter tdfaInterpreter;

  @Before
  public void setUp() {
    State.resetCount();
    History.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse("(((a+)b)+c)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

      assertThat(
          tnfa.toString(),
          is("q0 -> q14, {(q0, ANY)=[q0, NORMAL, NONE], "
              + "(q0, ε)=[q1, NORMAL, ➀0], "
              + "(q1, ε)=[q2, NORMAL, ➀1], "
              + "(q2, ε)=[q3, NORMAL, ➀2], "
              + "(q3, ε)=[q4, NORMAL, ➀3], "
              + "(q4, a-a)=[q5, NORMAL, NONE], "
              + "(q5, ε)=[q6, LOW, NONE, q4, NORMAL, NONE], "
              + "(q6, ε)=[q7, NORMAL, ➁3], "
              + "(q7, b-b)=[q8, NORMAL, NONE], "
              + "(q8, ε)=[q9, NORMAL, ➁2], "
              + "(q9, ε)=[q10, LOW, NONE, q2, NORMAL, NONE], "
              + "(q10, c-c)=[q11, NORMAL, NONE], "
              + "(q11, ε)=[q12, NORMAL, ➁1], "
              + "(q12, ε)=[q13, LOW, NONE, q1, NORMAL, NONE], "
              + "(q13, ε)=[q14, NORMAL, ➁0]}"));
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is("q0-a-a -> q1 [11->12, 7->13, 13<- pos, c↑(12), c↓(13)]\n"
        + "q1-a-a -> q1 [11->14, 7->15, 15<- pos, c↑(14), c↓(15), 14->12, 15->13]\n"
        + "q1-b-b -> q2 [10->16, 5->17, 17<- pos, c↑(16), c↓(17), 16->18, 18<- pos+1, 12->19, 19<- pos+1]\n"
));
  }

  @Test
  public void shouldNotMatchTwoBs() {
    final MatchResult res = tdfaInterpreter.interpret("aabbc");
    assertThat(res.toString(), is("NO_MATCH"));
  }

  @Test
  public void testMatch2() {
    final MatchResult res = tdfaInterpreter.interpret("aaabcaaabcaabc");
    assertThat(res.toString(), is("0-13"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(),
      is("q0-a-a -> q1 [11->12, 7->13, 13<- pos, c↑(12), c↓(13)]\n"
          + "q1-a-a -> q1 [11->14, 7->15, 15<- pos, c↑(14), c↓(15), 14->12, 15->13]\n"
          + "q1-b-b -> q2 [10->16, 5->17, 17<- pos, c↑(16), c↓(17), 16->18, 18<- pos+1, 12->19, 19<- pos+1]\n"
          + "q2-c-c -> q3 [9->20, 3->21, 21<- pos, c↑(20), c↓(21), 20->22, 22<- pos+1, 16->23, 23<- pos+1, 12->24, 24<- pos+1, 8->25, 1->26, 26<- pos, c↑(25), c↓(26)]\n"
          + "q3-a-a -> q1 [24->27, 13->28, 28<- pos, c↑(27), c↓(28), 22->9, 21->3, 23->10, 17->5, 24->11, 13->7, 27->12]\n"

      ));
  }

  @Test
  public void testMemoryAfterExecution() {
    RealMatchResult res = (RealMatchResult) tdfaInterpreter.interpret("aaabcaaabcaabc");
    assertThat(Arrays.toString(res.captureGroupPositions), is("[25(0 0 ), "
        + "26(13 13 ), "
        + "20(10 10 5 0 ), "
        + "21(13 13 9 4 ), "
        + "16(10 10 5 0 ), "
        + "17(12 12 8 3 ), "
        + "12(10 10 5 0 ), "
        + "13(11 11 7 2 )]"));
  }

  @Test
  public void testGroupMatch() {
    MatchResult result = tdfaInterpreter.interpret("aaabcaaabcaabc");
    assertThat(result.start(),  is(0));
    assertThat(result.end(),    is(13));
    assertThat(result.start(1), is(10));
    assertThat(result.end(1),   is(13));
    assertThat(result.start(2), is(10));
    assertThat(result.end(2),   is(12));
    assertThat(result.start(3), is(10));
    assertThat(result.end(3),   is(11));
  }
}
