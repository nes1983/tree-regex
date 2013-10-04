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

    public int nextId() {
      return ++id;
    }

    public Instruction reorder(final int from, final int to) {
      return new ReorderInstruction(from, to);
    }

    public Instruction storePos(final int tag) {
      return new SetInstruction(tag);
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
      memory.copyTo(from, to);
    }

    @Override
    public String toString() {
      return String.valueOf(from) + "->" + to;
    }
  }

  static class SetInstruction implements Instruction {
    final int tag;

    SetInstruction(final int tag) {
      this.tag = tag;
    }

    @Override
    public void execute(final Memory memory, final int pos) {
      memory.write(tag, pos);
    }

    @Override
    public String toString() {
      return "" + tag + "<- pos";
    }
  }

  static class CommitInstruction implements Instruction {
    final int memoryPos;

    CommitInstruction(final int memoryPos) {
      this.memoryPos = memoryPos;
    }

    @Override
    public void execute(Memory memory, int unusedPos) {
      memory.commit(memoryPos);
    }
  }

  public void execute(Memory memory, int pos);
}
