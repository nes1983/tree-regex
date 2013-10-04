package ch.unibe.scg.regex;

import java.util.Arrays;

class Memory {
  History[] histories = new History[8];

  /**
   * Flyweight for the shared history of the memory cells. Singly-linked list where the
   * head (and it's {@code cur}) is mutable, but the rest (everything visible from {@code prev})
   * is immutable.
   */
  private static class History {
    int cur;
    final History prev;

    History(History history) {
      cur = history.cur;
      prev = history.prev;
    }

    History() {
      prev = null;
    }

    IntIterator iterator() {
      return new RealIntIterator(this);
    }

    @Override
    public String toString() {
      final StringBuilder ret = new StringBuilder("(");
      final IntIterator iter = iterator();
      while (iter.hasNext()) {
        ret.append(iter.next());
        ret.append(' ');
      }
      ret.append(")");
      return ret.toString();
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
  }

  static interface IntIterator {
    public boolean hasNext();

    public int next();
  }

  void write(int pos, int value) {
    if (histories.length <= pos) {
      grow(pos);
    }
    if (histories[pos] == null) {
      histories[pos] = new History();
    }
    histories[pos].cur = value;
  }

  void copyTo(int from, int to) {
    // Copies the head of `from`, because it is mutable.
    histories[to] = new History(histories[from]);
  }

  void commit(int pos) {
    histories[pos] = new History(histories[pos]);
  }

  void grow(int pos) {
    final History[] newHistories = new History[Math.max(2 * histories.length, pos + 1)];
    System.arraycopy(histories, 0, newHistories, 0, histories.length);
    histories = newHistories;
  }

  @Override
  public String toString() {
    return Arrays.toString(histories);
  }

  // TODO kill this.
  int getLatestValue(int i) {
    return histories[i].cur;
  }
}
