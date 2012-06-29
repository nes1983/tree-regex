package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;
import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable.Builder;
import automaton.core.TransitionTable.TDFATransitionTable.Builder.Entry;

/**
 * A {@link TransitionTable} is the set of all possible transition of a
 * {@link TDFA}
 * 
 * @author Niko Schwarz, Fabien Dubosson
 */
interface TransitionTable {

	static class CaptureGroupMaker {
		/**
		 * Don't instantiate directly. Use {@link CaptureGroupMaker} instead.
		 */
		static class RealCaptureGroup implements CaptureGroup {
			static RealCaptureGroup make(final int number) {
				final RealCaptureGroup cg = new RealCaptureGroup(number);
				cg.startTag = Tag.RealTag.makeStartTag(cg);
				cg.endTag = Tag.RealTag.makeEndTag(cg);
				return cg;
			}

			Tag endTag;
			final int number;

			Tag startTag;

			RealCaptureGroup(final int number) {
				super();
				this.number = number;
			}

			public Tag getEndTag() {
				assert endTag != null;
				return endTag;
			}

			public int getNumber() {
				return number;
			}

			public Tag getStartTag() {
				assert startTag != null;
				return startTag;
			}

			@Override
			public String toString() {
				return "g" + number;
			}

		}

		CaptureGroup last = new RealCaptureGroup(-1);

		synchronized CaptureGroup next() {
			last = RealCaptureGroup.make(last.getNumber() + 1);
			return last;
		}
	}

	public static class DFATableTest {
		final int s1 = 0;
		final int s2 = 1;
		final int s3 = 2;
		final int s4 = 3;
		TDFATransitionTable table;

		@Before
		public void setUp() {
			table = new TDFATransitionTable(new char[] { 'c', 'l' },
					new char[] { 'k', 'm' }, new int[] { s1, s2 }, new int[] {
							s3, s4 }, new List[] { Collections.EMPTY_LIST,
							Collections.EMPTY_LIST });
		}

		@Test
		public void testTable() {
			final NextState pr = table.newStateAndInstructions(200, 'd');
			assertThat(pr, is(nullValue()));
		}

		@Test
		public void testTable2() {
			final NextState pr = table.newStateAndInstructions(s1, 'c');
			assertThat(pr.getNextState(), is(s3));

		}

		@Test
		public void testTable3() {
			final NextState pr = table.newStateAndInstructions(s1, 'k');
			assertThat(pr.getNextState(), is(s3));

		}

		@Test
		public void testTable4() {
			final NextState pr = table.newStateAndInstructions(s1, 'l');
			assertThat(pr, is(nullValue()));

		}

		@Test
		public void testTable5() {
			final NextState pr = table.newStateAndInstructions(s2, 'l');
			assertThat(pr.getNextState(), is(s4));
		}

	}

	public static class DFATTableBuilderTEst {
		automaton.core.TransitionTable.TDFATransitionTable.Builder builder;

		@Before
		public void setUp() {
			builder = new TDFATransitionTable.Builder();
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		public void testBuilder() {
			final Map q0 = mock(Map.class);
			final Map q1 = mock(Map.class);

			builder.addTransition(q0, new InputRange('a', 'c'), q1,
					(List) Collections.emptyList());

			final TDFATransitionTable dfa = builder.build();
			assertThat(dfa.toString(), is("q0-a-c -> q1"));
			final NextState pr = dfa.newStateAndInstructions(0, 'b');
			assertThat(pr.getNextState(), is(1));
			assertThat(pr.getInstructions(), is(Collections.EMPTY_LIST));

		}
	}

	static class NextState {
		List<Instruction> instructions;
		int nextState;

		public NextState(final int nextState,
				final List<Instruction> instructions) {
			super();
			this.nextState = nextState;
			this.instructions = instructions;
		}

		public List<Instruction> getInstructions() {
			return instructions;
		}

		public int getNextState() {
			return nextState;
		}
	}

	static abstract class RealTransitionTable<T> implements TransitionTable {
		//
		// static class TDFATransitionTable extends
		// RealTransitionTable<TransitionTriple> {
		//
		// TDFATransitionTable(
		// final TreeMap<Pair<State, InputRange>, TransitionTriple> map) {
		// super(map);
		// }
		//
		// /**
		// * Get the {@link SequenceOfInstructions} associated with the
		// * transition starting from a {@link State} with a specified
		// * {@link Character}.
		// *
		// * @param state
		// * The starting {@link State}
		// * @param character
		// * The specified {@link Character}
		// * @return The {@link SequenceOfInstructions} associated with the
		// * transition
		// */
		// public Tag getTag(final State state, final Character character) {
		// final TransitionTriple triple = getEntry(state, character);
		// return triple.getTag();
		// }
		//
		// /**
		// * Get the {@link State} reached from another {@link State} with a
		// * specific {@link Character}
		// *
		// * @param state
		// * The starting {@link State}
		// * @param character
		// * The specified {@link Character}
		// * @return The {@link State} reached by the transition
		// */
		// public State nextStateFor(final State state,
		// final Character character) {
		// final TransitionTriple pair = getEntry(state, character);
		// return pair.getState();
		// }
		//
		// }

