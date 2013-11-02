package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

class TDFATransitionTable {
  static class NextState {
    final Iterable<Instruction> instructions;
    final int nextState;

    NextState(final int nextState, final Iterable<Instruction> instructions) {
      this.nextState = nextState;
      this.instructions = instructions;
    }

    Iterable<Instruction> getInstructions() {
      return instructions;
    }

    int getNextState() {
      return nextState;
    }
  }

  static class NextDFAState {
    final Iterable<Instruction> instructions;
    final DFAState nextState;

    NextDFAState(Iterable<Instruction> instructions, DFAState nextState) {
      this.instructions = instructions;
      this.nextState = nextState;
    }

    @Override
    public String toString() {
      return nextState.toString() + " " + instructions;
    }
  }

  static class Builder {
    static class Entry implements Comparable<Builder.Entry> {
      final char from, to;
      final Iterable<Instruction> instructions;
      final int state, newState;
      final DFAState toDFA;

      public Entry(final char from, final char to, final Iterable<Instruction> c,
          final int state, final int newState, DFAState toDFA) {
        this.from = from;
        this.to = to;
        this.instructions = c;
        this.state = state;
        this.newState = newState;
        this.toDFA = toDFA;
      }

      @Override
      public int compareTo(final Builder.Entry o) {
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
      private int lastCreatedState = -1;

      /** Map from full DFAState to optimized state (an int) */
      final Map<DFAState, Integer> mapping = new LinkedHashMap<>();

      /** Map from optimized state (an integer) to full DFAState. */
      final List<DFAState> deoptimized = new ArrayList<>();

      int lookupOrMake(final DFAState state) {
        final Integer to = mapping.get(state);
        if (to != null) {
          return to;
        }

        lastCreatedState++;
        final int next = lastCreatedState;

        mapping.put(state, next);

        deoptimized.add(state);
        assert deoptimized.get(lastCreatedState).equals(state);

        return next;
      }
    }

    final Builder.Mapping mapping = new Mapping();
    final NavigableSet<Builder.Entry> transitions = new TreeSet<>();

    public void addTransition(final DFAState t, final InputRange inputRange,
        final DFAState newState, final Iterable<Instruction> c) {

      final Builder.Entry e =
          new Entry(inputRange.getFrom(), inputRange.getTo(), c,
              mapping.lookupOrMake(t), mapping.lookupOrMake(newState), newState);
      transitions.add(e);
    }

    public NextDFAState availableTransition(DFAState t, char a) {
      final Integer fromState = mapping.mapping.get(t);
      if (fromState == null) {
        return null;
      }
      final List<Instruction> emptyList = Collections.emptyList();
      final Builder.Entry probe = new Entry(a, a, emptyList, fromState, -1, null);
      final NavigableSet<Builder.Entry> headSet = transitions.headSet(probe, true);
      if (headSet.isEmpty()) {
        return null;
      }
      final Builder.Entry found = headSet.last();
      if (found.state != probe.state || !(found.from <= a && a <= found.to)) {
        return null;
      }
      return new NextDFAState(found.instructions, found.toDFA);
    }

    public TDFATransitionTable build() {
      final Iterator<Builder.Entry> transitionsIter = transitions.iterator();
      final int size = transitions.size();
      final char[] froms = new char[size];
      @SuppressWarnings("unchecked")
      // Suppress seems unavoidable. Checked on Stackoverflow.
      final Iterable<Instruction>[] instructions = new Iterable[size];
      final int[] newStates = new int[size];
      final int[] states = new int[size];
      final char[] tos = new char[size];

      for (int i = 0; i < size; i++) {
        final Builder.Entry e = transitionsIter.next();
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
  final Iterable<Instruction>[] instructions;
  final int[] newStates;
  final int size;
  final int[] states;
  final char[] tos;

  TDFATransitionTable(final char[] froms, final char[] tos, final int[] states,
      final int[] newStates, final Iterable<Instruction>[] instructions) {
    this.size = froms.length;
    assert tos.length == size && states.length == size && froms.length == size
        && newStates.length == size && instructions.length == size;
    this.froms = froms;
    this.tos = tos;
    this.states = states;
    this.newStates = newStates;
    this.instructions = instructions;
  }

  private int cmp(final int state1, final int state2, final char ch1, final char ch2) {
    final int scmp = Integer.compare(state1, state2);
    if (scmp != 0) {
      return scmp;
    }
    return Character.compare(ch1, ch2);
  }

  public NextState newStateAndInstructions(final int state, final char input) {
    int l = 0;
    int r = size - 1;
    int x = -1;
    while (r >= l) {
      x = (l + r) >>> 1;  // average and stays correct if addition overflows.
      final int cmp = cmp(state, states[x], input, froms[x]);
      if (cmp < 0) {
        r = x - 1;
      } else if (cmp > 0) {
        l = x + 1;
      } else {
        return new NextState(newStates[x], instructions[x]);
      }
    }

    assert x != -1;

    for (int i = -1; i <= 1; i++) {
      final int y = x + i;

      if (0 <= y && y < size) {
        if (states[y] == state && froms[y] <= input && input <= tos[y]) {
          return new NextState(newStates[y], instructions[y]);
        }
      }
    }

    return null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < size; i++) {
      final Builder.Entry e =
          new Builder.Entry(froms[i], tos[i], instructions[i], states[i], newStates[i], null);
      sb.append(e.toString());
      sb.append('\n');
    }
    return sb.toString();
  }
}
