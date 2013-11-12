package ch.unibe.scg.regex;

/**
 * A generic class representing a {@link Pair} of two kind of {@link Object}
 *
 * @author Fabien Dubosson
 * @param <A> The first {@link Object} {@link Class} type
 * @param <B> The second {@link Object} {@link Class} type
 */
class Pair<A extends Comparable<A>, B extends Comparable<B>> implements Comparable<Pair<A, B>> {
  final A first;
  final B second;

  /**
   * Constructor taking the two {@link Object} as parameters
   *
   * @param first The first {@link Object} of the {@link Pair}
   * @param second The second {@link Object} of the {@link Pair}
   */
  Pair(final A first, final B second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public int compareTo(final Pair<A, B> o) {
    final int result = (this.first).compareTo(o.first);
    if (result != 0) {
      return result;
    }
    return this.second.compareTo(o.second);
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
    final int prime = 31;
    int result = 1;
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }
}
