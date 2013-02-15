package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;

@SuppressWarnings("javadoc")
public class IntegrationTest2 {
  TDFAInterpreter tdfaInterpreter;

  private TDFAInterpreter makeInterpreter(String regex) {
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
        is("q0 -> [q9], {(q0, ANY)=[q0, NORMAL, NONE, q1, NORMAL, ➀1], (q1, ε)=[q2, NORMAL, ➀2], "
            + "(q2, ε)=[q3, NORMAL, ➀3], (q3, a-a)=[q4, NORMAL, NONE], "
            + "(q4, ε)=[q3, NORMAL, NONE, q5, NORMAL, ➁3], (q5, b-b)=[q6, NORMAL, NONE], "
            + "(q6, ε)=[q7, NORMAL, ➁2, q2, NORMAL, NONE], (q7, c-c)=[q8, NORMAL, NONE], "
            + "(q8, ε)=[q9, NORMAL, ➁1, q1, NORMAL, NONE]}"));
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
    assertThat(tdfaInterpreter.tdfaBuilder.build().toString(),
        is("q0-a-a -> q1 [6<- pos]\nq1-a-a -> q1 []\nq1-b-b -> q2 [14<- pos]\n"
            + "q2-b-b -> q3 []\nq3-c-c -> q4 [22<- pos]\n"
            + "q4-c-c -> q5 []\nq5-a-a -> q6 [30<- pos]\nq6-a-a -> q1 []\n"));
  }

  @Test
  public void shouldMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbc");
    assertThat(res.toString(), is("0-4"));
    // assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is(""));
  }

  @Test
  public void testNoMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabc");
    assertThat(res.toString(), is("0-3"));
    // assertThat(tdfaInterpreter.tdfaBuilder.build().toString(), is(""));
  }

  @Test
  public void testTwoRangesAndOnePlus() {
    assertThat(makeInterpreter("a+b").interpret("ab").toString(), is(""));
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
    // TODO: NFA must contain tags for entire match.
    assertThat(makeInterpreter("ab").interpret("ab").toString(), is(""));
  }

  @Test
  public void testTwoRangesNoMatch() {
    assertThat(makeInterpreter("ab").interpret("aba").toString(), is("NO_MATCH"));
  }
}
