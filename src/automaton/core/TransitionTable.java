package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

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
		CaptureGroup last = new RealCaptureGroup(-1);

		synchronized CaptureGroup next() {
			last = RealCaptureGroup.make(last.getNumber() + 1);
			return last;
		}
	}

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

	static abstract class RealTransitionTable<T> implements TransitionTable {

		static class TDFATransitionTable extends
				RealTransitionTable<Pair<State, Tag>> {
			TDFATransitionTable(
					final TreeMap<Pair<State, InputRange>, Pair<State, Tag>> map) {
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
				final Pair<State, Tag> pair = getEntry(state, character);
				return pair.getSecond();
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
				final Pair<State, Tag> pair = getEntry(state, character);
				return pair.getFirst();
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
			@Override
			public void put(final State startingState, final InputRange range,
					final State endingState, final Tag tag) {
				// TODO Some overlapping tests
				this.transitions.put(new Pair<>(startingState, range),
						new Pair<>(endingState, tag));
			}

		}

		static class TNFATransitionTable extends
				RealTransitionTable<Collection<Pair<State, Tag>>> {
			public static class Builder {

				final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();

				final TreeMap<Pair<State, InputRange>, Collection<Pair<State, Tag>>> transitions = new TreeMap<>();

				public void addEndTagTransition(final Collection<State> froms,
						final State to, final CaptureGroup captureGroup) {
					for (final State from : froms) {
						put(from, InputRange.EPSILON, to,
								captureGroup.getEndTag());
					}
				}

				public void addStartTagTransition(
						final Collection<State> froms, final State to,
						final CaptureGroup cg) {
					for (final State from : froms) {
						put(from, InputRange.EPSILON, to, cg.getStartTag());
					}
				}

				public TNFATransitionTable build() {
					return new TNFATransitionTable(
							Collections.unmodifiableSortedMap(transitions));
				}

				public SortedMap<Pair<State, InputRange>, Collection<Pair<State, Tag>>> getTransitions() {
					return Collections.unmodifiableSortedMap(transitions);
				}

				public CaptureGroup makeCaptureGroup() {
					return captureGroupMaker.next();
				}

				public void put(final State startingState,
						final InputRange range, final State endingState,
						final Tag tag) {
					// TODO Some overlapping tests
					assert startingState != null && range != null;
					final Pair<State, InputRange> key = new Pair<>(
							startingState, range);

					Collection<Pair<State, Tag>> col = transitions.get(key);
					if (col == null) {
						col = new ArrayList<>();
						transitions.put(key, col);
					}
					col.add(new Pair<>(endingState, tag));
				}

			}

			public static Builder builder() {
				return new Builder();
			}

			TNFATransitionTable(
					final SortedMap<Pair<State, InputRange>, Collection<Pair<State, Tag>>> transitions) {
				super(transitions);
			}

			public Collection<Pair<State, Tag>> nextAvailableTransitions(
					final State state, final Character input) {
				final Collection<Pair<State, Tag>> ret = getEntry(state, input);
				if (ret == null) {
					return Collections.emptyList();
				}
				assert ret != null;
				return ret;
			}

			@Override
			public void put(final State startingState, final InputRange range,
					final State endingState, final Tag tag) {
				assert false; // TODO delete.
				throw new RuntimeException("Not implemented");
			}

		}

		final SortedMap<Pair<State, InputRange>, T> transitions;

		RealTransitionTable(
				final SortedMap<Pair<State, InputRange>, T> transitions) {
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
			final SortedMap<Pair<State, InputRange>, T> tail = transitions
					.tailMap(new Pair<>(state, searched));
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
		public abstract void put(State startingState, InputRange range,
				State endingState, Tag tag);

		@Override
		public String toString() {
			return transitions.toString();
		}
	}

	public static final class TransitionTableTest {

		@Test
		public void getState() {

			final Builder tb = TNFATransitionTable.builder();

			// From states
			final State q1 = new State();
			final State q2 = new State();
			final State q3 = new State();

			// To states
			final State q4 = new State();
			final State q5 = new State();
			final State q6 = new State();
			final State q7 = new State();
			final State q8 = new State();
			final State q9 = new State();

			// Some input ranges
			final InputRange i1 = InputRange.make('a', 'd');
			final InputRange i2 = InputRange.make('k', 'o');
			final InputRange i3 = InputRange.make('y', 'z');

			// Creating some transitions

			tb.put(q2, i1, q4, null);
			tb.put(q2, i3, q8, null);
			tb.put(q3, i2, q9, null);
			tb.put(q2, i2, q3, null);
			tb.put(q4, i3, q2, null);
			tb.put(q1, i1, q7, null);
			tb.put(q3, i1, q3, null);
			tb.put(q1, i2, q6, null);
			tb.put(q3, i3, q5, null);
			tb.put(q1, i3, q3, null);
			tb.put(q4, i2, q2, null);
			final TNFATransitionTable t = tb.build();

			// Verify existing transitions
			assertThat(t.nextAvailableTransitions(q2, 'c').iterator().next()
					.getFirst(), is(q4));

			assertThat(t.nextAvailableTransitions(q3, 'z').iterator().next()
					.getFirst(), is(q5));
			assertThat(t.nextAvailableTransitions(q1, 'k').iterator().next()
					.getFirst(), is(q6));
			assertThat(t.nextAvailableTransitions(q1, 'a').iterator().next()
					.getFirst(), is(q7));
			assertThat(t.nextAvailableTransitions(q2, 'y').iterator().next()
					.getFirst(), is(q8));
			assertThat(t.nextAvailableTransitions(q3, 'o').iterator().next()
					.getFirst(), is(q9));

			// Verify missing transitions
			// assertThat(t.getState(q4, 'c'), is(nullValue()));
			// assertThat(t.getState(q6, 'a'), is(nullValue()));
		}
	}

}
