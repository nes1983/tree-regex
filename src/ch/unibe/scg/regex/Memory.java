package ch.unibe.scg.regex;

import java.util.Arrays;

class Memory {
  private int[] memory = new int[8];

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
