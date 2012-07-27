package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import automaton.core.Pair.Pr;
import automaton.core.Tag.MarkerTag;

class TNFAToTDFA {

	static class DFAState implements Comparable<DFAState> {
		public static String toString(final Map<State, int[]> states) {
			final StringBuilder sb = new StringBuilder();
			for (final Map.Entry<State, int[]> el : states.entrySet()) {
				sb.append(el.getKey());
				sb.append("->");
				sb.append(Arrays.toString(el.getValue()));
				sb.append(", ");
			}
			sb.delete(sb.length() - 2, sb.length());
			return sb.toString();
		}

		final Map<State, int[]> innerStates;

		public DFAState(final Map<State, int[]> innerStates) {
			this.innerStates = Collections.unmodifiableMap(innerStates);
		}

		public int compareTo(final DFAState o) {
			return DFAStateComparator.SINGLETON.compare(this.innerStates, o.innerStates);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final DFAState other = (DFAState) obj;
			if (innerStates == null) {
				if (other.innerStates != null) {
					return false;
				}
			} else if (!innerStates.keySet().equals(other.innerStates.keySet())) {
				return false;
			}

			return true;
		}

		Map<State, int[]> getData() {
			return innerStates;
		}

		@Override
		public int hashCode() {
			if (innerStates == null) {
				return 1;
			}

			return innerStates.keySet().hashCode();
		}

		boolean isMappable(final DFAState other, final int[] mapping) {
			Arrays.fill(mapping, -1);

			for (final Map.Entry<State, int[]> entry : innerStates.entrySet()) {
				final int[] mine = entry.getValue();
				final int[] theirs = other.innerStates.get(entry.getValue());
				final boolean success = updateMap(mapping, mine, theirs);
				if (!success) {
					return false;
				}
			}

			return true;
		}

		/**
		 * @return a mapping from this state to another, if there is one.
		 *         Otherwise, return null.
		 */
		int[] mappingIfAny(final DFAState other, final int maxLoc) {
			final int[] mapping = new int[maxLoc];
			if (this.isMappable(other, mapping)) {
				return mapping;
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return toString(innerStates);
		}

		/**
		 * Destructively update <code>map</code> until it maps from to to. A -1
		 * entry in map means that the value can still be changed. Other values
		 * are left untouched.
		 * 
		 * @param map
		 *            Must be at least as big as the biggest values in both from
		 *            and to. Elements must be >= -1. -1 stands for unassigned.
		 * @param from
		 *            same length as to.
		 * @param to
		 *            same length as from.
		 * @return True if the mapping was successful; false otherwise.
		 */
		private boolean updateMap(final int[] map, final int[] from, final int[] to) {
			assert from.length == to.length;
			for (int i = 0; i < from.length; i++) {
				if (map[from[i]] == -1) {
					map[from[i]] = to[i];
				} else if (map[from[i]] != to[i]) {
					return false;
				} // Else everything is fine.
			}
			return true;
		}

	}

	static class DFAState2 {
		final Collection<Pr<State, SortedSet<MapItem>>> madeUpOf;

		DFAState2(final Collection<Pr<State, SortedSet<MapItem>>> madeUpOf) {
			this.madeUpOf = madeUpOf;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final DFAState2 other = (DFAState2) obj;
			if (madeUpOf == null) {
				if (other.madeUpOf != null) {
					return false;
				}
			} else if (!madeUpOf.equals(other.madeUpOf)) {
				return false;
			}
			return true;
		}

		public Collection<Pr<State, SortedSet<MapItem>>> getMadeUpOf() {
			return madeUpOf;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((madeUpOf == null) ? 0 : madeUpOf.hashCode());
			return result;
		}
	}

	static enum DFAStateComparator implements Comparator<Map<State, int[]>> {
		SINGLETON;

		private int compare(final int[] a, final int[] b) {
			{
				final int cmp = Integer.compare(a.length, b.length);
				if (cmp != 0) {
					return cmp;
				}
			}
			for (int i = 0; i < a.length; i++) {
				final int cmp = Integer.compare(a[i], b[i]);
				if (cmp != 0) {
					return cmp;
				}
			}
			return 0;
		}

