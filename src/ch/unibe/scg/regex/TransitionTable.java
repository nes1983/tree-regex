package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.TNFAToTDFA.DFAState;
import ch.unibe.scg.regex.Tag.MarkerTag;
import ch.unibe.scg.regex.TransitionTable.TDFATransitionTable.Builder.Entry;
import ch.unibe.scg.regex.TransitionTriple.Priority;

/**
 * A {@link TransitionTable} is the set of all possible transition of a {@link TDFA}
 * 
 * @author Niko Schwarz, Fabien Dubosson
 */
interface TransitionTable {
  static class NextDFAState {
    List<Instruction> instructions;
    DFAState nextState;

    public NextDFAState(List<Instruction> instructions, DFAState nextState) {
      this.instructions = instructions;
      this.nextState = nextState;
    }

    public List<Instruction> getInstructions() {
      return instructions;
    }

    public DFAState getNextState() {
      return nextState;
    }

    @Override
    public String toString() {
      return nextState.toString() + " " + instructions;
    }
  }

  static class NextState {
    List<Instruction> instructions;
    int nextState;

    public NextState(final int nextState, final List<Instruction> instructions) {
      super();
      this.nextState = nextState;
      this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
      return instructions;
    }

    public int getNextState() {
      return nextState;
    }
  }

  static abstract class RealTransitionTable<T> implements TransitionTable {
    static class TNFATransitionTable extends RealTransitionTable<Collection<TransitionTriple>> {
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
          return new TNFATransitionTable(transitions); // There's no
          // unmodifiable
          // navigable
          // set :(
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
        super(transitions);
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
            if (!(tag instanceof MarkerTag)) {
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
    }

    final NavigableMap<Pair<State, InputRange>, T> transitions;

    RealTransitionTable(final NavigableMap<Pair<State, InputRange>, T> transitions) {
      super();
      this.transitions = transitions;
    }

    /**
     * Get the {@link Pair} of {@link State} and {@link SequenceOfInstructions} assigned when
     * starting from a {@link State} with a specified {@link Character}
     * 
     * @param state The starting {@link State}
     * @param character The specified {@link Character}. May be null. If so, only epsilon
     *        transitions are returned.
     * @return The {@link Pair} of {@link State} and {@link SequenceOfInstructions}. Null if there
     *         isn't one.
     */
    T getEntry(final State state, final Character character) {
      final InputRange searched =
          character != null ? InputRange.make(character, character) : InputRange.EPSILON;
      final Pair<State, InputRange> searchMarker = new Pair<>(state, searched);
      final SortedMap<Pair<State, InputRange>, T> tail =
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

    @Override
    public String toString() {
      return transitions.toString();
    }
  }

  static class TDFATransitionTable {
    // TODO optimizations: use int for state, then lookup state by
    // states[state].

    static class Builder {
      static class Entry implements Comparable<Entry> {
        final char from, to;
        final List<Instruction> instructions;
        final int state, newState;
        final DFAState toDFA;

        public Entry(final char from, final char to, final List<Instruction> instructions,
            final int state, final int newState, DFAState toDFA) {
          this.from = from;
          this.to = to;
          this.instructions = instructions;
          this.state = state;
          this.newState = newState;
          this.toDFA = toDFA;
        }

        public int compareTo(final Entry o) {
          final int cmp = Integer.compare(state, o.state);
          if (cmp != 0) {
            return cmp;
          }
          return Character.compare(from, o.from);
        }

        @Override
        public String toString() {
          return "q" + state + "-" + from + "-" + to + " -> q" + newState + " " + instructions;
        }

      }

      static class Mapping {
        int lastCreatedState = -1;

        final Map<DFAState, Integer> mapping = new LinkedHashMap<>();

        public int lookup(final DFAState state) {
          final Integer to = mapping.get(state);
          if (to != null) {
            return to;
          }
          final int next = next();
          mapping.put(state, next);
          return next;
        }

        int next() {
          return ++lastCreatedState;
        }
      }

      final Mapping mapping = new Mapping();
      final NavigableSet<Entry> transitions = new TreeSet<>();

      public void addTransition(final DFAState t, final InputRange inputRange,
          final DFAState newState, final List<Instruction> instructions) {

        final Entry e =
            new Entry(inputRange.getFrom(), inputRange.getTo(), instructions, mapping.lookup(t),
                mapping.lookup(newState), t);
        transitions.add(e);
      }

      public NextDFAState availableTransition(DFAState t, char a) {
        final int fromState = mapping.lookup(t);
        final List<Instruction> emptyList = Collections.emptyList();
        final Entry probe = new Entry(a, a, emptyList, fromState, -1, null);
        final NavigableSet<Entry> headSet = transitions.headSet(probe, true);
        if (headSet.isEmpty()) {
          return null;
        }
        final Entry found = headSet.last();
        if (found.state != probe.state || !(found.from <= a && a <= found.to)) {
          return null;
        }
        return new NextDFAState(found.instructions, found.toDFA);
      }

      public TDFATransitionTable build() {
        final Iterator<Entry> transitionsIter = transitions.iterator();
        final int size = transitions.size();
        final char[] froms = new char[size];
        @SuppressWarnings("unchecked")
        // Suppress seems unavoidable. Checked on Stackoverflow.
        final List<Instruction>[] instructions = new List[size];
        final int[] newStates = new int[size];
        final int[] states = new int[size];
        final char[] tos = new char[size];

        for (int i = 0; i < size; i++) {
          final Entry e = transitionsIter.next();
          froms[i] = e.from;
          tos[i] = e.to;
          states[i] = e.state;
          newStates[i] = e.newState;
          instructions[i] = e.instructions;
        }
        assert !transitionsIter.hasNext();
        return new TDFATransitionTable(froms, tos, states, newStates, instructions);
      }
    }

    final char[] froms;
    final List<Instruction>[] instructions;
    final int[] newStates;
    final int size;
    final int[] states;
    final char[] tos;

    TDFATransitionTable(final char[] froms, final char[] tos, final int[] states,
        final int[] newStates, final List<Instruction>[] instructions) {
      this.size = froms.length;
      assert tos.length == size && states.length == size && froms.length == size
          && newStates.length == size && instructions.length == size;
      this.froms = froms;
      this.tos = tos;
      this.states = states;
      this.newStates = newStates;
      this.instructions = instructions;
    }

    private int cmp(final int state, final char input, final int x) {
      final int scmp = Integer.compare(states[x], state); // XXX order?
      if (scmp != 0) {
        return scmp;
      }
      return Character.compare(input, froms[x]);
    }

    public NextState newStateAndInstructions(final int state, final char input) {
      int l = 0;
      int r = size - 1;
      int x = -1;
      while (r >= l) {
        x = (l + r) / 2;
        final int cmp = cmp(state, input, x);
        if (cmp == 0) {
          return new NextState(newStates[x], instructions[x]);
        } else if (cmp < 0) {
          r = x - 1;
        } else if (cmp > 0) {
          l = x + 1;
        } else {
          throw new AssertionError();
        }
      }

      assert x != -1;

      for (int i = -1; i <= 1; i++) {
        final int y = x + i;

        if (y < 0 || y >= size) {
          continue;
        }

        if (Integer.compare(states[y], state) == 0 && froms[y] <= input && input <= tos[y]) {
          return new NextState(newStates[y], instructions[y]);
        }
      }

      return null;
    }


    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < size; i++) {
        final Entry e =
            new Builder.Entry(froms[i], tos[i], instructions[i], states[i], newStates[i], null);
        sb.append(e.toString());
        sb.append('\n');
      }
      return sb.toString();
    }
  }
}
