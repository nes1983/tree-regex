package ch.unibe.scg.regex;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.unibe.scg.regex.TNFAToTDFA.DFAStateComparator;

class DFAState implements Comparable<DFAState> {
  final static DFAState INSTRUCTIONLESS_NO_STATE
      = new DFAState(Collections.<State, int[]> emptyMap());

  private final Map<State, int[]> innerStates;

  DFAState(final Map<State, int[]> innerStates) {
    this.innerStates = Collections.unmodifiableMap(innerStates);
  }

  static String statesToString(final Map<State, int[]> states) {
    final StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (final Map.Entry<State, int[]> el : states.entrySet()) {
      sb.append(el.getKey());
      sb.append("->");
      sb.append(Arrays.toString(el.getValue()));
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length());
    sb.append(')');
    return sb.toString();
  }

  @Override
  public int compareTo(final DFAState o) {
    return DFAStateComparator.SINGLETON.compare(this.innerStates, o.innerStates);
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
    final DFAState other = (DFAState) obj;
    if (innerStates == null) {
      if (other.innerStates != null) {
        return false;
      }
    } else if (!innerStates.keySet().equals(other.innerStates.keySet())) {
      return false;
    }

    return true;
  }

  Map<State, int[]> getData() {
    return innerStates;
  }

  @Override
  public int hashCode() {
    if (innerStates == null) {
      return 1;
    }

    return innerStates.keySet().hashCode();
  }

  /**
   * @param tnfa the TNFA that knows whether or not this state is final.
   * @return mapping if this state is final. Otherwise, return null.
   */
  int[] finalStateMappingIfAny(TNFA tnfa) {
    final Set<State> finalStates = new HashSet<>(tnfa.getFinalStates());
    finalStates.retainAll(innerStates.keySet());

    if (finalStates.isEmpty()) {
      return null;
    }

    if (finalStates.size() > 1) {
      throw new IllegalStateException("There should only be one final state, but there were "
          + finalStates);
    }

    return innerStates.get(finalStates.iterator().next());
  }

  boolean isMappable(final DFAState other, final int[] mapping) {
    if (!this.innerStates.keySet().equals(other.innerStates.keySet())) {
      return false;
    }
    Arrays.fill(mapping, -1);

    for (final Map.Entry<State, int[]> entry : innerStates.entrySet()) {
      final int[] mine = entry.getValue();
      final int[] theirs = other.innerStates.get(entry.getKey());
      final boolean success = updateMap(mapping, mine, theirs);
      if (!success) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString() {
    return statesToString(innerStates);
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
  private boolean updateMap(final int[] map, final int[] from, final int[] to) {
    assert from.length == to.length;

    // Go over the tag list and iteratively try to find counterexample.
    for (int i = 0; i < from.length; i++) {
      // if the tag hasn't been set in either state, it's ok.
      if (from[i] < 0 && to[i] < 0) {
        continue; // Both leave i unspecified: that's fine.
      } else if ((from[i] < 0 && to[i] >= 0) || (from[i] >= 0 && to[i] < 0)) {
        return false; // Only from specifies the mapping, that won't do.
      } else if (map[from[i]] == -1) {
        map[from[i]] = to[i];
      } else if (map[from[i]] != to[i]) {
        return false;
      } // Else everything is fine.
    }
    return true;
  }
}