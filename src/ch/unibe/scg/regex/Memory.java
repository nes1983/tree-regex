package ch.unibe.scg.regex;

import java.util.Arrays;
import java.util.NoSuchElementException;

class Memory {
  History[] histories = new History[8];

  static class History {
    int[] entries = new int[1];
    int pos = 0;

    History(History history) {
      this.entries = history.entries;
      this.pos = history.pos;
    }

    History() {}

    void push(int entry) {
      if (pos + 1 >= entries.length) {
        grow();
      }
      entries[pos] = entry;
      pos++;
    }

    private void grow() {
      final int[] newEntries = new int[Math.max(3 * entries.length / 2, 2)];
      System.arraycopy(entries, 0, newEntries, 0, entries.length);
      entries = newEntries;
    }

    IntIterator iterator() {
      return new RealIntIterator(this);
    }

    int latestValue() {
      if (pos <= 0) {
        throw new NoSuchElementException("This history was never written into, but is read.");
      }
      return entries[pos - 1];
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
      final History history;
      int pos = 0;

      RealIntIterator(History history) {
        this.history = history;
      }

      @Override
      public boolean hasNext() {
        return pos < history.pos;
      }

      @Override
      public int next() {
        final int ret = history.entries[pos];
        pos++;
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
    histories[pos].push(value);
  }

  void copyTo(int from, int to) {
    histories[to] = new History(histories[from]);
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
}
