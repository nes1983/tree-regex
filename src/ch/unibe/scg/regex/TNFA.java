package ch.unibe.scg.regex;

import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.TransitionTable.RealTransitionTable.TNFATransitionTable;
import ch.unibe.scg.regex.TransitionTriple.Priority;

interface TNFA {
  static class RealNFA implements TNFA {
    final Set<State> finalStates;
    final State initialState;
    final TNFATransitionTable transitionTable;

    RealNFA(final TNFATransitionTable transitionTable, final State initialState,
        final Set<State> finalStates) {
      this.transitionTable = transitionTable;
      this.initialState = initialState;
      this.finalStates = finalStates;
    }

    static class Builder {
      final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();
      final Set<State> finalStates = new TreeSet<>();
      State initialState;
      final TNFATransitionTable.Builder transitionTableBuilder = TNFATransitionTable.builder();

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
            Collections.unmodifiableSet(finalStates));
      }

      public CaptureGroup makeCaptureGroup(CaptureGroup parent) {
        return captureGroupMaker.next(parent);
      }

      public State makeInitialState() {
        initialState = State.get();
        return RegexToNFA.checkNotNull(initialState);
      }

      /** @return a new non-final state */
      public State makeState() {
        return State.get();
      }

      public void makeUntaggedEpsilonTransitionFromTo(final Collection<State> from,
          final Collection<State> to, final Priority priority) {
        addUntaggedTransition(InputRange.EPSILON, from, to, priority);
      }

      public void setAsAccepting(final State finishing) {
        finalStates.add(finishing);
      }
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

    public Set<State> getFinalStates() {
      return finalStates;
    }

    @Override
    public String toString() {
      final Formatter formatter = new Formatter();
      final String ret =
          formatter.format("%s -> %s, %s", initialState, finalStates, transitionTable).toString();
      formatter.close();
      return ret;
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

  public Set<State> getFinalStates();
}
