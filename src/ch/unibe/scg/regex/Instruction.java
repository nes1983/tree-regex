package ch.unibe.scg.regex;

import static java.util.Objects.requireNonNull;

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

    Instruction openingCommit(final History tdash) {
      return new OpeningCommitInstruction(tdash);
    }

    Instruction closingCommit(final History newHistory) {
      return new ClosingCommitInstruction(newHistory);
    }

    Instruction reorder(History target, History source) {
      return new ReorderInstruction(target, source);
    }

    Instruction storePos(final History newHistory) {
      return new SetInstruction(newHistory, 0);
    }

    Instruction storePosPlusOne(History newHistory) {
      return new SetInstruction(newHistory, 1);
    }
  }

  static class ReorderInstruction implements Instruction {
    final History from, to;

    ReorderInstruction(History to, History from) {
      this.to = requireNonNull(to);
      this.from = requireNonNull(from);
    }

    @Override
    public void execute(int pos) {
      to.cur = from.cur;
      to.prev = from.prev;
    }

    @Override
    public String toString() {
      return String.valueOf(from.id) + "->" + to.id;
    }
  }

  static class SetInstruction implements Instruction {
    final History history;
    final int offset;

    SetInstruction(final History newHistory, int offset) {
      this.history = requireNonNull(newHistory);
      this.offset = offset;
    }

    @Override
    public void execute(final int inputPos) {
      history.cur = inputPos + offset;
    }

    @Override
    public String toString() {
      if (offset == 0) {
        return "" + history.id + "<- pos";
      }

      return "" + history.id + "<- pos+" + offset;
    }
  }

  static class OpeningCommitInstruction implements Instruction {
    final History history;

    OpeningCommitInstruction(final History newHistory) {
      this.history = requireNonNull(newHistory);
    }

    @Override
    public void execute(int unusedPos) {
      history.prev = new History(-1L, history.cur, history.prev);
    }

    @Override
    public String toString() {
      return "c↑(" + history.id + ")";
    }
  }

  static class ClosingCommitInstruction implements Instruction {
    final History history;

    ClosingCommitInstruction(final History newHistory) {
      this.history = requireNonNull(newHistory);
    }

    @Override
    public void execute(int unusedPos) {
      history.prev = new History(-1L, history.cur, history.prev);
    }

    @Override
    public String toString() {
      return "c↓(" + history.id + ")";
    }
  }

  public void execute(int pos);
}
