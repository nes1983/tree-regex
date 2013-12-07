package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

/** A fixed-size copy-on-write datastructure that supports accessing histories. */
abstract class Arraylike implements Iterable<History> {
  public abstract Arraylike set(final int index, final History h);

  public abstract History get(final int index);

  public abstract int size();

  /** @return an Arraylike that is efficient for the size. */
  static Arraylike make(final int size) {
    // TODO back heuristic up with data
    if (size < 20) {
      return new HistoryArray(size);
    }
    return new TreeArray(size);
  }


  /** A fixed-size copy-on-write left-complete binary tree structure. */
  static final class TreeArray extends Arraylike {
    TreeArray left;
    TreeArray right;
    final int size;
    History payload;

    /**
     *  Tree creation ensures left-completeness structure. 
     *  It has log_2(n) depth, the order is left-first.
     * 
     * @param n number of elements.
     */
    TreeArray(int n) {
      // Recursion is log(n) downwards, so we should be fine.
      assert n > 0;
      size = n;
      // Every node has a payload
      payload = new History();
      // we fill maximal power of two smaller than n to the left.
      int rest = n - 1;
      final int rightSize = rest / 2;
      final int leftSize = rest - rightSize;
      assert leftSize >= rightSize;
      left = null;
      right = null;
      if (leftSize > 0) {
        left = new TreeArray(leftSize);
      }
      if (rightSize > 0) {
        right = new TreeArray(rightSize);
      }
    }

    private TreeArray(TreeArray ta) {
      this.size = ta.size;
      this.payload = ta.payload;
      this.left = ta.left;
      this.right = ta.right;
    }

    @Override
    public Arraylike set(int index, final History h) {
      assert 0 <= index;
      assert index < size;
      TreeArray top = new TreeArray(this);
      TreeArray current = top;
      while (index > 0) {
        index--;
        if (current.left.size > index) {
          current.left = new TreeArray(current.left);
          current = current.left;
        } else {
          index -= current.left.size;
          current.right = new TreeArray(current.right);
          current = current.right;
        }
      }
      current.payload = h;
      return top;
    }

    @Override
    public History get(int index) {
      assert 0 <= index;
      assert index < size;

      TreeArray current = this;
      while (index > 0) {
        index--;
        if (current.left.size > index) {
          current = current.left;
        } else {
          index -= current.left.size;
          current = current.right;
        }
      }
      return current.payload;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("[");
      for (History h : this) {
        sb.append(h);
        sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
    }

    @Override
    public Iterator<History> iterator() {
      return new TreeArrayIterator(this);
    }

    static final class TreeArrayIterator implements Iterator<History> {
      private final Deque<TreeArray> descentStack = new ArrayDeque<>();

      private TreeArrayIterator(final TreeArray treeArray) {
        descentStack.addFirst(treeArray);
      }

      @Override
      public boolean hasNext() {
        return !descentStack.isEmpty();
      }

      @Override
      public History next() {
        // First current ...
        TreeArray current = descentStack.removeFirst();
        History ret = current.payload;

        // then left, then right (but we fill a stack).
        if (current.right != null) {
          descentStack.addFirst(current.right);
        }
        if (current.left != null) {
          descentStack.addFirst(current.left);
        }
        return ret;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Removing from TreeArray not defined.");
      }
    }

    @Override
    public int size() {
      return size;
    }
  }


  /**
   * Trivial implementation of Arraylike as array of Histories.
   * 
   * This is fast for small sizes, but takes linear time to change.
   */
  final static class HistoryArray extends Arraylike {
    final private History[] histories;

    HistoryArray(final int n) {
      histories = new History[n];
      for (int i = 0; i < n; i++) {
        histories[i] = new History();
      }
    }

    private HistoryArray(final HistoryArray orig) {
      histories = Arrays.copyOf(orig.histories, orig.histories.length);
    }

    @Override
    public HistoryArray set(int index, History h) {
      HistoryArray newHistories = new HistoryArray(this);
      newHistories.histories[index] = h;
      return newHistories;
    }

    @Override
    public History get(final int index) {
      return histories[index];
    }

    @Override
    public Iterator<History> iterator() {
      return new ArrayIterator();
    }

    @Override
    public int size() {
      return histories.length;
    }

    private class ArrayIterator implements Iterator<History> {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < histories.length;
      }

      @Override
      public History next() {
        return histories[i++];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Removing elements from Array not supported.");
      }
    }
  }
}
