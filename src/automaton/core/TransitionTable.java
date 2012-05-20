package automaton.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import automaton.core.Automaton.TDFA;
import automaton.instructions.SequenceOfInstructions;

/**
 * A {@link TransitionTable} is the set of all possible transition of a
 * {@link TDFA}
 * 
 * @author Fabien Dubosson, Niko Schwarz
 */
public interface TransitionTable {

	static abstract class RealTransitionTable<T> implements TransitionTable {

		final SortedMap<Pair<State, InputRange>, T> transitions;

		RealTransitionTable(SortedMap<Pair<State, InputRange>, T> transitions) {
			super();
			this.transitions = transitions;
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

		/**
		 * Get the {@link Pair} of {@link State} and
		 * {@link SequenceOfInstructions} assigned when starting from a
		 * {@link State} with a specified {@link Character}
		 * 
		 * @param state
		 *            The starting {@link State}
		 * @param character
		 *            The specified {@link Character}
		 * @return The {@link Pair} of {@link State} and
		 *         {@link SequenceOfInstructions}
		 */
		T getEntry(State state, Character character) {
			InputRange searched = new InputRange(character, character);
			SortedMap<Pair<State, InputRange>, T> tail = transitions
					.tailMap(new Pair<>(state, searched));
			Pair<State, InputRange> pair = tail.firstKey();
			if (!pair.getFirst().equals(state))
				return null;
			if (!pair.getSecond().contains(character))
				return null;
			return transitions.get(tail.firstKey());
		}

		static class TNFATransitionTable extends
				RealTransitionTable<Collection<Pair<State, Tag>>> {

			TNFATransitionTable() {
				super(
						new TreeMap<Pair<State, InputRange>, Collection<Pair<State, Tag>>>());
			}

			public void put(State startingState, InputRange range,
					State endingState, Tag tag) {
				// TODO Some overlapping tests

				Pair<State, InputRange> key = new Pair<>(startingState, range);

				Collection<Pair<State, Tag>> col = transitions.get(key);
				if (col == null) {
					col = new ArrayList<>();
					transitions.put(key, col);
				}
				col.add(new Pair<>(endingState, tag));
			}

			public Collection<Pair<State, Tag>> nextAvailableTransitions(
					State state, Character input) {
				Collection<Pair<State, Tag>> ret = getEntry(state, input);
				if (ret == null) {
					return Collections.emptyList();
				}
				return ret;

			}

		}

		static class TDFATransitionTable extends
				RealTransitionTable<Pair<State, Tag>> {
			TDFATransitionTable() {
				super(new TreeMap<Pair<State, InputRange>, Pair<State, Tag>>());
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
			public void put(State startingState, InputRange range,
					State endingState, Tag tag) {
				// TODO Some overlapping tests
				this.transitions.put(new Pair<>(startingState, range),
						new Pair<>(endingState, tag));
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
			public State nextStateFor(State state, Character character) {
				Pair<State, Tag> pair = getEntry(state, character);
				return pair.getFirst();
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
			public Tag getTag(State state, Character character) {
				Pair<State, Tag> pair = getEntry(state, character);
				return pair.getSecond();
			}

		}
	}

	public static interface Tag extends Comparable<Tag> {

		int getGroup();

	}

}
