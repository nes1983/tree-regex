package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;

@SuppressWarnings("javadoc")
public final class MatchingTest {
  private TDFAInterpreter makeInterpreter(String regex) {
    final Regex parsed = new ParserProvider().regexp().parse(regex);
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    return new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Before
  public void setUp() {
    State.resetCount();
    History.resetCount();
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
        is("q0-a-a -> q1 []\nq1-b-b -> q2 [2->3, 1->4, 4<- pos, c↑(3), c↓(4)]\n"));
    assertThat(result.toString(), is("NO_MATCH"));
  }
}