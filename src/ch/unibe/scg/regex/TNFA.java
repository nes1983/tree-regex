package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.TransitionTriple.Priority;

class TNFA {
  private final Map<Pair<State, InputRange>, Collection<TransitionTriple>> transitions;
  private final Map<State, Collection<TransitionTriple>> epsilonTransitions;
  final State initialState;
  final State finalState;
  final List<Tag> tags;

  TNFA(Map<Pair<State, InputRange>, Collection<TransitionTriple>> transitions,
      Map<State, Collection<TransitionTriple>> epsilonTransitions, State initialState,
      State finalState, List<Tag> tags) {
    this.transitions = transitions;
    this.epsilonTransitions = epsilonTransitions;
    this.initialState = initialState;
    this.finalState = finalState;
    this.tags = tags;
  }

  static class Builder {
    final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();
    State finalState;
    State initialState;
    final List<Tag> tags = new ArrayList<>();
    final NavigableSet<InputRange> allInputRanges;
    final Map<Pair<State, InputRange>, Collection<TransitionTriple>> transitions = new LinkedHashMap<>();
    final Map<State, Collection<TransitionTriple>> epsilonTransitions = new LinkedHashMap<>();

    private Builder(NavigableSet<InputRange> allInputRanges) {
      this.allInputRanges = allInputRanges;
    }

    static Builder make(Collection<InputRange> uncleanInputRanges) {
      return new Builder(new TreeSet<>(new InputRangeCleanup().cleanUp(uncleanInputRanges)));
    }

    private void putEpsilon(final State state, final State endingState, final Priority priority,
        final Tag tag) {
      if (!epsilonTransitions.containsKey(state)) {
        epsilonTransitions.put(state, new ArrayList<TransitionTriple>());
      }
      epsilonTransitions.get(state).add(new TransitionTriple(endingState, priority, tag));
    }

    void addEndTagTransition(final Collection<State> froms, final State to,
        final CaptureGroup captureGroup, final Priority priority) {
      for (final State from : froms) {
        putEpsilon(from, to, priority, captureGroup.endTag);
      }
    }

    void addStartTagTransition(final Collection<State> froms, final State to,
        final CaptureGroup cg, final Priority priority) {
      for (final State from : froms) {
        putEpsilon(from, to, priority, cg.startTag);
      }
    }

    /** Add untagged transitions for all input that are overlapped by {@code overlappedBy}. */
    void addUntaggedTransition(final InputRange overlappedBy, final Collection<State> froms,
        final State to, final Priority priority) {
      for (final State from : froms) {
        NavigableSet<InputRange> overlappedRanges =
            allInputRanges.subSet(
              InputRange.make(overlappedBy.getFrom()), true,
              InputRange.make(overlappedBy.getTo()), true);
        for (InputRange ir : overlappedRanges) {
          final Pair<State, InputRange> key = new Pair<>(from, ir);
          if (!transitions.containsKey(key)) {
            transitions.put(key, new ArrayList<TransitionTriple>());
          }
          transitions.get(key).add(new TransitionTriple(to, priority, Tag.NONE));
        }
      }
    }

    void makeUntaggedEpsilonTransitionFromTo(final Collection<State> froms,
        final Collection<State> tos, final Priority priority) {
      for (State from : froms) {
        for (State to : tos) {
          putEpsilon(from, to, priority, Tag.NONE);
        }
      }
    }

    TNFA build() {
      return new TNFA(transitions, epsilonTransitions, initialState, finalState, tags);
    }

    CaptureGroup makeCaptureGroup(CaptureGroup parent) {
      return captureGroupMaker.next(parent);
    }

    State makeInitialState() {
      initialState = State.get();
      return initialState;
    }

    /** @return a new non-final state */
    State makeState() {
      return State.get();
    }

    /**
     * Sets the argument to be the single final state of the automaton. Must be called exactly once.
     */
    void setAsAccepting(final State finalState) {
      if (this.finalState != null) {
        throw new IllegalStateException("Only one final state can be handled.\n"
            + String.format("Old final state was %s%n New final state is %s", this.finalState,
                finalState));
      }
      this.finalState = finalState;
    }

    void registerCaptureGroup(CaptureGroup cg) {
      assert tags.size() / 2 == cg.number;
      tags.add(cg.startTag);
      tags.add(cg.endTag);
    }
  }

  /** @return all input ranges as they are, possibly with duplicates. */
  Collection<InputRange> allInputRanges() {
    final List<InputRange> ret = new ArrayList<>();

    for (final Pair<State, InputRange> range : transitions.keySet()) {
      final InputRange inputRange = range.second;
      ret.add(inputRange);
    }

    return ret;
  }

  /** @return all tags used anywhere, in any order, without duplicates. */
  Collection<Tag> allTags() {
    final Set<Tag> ret = new HashSet<>();

    Collection<Collection<TransitionTriple>> all =
        new ArrayList<>(transitions.size() + epsilonTransitions.size());
    all.addAll(transitions.values());
    all.addAll(epsilonTransitions.values());

    for (final Collection<TransitionTriple> triples : all) {
      for (final TransitionTriple triple : triples) {
        final Tag tag = triple.getTag();
        if (tag.isEndTag() || tag.isStartTag()) {
          ret.add(tag);
        }
      }
    }

    return ret;
  }

  Collection<TransitionTriple> availableTransitionsFor(State key, InputRange ir) {
    Collection<TransitionTriple> ret = transitions.get(new Pair<>(key, ir));
    if (ret == null) {
      return Collections.emptyList();
    }
    return ret;
  }

  Collection<TransitionTriple> availableEpsilonTransitionsFor(State q) {
    Collection<TransitionTriple> ret = epsilonTransitions.get(q);
    if (ret == null) {
      return Collections.emptyList();
    }
    return ret;
  }

  @Override
  public String toString() {
    return String.format("%s -> %s, %s, %s", initialState, finalState, transitions, epsilonTransitions);
  }
}
