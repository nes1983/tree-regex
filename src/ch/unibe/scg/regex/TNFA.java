package ch.unibe.scg.regex;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.TransitionTriple.Priority;

interface TNFA {
  static class RealNFA implements TNFA {
    final State finalState;
    final State initialState;
    final TNFATransitionTable transitionTable;
    final List<Tag> tags;

    RealNFA(final TNFATransitionTable transitionTable, final State initialState,
        final State finalState, List<Tag> tags) {
      this.transitionTable = transitionTable;
      this.initialState = requireNonNull(initialState);
      this.finalState = requireNonNull(finalState);
      this.tags = tags;
    }

    static class Builder {
      final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();
      State finalState;
      State initialState;
      final TNFATransitionTable.Builder transitionTableBuilder = TNFATransitionTable.builder();
      final List<Tag> tags = new ArrayList<>();

      public void addEndTagTransition(final Collection<State> from, final State to,
          final CaptureGroup captureGroup, final Priority priority) {
        transitionTableBuilder.addEndTagTransition(from, to, captureGroup, priority);
      }

      public void addStartTagTransition(final Collection<State> from, final State to,
          final CaptureGroup captureGroup, final Priority priority) {
        transitionTableBuilder.addStartTagTransition(from, to, captureGroup, priority);
      }

      public void addUntaggedTransition(final InputRange inputRange, final Collection<State> from,
          final Collection<State> to, final Priority priority) {
        for (final State t : to) {
          addUntaggedTransition(inputRange, from, t, priority);
        }
      }

      public void addUntaggedTransition(final InputRange any, final Collection<State> from,
          final State to, final Priority priority) {
        assert any != null && from != null && to != null;
        for (final State f : from) {
          addUntaggedTransition(any, f, to, priority);
        }
      }

      public void addUntaggedTransition(final InputRange inputRange, final State from,
          final State to, final Priority priority) {
        assert from != null;
        assert to != null;
        transitionTableBuilder.put(from, inputRange, to, priority, Tag.NONE);
      }

      public RealNFA build() {
        return new RealNFA(transitionTableBuilder.build(), initialState,
            finalState, new ArrayList<>(tags));
      }

      public CaptureGroup makeCaptureGroup(CaptureGroup parent) {
        return captureGroupMaker.next(parent);
      }

      public State makeInitialState() {
        initialState = State.get();
        return initialState;
      }

      /** @return a new non-final state */
      public State makeState() {
        return State.get();
      }

      public void makeUntaggedEpsilonTransitionFromTo(final Collection<State> from,
          final Collection<State> to, final Priority priority) {
        addUntaggedTransition(InputRange.EPSILON, from, to, priority);
      }

      /** Sets the argument to be the single final state of the automaton. Must be called exactly once. */
      public void setAsAccepting(final State finalState) {
        if (this.finalState != null) {
          throw new IllegalStateException("Only one final state can be handled.\n"
              + String.format("Old final state was %s\n New final state is %s", this.finalState, finalState));
        }
        this.finalState = finalState;
      }

      public void registerCaptureGroup(CaptureGroup cg) {
        assert tags.size() / 2 == cg.number;
        tags.add(cg.startTag);
        tags.add(cg.endTag);
      }
    }

    @Override
    public Collection<InputRange> allInputRanges() {
      return transitionTable.allInputRanges();
    }

    @Override
    public Collection<Tag> allTags() {
      return transitionTable.allTags();
    }

    @Override
    public Collection<TransitionTriple> availableTransitionsFor(final State state,
        final Character input) {
      return transitionTable.nextAvailableTransitions(state, input);
    }

    @Override
    public State getInitialState() {
      return initialState;
    }

    @Override
    public boolean isAccepting(final State state) {
      return finalState.equals(state);
    }

    @Override
    public State getFinalState() {
      return finalState;
    }

    @Override
    public String toString() {
      try (final Formatter formatter = new Formatter()) {
        final String ret =
            formatter.format("%s -> %s, %s", initialState, finalState, transitionTable).toString();
        return ret;
      }
    }

    @Override
    public List<Tag> getTags() {
      return tags;
    }
  }

  Collection<InputRange> allInputRanges();

  Collection<Tag> allTags();

  public Collection<TransitionTriple> availableTransitionsFor(State state, Character input);

  /**
   * @return the initial {@link State}.
   */
  public State getInitialState();

  /**
   * @param state not null.
   * @return whether or not {@code state} accepting. True if it is, false otherwise.
   */
  public boolean isAccepting(State state);

  public State getFinalState();

  public List<Tag> getTags();
}
