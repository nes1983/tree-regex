package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.ParserProvider.Node.Regex;
import ch.unibe.scg.regex.TDFATransitionTable.NextDFAState;
import ch.unibe.scg.regex.TNFAToTDFA.StateAndInstructions;

/** Interprets the known TDFA states. Compiles missing states on the fly. */
// TODO: Rename to Pattern. Make public.
public class TDFAInterpreter {
  final NavigableSet<DFAState> states = new TreeSet<>();

  final TDFATransitionTable.Builder tdfaBuilder = new TDFATransitionTable.Builder();
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
    final List<InputRange> inputRanges = tnfa2tdfa.allInputRanges();

    StateAndInstructions startState = tnfa2tdfa.makeStartState();
    DFAState t = startState.dfaState;
    states.add(t);

    for (final Instruction instruction : startState.instructions) {
      instruction.execute(-1);
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
      final StateAndInstructions uu = tnfa2tdfa.epsilonClosure(t.innerStates, a, false);
      final DFAState u = uu.dfaState;

      if (u.innerStates.isEmpty()) { // There is no matching NFA state.
        return RealMatchResult.NoMatchResult.SINGLETON;
      }

      Map<History, History> mapping = new LinkedHashMap<>();

      // If there is a valid mapping, findMappableStates will modify mapping into it.
      final DFAState mappedState = tnfa2tdfa.findMappableState(states, u, mapping);

      DFAState newState = mappedState;
      List<Instruction> c = new ArrayList<>(uu.instructions);
      if (mappedState == null) {
        mapping = null; // Won't be needed then.
        newState = u;
        states.add(newState);
      } else {
        final List<Instruction> mappingInstructions = tnfa2tdfa.mappingInstructions(mapping);
        c.addAll(mappingInstructions);
      }

      for (final Instruction instruction : c) {
        instruction.execute(pos);
      }

      // TODO: delete?
      // Invariant: opening and closing tags must have same length histories.
      for (final History[] s : newState.innerStates.values()) {
        for (int i = 0; i < s.length; i += 2) {
          if (s[i+1] != null) {
            assert s[i] != null;
            if (s[i].size() != s[i+1].size()) {
              assert false;
            }
          }
        }
      }

      tdfaBuilder.addTransition(t, inputRange, newState, c);

      t = newState;
    }

    final History[] fin = t.innerStates.get(tnfa2tdfa.tnfa.getFinalState());
    if (fin == null) {
      return RealMatchResult.NoMatchResult.SINGLETON;
    }

    int[] parentOf = tnfa2tdfa.makeParentOf();
    return new RealMatchResult(fin, input, parentOf);
  }
}