		static class TNFATransitionTable extends
				RealTransitionTable<Collection<TransitionTriple>> {
			public static class Builder {

				final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();

				final TreeMap<Pair<State, InputRange>, Collection<TransitionTriple>> transitions = new TreeMap<>();

				public void addEndTagTransition(final Collection<State> froms,
						final State to, final CaptureGroup captureGroup,
						final int priority) {
					for (final State from : froms) {
						put(from, InputRange.EPSILON, to, priority,
								captureGroup.getEndTag());
					}
				}

				public void addStartTagTransition(
						final Collection<State> froms, final State to,
						final CaptureGroup cg, final int priority) {
					for (final State from : froms) {
						put(from, InputRange.EPSILON, to, priority,
								cg.getStartTag());
					}
				}

				public TNFATransitionTable build() {
					return new TNFATransitionTable(transitions); // There's no
																	// unmodifiable
																	// navigable
																	// set :(
				}

				public CaptureGroup makeCaptureGroup() {
					return captureGroupMaker.next();
				}

				public void put(final State startingState,
						final InputRange range, final State endingState,
						final int priority, final Tag tag) {
					// TODO Some overlapping tests
					assert startingState != null && range != null;
					final Pair<State, InputRange> key = new Pair<>(
							startingState, range);

					Collection<TransitionTriple> col = transitions.get(key);
					if (col == null) {
						col = new ArrayList<>();
						transitions.put(key, col);
					}
					col.add(new TransitionTriple(endingState, priority, tag));

				}
			}

			public static Builder builder() {
				return new Builder();
			}

			TNFATransitionTable(
					final NavigableMap<Pair<State, InputRange>, Collection<TransitionTriple>> transitions) {
				super(transitions);
			}

			public Collection<InputRange> allInputRanges() {
				final List<InputRange> ret = new ArrayList<>();
				for (final Pair<State, InputRange> range : transitions.keySet()) {
					ret.add(range.getSecond());
				}
				return ret;
			}

			public Collection<TransitionTriple> nextAvailableTransitions(
					final State state, final Character input) {
				final Collection<TransitionTriple> ret = getEntry(state, input);
				if (ret == null) {
					return Collections.emptyList();
				}
				assert ret != null;
				return ret;
			}

			public void put(final State startingState, final InputRange range,
					final State endingState, final Tag tag) {
				assert false; // TODO delete.
				throw new RuntimeException("Not implemented");
			}

		}

		final NavigableMap<Pair<State, InputRange>, T> transitions;

		RealTransitionTable(
				final NavigableMap<Pair<State, InputRange>, T> transitions) {
			super();
			this.transitions = transitions;
		}

		/**
		 * Get the {@link Pair} of {@link State} and
		 * {@link SequenceOfInstructions} assigned when starting from a
		 * {@link State} with a specified {@link Character}
		 * 
		 * @param state
		 *            The starting {@link State}
		 * @param character
		 *            The specified {@link Character}. May be null. If so, only
		 *            epsilon transitions are returned.
		 * @return The {@link Pair} of {@link State} and
		 *         {@link SequenceOfInstructions}. Null if there isn't one.
		 */
		T getEntry(final State state, final Character character) {
			final InputRange searched = character != null ? InputRange.make(
					character, character) : InputRange.EPSILON;
			final Pair<State, InputRange> searchMarker = new Pair<>(state,
					searched);
			final SortedMap<Pair<State, InputRange>, T> tail = transitions
					.descendingMap().tailMap(searchMarker); // headMap and
															// tailMap are
															// different.
			// One is inclusive, the other is not. Therefore, reverse.
			if (tail.isEmpty()) {
				return null;
			}
			final Pair<State, InputRange> pair = tail.firstKey();
			if (!pair.getFirst().equals(state)) {
				return null;
			}
			if (character != null && !pair.getSecond().contains(character)) {
				return null;
			} // TODO what if character == null?
			return transitions.get(tail.firstKey());
		}

		@Override
		public String toString() {
			return transitions.toString();
		}
	}

	static class TDFATransitionTable {
		// XXX optimizations: use int for state, then lookup state by
		// states[state].

		static class Builder {
			static class Entry implements Comparable<Entry> {
				final char from, to;
				final List<Instruction> instructions;
				final int state, newState;

