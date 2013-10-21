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

    if (false) {
      assertThat(
          tnfa.toString(),
          is("q0 -> [q14], "
              + "{(q0, ANY)=[q0, NORMAL, NONE], "
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
    }
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is("q0-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
        + "q1-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
        + "q1-b-b -> q2 [c↑(2), 6<- pos, 7<- pos, 8<- pos, c↓(6)]\n"));
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
      is("q0-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
          + "q1-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
          + "q1-b-b -> q2 [c↑(2), 6<- pos, 7<- pos, 8<- pos, c↓(6)]\n"
          + "q2-c-c -> q3 [c↑(1), c↑(0), 9<- pos, 10<- pos, 11<- pos, 12<- pos, 13<- pos, c↓(9), c↓(13)]\n"
          + "q3-a-a -> q1 [c↑(12), 14<- pos, c↓(14)]\n"
      ));
  }

  @Test
  public void testMemoryAfterExecution() {
    RealMatchResult res = (RealMatchResult) tdfaInterpreter.interpret("aaabcaaabcaabc");
    assertThat(Arrays.toString(res.captureGroupPositions), is(""));
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
