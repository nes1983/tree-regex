package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
  }

  TNFA makeTheNFA() {
    State.resetCount();

    final State s0 = State.get();
    final State s1 = State.get();
    final State s2 = State.get();

    final TNFA tnfa = mock(TNFA.class);

    final Tag t0 = mock(Tag.class);
    final CaptureGroup cg = mock(CaptureGroup.class);

    when(cg.getNumber()).thenReturn(0);

    when(t0.toString()).thenReturn("t0");
    when(t0.getGroup()).thenReturn(cg);

    when(tnfa.allInputRanges()).thenReturn(Arrays.asList(InputRange.make('a')));
    when(tnfa.getInitialState()).thenReturn(s0);
    when(tnfa.allTags()).thenReturn(Arrays.asList(Tag.NONE));
    when(tnfa.availableTransitionsFor(eq(s0), isNull(Character.class))).thenReturn(
        Arrays.asList(new TransitionTriple(s1, Priority.NORMAL, t0), new TransitionTriple(s0,
            Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
        Arrays.asList(new TransitionTriple(s0, Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(s1, 'a')).thenReturn(
        Arrays.asList(new TransitionTriple(s2, Priority.NORMAL, Tag.NONE), new TransitionTriple(s1,
            Priority.LOW, Tag.NONE)));
    when(tnfa.availableTransitionsFor(s2, 'a')).thenReturn(new ArrayList<TransitionTriple>());
    when(tnfa.isAccepting(eq(s2))).thenReturn(true);
    when(tnfa.getFinalStates()).thenReturn(new HashSet<>(Arrays.asList(s2)));
    when(tnfa.isAccepting(eq(s1))).thenReturn(false);
    when(tnfa.isAccepting(eq(s0))).thenReturn(false);
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