				public Entry(final char from, final char to,
						final List<Instruction> instructions, final int state,
						final int newState) {
					this.from = from;
					this.to = to;
					this.instructions = instructions;
					this.state = state;
					this.newState = newState;
				}

				public int compareTo(final Entry o) {
					final int cmp = Integer.compare(state, o.state);
					if (cmp != 0) {
						return cmp;
					}
					return Character.compare(from, o.from);
				}

				@Override
				public String toString() {
					return "q" + state + "-" + from + "-" + to + " -> q"
							+ newState + " " + instructions;
				}

			}

			static class Mapping {
				int lastCreatedState = -1;

				final Map<Map<State, SortedSet<MapItem>>, Integer> mapping = new HashMap<>();

				public int lookup(final Map<State, SortedSet<MapItem>> state) {
					final Integer to = mapping.get(state);
					if (to != null) {
						return to;
					}
					final int next = next();
					mapping.put(state, next);
					return next;
				}

				int next() {
					return ++lastCreatedState;
				}
			}

			final Mapping mapping = new Mapping();
			final List<Entry> transitions = new ArrayList<>();

			public void addTransition(
					final Map<State, SortedSet<MapItem>> fromState,
					final InputRange inputRange,
					final Map<State, SortedSet<MapItem>> newState,
					final List<Instruction> instructions) {

				final Entry e = new Entry(inputRange.getFrom(),
						inputRange.getTo(), instructions,
						mapping.lookup(fromState), mapping.lookup(newState));
				transitions.add(e);
			}

			public TDFATransitionTable build() {
				Collections.sort(transitions);
				final int size = transitions.size();
				final char[] froms = new char[size];
				@SuppressWarnings("unchecked")
				final List<Instruction>[] instructions = new List[size];
				final int[] newStates = new int[size];
				final int[] states = new int[size];
				final char[] tos = new char[size];

				for (int i = 0; i < size; i++) {
					final Entry e = transitions.get(i);
					froms[i] = e.from;
					tos[i] = e.to;
					states[i] = e.state;
					newStates[i] = e.newState;
					instructions[i] = e.instructions;
				}
				return new TDFATransitionTable(froms, tos, states, newStates,
						instructions);
			}
		}

		final char[] froms;
		final List<Instruction>[] instructions;
		final int[] newStates;
		final int size;
		final int[] states;
		final char[] tos;

		TDFATransitionTable(final char[] froms, final char[] tos,
				final int[] states, final int[] newStates,
				final List<Instruction>[] instructions) {
			this.size = froms.length;
			assert tos.length == size && states.length == size
					&& froms.length == size && newStates.length == size
					&& instructions.length == size;
			this.froms = froms;
			this.tos = tos;
			this.states = states;
			this.newStates = newStates;
			this.instructions = instructions;
		}

		private int cmp(final int state, final char input, final int x) {
			final int scmp = Integer.compare(states[x], state); // XXX order?
			if (scmp != 0) {
				return scmp;
			}
			return Character.compare(input, froms[x]);
		}

		public NextState newStateAndInstructions(final int state,
				final char input) {
			int l = 0;
			int r = size - 1;
			int x = -1;
			while (r >= l) {
				x = (l + r) / 2;
				final int cmp = cmp(state, input, x);
				if (cmp == 0) {
					return new NextState(newStates[x], instructions[x]);
				} else if (cmp < 0) {
					r = x - 1;
				} else if (cmp > 0) {
					l = x + 1;
				} else {
					assert false;
				}
			}

			assert x != -1;

			for (int i = -1; i <= 1; i++) {
				final int y = x + i;

				if (y < 0 || y >= size) {
					continue;
				}

				if (Integer.compare(states[y], state) == 0 && froms[y] <= input
						&& input <= tos[y]) {
					return new NextState(newStates[y], instructions[y]);
				}
			}

			return null;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				final Entry e = new Builder.Entry(froms[i], tos[i],
						instructions[i], states[i], newStates[i]);
				sb.append(e.toString());
				sb.append('\n');
			}
			return sb.toString();
		}
	}

	public static final class TransitionTableTest {

