package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("javadoc")
public class NFA2DFATest {

  TNFA tnfa;
  TNFAToTDFA tnfa2tdfa;

  @SuppressWarnings({"unchecked", "rawtypes"})
  Map<State, SortedSet<MapItem>> myTestReach() {
    final TransitionTriple triple = mock(TransitionTriple.class);
    // final Pr<State, SortedSet<MapItem>> pr = mock(Pr.class);
    final Map<State, SortedSet<MapItem>> tState = new LinkedHashMap<>();

    final State s = mock(State.class);
    final MapItem mi = mock(MapItem.class);
    final Tag t = mock(Tag.class);
    final TreeSet<MapItem> ss = new TreeSet<>(Arrays.asList(mi));

    tState.put(s, ss);
    when(triple.getState()).thenReturn(s);
    when(triple.getTag()).thenReturn(t);

    // when(pr.getFirst()).thenReturn(s);
    // when(pr.getSecond()).thenReturn(ss);

    when(tnfa.availableTransitionsFor(eq(s), Mockito.any(Character.class))).thenReturn(
        Arrays.asList(triple));

    throw null;
    // final Map<State, SortedSet<MapItem>> states = tnfa2tdfa.reach(tState, 'b');
    // assertThat(states.size(), is(1));
    // assertThat(states.entrySet().iterator().next().getKey(), is(s));
    // assertThat(states.entrySet().iterator().next().getValue(), (Matcher) is(ss));
    // return states;
  }

  @Before
  public void setUp() {
    tnfa = mock(TNFA.class);
    tnfa2tdfa = new TNFAToTDFA(tnfa);

  }

  @Test
  public void testClosure() {
    final TransitionTriple triple = mock(TransitionTriple.class);
    final Map<State, SortedSet<MapItem>> tState = new LinkedHashMap<>();
    final Tag t = mock(Tag.class);

    final MapItem mi = new MapItem(t, 0);

    final State s = State.get(); // Hard to mock. Needs to sort.
    final TreeSet<MapItem> ss = new TreeSet<>(Arrays.asList(mi));

    tState.put(s, ss);

    when(t.getGroup()).thenReturn(0);
    when(triple.getState()).thenReturn(s);
    when(triple.getTag()).thenReturn(t);

    when(tnfa.availableTransitionsFor(eq(s), Mockito.any(Character.class))).thenReturn(
        Arrays.asList(triple));

    assertThat(s.compareTo(s), is(0));

    // final Map<State, SortedSet<MapItem>> states = tnfa2tdfa.e(tState, 'b');
    //
    // final Map<State, SortedSet<MapItem>> res = tnfa2tdfa.closure(states);
    // assertThat(res.entrySet().iterator().next().getKey(), is(s));
  }
}
