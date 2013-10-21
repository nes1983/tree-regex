package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import ch.unibe.scg.regex.Instruction.Instructions;


class TNFAToTDFA {
  static final StateAndInstructionsAndNewHistories NO_STATE =
      new StateAndInstructionsAndNewHistories(
        DFAState.INSTRUCTIONLESS_NO_STATE,
        Collections.<Instruction> emptyList(),
        Collections.<History> emptyList());

  static enum DFAStateComparator implements Comparator<Map<State, History[]>> {
    SINGLETON;

    private int compare(final History[] o1, final History[] o2) {
      assert o1 != null || o2 != null;

      int len1 = -1;
      if (o1 != null) {
        len1 = o1.length;
      }

      int len2 = -1;
      if (o2 != null) {
        len2 = o2.length;
      }

      final int lenCmp = Integer.compare(len1, len2);
      if (lenCmp != 0) {
        return lenCmp;
      }

      assert o1 != null && o2 != null; // The previous return, plus the initial assert ensure this.

      for (int i = 0; i < o1.length; i++) {
        long id1 = -1L;
        if (o1[i] != null) {
          id1 = o1[i].id;
        }
        long id2 = -1L;
        if (o2[i] != null) {
          id2 = o2[i].id;
        }
        final int cmp = Long.compare(id1, id2);
        if (cmp != 0) {
          return cmp;
        }
      }

      return 0;
    }

    @Override
    public int compare(final Map<State, History[]> o1, final Map<State, History[]> o2) {
      final int sizeCmp = Integer.compare(o1.size(), o2.size());
      if (sizeCmp != 0) {
        return sizeCmp;
      }

      HashSet<State> keys = new HashSet<>(o1.size() + o2.size());
      keys.addAll(o1.keySet());
      keys.addAll(o2.keySet());

      for (State k : keys) {
        final int cmp = compare(o1.get(k), o2.get(k));
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }
  }

  static class StateWithMemoryLocation implements Map.Entry<State, History[]> {
    final History[] memoryLocation;
    final State state;

    StateWithMemoryLocation(final State state, final History[] histories) {
      this.memoryLocation = histories;
      this.state = state;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final StateWithMemoryLocation other = (StateWithMemoryLocation) obj;
      if (state == null) {
        if (other.state != null) {
          return false;
        }
      } else if (!state.equals(other.state)) {
        return false;
      }
      return true;
    }

    @Override
    public State getKey() {
      return state;
    }

    public History[] getMemoryLocation() {
      return memoryLocation;
    }

    public State getState() {
      return state;
    }

    @Override
    public History[] getValue() {
      return memoryLocation;
    }

    @Override
    public int hashCode() {
      return ((state == null) ? 0 : state.hashCode());
    }

    @Override
    public History[] setValue(final History[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "" + state + "" + Arrays.toString(memoryLocation);
    }
  }

  public static TNFAToTDFA make(final TNFA tnfa) {
    return new TNFAToTDFA(tnfa);
  }

  final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker.get();

  final TDFATransitionTable.Builder tdfaBuilder = new TDFATransitionTable.Builder();

  final TNFA tnfa;

  public TNFAToTDFA(final TNFA tnfa) {
    this.tnfa = tnfa;
  }

  /** @return All input ranges, sorted */
  List<InputRange> allInputRanges() {
    final List<InputRange> ranges = new ArrayList<>(tnfa.allInputRanges());
    if (ranges.size() < 2) {
      return Collections.unmodifiableList(ranges);
    }

    final List<InputRange> ret = new ArrayList<>();
    Collections.sort(ranges);
    final Iterator<InputRange> iter = ranges.iterator();
    InputRange last = iter.next();
    InputRange cur = null;
    while (iter.hasNext()) {
      cur = iter.next();
      if (last.getTo() < cur.getFrom()) {
        ret.add(last);
      } else {
        last = InputRange.make((char) (last.getTo() + 1), cur.getTo());
      }
      last = cur;
    }
    assert cur != null;
    ret.add(cur);
    return new ArrayList<>(ret);
  }

