package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import ch.unibe.scg.regex.Instruction.Instructions;
import ch.unibe.scg.regex.TransitionTriple.Priority;


class TNFAToTDFA {
  static final StateAndInstructionsAndNewLocations NO_STATE =
      new StateAndInstructionsAndNewLocations(
        DFAState.INSTRUCTIONLESS_NO_STATE, Collections.<Instruction> emptyList(), new BitSet());

  static enum DFAStateComparator implements Comparator<Map<State, int[]>> {
    SINGLETON;

    private int compare(final int[] a, final int[] b) {
      {
        final int cmp = Integer.compare(a.length, b.length);
        if (cmp != 0) {
          return cmp;
        }
      }
      for (int i = 0; i < a.length; i++) {
        final int cmp = Integer.compare(a[i], b[i]);
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }

    @Override
    public int compare(final Map<State, int[]> o1, final Map<State, int[]> o2) {
      {
        final int cmp = Integer.compare(o1.size(), o2.size());
        if (cmp != 0) {
          return cmp;
        }
      }

      final List<State> states1 = new ArrayList<>(o1.keySet());
      Collections.sort(states1);
      final List<State> states2 = new ArrayList<>(o2.keySet());
      Collections.sort(states2);

      assert states1.size() == states2.size();

      for (int i = 0; i < states1.size(); i++) {
        final int cmp = states1.get(i).compareTo(states2.get(i));
        if (cmp != 0) {
          return cmp;
        }
      }

      for (int i = 0; i < states1.size(); i++) {
        assert states1.get(i).compareTo(states2.get(i)) == 0;
        final State s = states1.get(i);
        final int cmp = compare(o1.get(s), o2.get(s));
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }
  }

  static class StateWithMemoryLocation implements Map.Entry<State, int[]> {
    final int[] memoryLocation;
    final State state;

    StateWithMemoryLocation(final State state, final int[] memoryLocation) {
      this.memoryLocation = memoryLocation;
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

    public int[] getMemoryLocation() {
      return memoryLocation;
    }

    public State getState() {
      return state;
    }

    @Override
    public int[] getValue() {
      return memoryLocation;
    }

    @Override
    public int hashCode() {
      return ((state == null) ? 0 : state.hashCode());
    }

    @Override
    public int[] setValue(final int[] value) {
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

  int highestMapping = 0;

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
    final Map<State, int[]> initState = new HashMap<>();
    final int numTags = tnfa.allTags().size();
    final int[] initialMemoryLocations = makeInitialMemoryLocations(numTags);
    initState.put(s, initialMemoryLocations);
    return new DFAState(Collections.unmodifiableMap(initState));
  }

  static class StateAndInstructionsAndNewLocations {
    final DFAState dfaState;
    final Iterable<Instruction> instructions;
    final BitSet newLocations;

    StateAndInstructionsAndNewLocations(final DFAState dfaState, final Iterable<Instruction> instructions, final BitSet newLocations) {
      this.dfaState = dfaState;
      this.instructions = instructions;
      this.newLocations = newLocations;
    }
  }

  /**
   * Niko and Aaron's closure.
   *
   * @param startState if to generate the start state. If so, ignore a.
   * @param a the character that was read. Is ignored if startState == true.
   * @return The next state after state, for input a.
   */
  StateAndInstructionsAndNewLocations e(final Map<State, int[]> state, final char a, boolean startState) {
    final Map<State, int[]> R = new LinkedHashMap<>(); // Linked to simplify unit testing.

    final Deque<Map.Entry<State, int[]>> stack = new ArrayDeque<>(); // normal priority
    final Deque<Map.Entry<State, int[]>> lowStack = new ArrayDeque<>(); // low priority

    if (startState) { // TODO(nikoschwarz): Beautify.
      stack.addAll(state.entrySet());
    } else {
      for (final Entry<State, int[]> pr : state.entrySet()) {
        final int[] k = pr.getValue();
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
    final BitSet newLocations = new BitSet();
    do {
      Entry<State, int[]> s;
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
      final int[] l = s.getValue();

      nextTriple: for (final TransitionTriple triple : tnfa.availableTransitionsFor(q, null)) {
        final State qDash = triple.state;

        // Step 1.
        if ((R.containsKey(qDash) && (triple.priority == Priority.LOW)) || R.containsKey(qDash)) {
          continue nextTriple;
        }

        // Step 2.
        final Tag tau = triple.tag;
        int[] tdash;
        if (tau.isEndTag() || tau.isStartTag()) {
          tdash = Arrays.copyOf(l, l.length);
          final int newLoc = nextInt();
          newLocations.set(newLoc);
          tdash[positionFor(tau)] = newLoc;
          instructions.add(instructionMaker.storePos(newLoc));
          if (tau.isEndTag()) {
            instructions.add(instructionMaker.closingCommit(newLoc));
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
    return new StateAndInstructionsAndNewLocations(new DFAState(R), instructions, newLocations);
  }

  private BitSet extractLocs(final Map<State, int[]> oldState) {
    final BitSet ret = new BitSet();
    for (final int[] ary : oldState.values()) {
      for (final int val : ary) {
        if (val >= 0) { // TODO(nikoschwarz): Is this correct? Do we need negative indices or not?
          ret.set(val);
        }
      }
    }
    return ret;
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, int[] mapping) {
    final Map<State, int[]> fromElement = new LinkedHashMap<>(u.getData());
    {
      final int[] min = new int[0];
      for (final Entry<State, int[]> e : fromElement.entrySet()) {
        e.setValue(min);
      }
    }
    final Map<State, int[]> toElement = new LinkedHashMap<>(u.getData());
    {
      final int[] max = new int[highestMapping + 1];
      for (final Entry<State, int[]> e : toElement.entrySet()) {
        e.setValue(max);
      }
    }
    final NavigableSet<DFAState> range =
        states.subSet(new DFAState(fromElement), true, new DFAState(toElement), true);

    for (final DFAState candidate : range) {
      if (u.isMappable(candidate, mapping)) {
        return candidate;
      }
    }

    return null;
  }

  private int[] makeInitialMemoryLocations(final int numTags) {
    final int[] ret = new int[numTags];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = -1 * i - 1;
    }
    return ret;
  }

  StateAndInstructionsAndNewLocations makeStartState() {
    DFAState start = convertToDfaState(tnfa.getInitialState());

    return e(start.getData(), Character.MAX_VALUE, true);
  }

  Collection<Instruction> mappingInstructions(final int[] mapping, final DFAState to,
      BitSet newLocations) {
    final BitSet locs = extractLocs(to.getData());
    locs.andNot(newLocations); // New locations already led to stores.
    final List<Instruction> ret = new ArrayList<>();

    for (int i = locs.nextSetBit(0); i >= 0; i = locs.nextSetBit(i + 1)) {
      ret.add(instructionMaker.reorder(i, mapping[i]));
    }

    return ret;
  }

  int nextInt() {
    return highestMapping++;
  }

  private int positionFor(final Tag tau) {
    assert tau.isEndTag() || tau.isStartTag();

    int r = 2 * tau.getGroup().getNumber();
    if (tau.isEndTag()) {
      r += 1;
    }
    return r;
  }
}
