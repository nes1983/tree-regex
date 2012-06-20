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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import automaton.core.Pair.Pr;
import automaton.core.TNFAToTDFA.MapItems.MapItem;
import automaton.core.Tag.MarkerTag;

class TNFAToTDFA {

	static class DFAState {
		final Collection<Pair<State, Tag>> states;

		public DFAState(final Collection<Pair<State, Tag>> states) {
			super();
			this.states = states;
		}

		public Collection<Pair<State, Tag>> getStates() {
			return states;
		}

	}

	static class MapItems implements Iterable<MapItem> {

		static class MapItem implements Comparable<MapItem> {
			final int pos;
			final Tag tag;

			public MapItem(final Tag tag, final int pos) {
				super();
				this.tag = tag;
				this.pos = pos;
			}

			public int compareTo(final MapItem o) {
				final int comp = this.tag.compareTo(o.tag);
				if (comp != 0) {
					return comp;
				}
				return this.pos - o.pos;
			}

			@Override
			public String toString() {
				return "MapItem[" + pos + ", " + tag + "]";
			}
		}

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
			return new Iterator<TNFAToTDFA.MapItems.MapItem>() {

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

		@Before
		public void setUp() {
			tnfa = mock(TNFA.class);
			tnfa2tdfa = new TNFAToTDFA(tnfa);

		}

		@Test
		public void testReach() {
			final Pair<State, Tag> pr = mock(Pair.class);
			final DFAState tState = new DFAState(Arrays.asList(pr));
			final State s = mock(State.class);
			final Tag t = mock(Tag.class);
			when(
					tnfa.availableTransitionsFor(eq(s),
							Mockito.any(Character.class))).thenReturn(
					Arrays.asList(pr));
			when(pr.getFirst()).thenReturn(s);
			when(pr.getSecond()).thenReturn(t);

			final List<Pair<State, Tag>> states = tnfa2tdfa.reach(tState, 'b');

			assertThat(states.size(), is(1));
			assertThat(states.get(0).getFirst(), is(s));
			assertThat(states.get(0).getSecond(), is(t));

		}
	}

	static class Triple {
		public final SortedSet<MapItem> mapItems;
		public final int priority;
		public final State state;

		public Triple(final State state, final int priority,
				final SortedSet<MapItem> mapItems) {
			this.state = state;
			this.priority = priority;
			this.mapItems = mapItems;
		}
	}

	TNFA tnfa;

	public TNFAToTDFA(final TNFA tnfa) {
		this.tnfa = tnfa;
	}

	public void closure(final Collection<Pr<State, SortedSet<MapItem>>> S) {
		final Deque<Triple> stack = new ArrayDeque<>();
		for (final Pr<State, SortedSet<MapItem>> pr : S) {
			stack.push(new Triple(pr.getFirst(), 0, pr.getSecond()));
		}
		final Collection<Pr<State, SortedSet<MapItem>>> closure = new ArrayList<>(
				S);
		while (!stack.isEmpty()) {
			State s;
			int p;
			SortedSet<MapItem> k;
			{
				final Triple t = stack.pop();
				s = t.state;
				p = t.priority;
				k = t.mapItems;
			}
			for (final Pair<State, Tag> transition : tnfa
					.availableTransitionsFor(s, null)) {
				final Tag tag = transition.getSecond();
				if (!tag.equals(Tag.NONE)) {
					final MapItem toBeRemoved = toBeRemoved(k, tag);
					k.remove(toBeRemoved);
					final SortedSet<MapItem> range = getSame(k, tag);
				}
			}
		}
	}

	SortedSet<MapItem> getSame(final SortedSet<MapItem> k, final Tag tag) {
		final MapItem from = new MapItem(tag, 0);
		final MapItem to = new MapItem(new MarkerTag(tag.getGroup() + 1), 0);
		return k.subSet(from, to);
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
	 * @param t
	 * @param a
	 * @return
	 */

	public List<Pair<State, Tag>> reach(final DFAState t, final char a) {
		final List<Pair<State, Tag>> ret = new ArrayList<>();
		for (final Pair<State, Tag> pr : t.getStates()) {
			final Collection<Pair<State, Tag>> ts = tnfa
					.availableTransitionsFor(pr.getFirst(), a); // TODO switch
																// to
																// InputRange.
			for (final Pair<State, Tag> pr2 : ts) {
				ret.add(new Pair<>(pr2.getFirst(), pr.getSecond()));
			}
		}
		return Collections.unmodifiableList(ret);
	}

	MapItem toBeRemoved(final SortedSet<MapItem> k, final Tag tag) {
		final SortedSet<MapItem> sames = getSame(k, tag);
		if (k.isEmpty()) {
			return null;
		}
		return sames.first();
	}
}