  /** Used to create the initial state of the DFA. */
  private DFAState convertToDfaState(final State s) {
    final Map<State, History[]> initState = new HashMap<>();
    final int numTags = tnfa.allTags().size();
    final History[] initialMemoryLocations = new History[numTags];
    initState.put(s, initialMemoryLocations);
    return new DFAState(Collections.unmodifiableMap(initState));
  }

  static class StateAndInstructionsAndNewHistories {
    final DFAState dfaState;
    final Iterable<Instruction> instructions;
    final Collection<History> newHistories;

    StateAndInstructionsAndNewHistories(final DFAState dfaState, final Iterable<Instruction> instructions,
          final Collection<History> newHistories) {
      this.dfaState = dfaState;
      this.instructions = instructions;
      this.newHistories = newHistories;
    }
  }

  /**
   * Niko and Aaron's closure.
   *
   * @param startState if to generate the start state. If so, ignore a.
   * @param a the character that was read. Is ignored if startState == true.
   * @return The next state after state, for input a.
   */
  StateAndInstructionsAndNewHistories e(final Map<State, History[]> innerStates, final char a, boolean startState) {
    final Map<State, History[]> R = new LinkedHashMap<>(); // Linked to simplify unit testing.

    final Deque<Map.Entry<State, History[]>> stack = new ArrayDeque<>(); // normal priority
    final Deque<Map.Entry<State, History[]>> lowStack = new ArrayDeque<>(); // low priority

    if (startState) { // TODO(nikoschwarz): Beautify.
      stack.addAll(innerStates.entrySet());
    } else {
      for (final Entry<State, History[]> pr : innerStates.entrySet()) {
        final History[] k = pr.getValue();
        final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(pr.getKey(), a);
        for (final TransitionTriple t : ts) {
          switch (t.getPriority()) {
            case LOW:
              lowStack.add(new StateWithMemoryLocation(t.getState(), Arrays.copyOf(k, k.length)));
              break;
            case NORMAL: // Fall thru
            default:
              stack.add(new StateWithMemoryLocation(t.getState(), Arrays.copyOf(k, k.length)));
          }
        }
      }
    }

    if (lowStack.isEmpty() && stack.isEmpty()) {
      return NO_STATE;
    }

    Instructions instructions = new Instructions();
    final Collection<History> newHistories = new ArrayList<>();
    do {
      Entry<State, History[]> s;
      if (stack.isEmpty()) {
        s = lowStack.pop();
      } else {
        s = stack.pop();
      }
      assert s != null;

      if (R.containsKey(s.getKey())) {
        continue;
      }
      R.put(s.getKey(), s.getValue());

      final State q = s.getKey();
      final History[] l = s.getValue();

      nextTriple: for (final TransitionTriple triple : tnfa.availableTransitionsFor(q, null)) {
        final State qDash = triple.state;

        // Step 1.
        if (R.containsKey(qDash)) {
          continue nextTriple;
        }

        // Step 2.
        final Tag tau = triple.tag;
        History[] tdash;
        if (tau.isEndTag() || tau.isStartTag()) {
          tdash = Arrays.copyOf(l, l.length);
          final History newHistory = new History();
          newHistories.add(newHistory);
          tdash[positionFor(tau)] = newHistory;
          instructions.add(instructionMaker.storePos(newHistory));
          if (tau.isEndTag()) {
            instructions.add(instructionMaker.closingCommit(newHistory));
            instructions.add(instructionMaker.openingCommit(tdash[positionFor(tau.getGroup().getStartTag())]));
          }
        } else {
          tdash = l;
        }

        // Step 3. (TODO)

        // Step 4.
        if (!R.containsKey(triple.getState())) {
          switch (triple.getPriority()) {
            case LOW:
              lowStack.add(new StateWithMemoryLocation(triple.getState(), tdash));
              break;
            case NORMAL:
              stack.add(new StateWithMemoryLocation(triple.getState(), tdash));
              break;
            default:
              throw new AssertionError();
          }
        }
      }
    } while (!(stack.isEmpty() && lowStack.isEmpty()));
    return new StateAndInstructionsAndNewHistories(new DFAState(R), instructions, newHistories);
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, Map<History, History> mapping) {
    // DFA state that describes the lower bound of possibly mappable states:
    //    1. The range of `states` that we're looking for all contains exactly u.innerStates as states.
    //    2. As per DFAStateComparator, min is smaller than any other History array (because they're all bigger).
    //    3. As per DFAStateComparator, max is bigger than any other History array (because they're all smaller).
    final Map<State, History[]> fromElement = new HashMap<>(u.innerStates);
    {
      final History[] min = new History[0];
      for (final Entry<State, History[]> e : fromElement.entrySet()) {
        e.setValue(min);
      }
    }
    final Map<State, History[]> toElement = new HashMap<>(u.innerStates);
    {
      final History[] max = new History[tnfa.allTags().size() + 1];
      for (final Entry<State, History[]> e : toElement.entrySet()) {
        e.setValue(max);
      }
    }
    final NavigableSet<DFAState> range =
        states.subSet(new DFAState(fromElement), true, new DFAState(toElement), true);

    for (final DFAState candidate : range) {
      if (isMappable(u, candidate, mapping)) {
        return candidate;
      }
    }

    return null;
  }

