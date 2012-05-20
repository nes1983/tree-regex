package automaton.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;
import automaton.core.TransitionTable.Tag;
import automaton.instructions.SequenceOfInstructions;

public class Automaton {
	public static interface TNFA{

		public State getInitialState();

		public boolean isAccepting(State state); 
		
		public Collection<Pair<State, Tag>> availableTransitionsFor(State state, Character input);
	}
	
	public static interface TDFA extends TNFA {}
	
	public static class Builder {

		private Set<State> states;
		private SortedSet<InputRange> alphabet;
		private TransitionTable transitionTable;
		private State initialState;
		private Set<State> finalStates;
		
		Builder() {
			reset();
		}
		
		void reset() {
			 this.states = new HashSet<>();
			 this.alphabet = new TreeSet<>();
			 initialState = null;
			 initialState = null;
			 
			 transitionTable = new TNFATransitionTable();

			 finalStates = new HashSet<>();
		}

		public void setStates(Set<State> states) {
			this.states = states;
		}

		public void setAlphabet(SortedSet<InputRange> alphabet) {
			this.alphabet = alphabet;
		}

		public void setTransitionTable(TransitionTable transitionTable) {
			this.transitionTable = transitionTable;
		}

		public void setFinalStates(Set<State> finalStates) {
			this.finalStates = finalStates;
		}

		/**
		 * Define the initial {@link State} of the {@link TDFA}
		 * 
		 * @param initialState
		 *            The new initial {@link State} of {@link TDFA}
		 */
		public void setInitialState(State initialState) {
			this.initialState = checkNotNull(initialState);
		}

		/**
		 * Add a {@link Character} into the alphabet
		 * 
		 * @param input
		 *            the {@link Character} to add
		 */
		private void addAlphabet(InputRange input) {
			checkNotNull(input);
			shouldNotIntersect(input);
			alphabet.add(input);
		}

		void shouldNotIntersect(InputRange input) {
			throw null;
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
//		public TDFA build() {
//			TDFA tdfa = (TDFA) new RealNFA(checkNotNull(states), checkNotNull(alphabet),
//					checkNotNull(transitionTable), checkNotNull(initialState),
//					checkNotNull(finalStates));
//
//			reset();
//
//			return tdfa;
//		}

		public void addTransition(State startingState, InputRange range,
				State endingState, SequenceOfInstructions instruction) {
			this.states.add(startingState);
			this.states.add(endingState);
			this.addAlphabet(range);
			assert invariant();
		}

		/**
		 * Verify the consistency of the {@link TDFA}
		 * 
		 * @return <code>true</code> if the {@link TDFA} is consistent,
		 *         <code>false</code> otherwise
		 */
		protected boolean invariant() {
			Set<InputRange> alphabetOfTransitions = getAlphabetFrom(transitionTable);
			return alphabetOfTransitions.equals(alphabet);
		}

		private Set<InputRange> getAlphabetFrom(TransitionTable transitionTable2) {
			throw null;
		}

	}

	public static <T> T checkNotNull(T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}

	public static class RealNFA implements TNFA {

		/**
		 * {@link Set} of {@link State}s
		 */
		private final Set<State> states;

		/**
		 * {@link Set} of valid {@link Character}s
		 */
		private final SortedSet<InputRange> alphabet;

		/**
		 * {@link TransitionTable} representing all possible transition in
		 * {@link TDFA}
		 */
		private final TNFATransitionTable transitionTable;

		/**
		 * Initial {@link State}
		 */
		private final State initialState;

		/**
		 * {@link Set} of final {@link State}s
		 */
		private final Set<State> finalStates;

		RealNFA(Set<State> states, SortedSet<InputRange> alphabet,
				TNFATransitionTable transitionTable, State initialState,
				Set<State> finalStates) {
			super();
			this.states = states;
			this.alphabet = alphabet;
			this.transitionTable = transitionTable;
			this.initialState = initialState;
			this.finalStates = finalStates;
		}
		
		public boolean isAccepting(State state) {
			return finalStates.contains(state);
		}
		
		/**
		 * Return an unmodifiable {@link Set} of all {@link State}s of {@link TDFA}
		 * 
		 * @return an unmodifiable {@link Set} of {@link State}s of {@link TDFA}
		 */
		public Set<State> getStates() {
			return Collections.unmodifiableSet(states);
		}

		/**
		 * Return an unmodifiable {@link Set} of all {@link Character}s of
		 * {@link TDFA}
		 * 
		 * @return an unmodifiable {@link Set} of {@link Character}s of {@link TDFA}
		 */
		public Set<InputRange> getAlphabet() {
			return Collections.unmodifiableSet(alphabet);
		}

		/**
		 * The initial {@link State} of {@link TDFA}
		 * 
		 * @return the initial {@link State} of {@link TDFA}
		 */
		public State getInitialState() {
			return initialState;
		}
		
		/**
		 * Return an unmodifiable {@link Set} of all final {@link State}s of
		 * {@link TDFA}
		 * 
		 * @return an unmodifiable {@link Set} of final {@link State}s of
		 *         {@link TDFA}
		 */
		public Set<State> getFinalStates() {
			return finalStates;
		}

		/**
		 * Calculate the alphabet of a {@link TransitionTable}
		 * 
		 * @param transitionTable2
		 * @return the {@link Set} of {@link Character}s representing the alphabet
		 */
		private static Set<InputRange> getAlphabetFrom(
				TransitionTable transitionTable2) {
			throw null;
		}

		@Override
		public Collection<Pair<State, Tag>> availableTransitionsFor(
				State state, Character input) {
			return transitionTable.nextAvailableTransitions(state, input);
		}

	}
}
