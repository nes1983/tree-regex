package automaton.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;

import automaton.core.TransitionTable.RealTransitionTable.TNFATransitionTable;

interface TNFA {

	static class RealNFA implements TNFA {
		static class Builder {
			Set<State> finalStates = new TreeSet<>();
			State initialState;
			TNFATransitionTable.Builder transitionTableBuilder = TNFATransitionTable
					.builder();

			public void addEndTagTransition(final Collection<State> from,
					final State to, final CaptureGroup captureGroup) {
				transitionTableBuilder.addEndTagTransition(from, to,
						captureGroup);
			}

			public void addStartTagTransition(final Collection<State> from,
					final State to, final CaptureGroup captureGroup) {
				transitionTableBuilder.addStartTagTransition(from, to,
						captureGroup);
			}

			public void addUntaggedTransition(final InputRange inputRange,
					final Collection<State> from, final Collection<State> to) {
				for (final State t : to) {
					addUntaggedTransition(inputRange, from, t);
				}
			}

			public void addUntaggedTransition(final InputRange any,
					final Collection<State> from, final State to) {
				assert any != null && from != null && to != null;
				for (final State f : from) {
					addUntaggedTransition(any, f, to);
				}
			}

			public void addUntaggedTransition(final InputRange inputRange,
					final State from, final State to) {
				assert from != null;
				assert to != null;
				transitionTableBuilder.put(from, inputRange, to, Tag.NONE);
			}

			public RealNFA build() {
				return new RealNFA(transitionTableBuilder.build(),
						initialState, Collections.unmodifiableSet(finalStates));
			}

			public CaptureGroup makeCaptureGroup() {
				return transitionTableBuilder.makeCaptureGroup();
			}

			public State makeInitialState() {
				initialState = State.get();
				return RegexToNFA.checkNotNull(initialState);
			}

			public State makeState() {
				return State.get();
			}

			public void makeUntaggedEpsilonTransitionFromTo(
					final Collection<State> from, final Collection<State> to) {
				addUntaggedTransition(InputRange.EPSILON, from, to);
			}

			public void setAsAccepting(final State finishing) {
				finalStates.add(finishing);
			}

		}

		public static Builder builder() {
			return new Builder();
		}

		final Set<State> finalStates;
		final State initialState;

		final TNFATransitionTable transitionTable;

		RealNFA(final TNFATransitionTable transitionTable,
				final State initialState, final Set<State> finalStates) {
			super();
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

		@Override
		public String toString() {
			final Formatter formatter = new Formatter();
			final String ret = formatter.format("%s -> %s, %s", initialState,
					finalStates, transitionTable).toString();
			formatter.close();
			return ret;
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