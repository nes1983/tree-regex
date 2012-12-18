package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

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
    return tnfa;
  }

  @Before
  public void setUp() {
    tnfa = makeTheNFA();
    nfa2dfa = TNFAToTDFA.make(tnfa);
  }

  // @Test
  // public void testClosure() {
  // final Map<State, SortedSet<MapItem>> initState = //nfa2dfa
  // // .convertToDfaState(tnfa.getInitialState());
  // null;
  // assertThat(initState.toString(), is("{q0=[]}"));
  // final Map<State, SortedSet<MapItem>> withTags =
  // nfa2dfa.closure(initState);
  // final Iterator<Entry<State, SortedSet<MapItem>>> iter =
  // withTags.entrySet()
  // .iterator();
  // final Entry<State, SortedSet<MapItem>> e1 = iter.next();
  // final Entry<State, SortedSet<MapItem>> e2 = iter.next();
  // assertFalse(e1.getValue() == e2.getValue());
  // assertThat(withTags.size(), is(2));
  // assertThat(withTags.entrySet().iterator().next().getValue().isEmpty(),
  // is(true));
  // assertThat(withTags.toString(), is("{q0=[], q1=[MapItem[0, t0]]}"));
  //
  // }

  @Test
  public void testClosure2() {
    final Map<State, SortedSet<MapItem>> input = new LinkedHashMap<>();
    input.put(s0, new TreeSet<MapItem>());
    final SortedSet<MapItem> arg = new TreeSet<>();
    arg.add(new MapItem(t0, 0));
    input.put(s2, arg);
    input.put(s1, new TreeSet<>(arg));
    assertThat(input.toString(), is("{q0=[], q2=[MapItem[0, t0]], q1=[MapItem[0, t0]]}"));

    final Map<State, SortedSet<MapItem>> res = nfa2dfa.closure(input);
    assertThat(res.toString(), is(""));
  }

  @Test
  public void testConvert() {
    final TDFA tdfa = nfa2dfa.convert();
    System.out.println(tdfa);
    assertThat(tdfa.toString(), is(""));
  }

  @Test
  public void testReach() {
    final Map<State, SortedSet<MapItem>> arg = new LinkedHashMap<>();
    arg.put(s0, new TreeSet<MapItem>());
    final SortedSet<MapItem> argg = new TreeSet<>();
    argg.add(new MapItem(t0, 0));
    arg.put(s1, argg);
    final Map<State, SortedSet<MapItem>> res = nfa2dfa.reach(arg, 'a');
    assertThat(res.toString(), is("{q0=[], q2=[MapItem[0, t0]], q1=[MapItem[0, t0]]}"));
  }
}