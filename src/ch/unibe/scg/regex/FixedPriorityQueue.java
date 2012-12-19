package ch.unibe.scg.regex;

import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import ch.unibe.scg.regex.FixedPriorityQueue.Priorizable;

/**
 * A priority queue that assumes that there are only very few priorities.
 * 
 * <p>
 * Access is O(1) for fixed number of priorities.
 * 
 * <p>
 * The things that are added into the queue need to be priorizable. The priority must be an enum,
 * sorted from low priority to high priority. That is, the priority of an item {@code e} is assumed
 * to be {@code e.ordinal()}.
 * 
 * @author nes
 */
public class FixedPriorityQueue extends AbstractQueue<Priorizable> {
  static interface Priorizable {
    Enum<?> getPriority();
  }

  @SuppressWarnings("unchecked")
  Deque<Priorizable>[] stacks = new Deque[0];

  @Override
  public Iterator<Priorizable> iterator() {
    throw new RuntimeException("Not implemented");
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean offer(Priorizable e) {
    if (stacks.length == 0) {
      // stacks has not yet been initialized.
      final int capacity = e.getPriority().getClass().getEnumConstants().length;
      stacks = new Deque[capacity];
      for (int i = 0; i < stacks.length; i++) {
        stacks[i] = new ArrayDeque<>();
      }
    }

    stacks[e.getPriority().ordinal()].push(e);

    return true;
  }

  @Override
  public Priorizable peek() {
    for (int i = stacks.length - 1; i >= 0; i--) {
      if (!stacks[i].isEmpty()) {
        return stacks[i].peek();
      }
    }
    return null;
  }

  @Override
  public Priorizable poll() {
    for (int i = stacks.length - 1; i >= 0; i--) {
      if (!stacks[i].isEmpty()) {
        return stacks[i].pop();
      }
    }
    return null;
  }

  @Override
  public int size() {
    int sum = 0;
    for (final Deque<Priorizable> stack : stacks) {
      sum += stack.size();
    }
    return sum;
  }
}