		@Test
		public void crapTest() {

			final Builder tb = TNFATransitionTable.builder();

			// From states
			final State q0 = State.get();
			final State q1 = State.get();
			final State q2 = State.get();

			// To states
			final State q3 = State.get();
			final State q4 = State.get();
			final State q5 = State.get();
			final State q6 = State.get();
			final State q7 = State.get();
			final State q8 = State.get();

			// Some input ranges
			final InputRange i1 = InputRange.make('a', 'd');
			final InputRange i2 = InputRange.make('k', 'o');
			final InputRange i3 = InputRange.make('y', 'z');

			// Creating some transitions

			tb.put(q1, i1, q3, 0, null);
			tb.put(q1, i3, q7, 0, null);
			tb.put(q2, i2, q8, 0, null);
			tb.put(q1, i2, q2, 0, null);
			tb.put(q3, i3, q1, 0, null);
			tb.put(q0, i1, q6, 0, null);
			tb.put(q2, i1, q2, 0, null);
			tb.put(q0, i2, q5, 0, null);
			tb.put(q2, i3, q4, 0, null);
			tb.put(q0, i3, q2, 0, null);
			tb.put(q3, i2, q1, 0, null);
			final TNFATransitionTable t = tb.build();

			// Verify existing transitions
			assertThat(t.nextAvailableTransitions(q1, 'c').iterator().next()
					.getState(), is(q3));

			assertThat(t.nextAvailableTransitions(q2, 'z').iterator().next()
					.getState(), is(q4));

			assertThat(t.nextAvailableTransitions(q0, 'k').iterator().next()
					.getState(), is(q5));
			assertThat(t.nextAvailableTransitions(q0, 'a').iterator().next()
					.getState(), is(q6));
			assertThat(t.nextAvailableTransitions(q1, 'y').iterator().next()
					.getState(), is(q7));
			assertThat(t.nextAvailableTransitions(q2, 'o').iterator().next()
					.getState(), is(q8));

			// Verify missing transitions
			// assertThat(t.getState(q4, 'c'), is(nullValue()));
			// assertThat(t.getState(q6, 'a'), is(nullValue()));
		}

		@Test
		public void crapTest2() {

			final Builder tb = TNFATransitionTable.builder();

			// From states
			final State q0 = State.get();
			final State q1 = State.get();
			final State q2 = State.get();

			// To states
			final State q3 = State.get();
			final State q4 = State.get();
			final State q5 = State.get();
			final State q6 = State.get();
			final State q7 = State.get();
			final State q8 = State.get();

			// Some input ranges
			final InputRange ad = InputRange.make('a', 'd');
			final InputRange ko = InputRange.make('k', 'o');
			final InputRange yz = InputRange.make('y', 'z');

			// Creating some transitions

			tb.put(q1, ad, q3, 0, null);
			tb.put(q1, yz, q7, 0, null);
			tb.put(q2, ko, q8, 0, null);
			tb.put(q1, ko, q2, 0, null);
			tb.put(q3, yz, q1, 0, null);
			tb.put(q0, ad, q6, 0, null);
			tb.put(q2, ad, q2, 0, null);
			tb.put(q0, ko, q5, 0, null);
			tb.put(q2, yz, q4, 0, null);
			tb.put(q0, yz, q2, 0, null);
			tb.put(q3, ko, q1, 0, null);
			final TNFATransitionTable t = tb.build();
			// Verify existing transitions

			assertThat(t.nextAvailableTransitions(q1, 'z').iterator().next()
					.getState(), is(q7));
			assertThat(t.nextAvailableTransitions(q0, 'k').iterator().next()
					.getState(), is(q5));
			assertThat(t.nextAvailableTransitions(q0, 'a').iterator().next()
					.getState(), is(q6));
			assertThat(t.nextAvailableTransitions(q1, 'y').iterator().next()
					.getState(), is(q7));
			assertThat(t.nextAvailableTransitions(q2, 'o').iterator().next()
					.getState(), is(q8));

			// Verify missing transitions
			// assertThat(t.getState(q4, 'c'), is(nullValue()));
			// assertThat(t.getState(q6, 'a'), is(nullValue()));
		}

		@Before
		public void setUp() {
			State.resetCount();
		}

		@Test
		public void tableTest() {

			final Builder tb = TNFATransitionTable.builder();

			final State q0 = State.get();
			final State q1 = State.get();

			final InputRange ad = InputRange.make('a', 'd');
			tb.put(q0, ad, q1, 0, null);

			final TNFATransitionTable t = tb.build();

			assertThat(t.nextAvailableTransitions(q0, 'c').iterator().next()
					.getState(), is(q1));

		}

		@Test
		public void tableTest2() {

			final Builder tb = TNFATransitionTable.builder();

			final State q0 = State.get();
			final State q1 = State.get();

			final State q2 = State.get();
			final State q3 = State.get();

			final InputRange ad = InputRange.make('a', 'd');
			final InputRange ko = InputRange.make('k', 'o');

			tb.put(q0, ad, q2, 0, null);
			tb.put(q1, ko, q3, 0, null);
			tb.put(q0, ko, q2, 0, null);

			final TNFATransitionTable t = tb.build();
			assertThat(
					t.toString(),
					is("{(q0, a-d)=[q2, 0, null], (q0, k-o)=[q2, 0, null], (q1, k-o)=[q3, 0, null]}"));
		}

	}

}
