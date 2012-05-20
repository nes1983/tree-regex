package automaton.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;
import automaton.core.TransitionTable.Tag;

interface TNFA {

	static class RealNFA implements TNFA {
		static class Builder {
			Set<State> finalStates;
			State initialState;
			Set<State> states;
			TNFATransitionTable transitionTable;

			public RealNFA build() {
				return new RealNFA(Collections.unmodifiableSet(states),
						transitionTable, initialState,
						Collections.unmodifiableSet(finalStates));
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		final Set<State> finalStates;
		final State initialState;
		final Set<State> states;

		final TNFATransitionTable transitionTable;

		RealNFA(final Set<State> states,
				final TNFATransitionTable transitionTable,
				final State initialState, final Set<State> finalStates) {
			super();
			this.states = states;
			this.transitionTable = transitionTable;
			this.initialState = initialState;
			this.finalStates = finalStates;
		}

		public Collection<Pair<State, Tag>> availableTransitionsFor(
				final State state, final Character input) {
			return transitionTable.nextAvailableTransitions(state, input);
		}

		public State getInitialState() {
			return initialState;
		}

		public boolean isAccepting(final State state) {
			return finalStates.contains(state);
		}

	}

	public Collection<Pair<State, Tag>> availableTransitionsFor(State state,
			Character input);

	/**
	 * @return the initial {@link State}.
	 */
	public State getInitialState();

	/**
	 * 
	 * @param state
	 * @return whether or not {@code state} accepting. True if it is, false
	 *         otherwise.
	 */
	public boolean isAccepting(State state);
}