		@Override
		public int compare(final Map<State, int[]> o1, final Map<State, int[]> o2) {
			{
				final int cmp = Integer.compare(o1.size(), o2.size());
				if (cmp != 0) {
					return cmp;
				}
			}

			final List<State> states1 = new ArrayList<>(o1.keySet());
			Collections.sort(states1);
			final List<State> states2 = new ArrayList<>(o2.keySet());
			Collections.sort(states2);

			assert states1.size() == states2.size();

			for (int i = 0; i < states1.size(); i++) {
				final int cmp = states1.get(i).compareTo(states2.get(i));
				if (cmp != 0) {
					return cmp;
				}
			}

			for (int i = 0; i < states1.size(); i++) {
				assert states1.get(i).compareTo(states2.get(i)) == 0;
				final State s = states1.get(i);
				final int cmp = compare(o1.get(s), o2.get(s));
				if (cmp != 0) {
					return cmp;
				}
			}
			return 0;
		}
	}

	public static class IntegrationTest {

		TNFAToTDFA nfa2dfa;

		State s0;
		State s1;
		State s2;
		Tag t0;
		TNFA tnfa;

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
			when(tnfa.availableTransitionsFor(eq(s0), isNull(Character.class)))
					.thenReturn(
							Arrays.asList(new TransitionTriple(s1, 1, t0),
									new TransitionTriple(s0, 0, Tag.NONE)));
			when(tnfa.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
					Arrays.asList(new TransitionTriple(s0, 0, Tag.NONE)));
			when(tnfa.availableTransitionsFor(s1, 'a')).thenReturn(
					Arrays.asList(new TransitionTriple(s2, 1, Tag.NONE),
							new TransitionTriple(s1, 0, Tag.NONE)));
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
			assertThat(input.toString(),
					is("{q0=[], q2=[MapItem[0, t0]], q1=[MapItem[0, t0]]}"));

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
			assertThat(res.toString(),
					is("{q0=[], q2=[MapItem[0, t0]], q1=[MapItem[0, t0]]}"));
		}
	}

	static class MapItems implements Iterable<MapItem> {

		final Map<Tag, BitSet> mapItems;

		public MapItems(final Map<Tag, BitSet> mapItems) {
			this.mapItems = mapItems;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MapItems other = (MapItems) obj;
			if (mapItems == null) {
				if (other.mapItems != null) {
					return false;
				}
			} else if (!mapItems.equals(other.mapItems)) {
				return false;
			}
			return true;
		}

