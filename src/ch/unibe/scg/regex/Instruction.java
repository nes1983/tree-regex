package ch.unibe.scg.regex;

import java.util.LinkedHashMap;
import java.util.Map;

interface Instruction {
  static class CopyInstruction implements Instruction {
    public static Instruction make(final int fromTag, final int fromPos, final int toTag,
        final int toPos) {
      return new CopyInstruction(fromTag, fromPos, toTag, toPos);
    }

    final int fromTag, fromPos, toTag, toPos;

    public CopyInstruction(final int fromTag, final int fromPos, final int toTag, final int toPos) {
      this.fromTag = fromTag;
      this.fromPos = fromPos;
      this.toTag = toTag;
      this.toPos = toPos;
    }

    @Override
    public void execute(Memory memory, int pos) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public String toString() {
      return "" + fromTag + "," + fromPos + " <- " + toTag + "," + toPos;
    }
  }

  /** Not threadsafe. */
  class InstructionMaker {
    public static InstructionMaker get() {
      return new InstructionMaker();
    }

    int id = -1;

    Map<Tag, Integer> tagIds = new LinkedHashMap<>();

    int lookup(final Tag tag) {
      final Integer id = tagIds.get(tag);
      if (id != null) {
        return id;
      }
      final int ret = nextId();
      tagIds.put(tag, ret);
      return ret;
    }

    public int nextId() {
      return ++id;
    }

    public Instruction reorder(final int from, final int to) {
      return new ReorderInstruction(from, to);
    }

    public Instruction storePos(final int tag) {
      return SetInstruction.make(tag);
    }
  }

  static class ReorderInstruction implements Instruction {
    final int from, to;

    ReorderInstruction(int from, int to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public void execute(Memory memory, int pos) {
      memory.write(to, memory.read(from));
    }

    @Override
    public String toString() {
      return String.valueOf(from) + "->" + to;
    }
  }

  static class SetInstruction implements Instruction {
    static SetInstruction make(final int tag) {
      return new SetInstruction(tag);
    }

    final int tag;

    public SetInstruction(final int tag) {
      this.tag = tag;
    }

    public void execute(final Memory memory, final int pos) {
      memory.write(tag, pos);
    }

    @Override
    public String toString() {
      return "" + tag + "<- pos";
    }
  }

  public void execute(Memory memory, int pos);
}
