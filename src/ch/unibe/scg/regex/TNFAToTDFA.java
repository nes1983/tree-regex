package ch.unibe.scg.regex;

import static ch.unibe.scg.regex.TNFAToTDFA.Hunger.FED;
import static ch.unibe.scg.regex.TNFAToTDFA.Hunger.HUNGRY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;


class TNFAToTDFA {
  static TNFAToTDFA make(final TNFA tnfa) {
    return new TNFAToTDFA(tnfa);
  }

  final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker.get();

  final TDFATransitionTable.Builder tdfaBuilder = new TDFATransitionTable.Builder();

  final TNFA tnfa;

  TNFAToTDFA(final TNFA tnfa) {
    this.tnfa = tnfa;
  }

  /** Used to create the initial state of the DFA. */
  private LinkedHashMap<State, History[]> convertToDfaState(final State s) {
    final LinkedHashMap<State, History[]> initState = new LinkedHashMap<>();
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

  private static class StateThread {
    final State state;
    final History[] histories;
    final Hunger hunger;

    StateThread(State state, History[] histories, Hunger consumed) {
      this.state = state;
      this.histories = histories;
      this.hunger = consumed;
    }
  }

  static enum Hunger {
    HUNGRY, FED;
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
   * @return The next state after state, for input a. Null if there isn't a follow-up state.
   */
  StateAndInstructions oneStep(final LinkedHashMap<State, History[]> innerStates, InputRange ir, boolean startState) {
    final LinkedHashMap<State, History[]> newInner = new LinkedHashMap<>();
    final List<Instruction> instructions = new ArrayList<>();
    final Set<State> seen = new HashSet<>();

    final Deque<StateThread> stack = new ArrayDeque<>(); // normal priority
    final Deque<StateThread> lowStack = new ArrayDeque<>(); // low priority
    final Deque<StateThread> workStack = new ArrayDeque<>();

    // Enqueue all states we're in as consuming thread to lowStack, or non-consuming if startState.
    for (Entry<State, History[]> e : innerStates.entrySet()) {
      Hunger h = HUNGRY;
      if (startState) {
        h = FED;
      }
      lowStack.addLast(new StateThread(e.getKey(), e.getValue(), h));
    }

    while (!stack.isEmpty() || !lowStack.isEmpty()) {
      // take topmost as t from high if possible or else from low.
      StateThread thread;
      if (!stack.isEmpty()) {
        thread = stack.removeFirst();
      } else {
        thread = lowStack.removeFirst();
        while (!workStack.isEmpty()) {
          StateThread tt = workStack.removeFirst();
          newInner.put(tt.state, tt.histories);
        }
      }

      if (thread.hunger == HUNGRY) {
        final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(thread.state, ir);
        for (TransitionTriple transition : ts) {
          // push new thread with the new state that isn't consuming to high.
          stack.addFirst(new StateThread(transition.state, thread.histories, FED));
        }
        continue;
      }

      if (seen.contains(thread.state)) {
        continue;
      }

      workStack.addFirst(thread);
      seen.add(thread.state);

      for (final TransitionTriple triple : tnfa.availableEpsilonTransitionsFor(thread.state)) {
        final Tag tau = triple.tag;
        History[] newHistories = Arrays.copyOf(thread.histories, thread.histories.length);

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
        // push new thread with the new state to the corresponding stack.
        StateThread newThread = new StateThread(triple.getState(), newHistories, FED);
        switch (triple.getPriority()) {
          case LOW:
            lowStack.add(newThread);
            break;
          case NORMAL:
            stack.add(newThread);
            break;
          default:
            throw new AssertionError();
        }
      }
    }

    while (!workStack.isEmpty()) {
      StateThread tt = workStack.removeFirst();
      newInner.put(tt.state, tt.histories);
    }

    return new StateAndInstructions(
      new DFAState(newInner, DFAState.makeComparisonKey(newInner)),
      instructions);
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, Map<History, History> mapping) {
    // `from` is a key that is smaller than all possible full keys. Likewise, `to` is bigger than all.
    DFAState from = new DFAState(null, DFAState.makeStateComparisonKey(u.innerStates));
    byte[] toKey = DFAState.makeStateComparisonKey(u.innerStates);
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
    Map<History, History> reverse = new HashMap<>();

    // A state is only mappable if its histories are mappable too.
    for (final Map.Entry<State, History[]> entry : first.innerStates.entrySet()) {
      final History[] mine = entry.getValue();
      final History[] theirs = second.innerStates.get(entry.getKey());
      final boolean success = updateMap(mapping, reverse, mine, theirs);
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
  private boolean updateMap(final Map<History, History> map, Map<History, History> reverse,
        final History[] from, final History[] to) {
    assert from.length == to.length;

    // Go over the tag list and iteratively try to find counterexample.
    for (int i = 0; i < from.length; i++) {
      if (!map.containsKey(from[i])) {
        // If we don't know any mapping for from[i], we set it to the only mapping that can work.

        if (reverse.containsKey(to[i])) { // But the target is taken already
          return false;
        }

        map.put(from[i], to[i]);
        reverse.put(to[i], from[i]);
      } else if (!map.get(from[i]).equals(to[i]) || !from[i].equals(reverse.get(to[i]))) {
        // Only mapping that could be chosen for from[i] and to[i] contradicts existing mapping.
        return false;
      } // That means the existing mapping matches.
    }
    return true;
  }

  StateAndInstructions makeStartState() {
    LinkedHashMap<State, History[]> start = convertToDfaState(tnfa.initialState);

    return oneStep(start, InputRange.EOS, true);
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
