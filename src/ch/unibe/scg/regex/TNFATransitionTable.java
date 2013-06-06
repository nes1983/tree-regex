package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.Tag.NoTag;
import ch.unibe.scg.regex.TransitionTriple.Priority;

/**
 * All possible transitions from one state to another.
 * 
 * @author Niko Schwarz, Fabien Dubosson
 */
class TNFATransitionTable {
  final NavigableMap<Pair<State, InputRange>, Collection<TransitionTriple>> transitions;

  public static class Builder {
    final CaptureGroupMaker captureGroupMaker = new CaptureGroupMaker();

    final TreeMap<Pair<State, InputRange>, Collection<TransitionTriple>> transitions =
        new TreeMap<>();

    public void addEndTagTransition(final Collection<State> froms, final State to,
        final CaptureGroup captureGroup, final Priority priority) {
      for (final State from : froms) {
        put(from, InputRange.EPSILON, to, priority, captureGroup.getEndTag());
      }
    }

    public void addStartTagTransition(final Collection<State> froms, final State to,
        final CaptureGroup cg, final Priority priority) {
      for (final State from : froms) {
        put(from, InputRange.EPSILON, to, priority, cg.getStartTag());
      }
    }

    public TNFATransitionTable build() {
      return new TNFATransitionTable(transitions);
      // There's no unmodifiable navigable set :(
    }

    public void put(final State startingState, final InputRange range, final State endingState,
        final Priority priority, final Tag tag) {
      // TODO Some overlapping tests
      assert startingState != null && range != null;
      final Pair<State, InputRange> key = new Pair<>(startingState, range);

      Collection<TransitionTriple> col = transitions.get(key);
      if (col == null) {
        col = new ArrayList<>();
        transitions.put(key, col);
      }
      col.add(new TransitionTriple(endingState, priority, tag));
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  TNFATransitionTable(
      final NavigableMap<Pair<State, InputRange>, Collection<TransitionTriple>> transitions) {
    this.transitions = transitions;
  }

  Collection<TransitionTriple> getEntry(final State state, final Character character) {
    // TODO(niko) get rid of Character, and use a char instead.
    final InputRange searched =
        character != null ? InputRange.make(character, character) : InputRange.EPSILON;
    final Pair<State, InputRange> searchMarker = new Pair<>(state, searched);
    final SortedMap<Pair<State, InputRange>, Collection<TransitionTriple>> tail =
        transitions.descendingMap().tailMap(searchMarker);
    // headMap and tailMap are different.
    // One is inclusive, the other is not. Therefore, reverse.
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

  public Collection<InputRange> allInputRanges() {
    final List<InputRange> ret = new ArrayList<>();
    for (final Pair<State, InputRange> range : transitions.keySet()) {
      final InputRange inputRange = range.getSecond();
      if (!(inputRange instanceof InputRange.SpecialInputRange)) {
        ret.add(inputRange);
      }
    }
    return ret;
  }

  public Collection<Tag> allTags() {
    final Set<Tag> ret = new LinkedHashSet<>();
    for (final Collection<TransitionTriple> triples : transitions.values()) {
      for (final TransitionTriple triple : triples) {
        final Tag tag = triple.getTag();
        if (!(tag instanceof NoTag)) {
          ret.add(tag);
        }
      }
    }
    return ret;
  }

  public Collection<TransitionTriple> nextAvailableTransitions(final State state,
      final Character input) {
    final Collection<TransitionTriple> ret = getEntry(state, input);
    if (ret == null) {
      return Collections.emptyList();
    }
    assert ret != null;
    return ret;
  }

  @Override
  public String toString() {
    return transitions.toString();
  }
}
