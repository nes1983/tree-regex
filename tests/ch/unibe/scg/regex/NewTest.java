package ch.unibe.scg.regex;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TNFAToTDFA.DFAState;
import ch.unibe.scg.regex.TransitionTriple.Priority;

@SuppressWarnings("javadoc")
public final class NewTest {
  TNFAToTDFA nfa2dfa;
  State s0, s1, s2;
  Tag t0;
  TNFA tnfa;

  TNFA makeTheNFA() {
    State.resetCount();

    s0 = State.get();
    s1 = State.get();
    s2 = State.get();

    final TNFA ret = mock(TNFA.class);

    t0 = mock(Tag.class);
    final CaptureGroup cg = mock(CaptureGroup.class);

    when(cg.getNumber()).thenReturn(0);

    when(t0.toString()).thenReturn("t0");
    when(t0.getGroup()).thenReturn(cg);
    when(t0.isStartTag()).thenReturn(true);

    when(ret.allInputRanges()).thenReturn(Arrays.asList(InputRange.make('a')));
    when(ret.getInitialState()).thenReturn(s0);
    when(ret.availableTransitionsFor(eq(s0), isNull(Character.class))).thenReturn(
        Arrays.asList(new TransitionTriple(s1, Priority.NORMAL, t0), new TransitionTriple(s0,
            Priority.NORMAL, Tag.NONE)));
    when(ret.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
        Arrays.asList(new TransitionTriple(s0, Priority.NORMAL, Tag.NONE)));
    when(ret.availableTransitionsFor(s1, 'a')).thenReturn(
        Arrays.asList(new TransitionTriple(s2, Priority.NORMAL, Tag.NONE), new TransitionTriple(s1,
            Priority.NORMAL, Tag.NONE)));
    when(ret.availableTransitionsFor(s2, 'a')).thenReturn(new ArrayList<TransitionTriple>());
    when(ret.isAccepting(eq(s2))).thenReturn(Boolean.TRUE);
    when(ret.isAccepting(eq(s1))).thenReturn(Boolean.FALSE);
    when(ret.isAccepting(eq(s0))).thenReturn(Boolean.FALSE);
    when(ret.allTags()).thenReturn(Arrays.asList(t0));
    return ret;
  }

  @Before
  public void setUp() {
    tnfa = makeTheNFA();
    nfa2dfa = TNFAToTDFA.make(tnfa);
  }

  @Test
  public void testInitialState() {
    assertEquals("[t0]", tnfa.allTags().toString());
    final DFAState converted = nfa2dfa.makeStartState().dfaState;
    assertEquals("q0->[-1, -2], q1->[0, -2]", converted.toString());
  }
}
