package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Gets a sorted list of possibly intersecting input ranges, and makes them non-intersecting.
 *
 * @author nes
 */
class InputRangeCleanup {
  List<InputRange> cleanUp(final List<InputRange> ranges) {
    if (ranges.isEmpty()) {
      return new ArrayList<>(ranges);
    }

    final List<InputRange> ret = new ArrayList<>();

    final Iterator<InputRange> current = ranges.iterator();
    final Iterator<InputRange> next = ranges.iterator();
    next.next();

    // break appart input ranges to make them non-intersecting

    InputRange c = current.next();
    while (next.hasNext()) {
      final InputRange n = next.next();
      char c1 = c.getFrom(), c2 = c.getTo();
      char n1 = n.getFrom(), n2 = n.getTo();
      // Three cases:
      //  1. [c1, c2] and [n1, n2] don't intersect: [c1-----c2]  [n1----n2]
      //     add [c1, c2] unmodified.
      if (c2 < n1) {
    	  ret.add(c);
    	  c = current.next();
      } 
      //  2. [c1, c2] and [n1, n2] intersect, but n2 >= c2: [c1-------c2]
      //     add [c1, n1-1]                                       [n1---------n2]
      else if (c2 <= n2) {
    	  ret.add(InputRange.make(c1, (char) (n1 - 1)));
    	  c = current.next();
      }
      //  3. [c1, c2] and [n1, n2] intersect so that n2 < c2 [c1----------------c2]
      //     add [c1, n1-1] and [n1,n2] and continue with              [n1---n2]
      //     [n2+1, c2] as next c.
      else {
    	  assert n1 != Character.MIN_VALUE;  // underflow
    	  ret.add(InputRange.make(c1, (char) (n1 - 1)));
    	  ret.add(n);
    	  assert n2 != Character.MAX_VALUE;  // underflow
    	  c = InputRange.make((char) (n2+1), c2);
      }
    }
    ret.add(c);

    return Collections.unmodifiableList(ret);
  }
}
