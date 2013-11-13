package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;
import ch.unibe.scg.regex.TDFATransitionTable.NextDFAState;
import ch.unibe.scg.regex.TDFATransitionTable.NextState;
import ch.unibe.scg.regex.TNFAToTDFA.StateAndInstructions;

/** Interprets the known TDFA states. Compiles missing states on the fly. */
// TODO: Rename to Pattern. Make public.
public class TDFAInterpreter {
  private static final int COMPILE_THRESHOLD = 2;

  final NavigableSet<DFAState> states = new TreeSet<>();

  final TDFATransitionTable.Builder tdfaBuilder = new TDFATransitionTable.Builder();
  final InputRangeCleanup inputRangeCleanup = new InputRangeCleanup();
  final TNFAToTDFA tnfa2tdfa;

  TDFAInterpreter(TNFAToTDFA tnfa2tdfa) {
    this.tnfa2tdfa = tnfa2tdfa;
  }

  public static TDFAInterpreter compile(String regex) {
    final Regex parsed = new ParserProvider().regexp().parse(regex);
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    return new TDFAInterpreter(TNFAToTDFA.make(tnfa));
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

  public MatchResultTree interpret(CharSequence input) {
    final List<InputRange> inputRanges = inputRangeCleanup.cleanUp(tnfa2tdfa.tnfa.allInputRanges());
    List<RThread> startUnexpanded = tnfa2tdfa.convertToDfaState(tnfa2tdfa.tnfa.initialState);
    StateAndInstructions startState = tnfa2tdfa.oneStep(startUnexpanded, null);

    DFAState dfaState = startState.dfaState;
    states.add(dfaState);

    int cacheHits = 0;
    TDFATransitionTable tdfa = null;
    int tdfaState = -1;

    for (final Instruction instruction : startState.instructions) {
      instruction.execute(-1);
    }

    NextState newState = new NextState(); // Output parameter to save allocations
    int inputLen = input.length(); // Prevent re-executing on every loop step.
    for (int pos = 0; pos < inputLen; pos++) {
      final char a = input.charAt(pos);

      // If there is a TDFA, see if it has a transition. Execute if there and continue.
      if (tdfa != null) {
        tdfa.newStateAndInstructions(tdfaState, a, newState);
        if (newState.found) {
          for (Instruction i : newState.instructions) {
            i.execute(pos);
          }
          tdfaState = newState.nextState;
          continue;
        }
        tdfa = null;
        dfaState = tdfaBuilder.mapping.deoptimized.get(tdfaState);
        tdfaState = -1;
        cacheHits = 0;
      } else { // Find the transition in the builder. Execute if there and continue.
        NextDFAState nextState = tdfaBuilder.availableTransition(dfaState, a);
        if (nextState != null) {
          for (final Instruction i : nextState.instructions) {
            i.execute(pos);
          }

          dfaState = nextState.nextState;

          cacheHits++;
          if (cacheHits > COMPILE_THRESHOLD) {
            tdfa = tdfaBuilder.build();
            tdfaState = tdfaBuilder.mapping.mapping.get(dfaState);
          }

          continue;
        }
      }

      cacheHits = 0; // We got here because the cache hasn't seen this transition before.

      final InputRange inputRange = findInputRange(inputRanges, a);
      if (inputRange == null) {
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      // TODO this is ugly. Clearly, e should return StateAndPositions.
      final StateAndInstructions uu = tnfa2tdfa.oneStep(dfaState.threads, inputRange);
      if (uu == null) { // There is no matching NFA state.
        return RealMatchResult.NoMatchResult.SINGLETON;
      }
      final DFAState u = uu.dfaState;

      Map<History, History> mapping = new LinkedHashMap<>();

      // If there is a valid mapping, findMappableStates will modify mapping into it.
      final DFAState mappedState = tnfa2tdfa.findMappableState(states, u, mapping);

      DFAState nextState = mappedState;
      List<Instruction> c = new ArrayList<>(uu.instructions);
      if (mappedState == null) {
        mapping = null; // Won't be needed then.
        nextState = u;
        states.add(nextState);
      } else {
        final List<Instruction> mappingInstructions = tnfa2tdfa.mappingInstructions(mapping);
        c.addAll(mappingInstructions);
      }

      for (final Instruction instruction : c) {
        instruction.execute(pos);
      }

      assert historiesOk(nextState.threads) : nextState.threads;

      tdfaBuilder.addTransition(dfaState, inputRange, nextState, c);

      dfaState = nextState;
    }

    // Restore full state before extracing information.
    if (tdfa != null) {
      dfaState = tdfaBuilder.mapping.deoptimized.get(tdfaState);
    }

    final History[] fin = dfaState.finalHistories;
    if (fin == null) {
      return RealMatchResult.NoMatchResult.SINGLETON;
    }

    int[] parentOf = tnfa2tdfa.makeParentOf();
    return new RealMatchResult(fin, input, parentOf);
  }

  /** Invariant: opening and closing tags must have same length histories. */
  private boolean historiesOk(List<RThread> threads) {
    for (final RThread thread : threads) {
      History[] h = thread.histories;
      for (int i = 0; i < h.length; i += 2) {
        if (h[i+1] != null) {
          assert h[i] != null;
          if (h[i].size() != h[i+1].size()) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
