package ch.unibe.scg.regex;

import java.util.Objects;

/**
 * A generic class representing a {@link Pair} of two kind of {@link Object}
 *
 * @author Fabien Dubosson
 */
class Pair<A extends Comparable<A>, B extends Comparable<B>> implements Comparable<Pair<A, B>> {
  final A first;
  final B second;

  Pair(final A first, final B second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public int compareTo(final Pair<A, B> o) {
    final int result = first.compareTo(o.first);
    if (result != 0) {
      return result;
    }
    return second.compareTo(o.second);
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
    final Pair<?, ?> other = (Pair<?, ?>) obj;
    if (first == null) {
      if (other.first != null) {
        return false;
      }
    } else if (!first.equals(other.first)) {
      return false;
    }
    if (second == null) {
      if (other.second != null) {
        return false;
      }
    } else if (!second.equals(other.second)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }
}
