package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Immutable instruction for interpretation in tagged automata. */
interface Instruction {
  /** Not threadsafe. */
  class InstructionMaker {
    public static InstructionMaker get() {
      return new InstructionMaker();
    }

    int id = -1;

    public int nextId() {
      return ++id;
    }

    Instruction openingCommit(final int memoryPos) {
      return new OpeningCommitInstruction(memoryPos);
    }

    Instruction closingCommit(final int memoryPos) {
      return new ClosingCommitInstruction(memoryPos);
    }

    Instruction reorder(final int from, final int to) {
      return new ReorderInstruction(from, to);
    }

    Instruction storePos(final int tag) {
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
      memory.copyTo(to, from);
    }

    @Override
    public String toString() {
      return String.valueOf(from) + "->" + to;
    }

    @Override
    public Instruction remap(int[] mapping) {
      throw new UnsupportedOperationException("Mappings should not be mapped again.");
    }
  }

  static class SetInstruction implements Instruction {
    final int memoryPos;

    SetInstruction(final int tag) {
      this.memoryPos = tag;
    }

    @Override
    public void execute(final Memory memory, final int inputPos) {
      memory.write(memoryPos, inputPos);
    }

    @Override
    public String toString() {
      return "" + memoryPos + "<- pos";
    }

    @Override
    public Instruction remap(int[] mapping) {
      return new SetInstruction(mapping[memoryPos]);
    }
  }

  static class ClosingCommitInstruction implements Instruction {
    final int memoryPos;

    ClosingCommitInstruction(final int memoryPos) {
      this.memoryPos = memoryPos;
    }

    @Override
    public void execute(Memory memory, int unusedPos) {
      memory.commit(memoryPos);
    }

    @Override
    public String toString() {
      return "c↓(" + memoryPos + ")";
    }

    @Override
    public Instruction remap(int[] mapping) {
      return new ClosingCommitInstruction(mapping[memoryPos]);
    }
  }

  static class OpeningCommitInstruction implements Instruction {
    final int memoryPos;

    OpeningCommitInstruction(final int memoryPos) {
      this.memoryPos = memoryPos;
    }

    @Override
    public void execute(Memory memory, int unusedPos) {
      memory.commit(memoryPos);
    }

    @Override
    public String toString() {
      return "c↑(" + memoryPos + ")";
    }

    @Override
    public Instruction remap(int[] mapping) {
      return new OpeningCommitInstruction(mapping[memoryPos]);
    }
  }

  static class Instructions implements Iterable<Instruction> {
    final List<ReorderInstruction> moves = new ArrayList<>();
    final List<OpeningCommitInstruction> openingCommits = new ArrayList<>();
    final List<SetInstruction> stores = new ArrayList<>();
    final List<ClosingCommitInstruction> closingCommits = new ArrayList<>();

    Instructions() {}

    Instructions(Iterable<? extends Instruction> instructions) {
      for (Instruction i: instructions) {
        add(i);
      }
    }

    @Override
    public Iterator<Instruction> iterator() {
      // TODO(nikoschwarz): make iterator that does not copy the lists.
      List<Instruction> all = new ArrayList<>(moves.size() + openingCommits.size() + stores.size() + closingCommits.size());
      all.addAll(moves);
      all.addAll(openingCommits);
      all.addAll(stores);
      all.addAll(closingCommits);
      return all.iterator();
    }

    final void add(Instruction i) {
      if (i instanceof ReorderInstruction) {
        moves.add((ReorderInstruction) i);
      } else if (i instanceof OpeningCommitInstruction) {
        openingCommits.add((OpeningCommitInstruction) i);
      } else if (i instanceof SetInstruction) {
        stores.add((SetInstruction) i);
      } else if (i instanceof ClosingCommitInstruction) {
        closingCommits.add((ClosingCommitInstruction) i);
      } else {
        throw new AssertionError("Unknown instruction type: " + i.getClass());
      }
    }

    @Override
    public String toString() {
      List<Instruction> all = new ArrayList<>();
      for (Instruction i : this) {
        all.add(i);
      }
      return all.toString();
    }
  }

  public void execute(Memory memory, int pos);

  /** @return Same instruction as if the mapping was prepended. */
  public Instruction remap(int[] mapping);
}