  /** @return a mapping into {@code mapping} if one exists and returns false otherwise. */
  private boolean isMappable(final DFAState first, final DFAState second, final Map<History, History> mapping) {
    // We checked that the same NFA states exist in findMappableState
    if (!first.innerStates.keySet().equals(second.innerStates.keySet())) {
      throw new AssertionError("The candidate range must contain the right states!");
    }
    mapping.clear();

    // A state is only mappable if its histories are mappable too.
    for (final Map.Entry<State, History[]> entry : first.innerStates.entrySet()) {
      final History[] mine = entry.getValue();
      final History[] theirs = second.innerStates.get(entry.getKey());
      final boolean success = updateMap(mapping, mine, theirs);
      if (!success) {
        return false;
      }
    }

    return true;
  }

  /**
   * Destructively update <code>map</code> until it maps from to to. A -1 entry in map means that
   * the value can still be changed. Other values are left untouched.
   *
   * @param map Must be at least as big as the biggest values in both from and to. Elements must
   *        be >= -1. -1 stands for unassigned.
   * @param from same length as to.
   * @param to same length as from.
   * @return True if the mapping was successful; false otherwise.
   */
  private boolean updateMap(final Map<History, History> map, final History[] from, final History[] to) {
    assert from.length == to.length;

    // Go over the tag list and iteratively try to find counterexample.
    for (int i = 0; i < from.length; i++) {
      // if the tag hasn't been set in either state, it's ok.
      if (from[i] == null && to[i] == null) {
        continue; // Both leave i unspecified: that's fine.
      } else if ((from[i] == null && to[i] != null) || (from[i] != null && to[i] == null)) {
        return false; // Only from specifies the mapping, that won't do.
      }

      if (!map.containsKey(from[i])) {
        // If we don't know any mapping for from[i], we set it to the only mapping that can work.
        map.put(from[i], to[i]);
      } else if (!map.get(from[i]).equals(to[i])) {
        // Only mapping that could be chosen for from[i] and to[i] contradicts existing mapping.
        return false;
      } // That means the existing mapping matches.
    }
    return true;
  }

  StateAndInstructionsAndNewHistories makeStartState() {
    DFAState start = convertToDfaState(tnfa.getInitialState());

    return e(start.innerStates, Character.MAX_VALUE, true);
  }

  Collection<Instruction> mappingInstructions(final Map<History, History> mapping,
        Iterable<History> oldHistories) {
    final List<Instruction> ret = new ArrayList<>();

    for (History to : oldHistories) {
      History from = mapping.get(to);
      if (!from.equals(to)) {
        ret.add(instructionMaker.reorder(to, from));
      }
    }

    return ret;
  }

  private int positionFor(final Tag tau) {
    assert tau.isEndTag() || tau.isStartTag();

    int r = 2 * tau.getGroup().getNumber();
    if (tau.isEndTag()) {
      r++;
    }
    return r;
  }
}
