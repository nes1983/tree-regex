package ch.unibe.scg.regex;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import ch.unibe.scg.regex.TNFAToTDFA.DFAStateComparator;

class DFAState implements Comparable<DFAState> {
  final static DFAState INSTRUCTIONLESS_NO_STATE
      = new DFAState(Collections.<State, History[]> emptyMap());

  final Map<State, History[]> innerStates;

  DFAState(final Map<State, History[]> map) {
    this.innerStates = Collections.unmodifiableMap(map);
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

  @Override
  public int hashCode() {
    if (innerStates == null) {
      return 1;
    }

    return innerStates.keySet().hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (final Map.Entry<State, History[]> el : innerStates.entrySet()) {
      sb.append(el.getKey());
      sb.append("->");
      sb.append(Arrays.toString(el.getValue()));
      sb.append(", ");
    }
    sb.delete(sb.length() - 2, sb.length());
    sb.append(')');
    return sb.toString();
  }
}