package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import automaton.core.Automaton.TDFA;
import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;
import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable.Builder;
import automaton.instructions.SequenceOfInstructions;

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

	// /**
	// * Compares only the first entry in a pair.
	// */
	// static class PairComparator<A extends Comparable<A>, B extends
	// Comparable<B>>
	// implements Comparator<Pair<A, B>> {
	//
	// @Override
	// public int compare(final Pair<A, B> o1, final Pair<A, B> o2) {
	// return o1.getFirst().compareTo(o2.getFirst());
	// }
	//
	// }

	static abstract class RealTransitionTable<T> implements TransitionTable {

		static class TDFATransitionTable extends
				RealTransitionTable<TransitionTriple> {
			TDFATransitionTable(
					final TreeMap<Pair<State, InputRange>, TransitionTriple> map) {
				super(map);
			}

			/**
			 * Get the {@link SequenceOfInstructions} associated with the
			 * transition starting from a {@link State} with a specified
			 * {@link Character}.
			 * 
			 * @param state
			 *            The starting {@link State}
			 * @param character
			 *            The specified {@link Character}
			 * @return The {@link SequenceOfInstructions} associated with the
			 *         transition
			 */
			public Tag getTag(final State state, final Character character) {
				final TransitionTriple triple = getEntry(state, character);
				return triple.getTag();
			}

			/**
			 * Get the {@link State} reached from another {@link State} with a
			 * specific {@link Character}
			 * 
			 * @param state
			 *            The starting {@link State}
			 * @param character
			 *            The specified {@link Character}
			 * @return The {@link State} reached by the transition
			 */
			public State nextStateFor(final State state,
					final Character character) {
				final TransitionTriple pair = getEntry(state, character);
				return pair.getState();
			}

			/**
			 * Put a new transition in the {@link TransitionTable}
			 * 
			 * @param startingState
			 *            The starting {@link State} of the transition
			 * @param range
			 *            The {@link Character}s representing the transition
			 * @param endingState
			 *            The ending {@link State} of the transition
			 * @param instruction
			 *            The {@link SequenceOfInstructions} to be executed when
			 *            using the transition
			 */
			public void put(final State startingState, final InputRange range,
					final State endingState, final Tag tag, final int priority) {
				// TODO Some overlapping tests
				this.transitions.put(new Pair<>(startingState, range),
						new TransitionTriple(endingState, priority, tag));
			}

		}

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
