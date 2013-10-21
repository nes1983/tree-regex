package ch.unibe.scg.regex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.MatchResult;

import ch.unibe.scg.regex.Instruction.Instructions;
import ch.unibe.scg.regex.TDFATransitionTable.NextDFAState;
import ch.unibe.scg.regex.TNFAToTDFA.StateAndInstructionsAndNewHistories;

/** Interprets the known TDFA states. Compiles missing states on the fly. */
class TDFAInterpreter {
  final NavigableSet<DFAState> states = new TreeSet<>();

  final TDFATransitionTable.Builder tdfaBuilder = new TDFATransitionTable.Builder();
  final TNFAToTDFA tnfa2tdfa;


  TDFAInterpreter(TNFAToTDFA tnfa2tdfa) {
    this.tnfa2tdfa = tnfa2tdfa;
  }

  /** @return the range containing input. Null if there isn't one. */
  InputRange findInputRange(List<InputRange> ranges, char input) {
    int l = 0;
    int r = ranges.size() - 1;
    while (l <= r) {
      final int m = (l + r) / 2;
      final InputRange splitPoint = ranges.get(m);
      if (splitPoint.contains(input)) {
        return splitPoint;
      } else if (input < splitPoint.getFrom()) {
        r = m - 1;
      } else {
        l = m + 1;
      }
    }

    return null; // Found nothing
  }

  public MatchResult interpret(CharSequence input) {
    final List<InputRange> inputRanges = tnfa2tdfa.allInputRanges();

    StateAndInstructionsAndNewHistories startState = tnfa2tdfa.makeStartState();
    DFAState t = startState.dfaState;
    states.add(t);

    for (final Instruction instruction : startState.instructions) {
      instruction.execute(0);
    }

    for (int pos = 0; pos < input.length(); pos++) {
      final char a = input.charAt(pos);

      {
        NextDFAState nextState = tdfaBuilder.availableTransition(t, a);
        if (nextState != null) {
          // TODO check for fail state.
          for (final Instruction instruction : nextState.getInstructions()) {
            instruction.execute(pos);
          }
          t = nextState.getNextState();
          continue;
        }
      }

      final InputRange inputRange = findInputRange(inputRanges, a);
      if (inputRange == null) {
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      // TODO this is ugly. Clearly, e should return StateAndPositions.
      final StateAndInstructionsAndNewHistories uu = tnfa2tdfa.e(t.innerStates, a, false);
      final DFAState u = uu.dfaState;

      if (u.innerStates.isEmpty()) { // There is no matching NFA state.
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      Map<History, History> mapping = new HashMap<>();

      // If there is a valid mapping, findMappableStates will modify mapping into it.
      final DFAState mappedState = tnfa2tdfa.findMappableState(states, u, mapping);

      DFAState newState = mappedState;
      Instructions c = new Instructions();
      if (mappedState == null) {
        mapping = null; // Won't be needed then.
        newState = u;
        states.add(newState);
        for (Instruction i : uu.instructions) {
          c.add(i);
        }
      } else {
        for (final Instruction i : uu.instructions) {
          Iterable<Instruction> remap = i.remap(mapping);
          for (Instruction r : remap) {
            c.add(r);
          }
        }
        Set<History> oldHistories = new HashSet<>();
        for (History[] hs : u.innerStates.values()) {
          for (History h : hs) {
            if (h != null) {
              oldHistories.add(h);
            }
          }
        }
        oldHistories.removeAll(uu.newHistories);
        Collection<Instruction> mappingInstructions = tnfa2tdfa.mappingInstructions(mapping, oldHistories);
        for (final Instruction i : mappingInstructions) {
          c.add(i);
        }
      }
      assert newState != null;

      for (final Instruction instruction : c) {
        instruction.execute(pos);
      }

      tdfaBuilder.addTransition(t, inputRange, newState, c);
      t = newState;
    }

    final History[] fin = t.innerStates.get(tnfa2tdfa.tnfa.getFinalState());
    if (fin == null) {
      return RealMatchResult.NoMatchResult.SINGLETON;
    }

    return new RealMatchResult(fin, input);
  }
}
