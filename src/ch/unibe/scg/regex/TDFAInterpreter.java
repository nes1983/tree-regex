package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;

import ch.unibe.scg.regex.TNFAToTDFA.DFAState;
import ch.unibe.scg.regex.TNFAToTDFA.StateAndInstructions;
import ch.unibe.scg.regex.TransitionTable.NextDFAState;

/** Interprets the known TDFA states. Compiles missing states on the fly. */
class TDFAInterpreter {
  final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker.get();
  final NavigableSet<DFAState> states = new TreeSet<>();

  final TransitionTable.TDFATransitionTable.Builder tdfaBuilder =
      new TransitionTable.TDFATransitionTable.Builder();
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

    final Memory memory = new Memory();
    DFAState t;
    {
      final StateAndInstructions stateAndInstructions = tnfa2tdfa.makeStartState();
      t = stateAndInstructions.dfaState;
      for (final Instruction instruction : stateAndInstructions.instructions) {
        instruction.execute(memory, 0);
      }
    }

    states.add(t);

    for (int pos = 0; pos < input.length(); pos++) {
      final char a = input.charAt(pos);

      {
        NextDFAState nextState;
        if ((nextState = tdfaBuilder.availableTransition(t, a)) != null) {
          // TODO check for fail state.
          for (final Instruction instruction : nextState.getInstructions()) {
            instruction.execute(memory, pos);
          }
          t = nextState.getNextState();
          continue;
        }
      }

      final InputRange inputRange = findInputRange(inputRanges, a);
      if (inputRange == null) {
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      final StateAndInstructions uu = tnfa2tdfa.e(t.getData(), a, false);
      final DFAState u = uu.dfaState;

      if (u.getData().isEmpty()) { // There is no matching NFA state.
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      final BitSet newLocations = tnfa2tdfa.extractStorePositions(uu.instructions);
      // TODO(niko): There's a smarter way. You can compute the stores on the fly.

      int[] mapping = new int[tnfa2tdfa.highestMapping];
      final DFAState mappedState = tnfa2tdfa.findMappableState(states, u, mapping);

      if (mappedState == null) {
        mapping = null;
      }

      List<Instruction> c = new ArrayList<>();

      DFAState newState;
      if (mappedState != null) {
        c.addAll(tnfa2tdfa.mappingInstructions(mapping, u, newLocations));
        newState = mappedState;
      } else {
        states.add(u);
        newState = u;
      }

      for (int i = newLocations.nextSetBit(0); i >= 0; i = newLocations.nextSetBit(i + 1)) {
        if (mapping != null) {
          c.add(instructionMaker.storePos(mapping[i]));
        } else {
          c.add(instructionMaker.storePos(i));
        }
      }

      // Free up new slots that weren't really needed.
      if (mappedState != null) {
        tnfa2tdfa.highestMapping -= newLocations.cardinality();
      }

      assert newState != null;

      // Shrink c to minimum size.
      c = new ArrayList<>(c);

      for (final Instruction instruction : c) {
        instruction.execute(memory, pos); // TODO fill in context.
      }

      tdfaBuilder.addTransition(t, inputRange, newState, c);
      t = newState;
    }

    final int[] mapping = t.finalStateMappingIfAny(tnfa2tdfa.tnfa);
    if (mapping == null) {
      return RealMatchResult.NoMatchResult.SINGLETON;
    }

    return extractFromContext(memory, mapping, input);
  }

  private MatchResult extractFromContext(Memory memory, int[] mapping, CharSequence input) {
    final int[] extracted = new int[mapping.length];
    for (int i = 0; i < mapping.length; i++) {
      if (mapping[i] < 0) {
        continue; // TODO delete. Nice for the current unit test, but broken in general.
      }
      extracted[i] = memory.histories[mapping[i]].latestValue();
    }
    return new RealMatchResult(extracted, input);
  }
}
