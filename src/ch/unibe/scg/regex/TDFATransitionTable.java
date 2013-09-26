package ch.unibe.scg.regex;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.TNFAToTDFA.DFAState;

class TDFATransitionTable {
  static class NextState {
    List<Instruction> instructions;
    int nextState;

    public NextState(final int nextState, final List<Instruction> instructions) {
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

  static class Builder {
    static class Entry implements Comparable<Builder.Entry> {
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
      int lastCreatedState = -1;

      final Map<DFAState, Integer> mapping = new LinkedHashMap<>();

      public int lookupOrMake(final DFAState state) {
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

      Integer lookup(DFAState t) {
        return mapping.get(t);
      }
    }

    final Builder.Mapping mapping = new Mapping();
    final NavigableSet<Builder.Entry> transitions = new TreeSet<>();

    public void addTransition(final DFAState t, final InputRange inputRange,
        final DFAState newState, final List<Instruction> instructions) {

      final Builder.Entry e =
          new Entry(inputRange.getFrom(), inputRange.getTo(), instructions,
              mapping.lookupOrMake(t), mapping.lookupOrMake(newState), newState);
      transitions.add(e);
    }

    public NextDFAState availableTransition(DFAState t, char a) {
      final Integer fromState = mapping.lookup(t);
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
      final List<Instruction>[] instructions = new List[size];
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
      final Builder.Entry e =
          new Builder.Entry(froms[i], tos[i], instructions[i], states[i], newStates[i], null);
      sb.append(e.toString());
      sb.append('\n');
    }
    return sb.toString();
  }
}