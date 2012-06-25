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
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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

	static class DFAState {
		final Collection<Pr<State, SortedSet<MapItem>>> madeUpOf;

		DFAState(final Collection<Pr<State, SortedSet<MapItem>>> madeUpOf) {
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
			final DFAState other = (DFAState) obj;
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
			result = prime * result
					+ ((madeUpOf == null) ? 0 : madeUpOf.hashCode());
			return result;
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
			result = prime * result
					+ ((mapItems == null) ? 0 : mapItems.hashCode());
			return result;
		}

		public Iterator<MapItem> iterator() {
			return new Iterator<MapItem>() {

				Entry<Tag, BitSet> currentEntry;
				final Iterator<Entry<Tag, BitSet>> iterator = mapItems
						.entrySet().iterator();
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

	public static class NFA2DFATest {

		TNFA tnfa;
		TNFAToTDFA tnfa2tdfa;

		@SuppressWarnings("unchecked")
		Collection<Pr<State, SortedSet<MapItem>>> myTestReach() {
			final TransitionTriple triple = mock(TransitionTriple.class);
			final Pr<State, SortedSet<MapItem>> pr = mock(Pr.class);
			final Collection<Pr<State, SortedSet<MapItem>>> tState = Arrays
					.asList(pr);
			final State s = mock(State.class);
			final MapItem mi = mock(MapItem.class);
			final Tag t = mock(Tag.class);
			final TreeSet<MapItem> ss = new TreeSet<>(Arrays.asList(mi));

			when(triple.getState()).thenReturn(s);
			when(triple.getTag()).thenReturn(t);

			when(pr.getFirst()).thenReturn(s);
			when(pr.getSecond()).thenReturn(ss);

			when(
					tnfa.availableTransitionsFor(eq(s),
							Mockito.any(Character.class))).thenReturn(
					Arrays.asList(triple));

			final Collection<Pr<State, SortedSet<MapItem>>> states = tnfa2tdfa
					.reach(tState, 'b');
			assertThat(states.size(), is(1));
			assertThat(states.iterator().next().getFirst(), is(s));
			assertThat(states.iterator().next().getSecond(), (Matcher) is(ss));
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
			final Pr<State, SortedSet<MapItem>> pr = mock(Pr.class);
			final Collection<Pr<State, SortedSet<MapItem>>> tState = Arrays
					.asList(pr);
			final State s = State.get(); // Hard to mock. Needs to sort.
			final Tag t = mock(Tag.class);

			final MapItem mi = new MapItem(t, 0);

			final TreeSet<MapItem> ss = new TreeSet<>(Arrays.asList(mi));

			when(t.getGroup()).thenReturn(0);
			when(triple.getState()).thenReturn(s);
			when(triple.getTag()).thenReturn(t);

			when(pr.getFirst()).thenReturn(s);
			when(pr.getSecond()).thenReturn(ss);

			when(
					tnfa.availableTransitionsFor(eq(s),
							Mockito.any(Character.class))).thenReturn(
					Arrays.asList(triple));

			assertThat(s.compareTo(s), is(0));

			final Collection<Pr<State, SortedSet<MapItem>>> states = tnfa2tdfa
					.reach(tState, 'b');

			final Collection<Pr<State, SortedSet<MapItem>>> res = tnfa2tdfa
					.closure(states);
			assertThat(res.iterator().next().getFirst(), is(s));
		}

		@Test
		public void testReach() {
			myTestReach();
		}
	}

	final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker
			.get();

	TNFA tnfa;

	public TNFAToTDFA(final TNFA tnfa) {
		this.tnfa = tnfa;
	}

	/**
	 * Maps directly to section 4 of the paper. That's why it looks so ugly.
	 * 
	 * @param S
	 */
	Collection<Pr<State, SortedSet<MapItem>>> closure(
			final Collection<Pr<State, SortedSet<MapItem>>> S) {
		final Deque<Triple> stack = new ArrayDeque<>();
		for (final Pr<State, SortedSet<MapItem>> pr : S) {
			stack.push(new Triple(pr.getFirst(), 0, pr.getSecond()));
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
				final Tag tag = transition.getTag();
				if (!tag.equals(Tag.NONE)) {
					{
						final MapItem toBeRemoved = toBeRemoved(k, tag);

						if (toBeRemoved != null) {
							k.remove(toBeRemoved);
						}
						final Set<MapItem> range = getSame(k, tag);
						final int x = minimumX(k, tag, range);

						k.add(new MapItem(tag, x));
					}
				}

				{
					final State u = transition.getState();
					final Triple fromElement = new Triple(u, Integer.MIN_VALUE,
							null);
					final Triple toElement = new Triple(u,
							transition.getPriority(), null);
					final SortedSet<Triple> removeThem = closure.subSet(
							fromElement, toElement);
					closure.removeAll(removeThem);
				}

				{
					final State u = transition.getState();
					final int priority = transition.getPriority();
					final Triple t = new Triple(u, priority,
							Collections.unmodifiableSortedSet(k));
					if (!closure.contains(t)) {
						closure.add(t);
						stack.push(t);
					}
				}

			}
		}
		return removePriorities(closure);
	}

	/**
	 * Maps to function TNFA_to_TDFA() in section 4 of
	 * "NFAs with Tagged Transitions …"
	 */
	public void convert(final Collection<Pr<State, SortedSet<MapItem>>> s) {
		final Collection<Pr<State, SortedSet<MapItem>>> i = closure(s);
		final Collection<Instruction> initializer = new ArrayList<>();

		for (final Pr<State, SortedSet<MapItem>> pr : i) {
			for (final MapItem mi : pr.getSecond()) {
				if (mi.getPos() == 0) {
					initializer.add(automaton.core.Instruction.SetInstruction
							.make(mi));
				}
			}
		}

		final Deque<Collection<Pr<State, SortedSet<MapItem>>>> unmarkedStates = new ArrayDeque<>();
		final Set<Collection<Pr<State, SortedSet<MapItem>>>> states = new HashSet<>();

		unmarkedStates.add(i);

		for (Collection<Pr<State, SortedSet<MapItem>>> t; !unmarkedStates
				.isEmpty();) {
			t = unmarkedStates.pop();
			markedStates.add(t);
			for (final char a : inputRangeExemplaries()) {
				final Collection<Pr<State, SortedSet<MapItem>>> k = reach(t, a);
				final Collection<Pr<State, SortedSet<MapItem>>> u = closure(k);
				final Collection<MapItem> newStates = newStates(u, k);

				findReusableState(u);
				final List<Instruction> c = new ArrayList<>();
				addInstructions(k, c);

			}

		}

	}

	private Set<MapItem> extractMIs(
			final Collection<Pr<State, SortedSet<MapItem>>> oldState) {
		final Set<MapItem> oldMIs = new LinkedHashSet<>();
		for (final Pr<State, SortedSet<MapItem>> pr : oldState) {
			for (final MapItem mi : pr.getSecond()) {
				oldMIs.add(mi);
			}
		}
		return oldMIs;
	}

	SortedSet<MapItem> getSame(final SortedSet<MapItem> k, final Tag tag) {
		final MapItem from = new MapItem(tag, 0);
		final MapItem to = new MapItem(new MarkerTag(tag.getGroup() + 1), 0);
		return k.subSet(from, to);
	}

	NavigableSet<Triple> initClosure(
			final Collection<Pr<State, SortedSet<MapItem>>> S) {
		final NavigableSet<Triple> closure = new TreeSet<>();
		for (final Pr<State, SortedSet<MapItem>> pr : S) {
			closure.add(new Triple(pr.getFirst(), 0, pr.getSecond()));
		}
		return closure;
	}

	char[] inputRangeExemplaries() {
		throw null;
	}

	int minimumX(final SortedSet<MapItem> k, final Tag tag,
			final Set<MapItem> range) {
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

	Collection<MapItem> newStates(
			final Collection<Pr<State, SortedSet<MapItem>>> oldState,
			final Collection<Pr<State, SortedSet<MapItem>>> newState) {
		final Set<MapItem> oldMIs = extractMIs(oldState);
		final Set<MapItem> newMIs = extractMIs(newState);
		newMIs.removeAll(oldMIs);
		return Collections.unmodifiableSet(newMIs);
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

	Collection<Pr<State, SortedSet<MapItem>>> reach(
			final Collection<Pr<State, SortedSet<MapItem>>> state, final char a) {
		final List<Pr<State, SortedSet<MapItem>>> ret = new ArrayList<>();

		for (final Pr<State, SortedSet<MapItem>> pr : state) {
			final SortedSet<MapItem> k = pr.getSecond();
			final Collection<TransitionTriple> ts = tnfa
					.availableTransitionsFor(pr.getFirst(), a);
			for (final TransitionTriple t : ts) {
				ret.add(new Pr<>(t.getState(), k));
			}
		}
		return Collections.unmodifiableList(ret);
	}

	Collection<Pr<State, SortedSet<MapItem>>> removePriorities(
			final NavigableSet<Triple> closure) {
		final Collection<Pr<State, SortedSet<MapItem>>> ret = new ArrayList<>(
				closure.size());
		for (final Triple t : closure) {
			ret.add(new Pr<>(t.state, t.mapItems));
		}
		return Collections.unmodifiableCollection(ret);
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

	public Triple(final State state, final int priority,
			final SortedSet<MapItem> mapItems) {
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
		result = prime * result
				+ ((mapItems == null) ? 0 : mapItems.hashCode());
		result = prime * result + priority;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "[" + mapItems + ", " + priority + ", " + state + "]";
	}
}
