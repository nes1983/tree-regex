package ch.unibe.scg.regex;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Gets a sorted set of possibly intersecting input ranges, and makes them non-intersecting.
 * 
 * @author nes
 */
class InputRangeCleanup {
  public SortedSet<InputRange> cleanUp(final SortedSet<InputRange> ranges) {
    if (ranges.isEmpty()) {
      return ranges;
    }

    final TreeSet<InputRange> ret = new TreeSet<>();

    final Iterator<InputRange> current = ranges.iterator();
    final Iterator<InputRange> next = ranges.iterator();
    next.next();

    do {
      final InputRange c = current.next();
      final InputRange n = next.next();
      final InputRange shrunk = shrink(c, n);
      System.out.println(shrunk);
      ret.add(shrunk);
    } while (next.hasNext());
    ret.add(current.next());

    return Collections.unmodifiableSortedSet(ret);

  }

  public InputRange shrink(final InputRange toBeShrunk, final InputRange fixed) {
    if (toBeShrunk.getTo() < fixed.getFrom()) {
      return toBeShrunk;
    }
    return InputRange.make(toBeShrunk.getFrom(), (char) (fixed.getFrom() - 1));
  }
}
