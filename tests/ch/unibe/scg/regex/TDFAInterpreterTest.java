package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

/** Testing {@link TDFAInterpreter} */
@SuppressWarnings("javadoc")
public class TDFAInterpreterTest {
  private TDFAInterpreter interpreter;

  @Before
  public void setUp() throws Exception {
    interpreter = new TDFAInterpreter(new TNFAToTDFA(new IntegrationTest().makeTheNFA()));
  }

  @Test
  public void testBuiltAutomaton() {
    interpreter.interpret("aaaaaa");
    assertThat(interpreter.tdfaBuilder.build().toString(),
        is("q0-a-a -> q1 [1<- pos]\nq1-a-a -> q1 [1->0, 1<- pos]\n"));
  }

  @Test
  public void testInvalidString() {
    assertThat(interpreter.interpret("b"),
        is((MatchResult) RealMatchResult.NoMatchResult.SINGLETON));
  }

  @Test
  public void testInvalidStringCompiled() {
    interpreter.interpret("b");
    assertThat(interpreter.tdfaBuilder.build().toString(), is(""));
    // Input falls outside of supported input ranges, so this is legal.
  }


  @Test
  public void testMatch() {
    final MatchResult matchResult = interpreter.interpret("aaaaaa");
    assertThat(matchResult.toString(), is("4-0"));
  }
}
