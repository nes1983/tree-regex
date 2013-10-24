package ch.unibe.scg.regex;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Map;

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

    @Override
    public Iterable<Instruction> remap(Map<History, History> mapping) {
      assert mapping.containsKey(to);

      return Arrays.<Instruction> asList(new ReorderInstruction(mapping.get(to), from));
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

    @Override
    public Iterable<Instruction> remap(Map<History, History> mapping) {
      assert mapping.containsKey(history) : String.format("%s %s", history, mapping);

      return Arrays.<Instruction> asList(new SetInstruction(mapping.get(history), offset));
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

    @Override
    public Iterable<Instruction> remap(Map<History, History> mapping) {
      assert mapping.containsKey(history);

      return Arrays.<Instruction> asList(new OpeningCommitInstruction(mapping.get(history)));
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

    @Override
    public Iterable<Instruction> remap(Map<History, History> mapping) {
      assert mapping.containsKey(history);

      final History mappedHistory = mapping.get(history);
      return Arrays.<Instruction> asList(
        new ResetInstruction(mappedHistory),
        new ClosingCommitInstruction(mappedHistory));
    }
  }

  static class ResetInstruction implements Instruction {
    final History history;

    ResetInstruction(History history) {
      this.history = history;
    }

    @Override
    public void execute(int pos) {
      history.prev = null;
    }

    @Override
    public Iterable<Instruction> remap(Map<History, History> mapping) {
      throw new UnsupportedOperationException(
        "ResetInstructions are only produced in the remapping phase.");
    }

    @Override
    public String toString() {
      return "r(" + history.id + ")";
    }
  }

  public void execute(int pos);

  /** @return Same instruction as if the mapping was prepended. */
  public Iterable<Instruction> remap(Map<History, History> mapping);
}
