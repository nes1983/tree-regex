package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import ch.unibe.scg.regex.IntIterable.IntIterator;

class RealMatchResult implements MatchResultTree {
  final History[] captureGroupPositions;
  /** The parent capture group number `t` is parentOf[t]. */
  final int[] parentOf;
  final CharSequence input;

  RealMatchResult(History[] fin, CharSequence input, int[] parentOf) {
    this.captureGroupPositions = fin;
    this.input = input;
    this.parentOf = parentOf;
  }

  static enum NoMatchResult implements MatchResultTree {
    SINGLETON;

    @Override
    public int end() {
      return -1;
    }

    @Override
    public int end(final int group) {
      if (group == 0) {
        return end();
      }
      throw new NoSuchElementException();
    }

    @Override
    public String group() {
      throw new NoSuchElementException();
    }

    @Override
    public String group(final int group) {
      throw new NoSuchElementException();
    }

    @Override
    public int groupCount() {
      return -1;
    }

    @Override
    public int start() {
      return -1;
    }

    @Override
    public int start(final int group) {
      throw new NoSuchElementException();
    }

    @Override
    public String toString() {
      return "NO_MATCH";
    }

    @Override
    public TreeNode getRoot() {
      throw new NoSuchElementException("There was no match!");
    }
  }

  class RealTreeNode implements TreeNode, Comparable<RealTreeNode>  {
    final private int captureGroup;
    final List<TreeNode> children = new ArrayList<>();
    final int from;
    final int to;

    RealTreeNode(int captureGroup, int from, int to) {
      this.captureGroup = captureGroup;
      this.from = from;
      this.to = to;
    }

    @Override
    public Iterable<TreeNode> getChildren() {
      return children;
    }

    @Override
    public int getGroup() {
      return captureGroup;
    }

    @Override
    public String toString() {
      return input.subSequence(from, to).toString();
    }

    @Override
    public int compareTo(RealTreeNode that) {
      return Integer.compare(this.from, that.from);
    }
  }

  @Override
  public int end() {
    return end(0);
  }

  @Override
  public int end(final int group) {
    return captureGroupPositions[group * 2 + 1].iterator().next();
  }

  @Override
  public String group() {
    return group(0);
  }

  @Override
  public String group(final int group) {
    return input.subSequence(start(group), end(group)).toString();
  }

  @Override
  public int groupCount() {
    return captureGroupPositions.length / 2;
  }

  @Override
  public int start() {
    return start(0);
  }

  @Override
  public int start(final int group) {
    return captureGroupPositions[group * 2].iterator().next();
  }

  @Override
  public String toString() {
    return "" + start() + "-" + end();
  }

  @Override
  public TreeNode getRoot() {
    // copy captureGroupPositions into hs, then move all histories one step down,
    // to see only committed values.
    History[] hs = Arrays.copyOf(captureGroupPositions, captureGroupPositions.length);
    for (int i = 0; i < hs.length; i++) {
      if (hs[i] != null) {
        hs[i] = hs[i].prev;
      }
    }

    // Copy the input, which is an array of linked lists into an array of arrays.
    List<List<RealTreeNode>> cols = new ArrayList<>(parentOf.length);
    for (int col = 0; col < parentOf.length; col++) {
      List<RealTreeNode> curCol = new ArrayList<>();
        while (hs[2 * col] != null) {
          curCol.add(new RealTreeNode(col, hs[2*col].cur, hs[2*col + 1].cur + 1));

          // Forward
          hs[2 * col] = hs[2 * col].prev;
          hs[2 * col + 1] = hs[2 * col + 1].prev;
        }
        Collections.reverse(curCol); // Prefer ascending order.
        cols.add(curCol);
    }

    for (int col = 1; col < cols.size(); col++) {
      // in parent column, find only(!) matching parent
      for (RealTreeNode n : cols.get(col)) {
        List<RealTreeNode> parentCol = cols.get(parentOf[col]);
        int idx = Collections.binarySearch(parentCol, n);
        if (idx < 0) { // the `from` index of n isn't the same as the `from` index of parent.
          idx = ~idx - 1; // One before insertion point.
        }
        parentCol.get(idx).children.add(n);
      }
    }

    return cols.get(0).get(0); // The root is in capture group 0, which has only one entry.
  }

  /**
   * Testing only!
   * @return a string dump of all matched positions for all groups, in reverse.
   */
  String matchPositionsDebugString() {
    StringBuilder ret = new StringBuilder();
    for (History h : captureGroupPositions) {
      ret.append('(');
      IntIterator iter = h.iterator();
      iter.next(); // Ignore uncommitted.
      while (iter.hasNext()) {
        ret.append(iter.next());
        ret.append(", ");
      }
      ret.append(") ");
    }

    return ret.toString();
  }
}
