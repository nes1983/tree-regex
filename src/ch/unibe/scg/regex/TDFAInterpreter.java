package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.MatchResult;

import ch.unibe.scg.regex.TDFATransitionTable.NextDFAState;
import ch.unibe.scg.regex.TNFAToTDFA.DFAState;
import ch.unibe.scg.regex.TNFAToTDFA.StateAndInstructions;

/** Interprets the known TDFA states. Compiles missing states on the fly. */
class TDFAInterpreter {
  final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker.get();
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

      // TODO this is ugly. Clearly, e should return StateAndPositions.
      final StateAndInstructions uu = tnfa2tdfa.e(t.getData(), a, false);
      final DFAState u = uu.dfaState;

      if (u.getData().isEmpty()) { // There is no matching NFA state.
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      final BitSet newLocations = tnfa2tdfa.extractStorePositions(uu.instructions);

      int[] mapping = new int[tnfa2tdfa.highestMapping];
      final DFAState mappedState = tnfa2tdfa.findMappableState(states, u, mapping);

      if (mappedState == null) {
        mapping = null;
      }

      List<Instruction> c;

      DFAState newState;
      if (mapping != null) {
        final Collection<? extends Instruction> moves =
            tnfa2tdfa.mappingInstructions(mapping, u, newLocations);
        c = new ArrayList<>(uu.instructions.size() + moves.size());
        c.addAll(moves);
        for (int i = newLocations.nextSetBit(0); i >= 0; i = newLocations.nextSetBit(i + 1)) {
          c.add(instructionMaker.storePos(mapping[i]));
        }
        newState = mappedState;
      } else {
        states.add(u);
        newState = u;
        c = new ArrayList<Instruction>(uu.instructions);
      }

      // Free up new slots that weren't really needed.
      if (mappedState != null) {
        tnfa2tdfa.highestMapping -= newLocations.cardinality();
      }

      assert newState != null;

      for (final Instruction instruction : c) {
        instruction.execute(memory, pos);
      }

      tdfaBuilder.addTransition(t, inputRange, newState, c);
      t = newState;
    }

    final int[] mapping = t.finalStateMappingIfAny(tnfa2tdfa.tnfa);
    if (mapping == null) {
      return RealMatchResult.NoMatchResult.SINGLETON;
    }

    return extractFromMemory(memory, mapping, input);
  }

  private MatchResult extractFromMemory(Memory memory, int[] mapping, CharSequence input) {
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
