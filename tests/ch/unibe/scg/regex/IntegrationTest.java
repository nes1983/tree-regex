package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;

@SuppressWarnings("javadoc")
public class IntegrationTest {
  TDFAInterpreter tdfaInterpreter;

  private TDFAInterpreter makeInterpreter(String regex) {
    State.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse(regex);
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    return new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Before
  public void setUp() {
    State.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse("(((a+)b)+c)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    assertThat(
        tnfa.toString(),
        is("q0 -> [q15], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ε)=[q2, NORMAL, ➀1], (q2, ε)=[q3, NORMAL, ➀2], (q3, ε)=[q4, NORMAL, ➀3], "
            + "(q4, a-a)=[q5, NORMAL, NONE], (q5, ε)=[q4, NORMAL, NONE, q6, NORMAL, ➁3], "
            + "(q6, ε)=[q7, NORMAL, C3], (q7, b-b)=[q8, NORMAL, NONE], "
            + "(q8, ε)=[q9, NORMAL, ➁2, q3, NORMAL, NONE], (q9, ε)=[q10, NORMAL, C2], "
            + "(q10, c-c)=[q11, NORMAL, NONE], (q11, ε)=[q12, NORMAL, ➁1, q2, NORMAL, NONE], "
            + "(q12, ε)=[q13, NORMAL, C1], (q13, ε)=[q14, NORMAL, ➁0], (q14, ε)=[q15, NORMAL, C0]}"));
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is("q0-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
        + "q1-a-a -> q1 [c↑(3), 5<- pos, c↓(5)]\n"
        + "q1-b-b -> q2 [c↑(2), 6<- pos, 7<- pos, c↓(6)]\n"));
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
    assertThat(
        tdfaInterpreter.tdfaBuilder.build().toString(),
        // It's ok that stores start at 4. The previous 3 were stored in the initializer.
        is("q0-a-a -> q1 [c↑(3), 4<- pos, c↓(4)]\n"
            + "q1-a-a -> q1 [c↑(3), 5<- pos, c↓(5)]\n"
            + "q1-b-b -> q2 [c↑(2), 6<- pos, 7<- pos, c↓(6)]\n"
            + "q2-c-c -> q3 [c↑(1), c↑(0), 8<- pos, 9<- pos, 10<- pos, 11<- pos, c↓(8), c↓(10)]\n"
            + "q3-a-a -> q1 [c↑(11), 12<- pos, c↓(12)]\n"));
  }

  @Test
  public void testTwoRangesAndOnePlus() {
    assertThat(makeInterpreter("a+b").interpret("ab").toString(), is("0-1"));
  }

  @Test
  public void testTwoRangesAndOnePlusNoMatch() {
    assertThat(makeInterpreter("a+b").interpret("aba").toString(), is("NO_MATCH"));
  }

  @Test
  public void testTwoRangesAndTwoPlusNoMatch() {
    assertThat(makeInterpreter("a+b+").interpret("aba").toString(), is("NO_MATCH"));
  }

  @Test
  public void testTwoRangesMatch() {
    assertThat(makeInterpreter("ab").interpret("ab").toString(), is("0-1"));
  }

  @Test
  public void testTwoRangesNoMatch() {
    final TDFAInterpreter interpreter = makeInterpreter("ab");
    final MatchResult result = interpreter.interpret("aba");

    assertThat(interpreter.tdfaBuilder.build().toString(),
        is("q0-a-a -> q1 []\nq1-b-b -> q2 [c↑(0), 1<- pos, c↓(1)]\n"));
    assertThat(result.toString(), is("NO_MATCH"));
  }
}
