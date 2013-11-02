package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;


class TNFAToTDFA {
  static final StateAndInstructions NO_STATE =
      new StateAndInstructions(
        DFAState.INSTRUCTIONLESS_NO_STATE,
        Collections.<Instruction> emptyList());

  private static class StateWithMemoryLocation {
    final History[] memoryLocation;
    final State state;

    StateWithMemoryLocation(final State state, final History[] histories) {
      this.memoryLocation = histories;
      this.state = state;
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
  private Map<State, History[]> convertToDfaState(final State s) {
    final Map<State, History[]> initState = new HashMap<>();
    final History[] initialMemoryLocations = new History[tnfa.allTags().size()];
    for (int i = 0; i < initialMemoryLocations.length; i++) {
      initialMemoryLocations[i] = new History();
    }
    initState.put(s, initialMemoryLocations);
    return initState;
  }

  static class StateAndInstructions {
    final DFAState dfaState;
    final Collection<Instruction> instructions;

    StateAndInstructions(final DFAState dfaState,
        final Collection<Instruction> instructions) {
      this.dfaState = dfaState;
      this.instructions = instructions;
    }
  }

  /**
   * Niko and Aaron's closure.
   *
   * All states after following the epsilon edges of the NFA. Produces instructions
   * when Tags are crossed. This is the transitive closure on the subgraph of epsilon
   * edges.
   *
   * @param startState if to generate the start state. If so, ignore a.
   * @param a the character that was read. Is ignored if startState == true.
   * @return The next state after state, for input a.
   */
  StateAndInstructions epsilonClosure(final Map<State, History[]> innerStates, final char a, boolean startState) {
    final Map<State, History[]> R = new LinkedHashMap<>(); // Linked to simplify unit testing.

    final Deque<StateWithMemoryLocation> stack = new ArrayDeque<>(); // normal priority
    final Deque<StateWithMemoryLocation> lowStack = new ArrayDeque<>(); // low priority

    if (startState) { // TODO(nikoschwarz): Beautify.
      for (Entry<State, History[]> entry : innerStates.entrySet()) {
        stack.add(new StateWithMemoryLocation(entry.getKey(), entry.getValue()));
      }
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

    List<Instruction> instructions = new ArrayList<>();
    do {
      StateWithMemoryLocation s;
      if (stack.isEmpty()) {
        s = lowStack.pop();
      } else {
        s = stack.pop();
      }
      assert s != null;

      final State q = s.state;
      final History[] l = s.memoryLocation;

      if (R.containsKey(q)) {
        continue;
      }
      R.put(q, l);

      nextTriple: for (final TransitionTriple triple : tnfa.availableTransitionsFor(q, null)) {
        final State qDash = triple.state;

        // Step 1.
        if (R.containsKey(qDash)) {
          continue nextTriple;
        }

        // Step 2.
        final Tag tau = triple.tag;
        History[] newHistories = Arrays.copyOf(l, l.length);

        if (tau.isStartTag() || tau.isEndTag()) {
          final History newHistoryOpening = new History();
          int openingPos = positionFor(tau.getGroup().startTag);
          instructions.add(instructionMaker.reorder(newHistoryOpening, newHistories[openingPos]));
          newHistories[openingPos] = newHistoryOpening;

          if (tau.isStartTag()) {
            instructions.add(instructionMaker.storePosPlusOne(newHistoryOpening));
          } else {
            final History newHistoryClosing = new History();
            int closingPos = positionFor(tau.getGroup().endTag);
            instructions.add(instructionMaker.reorder(newHistoryClosing, newHistories[closingPos]));
            newHistories[closingPos] = newHistoryClosing;
            instructions.add(instructionMaker.storePos(newHistoryClosing));
            instructions.add(instructionMaker.openingCommit(newHistoryOpening));
            instructions.add(instructionMaker.closingCommit(newHistoryClosing));
          }
        }

        // Step 3.
        switch (triple.getPriority()) {
          case LOW:
            lowStack.add(new StateWithMemoryLocation(triple.getState(), newHistories));
            break;
          case NORMAL:
            stack.add(new StateWithMemoryLocation(triple.getState(), newHistories));
            break;
          default:
            throw new AssertionError();
        }
      }
    } while (!(stack.isEmpty() && lowStack.isEmpty()));
    return new StateAndInstructions(
      new DFAState(R, DFAState.makeComparisonKey(R)),
      instructions);
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, Map<History, History> mapping) {
    // `from` is a key that is smaller than all possible full keys. Likewise, `to` is bigger than all.
    DFAState from = new DFAState(null, DFAState.makeStateComparisonKey(u.innerStates.keySet()));
    byte[] toKey = DFAState.makeStateComparisonKey(u.innerStates.keySet());
    // Assume that toKey is not full of Byte.MAX_VALUE. That would be really unlucky.
    // Also a bit unlikely, given that it's an MD5 hash, and therefore pretty random.
    for (int i = toKey.length - 1; true; i--) {
      if (toKey[i] != Byte.MAX_VALUE) {
        toKey[i]++;
        break;
      }
    }
    DFAState to = new DFAState(null, toKey);

    final NavigableSet<DFAState> range = states.subSet(from, true, to, false);
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

  StateAndInstructions makeStartState() {
    Map<State, History[]> start = convertToDfaState(tnfa.getInitialState());

    return epsilonClosure(start, Character.MAX_VALUE, true);
  }

  /** @return Ordered instructions for mapping. The ordering is such that they don't interfere with each other. */
  List<Instruction> mappingInstructions(final Map<History, History> map) {
    // Reverse topological sort of map.
    // For the purpose of this method, map is a restricted DAG.
    // Nodes have at most one incoming and outgoing edges.
    // The instructions that we return are the *edges* of the graph, histories are the nodes.
    // Because of the instructions, we can identify every edge by its *source* node.
    List<Instruction> ret = new ArrayList<>(map.size());
    Deque<History> stack = new ArrayDeque<>();
    Set<History> visitedSources = new HashSet<>();

    // Go through the edges of the graph. Identify edge e by source node source:
    for (History source : map.keySet()) {

      // Push e on stack, unless e deleted
      if (visitedSources.contains(source)) {
        continue;
      }

      stack.push(source);
      // while cur has undeleted following edges, mark cur as deleted, follow the edge, repeat.
      while (source != null && !visitedSources.contains(source)) {
        source = map.get(source);
        stack.push(source);
        visitedSources.add(source);
      }

      // walk stack backward, add to ret.
      stack.pop(); // top element is no source node.
      while (!stack.isEmpty()) {
        History cur = stack.pop();
        History target = map.get(cur);
        if (!cur.equals(target)) {
          ret.add(instructionMaker.reorder(target, cur));
        }
      }
    }
    return ret;
  }

  private int positionFor(final Tag tau) {
    assert tau.isEndTag() || tau.isStartTag();

    int r = 2 * tau.getGroup().number;
    if (tau.isEndTag()) {
      r++;
    }
    return r;
  }

  /**
   * @return an array {@code parentOf} such that for capture group `t`,
   * its parent is {@code parentOf[t]}.
   */
  int[] makeParentOf() {
    Collection<Tag> allTags = tnfa.allTags();
    int[] ret = new int[allTags.size() / 2];
    for (Tag t : allTags) {
      ret[t.getGroup().number] = t.getGroup().parent.number;
    }

    return ret;
  }
}
