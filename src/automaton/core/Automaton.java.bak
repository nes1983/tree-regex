package automaton.core;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;
import automaton.instructions.SequenceOfInstructions;

public class Automaton {
	public static class Builder {

		private SortedSet<InputRange> alphabet;
		private Set<State> finalStates;
		private State initialState;
		private Set<State> states;
		private TransitionTable transitionTable;

		Builder() {
			reset();
		}

		/**
		 * Add a {@link Character} into the alphabet
		 * 
		 * @param input
		 *            the {@link Character} to add
		 */
		private void addAlphabet(final InputRange input) {
			checkNotNull(input);
			shouldNotIntersect(input);
			alphabet.add(input);
		}

		/**
		 * Construct {@link TDFA} by adding new transition.
		 * 
		 * @param startingState
		 *            Starting {@link State} of the transition
		 * @param range
		 *            Assigned {@link Character}s of the transition
		 * @param endingState
		 *            Ending {@link State} of the transition
		 * @param instruction
		 *            Assigned {@link SequenceOfInstructions} to execute when
		 *            using the transition
		 */
		// public TDFA build() {
		// TDFA tdfa = (TDFA) new RealNFA(checkNotNull(states),
		// checkNotNull(alphabet),
		// checkNotNull(transitionTable), checkNotNull(initialState),
		// checkNotNull(finalStates));
		//
		// reset();
		//
		// return tdfa;
		// }

		public void addTransition(final State startingState,
				final InputRange range, final State endingState,
				final SequenceOfInstructions instruction) {
			this.states.add(startingState);
			this.states.add(endingState);
			this.addAlphabet(range);
			assert invariant();
		}

		private Set<InputRange> getAlphabetFrom(
				final TransitionTable transitionTable2) {
			throw null;
		}

		/**
		 * Verify the consistency of the {@link TDFA}
		 * 
		 * @return <code>true</code> if the {@link TDFA} is consistent,
		 *         <code>false</code> otherwise
		 */
		protected boolean invariant() {
			final Set<InputRange> alphabetOfTransitions = getAlphabetFrom(transitionTable);
			return alphabetOfTransitions.equals(alphabet);
		}

		void reset() {
			this.states = new HashSet<>();
			this.alphabet = new TreeSet<>();
			initialState = null;
			initialState = null;

			transitionTable = new TNFATransitionTable();

			finalStates = new HashSet<>();
		}

		public void setAlphabet(final SortedSet<InputRange> alphabet) {
			this.alphabet = alphabet;
		}

		public void setFinalStates(final Set<State> finalStates) {
			this.finalStates = finalStates;
		}

		/**
		 * Define the initial {@link State} of the {@link TDFA}
		 * 
		 * @param initialState
		 *            The new initial {@link State} of {@link TDFA}
		 */
		public void setInitialState(final State initialState) {
			this.initialState = checkNotNull(initialState);
		}

		public void setStates(final Set<State> states) {
			this.states = states;
		}

		public void setTransitionTable(final TransitionTable transitionTable) {
			this.transitionTable = transitionTable;
		}

		void shouldNotIntersect(final InputRange input) {
			throw null;
		}

	}

	public static interface TDFA extends TNFA {
	}

	public static <T> T checkNotNull(final T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}
}
