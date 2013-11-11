package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TransitionTriple.Priority;

/** Testing {@link TDFAInterpreter} */
@SuppressWarnings("javadoc")
public class TDFAInterpreterTest {
  private TDFAInterpreter interpreter;

  @Before
  public void setUp() throws Exception {
    interpreter = new TDFAInterpreter(new TNFAToTDFA(makeTheNFA()));
    History.resetCount();
  }

  private TNFA makeTheNFA() {
    State.resetCount();

    final State s0 = State.get();
    final State s1 = State.get();
    final State s2 = State.get();

    final TNFA tnfa = mock(TNFA.class);

    final Tag t0Open = mock(Tag.class);
    final Tag t0Close = mock(Tag.class);
    final CaptureGroup cg = mock(CaptureGroup.class);

    when(t0Open.toString()).thenReturn("t0↑");
    when(t0Open.getGroup()).thenReturn(cg);
    when(t0Open.isStartTag()).thenReturn(true);
    when(t0Close.toString()).thenReturn("t0↓");
    when(t0Close.getGroup()).thenReturn(cg);
    when(t0Close.isEndTag()).thenReturn(true);

    when(tnfa.allInputRanges()).thenReturn(Arrays.asList(InputRange.make('a')));
    when(tnfa.getInitialState()).thenReturn(s0);
    when(tnfa.allTags()).thenReturn(Arrays.asList(t0Open, t0Close));
    when(tnfa.availableTransitionsFor(eq(s0), isNull(Character.class))).thenReturn(
        Arrays.asList(new TransitionTriple(s1, Priority.NORMAL, t0Open), new TransitionTriple(s0,
            Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
        Arrays.asList(new TransitionTriple(s0, Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(s1, 'a')).thenReturn(
        Arrays.asList(new TransitionTriple(s2, Priority.NORMAL, t0Close), new TransitionTriple(s1,
            Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(s2, 'a')).thenReturn(Collections.<TransitionTriple> emptyList());
    when(tnfa.isAccepting(eq(s2))).thenReturn(Boolean.TRUE);
    when(tnfa.getFinalState()).thenReturn(s2);
    when(tnfa.isAccepting(eq(s1))).thenReturn(Boolean.FALSE);
    when(tnfa.isAccepting(eq(s0))).thenReturn(Boolean.FALSE);
    return tnfa;
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
    final MatchResult matcher = interpreter.interpret("b");
    assertThat(interpreter.tdfaBuilder.build().toString(), is(""));
    // Input falls outside of supported input ranges, so this is legal.
    assertThat(matcher.toString(), is("NO_MATCH"));
  }


  @Test
  public void testMatch() {
    final MatchResult matchResult = interpreter.interpret("aaaaaa");
    assertThat(matchResult.toString(), is("4-0"));
  }
}
