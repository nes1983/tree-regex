package ch.unibe.scg.regex;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Flyweight for the shared history of the memory cells. Singly-linked list where the
 * head (and it's {@code cur}) is mutable, but the rest (everything visible from {@code prev})
 * is immutable.
 */
class History implements IntIterable {
  final static private AtomicLong nextId = new AtomicLong();
  static {
    resetCount();
  }

  final long id;

  int cur;
  History prev;

  History(long id, int head, History history) {
    this.id = id;
    cur = head;
    prev = history;
  }

  static class RealIntIterator implements IntIterator {
    History history;

    RealIntIterator(History history) {
      this.history = history;
    }

    @Override
    public boolean hasNext() {
      return history != null;
    }

    @Override
    public int next() {
      final int ret = history.cur;
      history = history.prev;
      return ret;
    }
  }

  static void resetCount() {
    nextId.set(-1L);
  }

  History() {
    this(nextId.incrementAndGet(), 0, null);
  }

  @Override
  public IntIterator iterator() {
    return new RealIntIterator(this);
  }

  @Override
  public String toString() {
    final StringBuilder ret = new StringBuilder(String.valueOf(id) + "(");
    final IntIterator iter = iterator();
    while (iter.hasNext()) {
      ret.append(iter.next());
      ret.append(' ');
    }
    ret.append(")");
    return ret.toString();
  }

  @Override
  public int hashCode() {
    return (int) id;
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
    History other = (History) obj;
    return this.id == other.id;
  }
}
