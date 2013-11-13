package ch.unibe.scg.regex;

import static ch.unibe.scg.regex.TNFAToTDFA.Hunger.FED;
import static ch.unibe.scg.regex.TNFAToTDFA.Hunger.HUNGRY;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  List<RThread> convertToDfaState(final State state) {
    final History[] initialMemoryLocations = new History[tnfa.allTags().size()];
    for (int i = 0; i < initialMemoryLocations.length; i++) {
      initialMemoryLocations[i] = new History();
    }
    return Collections.singletonList(new RThread(state, initialMemoryLocations));
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

  private static class TransitioningThread {
    final RThread thread;
    final Hunger hunger;
    final List<Instruction> instructions;

    TransitioningThread(RThread thread, Hunger hunger, List<Instruction> instructions) {
      this.thread = thread;
      this.hunger = hunger;
      this.instructions = instructions;
    }

    @Override
    public String toString() {
      return String.format("(%s, %s, %s)", thread, hunger, instructions);
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
   * @param ir the input range that was read. For start states, this is null.
   * @return The next state after state, for input a. Null if there isn't a follow-up state.
   */
  StateAndInstructions oneStep(final List<RThread> threads, InputRange ir) {
    final List<RThread> newInner = new ArrayList<>();
    final Set<State> seen = new HashSet<>();
    List<Instruction> instructions = new ArrayList<>();

    final Deque<TransitioningThread> stack = new ArrayDeque<>(); // normal priority
    final Deque<TransitioningThread> lowStack = new ArrayDeque<>(); // low priority
    final Deque<TransitioningThread> workStack = new ArrayDeque<>();

    History[] finalHistories = null;

    // Enqueue all states we're in as consuming thread to lowStack, or non-consuming if startState.
    for (RThread e : threads) {
      Hunger h = HUNGRY;
      if (ir == null) {
        h = FED;
      }
      lowStack.addLast(new TransitioningThread(e, h, Collections.<Instruction> emptyList()));
    }

    while (!stack.isEmpty() || !lowStack.isEmpty()) {
      // take topmost as t from high if possible or else from low.
      TransitioningThread tt;
      if (!stack.isEmpty()) {
        tt = stack.removeFirst();
      } else {
        tt = lowStack.removeFirst();
        finalHistories = fillRet(newInner, instructions, workStack, finalHistories);
      }

      if (tt.hunger == HUNGRY) {
        final Collection<Transition> ts = tnfa.availableTransitionsFor(tt.thread.state, ir);
        for (Transition transition : ts) {
          // push new thread with the new state that isn't consuming to high.
          stack.addFirst(new TransitioningThread(new RThread(transition.state, tt.thread.histories),
            FED, Collections.<Instruction> emptyList()));
        }
        continue;
      }

      if (seen.contains(tt.thread.state)) {
        continue;
      }
      seen.add(tt.thread.state);
      workStack.addFirst(tt);

      for (final Transition trans : tnfa.availableEpsilonTransitionsFor(tt.thread.state)) {
        final Tag tau = trans.tag;
        final List<Instruction> transInstr = new ArrayList<>();
        History[] newHistories = Arrays.copyOf(tt.thread.histories, tt.thread.histories.length);

        if (tau.isStartTag() || tau.isEndTag()) {
          final History newHistoryOpening = new History();
          int openingPos = positionFor(tau.getGroup().startTag);
          transInstr.add(instructionMaker.reorder(newHistoryOpening, newHistories[openingPos]));
          newHistories[openingPos] = newHistoryOpening;

          if (tau.isStartTag()) {
            transInstr.add(instructionMaker.storePosPlusOne(newHistoryOpening));
          } else {
            final History newHistoryClosing = new History();
            int closingPos = positionFor(tau.getGroup().endTag);
            transInstr.add(instructionMaker.reorder(newHistoryClosing, newHistories[closingPos]));
            newHistories[closingPos] = newHistoryClosing;
            transInstr.add(instructionMaker.storePos(newHistoryClosing));
            transInstr.add(instructionMaker.openingCommit(newHistoryOpening));
            transInstr.add(instructionMaker.closingCommit(newHistoryClosing));
          }
        }
        // push new thread with the new state to the corresponding stack.
        TransitioningThread newThread = new TransitioningThread(new RThread(trans.state, newHistories),
          FED, transInstr);
        switch (trans.priority) {
          case LOW:
            lowStack.addFirst(newThread);
            break;
          case NORMAL:
            stack.addFirst(newThread);
            break;
          default:
            throw new AssertionError();
        }
      }
    }

    finalHistories = fillRet(newInner, instructions, workStack, finalHistories);

    if (newInner.isEmpty()) {
      return null;
    }

    return new StateAndInstructions(
      new DFAState(newInner, DFAState.makeComparisonKey(newInner), finalHistories),
      instructions);
  }

  /**
   * Empties the {@code workStack} and fills it into {@code newInner} and its instructions into {@code instructions}.
   *
   * @return The new final history, if we found one. Otherwise, param {@code finalHistories}.
   */
  private History[] fillRet(final List<RThread> newInner, List<Instruction> instructions,
      final Deque<TransitioningThread> workStack, History[] finalHistories) {
    // Add instructions in the order they were created.
    Iterator<TransitioningThread> iter = workStack.descendingIterator();
    while (iter.hasNext()) {
      instructions.addAll(iter.next().instructions);
    }

    while (!workStack.isEmpty()) {
      final TransitioningThread workTransition = workStack.removeFirst();
      newInner.add(workTransition.thread);
      if (tnfa.finalState.equals(workTransition.thread.state)) {
        assert finalHistories == null;
        finalHistories = workTransition.thread.histories;
      }
    }
    return finalHistories;
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, Map<History, History> mapping) {
    // `from` is a key that is smaller than all possible full keys. Likewise, `to` is bigger than all.
    DFAState from = new DFAState(null, DFAState.makeStateComparisonKey(u.threads), null);
    byte[] toKey = DFAState.makeStateComparisonKey(u.threads);
    // Assume that toKey is not full of Byte.MAX_VALUE. That would be really unlucky.
    // Also a bit unlikely, given that it'state an MD5 hash, and therefore pretty random.
    for (int i = toKey.length - 1; true; i--) {
      if (toKey[i] != Byte.MAX_VALUE) {
        toKey[i]++;
        break;
      }
    }
    DFAState to = new DFAState(null, toKey, null);

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
    mapping.clear();
    Map<History, History> reverse = new HashMap<>();
    assert first.threads.size() == second.threads.size();

    // A state is only mappable if its histories are mappable too.
    for (int i = 0; i < first.threads.size(); i++) {
      final History[] mine = first.threads.get(i).histories;
      final History[] theirs = second.threads.get(i).histories;
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

  /** @return Ordered instructions for mapping. The ordering is such that they don't interfere with each other. */
  List<Instruction> mappingInstructions(final Map<History, History> map) {
    // Reverse topological sort of map.
    // For the purpose of this method, map is a restricted DAG.
    // Nodes have at most one incoming and outgoing edges.
    // The instructions that we return are the *edges* of the graph, histories are the nodes.
    // We identify every edge by its *source* node.
    List<Instruction> ret = new ArrayList<>(map.size());
    Deque<History> stack = new ArrayDeque<>();
    Set<History> visitedSources = new HashSet<>();

    // Go through the edges of the graph. Identify edge e by source node source:
    for (History source : map.keySet()) {
      assert source != null;
      // Push e on stack, unless e deleted
      if (visitedSources.contains(source)) {
        continue;
      }

      stack.push(source);
      // while cur has undeleted following edges, mark cur as deleted, follow the edge, repeat.
      while (source != null && !visitedSources.contains(source)) {
        visitedSources.add(source);
        source = map.get(source);
        if (source != null) {
          stack.push(source);
        }
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
    assert stack.isEmpty();
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
