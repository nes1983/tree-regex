package ch.unibe.scg.regex;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
      return new SetInstruction(newHistory);
    }
  }

  static class ReorderInstruction implements Instruction {
    final History from, to;

    ReorderInstruction(History from, History to) {
      this.from = requireNonNull(from);
      this.to = requireNonNull(to);
    }

    @Override
    public void execute(int pos) {
      from.cur = to.cur;
      from.prev = to.prev;
    }

    @Override
    public String toString() {
      return String.valueOf(from.id) + "->" + to.id;
    }

    @Override
    public Instruction remap(Map<History, History> mapping) {
      throw new UnsupportedOperationException("Mappings should not be mapped again.");
    }
  }

  static class SetInstruction implements Instruction {
    final History history;

    SetInstruction(final History newHistory) {
      this.history = requireNonNull(newHistory);
    }

    @Override
    public void execute(final int inputPos) {
      history.cur = inputPos;
    }

    @Override
    public String toString() {
      return "" + history.id + "<- pos";
    }

    @Override
    public Instruction remap(Map<History, History> mapping) {
      return new SetInstruction(mapping.get(history));
    }
  }

  static class ClosingCommitInstruction implements Instruction {
    final History history;

    ClosingCommitInstruction(final History newHistory) {
      this.history = requireNonNull(newHistory);
    }

    @Override
    public void execute(final int unusedPos) {
      history.prev = new History(-1L, history.cur, history.prev);
    }

    @Override
    public String toString() {
      return "c↓(" + history.id + ")";
    }

    @Override
    public Instruction remap(Map<History, History> mapping) {
      return new ClosingCommitInstruction(mapping.get(history));
    }
  }

  static class OpeningCommitInstruction implements Instruction {
    final History history;

    OpeningCommitInstruction(final History history) {
      this.history = requireNonNull(history);
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
    public Instruction remap(Map<History, History> mapping) {
      return new OpeningCommitInstruction(mapping.get(history));
    }
  }

  static class Instructions implements Iterable<Instruction> {
    final List<ReorderInstruction> moves = new ArrayList<>();
    final List<OpeningCommitInstruction> openingCommits = new ArrayList<>();
    final List<SetInstruction> stores = new ArrayList<>();
    final List<ClosingCommitInstruction> closingCommits = new ArrayList<>();

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

  public void execute(int pos);

  /** @return Same instruction as if the mapping was prepended. */
  public Instruction remap(Map<History, History> mapping);
}
