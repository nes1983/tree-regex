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
			public static final int DEFAULT = 0;
			public final static int GREEDY = 0;
			public final static int NON_GREEDY = Integer.MIN_VALUE;

			Set<State> finalStates = new TreeSet<>();
			State initialState;
			TNFATransitionTable.Builder transitionTableBuilder = TNFATransitionTable
					.builder();

			public void addEndTagTransition(final Collection<State> from, final State to,
					final CaptureGroup captureGroup, final int priority) {
				transitionTableBuilder.addEndTagTransition(from, to, captureGroup,
						priority);
			}

			public void addStartTagTransition(final Collection<State> from,
					final State to, final CaptureGroup captureGroup, final int priority) {
				transitionTableBuilder.addStartTagTransition(from, to, captureGroup,
						priority);
			}

			public void addUntaggedTransition(final InputRange inputRange,
					final Collection<State> from, final Collection<State> to,
					final int priority) {
				for (final State t : to) {
					addUntaggedTransition(inputRange, from, t, priority);
				}
			}

			public void addUntaggedTransition(final InputRange any,
					final Collection<State> from, final State to, final int priority) {
				assert any != null && from != null && to != null;
				for (final State f : from) {
					addUntaggedTransition(any, f, to, priority);
				}
			}

			public void addUntaggedTransition(final InputRange inputRange,
					final State from, final State to, final int priority) {
				assert from != null;
				assert to != null;
				transitionTableBuilder.put(from, inputRange, to, priority, Tag.NONE);
			}

			public RealNFA build() {
				return new RealNFA(transitionTableBuilder.build(), initialState,
						Collections.unmodifiableSet(finalStates));
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

			public void makeUntaggedEpsilonTransitionFromTo(final Collection<State> from,
					final Collection<State> to, final int priority) {
				addUntaggedTransition(InputRange.EPSILON, from, to, priority);
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

		RealNFA(final TNFATransitionTable transitionTable, final State initialState,
				final Set<State> finalStates) {
			super();
			this.transitionTable = transitionTable;
			this.initialState = initialState;
			this.finalStates = finalStates;
		}

		public Collection<InputRange> allInputRanges() {
			return transitionTable.allInputRanges();
		}

		public Collection<Tag> allTags() {
			return transitionTable.allTags();
		}

		public Collection<TransitionTriple> availableTransitionsFor(final State state,
				final Character input) {
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

	Collection<InputRange> allInputRanges();

	Collection<Tag> allTags();

	public Collection<TransitionTriple> availableTransitionsFor(State state,
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