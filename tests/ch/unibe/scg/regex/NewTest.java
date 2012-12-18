package ch.unibe.scg.regex;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TNFAToTDFA.DFAState;

@SuppressWarnings("javadoc")
public final class NewTest {

  TNFAToTDFA nfa2dfa;

  State s0;
  State s1;
  State s2;
  Tag t0;
  TNFA tnfa;

  @SuppressWarnings({"unchecked", "rawtypes"})
  TNFA makeTheNFA() {
    State.resetCount();

    s0 = State.get();
    s1 = State.get();
    s2 = State.get();

    final TNFA tnfa = mock(TNFA.class);

    t0 = mock(Tag.class);

    when(t0.toString()).thenReturn("t0");
    when(t0.getGroup()).thenReturn(1);

    when(tnfa.allInputRanges()).thenReturn(Arrays.asList(InputRange.make('a')));
    when(tnfa.getInitialState()).thenReturn(s0);
    when(tnfa.availableTransitionsFor(eq(s0), isNull(Character.class))).thenReturn(
        Arrays.asList(new TransitionTriple(s1, 1, t0), new TransitionTriple(s0, 0, Tag.NONE)));
    when(tnfa.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
        Arrays.asList(new TransitionTriple(s0, 0, Tag.NONE)));
    when(tnfa.availableTransitionsFor(s1, 'a'))
        .thenReturn(
            Arrays.asList(new TransitionTriple(s2, 1, Tag.NONE), new TransitionTriple(s1, 0,
                Tag.NONE)));
    when(tnfa.availableTransitionsFor(s2, 'a')).thenReturn(new ArrayList());
    when(tnfa.isAccepting(eq(s2))).thenReturn(true);
    when(tnfa.isAccepting(eq(s1))).thenReturn(false);
    when(tnfa.isAccepting(eq(s0))).thenReturn(false);
    when(tnfa.allTags()).thenReturn(Arrays.asList(t0));
    return tnfa;
  }

  @Before
  public void setUp() {
    tnfa = makeTheNFA();
    nfa2dfa = TNFAToTDFA.make(tnfa);
  }

  @Test
  public void test() {
    final DFAState initState = nfa2dfa.convertToDfaState(tnfa.getInitialState());
    final DFAState res = nfa2dfa.e(initState);
    assertEquals("q1->[0, -2], q0->[-1, -2]", res.toString());
  }

  @Test
  public void testInitialState() {
    assertEquals("[t0]", tnfa.allTags().toString());
    final DFAState converted = nfa2dfa.convertToDfaState(tnfa.getInitialState());
    assertEquals("", converted.toString());
    // assertThat(converted.getData(size(), is(1));
    // final int[] ary = converted.values().iterator().next();
    // assertEquals("[-1, -2]", Arrays.toString(ary));
    // final State state = converted.keySet().iterator().next();
    // assertEquals("q0", state.toString());
  }
}
