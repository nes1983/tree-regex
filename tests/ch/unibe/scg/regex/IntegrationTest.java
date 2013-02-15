package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TransitionTriple.Priority;


@SuppressWarnings("javadoc")
public final class IntegrationTest {
  TNFAToTDFA nfa2dfa;

  State s0;
  State s1;
  State s2;
  Tag t0;
  TNFA tnfa;

  @SuppressWarnings({"rawtypes", "unchecked"})
  TNFA makeTheNFA() {
    State.resetCount();

    s0 = State.get();
    s1 = State.get();
    s2 = State.get();

    final TNFA tnfa = mock(TNFA.class);

    t0 = mock(Tag.class);

    when(t0.toString()).thenReturn("t0");
    when(t0.getGroup()).thenReturn(1); // Must be 1, not 0, because 0 is the entire match.

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
    when(tnfa.availableTransitionsFor(s2, 'a')).thenReturn(new ArrayList());
    when(tnfa.isAccepting(eq(s2))).thenReturn(true);
    when(tnfa.getFinalStates()).thenReturn(new HashSet<>(Arrays.asList(s2)));
    when(tnfa.isAccepting(eq(s1))).thenReturn(false);
    when(tnfa.isAccepting(eq(s0))).thenReturn(false);
    return tnfa;
  }

  @Before
  public void setUp() {
    tnfa = makeTheNFA();
    nfa2dfa = TNFAToTDFA.make(tnfa);
  }

  @Test
  public void testConvert() {
    final TDFA tdfa = nfa2dfa.convert();
    assertThat(tdfa.toString(),
        is("q0-a-a -> q1 [1<- pos]\nq1-a-a -> q1 [1<- pos, 1->0]\n\n[0<- pos]"));
  }
}