		public Map<Tag, BitSet> getMapItems() {
			return mapItems;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mapItems == null) ? 0 : mapItems.hashCode());
			return result;
		}

		public Iterator<MapItem> iterator() {
			return new Iterator<MapItem>() {

				Entry<Tag, BitSet> currentEntry;
				final Iterator<Entry<Tag, BitSet>> iterator = mapItems.entrySet()
						.iterator();
				int position = -1;

				private void ensureCurrentEntry() {
					if (currentEntry == null) {
						currentEntry = iterator.next();
					}
					assert currentEntry != null;
				}

				@Override
				public boolean hasNext() {
					if (iterator.hasNext()) {
						return true;
					}
					ensureCurrentEntry();
					return currentEntry.getValue().nextSetBit(position + 1) >= 0;
				}

				@Override
				public MapItem next() {
					ensureCurrentEntry();
					BitSet bs;
					restart: do {
						bs = currentEntry.getValue();
						position = bs.nextSetBit(position + 1);
						if (position < 0) {
							currentEntry = iterator.next();
							position = -1;
							continue restart;
						}
					} while (false);
					assert position >= 0 && bs != null;

					return new MapItem(currentEntry.getKey(), position);
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

	}

	public static class NewTest {

		TNFAToTDFA nfa2dfa;

		State s0;
		State s1;
		State s2;
		Tag t0;
		TNFA tnfa;

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
			when(tnfa.availableTransitionsFor(eq(s0), isNull(Character.class)))
					.thenReturn(
							Arrays.asList(new TransitionTriple(s1, 1, t0),
									new TransitionTriple(s0, 0, Tag.NONE)));
			when(tnfa.availableTransitionsFor(eq(s0), eq('a'))).thenReturn(
					Arrays.asList(new TransitionTriple(s0, 0, Tag.NONE)));
			when(tnfa.availableTransitionsFor(s1, 'a')).thenReturn(
					Arrays.asList(new TransitionTriple(s2, 1, Tag.NONE),
							new TransitionTriple(s1, 0, Tag.NONE)));
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

	public static class NFA2DFATest {

		TNFA tnfa;
		TNFAToTDFA tnfa2tdfa;

		@SuppressWarnings("unchecked")
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

			when(tnfa.availableTransitionsFor(eq(s), Mockito.any(Character.class)))
					.thenReturn(Arrays.asList(triple));

			final Map<State, SortedSet<MapItem>> states = tnfa2tdfa.reach(tState, 'b');
			assertThat(states.size(), is(1));
			assertThat(states.entrySet().iterator().next().getKey(), is(s));
			assertThat(states.entrySet().iterator().next().getValue(), (Matcher) is(ss));
			return states;
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

			when(tnfa.availableTransitionsFor(eq(s), Mockito.any(Character.class)))
					.thenReturn(Arrays.asList(triple));

			assertThat(s.compareTo(s), is(0));

			final Map<State, SortedSet<MapItem>> states = tnfa2tdfa.reach(tState, 'b');

			final Map<State, SortedSet<MapItem>> res = tnfa2tdfa.closure(states);
			assertThat(res.entrySet().iterator().next().getKey(), is(s));
		}

		@Test
		public void testReach() {
			myTestReach();
		}
	}

	static enum StatesComparator implements Comparator<Map<State, SortedSet<MapItem>>> {
		SINGLETON;

		@Override
		public int compare(final Map<State, SortedSet<MapItem>> o1,
				final Map<State, SortedSet<MapItem>> o2) {
			{
				final int cmp = Integer.compare(o1.size(), o2.size());
				if (cmp != 0) {
					return cmp;
				}
			}

			final List<State> states1 = new ArrayList<>(o1.keySet());
			Collections.sort(states1);
			final List<State> states2 = new ArrayList<>(o2.keySet());
			Collections.sort(states2);

			assert states1.size() == states2.size();

			for (int i = 0; i < states1.size(); i++) {
				final int cmp = states1.get(i).compareTo(states2.get(i));
				if (cmp != 0) {
					return cmp;
				}
			}
			return 0;
		}
	}

	static class StateWithMemoryLocation implements Map.Entry<State, int[]> {
		final int[] memoryLocation;
		final State state;

		StateWithMemoryLocation(final State state, final int[] memoryLocation) {
			this.memoryLocation = memoryLocation;
			this.state = state;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final StateWithMemoryLocation other = (StateWithMemoryLocation) obj;
			if (state == null) {
				if (other.state != null) {
					return false;
				}
			} else if (!state.equals(other.state)) {
				return false;
			}
			return true;
		}

		public State getKey() {
			return state;
		}

		public int[] getMemoryLocation() {
			return memoryLocation;
		}

		public State getState() {
			return state;
		}

		public int[] getValue() {
			return memoryLocation;
		}

		@Override
		public int hashCode() {
			return ((state == null) ? 0 : state.hashCode());
		}

		public int[] setValue(final int[] value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "" + state + "" + Arrays.toString(memoryLocation);
		}

	}

	private static final int HIGH = 0;

	private static final int LOW = 0;

	public static TNFAToTDFA make(final TNFA tnfa) {
		return new TNFAToTDFA(tnfa);
	}

	private int currentPos = -1;

	final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker
			.get();

	final TransitionTable.TDFATransitionTable.Builder tdfaBuilder = new TransitionTable.TDFATransitionTable.Builder();

	final TNFA tnfa;

	public TNFAToTDFA(final TNFA tnfa) {
		this.tnfa = tnfa;
	}

	Collection<InputRange> allInputRanges() {
		final List<InputRange> ranges = new ArrayList<>(tnfa.allInputRanges());
		if (ranges.size() < 2) {
			return Collections.unmodifiableCollection(ranges);
		}

		final List<InputRange> ret = new ArrayList<>();
		Collections.sort(ranges);
		final Iterator<InputRange> iter = ranges.iterator();
		final InputRange last = iter.next();
		InputRange cur = null;
		while (iter.hasNext()) {
			cur = iter.next();
			if (last.getTo() < cur.getFrom()) {
				ret.add(last);
			}
			ret.add(new InputRange(last.getFrom(), ((char) (cur.getFrom() - 1))));
		}
		assert cur != null;
		ret.add(cur);
		return ret;
	}

	/**
	 * Maps directly to section 4 of the paper. That's why it looks so ugly.
	 * 
	 * @param S
	 */
	Map<State, SortedSet<MapItem>> closure(final Map<State, SortedSet<MapItem>> S) {
		final Deque<Triple> stack = new ArrayDeque<>();
		for (final Entry<State, SortedSet<MapItem>> pr : S.entrySet()) {
			stack.push(new Triple(pr.getKey(), 0, pr.getValue()));
		}
		final NavigableSet<Triple> closure = initClosure(S);
		while (!stack.isEmpty()) {
			State s;
			SortedSet<MapItem> k;
			{
				final Triple t = stack.pop();
				s = t.state;
				k = t.mapItems;
			}
			for (final TransitionTriple transition : tnfa
					.availableTransitionsFor(s, null)) {
				final SortedSet<MapItem> kk = new TreeSet<>(k);
				final Tag tag = transition.getTag();

				if (!tag.equals(Tag.NONE)) {

					{
						final MapItem toBeRemoved = toBeRemoved(kk, tag);
						if (toBeRemoved != null) {
							k.remove(toBeRemoved);
						}
						final Set<MapItem> range = null;// getSame(closure,
														// tag);
						if (true) {
							throw new AssertionError("end of editing.");
						}
						final int x = minimumX(kk, tag, range);

						kk.add(new MapItem(tag, x));
					}
				}

				{
					final State u = transition.getState();
					final Triple fromElement = new Triple(u, Integer.MIN_VALUE, null);
					final Triple toElement = new Triple(u, transition.getPriority(), null);
					final SortedSet<Triple> removeThem = closure.subSet(fromElement,
							toElement);
					closure.removeAll(removeThem);
				}

				{
					final State u = transition.getState();
					final int priority = transition.getPriority();
					final Triple t = new Triple(u, priority,
							Collections.unmodifiableSortedSet(kk));
					if (!closure.contains(t)) {
						closure.add(t);
						stack.push(t);
					}
				}

			}
		}
		return removePriorities(closure);
	}

	// public TDFA convert() {
	// final State s = tnfa.getInitialState();
	// if (true) {
	// throw new AssertionError();
	// }
	// return null;
	// // final Map<State, SortedSet<MapItem>> initState =
	// // convertToDfaState(s);
	// // return convert(initState);
	// }

	public TDFA convert() {
		DFAState start;
		{
			final State nfaStart = tnfa.getInitialState();
			start = convertToDfaState(nfaStart);
		}

		start = e(start);

		final List<Instruction> initializer = makeInitializer(start);

		final Deque<DFAState> unmarkedStates = new ArrayDeque<>();
		final NavigableSet<DFAState> states = new TreeSet<>();

		states.add(start);
		unmarkedStates.add(start);

		for (DFAState t; !unmarkedStates.isEmpty();) {
			t = unmarkedStates.pop();

			for (final InputRange inputRange : allInputRanges()) {
				final char a = inputRange.getFrom();
				DFAState u;

				final Map<State, int[]> k = reachable(t.getData(), a);
				u = e(k);

				final List<Instruction> c = new ArrayList<>();

				final BitSet newLocations = newMemoryLocations(u.getData(), k);
				System.out.println("k" + k);
				System.out.println("newLocs: " + newLocations);

				int[] mapping = new int[currentPos];
				final DFAState mappedState = findMappableState(states, u, mapping);
				if (mappedState == null) {
					mapping = null;
				}

				for (int i = newLocations.nextSetBit(0); i >= 0; i = newLocations
						.nextSetBit(i + 1)) {
					if (mapping != null) {
						c.add(instructionMaker.storePos(mapping[i]));
					} else {
						c.add(instructionMaker.storePos(i));
					}
				}

				DFAState newState;
				if (mappedState != null) {
					c.addAll(mappingInstructions(mapping, t));
					newState = mappedState;
				} else {
					states.add(u);
					unmarkedStates.add(u);
					newState = u;
				}

				assert newState != null;

				tdfaBuilder.addTransition(t, inputRange, newState, c);

				// final Entry<State, SortedSet<MapItem>> smallestFinishing =
				// smallestFinishing(newState);
				// XXX finishing stuff.

			}

		}
		return new TDFA(tdfaBuilder.build(), initializer);

	}

	/**
	 * Used to create the initial state of the DFA.
	 */
	DFAState convertToDfaState(final State s) {
		final Map<State, int[]> initState = new HashMap<>();
		final int numTags = tnfa.allTags().size();
		final int[] initialMemoryLocations = makeInitialMemoryLocations(numTags);
		initState.put(s, initialMemoryLocations);
		return new DFAState(Collections.unmodifiableMap(initState));
	}

	// /**
	// * Maps to function TNFA_to_TDFA() in section 4 of
	// * "NFAs with Tagged Transitions …"
	// */
	// public TDFA convert(final Map<State, SortedSet<MapItem>> s) {
	// final Map<State, SortedSet<MapItem>> i = closure(s);
	//
	// final List<Instruction> initializer = new ArrayList<>();
	//
	// for (final SortedSet<MapItem> mis : i.values()) {
	// for (final MapItem mi : mis) {
	// if (mi.getPos() == 0) {
	// initializer.add(instructionMaker.storePos(mi));
	// }
	// }
	// }
	//
	// final Deque<Map<State, SortedSet<MapItem>>> unmarkedStates = new
	// ArrayDeque<>();
	// final NavigableSet<Map<State, SortedSet<MapItem>>> states = new
	// TreeSet<>(
	// StatesComparator.SINGLETON);
	//
	// states.add(i);
	// unmarkedStates.add(i);
	//
	// for (Map<State, SortedSet<MapItem>> t; !unmarkedStates.isEmpty();) {
	// t = unmarkedStates.pop();
	//
	// for (final InputRange inputRange : allInputRanges()) {
	// final char a = inputRange.getFrom();
	// final Map<State, SortedSet<MapItem>> k = reach(t, a);
	// final Map<State, SortedSet<MapItem>> u = closure(k);
	// final List<Instruction> c = new ArrayList<>();
	//
	// final Collection<MapItem> newStates = newStates(u, k);
	// System.out.println("k" + k);
	// for (final MapItem mi : newStates) {
	// c.add(instructionMaker.storePos(mi));
	// }
	//
	// Map<State, SortedSet<MapItem>> newState = null;
	// {
	// final NavigableSet<Map<State, SortedSet<MapItem>>> candidates = states
	// .subSet(u, true, u, true);
	// Collection<Instruction> mapping = null;
	// for (final Map<State, SortedSet<MapItem>> candidate : candidates) {
	// mapping = isStateMappable(u, candidate);
	// if (mapping != null) {
	// newState = candidate;
	// break;
	// }
	// }
	//
	// if (mapping != null) {
	// c.addAll(mapping);
	// } else {
	// newState = u;
	// states.add(u);
	// unmarkedStates.add(u);
	// }
	// }
	// assert newState != null;
	//
	// tdfaBuilder.addTransition(t, inputRange, newState, c);
	//
	// final Entry<State, SortedSet<MapItem>> smallestFinishing =
	// smallestFinishing(newState);
	// // XXX finishing stuff.
	//
	// }
	//
	// }
	// return new TDFA(tdfaBuilder.build(), initializer);
	//
	// }

	DFAState e(final DFAState startState) {
		return e(startState.getData());
	}

	/**
	 * Niko and Aaron's closure.
	 * 
	 */
	DFAState e(final Map<State, int[]> startState) {
		final Map<State, int[]> R = new LinkedHashMap<>();

		final Deque<Map.Entry<State, int[]>> stack = new ArrayDeque<>();
		for (final Map.Entry<State, int[]> i : startState.entrySet()) {
			stack.push(i);
		}

		while (!stack.isEmpty()) {

			final Entry<State, int[]> s = stack.pop();
			final State q = s.getKey();
			final int[] l = s.getValue();

			if (R.containsKey(s.getKey())) {
				continue;
			}

			nextTriple: for (final TransitionTriple triple : tnfa
					.availableTransitionsFor(q, null)) {
				final State qDash = triple.state;

				// Step 1.
				if (R.containsKey(qDash) && triple.priority == LOW) {
					continue nextTriple;
				} else if (R.containsKey(qDash)) {
					assert triple.priority == HIGH;
				}

				// Step 2.
				final Tag tau = triple.tag;
				int[] tdash;
				if (!tau.equals(Tag.NONE)) {
					final int pos = positionFor(tau);
					tdash = Arrays.copyOf(l, l.length);
					tdash[pos] = nextInt();
				} else {
					tdash = l;
				}

				// Step 3
				R.remove(triple.getState());
				R.put(triple.getState(), tdash);

				stack.push(new StateWithMemoryLocation(triple.getState(), tdash));
			}
		}
		return new DFAState(R);
	}

	// Map<State, SortedSet<MapItem>> convertToDfaState(final State s) {
	// final Map<State, SortedSet<MapItem>> initState = new HashMap<>();
	// initState.put(s, new TreeSet<MapItem>());
	// return initState;
	// }

	private BitSet extractLocs(final Map<State, int[]> oldState) {
		final BitSet ret = new BitSet();
		for (final int[] ary : oldState.values()) {
			for (final int val : ary) {
				ret.set(val);
			}
		}
		return ret;
	}

	private Set<MapItem> extractMIs(final Map<State, SortedSet<MapItem>> oldState) {
		final Set<MapItem> oldMIs = new LinkedHashSet<>();
		for (final SortedSet<MapItem> mis : oldState.values()) {
			for (final MapItem mi : mis) {
				oldMIs.add(mi);
			}
		}
		return oldMIs;
	}

	private DFAState findMappableState(final NavigableSet<DFAState> states,
			final DFAState u, final int[] mapping) {

		final Map<State, int[]> fromElement = new LinkedHashMap<>(u.getData());
		{
			final int[] min = new int[0];
			for (final Entry<State, int[]> e : fromElement.entrySet()) {
				e.setValue(min);
			}
		}
		final Map<State, int[]> toElement = new LinkedHashMap<>(u.getData());
		{
			final int[] max = new int[currentPos + 1];
			for (final Entry<State, int[]> e : toElement.entrySet()) {
				e.setValue(max);
			}
		}
		final NavigableSet<DFAState> range = states.subSet(new DFAState(fromElement),
				true, new DFAState(toElement), true);

		for (final DFAState candidate : range) {
			if (u.isMappable(candidate, mapping)) {
				return candidate;
			}
		}

		return null;
	}

	SortedSet<MapItem> getSame(final SortedSet<MapItem> k, final Tag tag) {
		final MapItem from = new MapItem(tag, 0);
		final MapItem to = new MapItem(new MarkerTag(tag.getGroup() + 1), 0);
		return k.subSet(from, to);
	}

	NavigableSet<Triple> initClosure(final Map<State, SortedSet<MapItem>> S) {
		final NavigableSet<Triple> closure = new TreeSet<>();
		for (final Entry<State, SortedSet<MapItem>> pr : S.entrySet()) {
			closure.add(new Triple(pr.getKey(), 0, pr.getValue()));
		}
		return closure;
	}

	/**
	 * Return a mapping from an existing state to a mapped state, if one exists.
	 * Otherwise, null.
	 */
	Collection<Instruction> isStateMappable(
			final Map<State, SortedSet<MapItem>> toBeMappedState,
			final Map<State, SortedSet<MapItem>> existingState) {
		final Map<MapItem, MapItem> mappings = mappingsForReuse(toBeMappedState,
				existingState);
		if (mappings == null) {
			return null;
		}
		final Collection<Instruction> ret = new ArrayList<>();
		for (final Entry<MapItem, MapItem> e : mappings.entrySet()) {
			instructionMaker.reorder(e.getKey(), e.getValue());
		}
		return Collections.unmodifiableCollection(ret);
	}

	private List<Instruction> makeInitializer(final DFAState start) {
		final List<Instruction> initializer = new ArrayList<>();
		final BitSet locs = extractLocs(start.getData());
		for (int i = locs.nextSetBit(0); i >= 0; i = locs.nextSetBit(i + 1)) {
			initializer.add(instructionMaker.storePos(i));
		}
		return initializer;
	}

	private int[] makeInitialMemoryLocations(final int numTags) {
		final int[] ret = new int[numTags * 2];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = -1 * i - 1;
		}
		return ret;
	}

	private Collection<? extends Instruction> mappingInstructions(final int[] mapping,
			final DFAState from) {
		final BitSet locs = extractLocs(from.getData());
		final List<Instruction> ret = new ArrayList<>();

		for (int i = locs.nextSetBit(0); i >= 0; i = locs.nextSetBit(i + 1)) {
			ret.add(instructionMaker.reorder(i, mapping[i]));
		}

		return ret;
	}

	/**
	 * If there's a mapping that allows reuse, return it. Otherwise, return
	 * null.
	 */
	private Map<MapItem, MapItem> mappingsForReuse(
			final Map<State, SortedSet<MapItem>> toBeMappedState,
			final Map<State, SortedSet<MapItem>> states) {
		final Map<MapItem, MapItem> mappings = new LinkedHashMap<>();
		for (final Entry<State, SortedSet<MapItem>> e : toBeMappedState.entrySet()) {
			final SortedSet<MapItem> toBeMappedItems = e.getValue();
			final SortedSet<MapItem> fixedItems = states.get(e.getKey());
			if (toBeMappedItems.size() != fixedItems.size()) {
				return null;
			}

			final Iterator<MapItem> toBeMapped = toBeMappedItems.iterator();
			final Iterator<MapItem> fixed = fixedItems.iterator();

			while (toBeMapped.hasNext()) {
				assert fixed.hasNext();

				final MapItem from = toBeMapped.next();
				final MapItem to = fixed.next();

				final MapItem currentlySetTo = mappings.get(from);
				if (currentlySetTo != null && !currentlySetTo.equals(to)) {
					return null;
				}
				mappings.put(from, to);
			}
			return mappings;

		}
		return Collections.unmodifiableMap(mappings);
	}

	int minimumX(final SortedSet<MapItem> k, final Tag tag, final Set<MapItem> range) {
		int last = -1;
		int x = -1;
		if (range.isEmpty()) {
			return 0;
		}
		for (final MapItem mi : range) { // XXX I think there's

			// an easier way.
			if (last + 1 < mi.getPos()) {
				x = last + 1;
				break;
			}
			last = mi.getPos();
		}
		if (x == -1) {
			x = last + 1;
		}
		assert !k.contains(new MapItem(tag, x));
		return x;
	}

	BitSet newMemoryLocations(final Map<State, int[]> oldState,
			final Map<State, int[]> newState) {
		final BitSet oldLocs = extractLocs(oldState);
		final BitSet newLocs = extractLocs(newState);
		newLocs.andNot(oldLocs);
		return newLocs;
	}

	int nextInt() {
		// XXX One could plug more logic into this method, to eliminate
		// post-processing.
		return ++currentPos;
	}

	private int positionFor(final Tag tau) {
		int r = 2 * tau.getGroup() - 2;
		if (tau.isEndTag()) {
			r += 1;
		}
		return r;
	}

	/**
	 * tε closure(S) for each (u, k) ∈ S do push (u, 0, k) to stack initialize
	 * closure to S while stack is not empty pop (s, p, k), the top element, off
	 * of stack for each ε-transition from s to some state u do if the
	 * ε-transition was tagged with tn then if∃m:mmn ∈kthen remove mmn from k
	 * add mxn to k, where x is the smallest nonnegative integer such that mxn
	 * does not occur in S if ∃p′,k′ : (u,p′,k′) ∈ closure and p
	 * < p
	 * ′ then remove (u, p′, k′) from closure if (u, p, k) ∈/ closure then add
	 * (u, p, k) to closure push (u, p, k) onto stack remove the middle element,
	 * the priority, from all triples in closure return closure
	 * 
	 * @return
	 */
	Map<State, SortedSet<MapItem>> reach(final Map<State, SortedSet<MapItem>> state,
			final char a) {
		final Map<State, SortedSet<MapItem>> ret = new LinkedHashMap<>();

		for (final Entry<State, SortedSet<MapItem>> pr : state.entrySet()) {
			final SortedSet<MapItem> k = pr.getValue();

			final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(
					pr.getKey(), a);
			for (final TransitionTriple t : ts) {
				ret.put(t.getState(), new TreeSet<>(k));
			}
		}
		return Collections.unmodifiableMap(ret);
	}

	Map<State, int[]> reachable(final Map<State, int[]> state, final char a) {
		final Map<State, int[]> ret = new LinkedHashMap<>();

		for (final Entry<State, int[]> pr : state.entrySet()) {
			final int[] k = pr.getValue();

			final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(
					pr.getKey(), a);
			for (final TransitionTriple t : ts) {
				ret.put(t.getState(), Arrays.copyOf(k, k.length));
			}
		}
		return Collections.unmodifiableMap(ret);
	}

	Map<State, SortedSet<MapItem>> removePriorities(final NavigableSet<Triple> closure) {
		final Map<State, SortedSet<MapItem>> ret = new LinkedHashMap<>(closure.size());
		for (final Triple t : closure) {
			ret.put(t.state, t.mapItems);
		}
		return Collections.unmodifiableMap(ret);
	}

	Entry<State, SortedSet<MapItem>> smallestFinishing(
			final Map<State, SortedSet<MapItem>> newState) {

		final List<Entry<State, SortedSet<MapItem>>> finishing = new ArrayList<>();
		for (final Entry<State, SortedSet<MapItem>> ss : newState.entrySet()) {
			if (tnfa.isAccepting(ss.getKey())) {
				finishing.add(ss);
			}
		}
		if (finishing.isEmpty()) {
			return null;
		}
		Collections.sort(finishing, new Comparator<Entry<State, SortedSet<MapItem>>>() {
			public int compare(final Entry<State, SortedSet<MapItem>> o1,
					final Entry<State, SortedSet<MapItem>> o2) {
				return Integer.compare(o1.getValue().size(), o2.getValue().size());
			}
		});

		return finishing.get(0);
	}

	MapItem toBeRemoved(final SortedSet<MapItem> k, final Tag tag) {
		final SortedSet<MapItem> sames = getSame(k, tag);
		if (sames.isEmpty()) {
			return null;
		}
		return sames.last();
	}
}

class Triple implements Comparable<Triple> {
	public final SortedSet<MapItem> mapItems;
	public final int priority;
	public final State state;

	public Triple(final State state, final int priority, final SortedSet<MapItem> mapItems) {
		this.state = state;
		this.priority = priority;
		this.mapItems = mapItems;
	}

	public int compareTo(final Triple o) {
		final int cmp = this.state.compareTo(o.state);
		if (cmp != 0) {
			return cmp;
		}
		return Integer.compare(priority, o.priority);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Triple other = (Triple) obj;
		if (mapItems == null) {
			if (other.mapItems != null) {
				return false;
			}
		} else if (!mapItems.equals(other.mapItems)) {
			return false;
		}
		if (priority != other.priority) {
			return false;
		}
		if (state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!state.equals(other.state)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mapItems == null) ? 0 : mapItems.hashCode());
		result = prime * result + priority;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "[" + mapItems + ", " + priority + ", " + state + "]";
	}
}
