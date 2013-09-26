package ch.unibe.scg.regex;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable NFA state.
 *
 * <p>
 * The NFA keeps track of whether or not the state is final.
 *
 * @author Fabien Dubosson
 * @author Niko Schwarz
 */
class State implements Comparable<State> {
  /**
   * The unique identifier of the {@link State}
   */
  private final int id;

  /**
   * Constructs a {@link State} and assign it the next unique identifier
   */
  private State() {
    this.id = lastId.getAndIncrement();
  }

  /**
   * Counter to assign next unique identifier
   */
  private final static AtomicInteger lastId = new AtomicInteger(0);

  static State get() {
    return new State();
  }

  /**
   * Testing only.
   */
  static void resetCount() {
    lastId.set(0);
  }

  @Override
  public int compareTo(final State o) {
    return Integer.compare(getId(), o.getId());
  }

  /**
   * Gets the identifier of the {@link State}
   *
   * @return an {@link Integer} representing the {@link State} identifier
   */
  public int getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return "q" + id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final State other = (State) obj;
    if (id != other.id) {
      return false;
    }
    return true;
  }
}
