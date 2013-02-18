package ch.unibe.scg.regex;

import java.util.Arrays;

class Memory {
  private int[] memory = new int[8];

  static class History {
    int[] entries = new int[1];
    int pos = 0;

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
    ensureSize(pos);
    memory[pos] = value;
  }

  int read(int pos) {
    return memory[pos];
  }

  private void ensureSize(int pos) {
    if (memory.length <= pos) {
      final int[] newMemory = new int[Math.max(2 * memory.length, pos + 1)];
      System.arraycopy(memory, 0, newMemory, 0, memory.length);
      memory = newMemory;
    }
  }

  @Override
  public String toString() {
    return Arrays.toString(memory);
  }
}
