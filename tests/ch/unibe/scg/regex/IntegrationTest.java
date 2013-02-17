package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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

    assertThat(tnfa.toString(),
        is("q0 -> [q11], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ε)=[q2, NORMAL, ➀1], (q2, ε)=[q3, NORMAL, ➀2], (q3, ε)=[q4, NORMAL, ➀3],"
            + " (q4, a-a)=[q5, NORMAL, NONE], (q5, ε)=[q4, NORMAL, NONE, q6, NORMAL, ➁3], "
            + "(q6, b-b)=[q7, NORMAL, NONE], (q7, ε)=[q8, NORMAL, ➁2, q3, NORMAL, NONE], "
            + "(q8, c-c)=[q9, NORMAL, NONE], (q9, ε)=[q10, NORMAL, ➁1, q2, NORMAL, NONE], "
            + "(q10, ε)=[q11, NORMAL, ➁0]}"));
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is("q0-a-a -> q1 [4<- pos]\n"
        + "q1-a-a -> q1 [5<- pos]\n" + "q1-b-b -> q2 [6<- pos, 7<- pos]\n"));
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
        is("q0-a-a -> q1 [4<- pos]\nq1-a-a -> q1 [5<- pos]\n" + "q1-b-b -> q2 [6<- pos, 7<- pos]\n"
            + "q2-c-c -> q3 [8<- pos, 9<- pos, 10<- pos, 11<- pos]\n" + "q3-a-a -> q1 [12<- pos]\n"));
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
        is("q0-a-a -> q1 []\nq1-b-b -> q2 [1<- pos]\n"));
    assertThat(result.toString(), is("NO_MATCH"));
  }
}
