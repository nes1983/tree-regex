package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;


/** Gets a collection of possibly intersecting input ranges, and makes them non-intersecting. */
class InputRangeCleanup {
  List<InputRange> cleanUp(final Collection<InputRange> ranges) {
    if (ranges.isEmpty()) {
      return new ArrayList<>(ranges);
    }

    List<InputRange> ret = new ArrayList<>();

    // Remove duplicates, add to pq.
    PriorityQueue<InputRange> pq = new PriorityQueue<>(new HashSet<>(ranges));
    while (pq.size() > 1) {
      final InputRange c = pq.poll();
      final InputRange n = pq.poll();

      final char c1 = c.getFrom(), c2 = c.getTo();
      final char n1 = n.getFrom(), n2 = n.getTo();
      // Three cases:
      //  1. [c1, c2] and [n1, n2] don't intersect: [c1-----c2]  [n1----n2]
      //     add [c1, c2] unmodified.
      if (c2 < n1) {
        ret.add(c);
        pq.add(n);
      }
      //  2. [c1, c2] and [n1, n2] intersect, but n2 >= c2: [c1-------c2]
      //     add [c1, n1-1]                                       [n1---------n2]
      else if (c2 <= n2) {
        if (n1 > c1) {
          ret.add(InputRange.make(c1, (char) (n1 - 1)));
          pq.add(InputRange.make(n1, c2));
          if (n2 > c2) {
            pq.add(InputRange.make((char) (c2 + 1), n2));
          }
        } else {
          assert n1 == c1;                         //      [c1-------c2]
          assert n2 > c2;                          //      [n1---------------n2]
          ret.add(c);
          pq.add(InputRange.make((char) (c2 + 1), n2));
        }
      }
      //  3. [c1, c2] and [n1, n2] intersect so that n2 < c2 [c1----------------c2]
      //     add [c1, n1-1] and [n1,n2] and continue with              [n1---n2]
      //     [n2+1, c2] as next c.
      else {
        assert n2 < c2;
        assert c2 >= n1;
        assert c1 < n1;

        ret.add(InputRange.make(c1, (char) (n1 - 1)));
        pq.add(n);
        pq.add(InputRange.make((char) (n2 + 1), c2));
      }
    }

    if (!pq.isEmpty()) {
      ret.add(pq.poll());
    }

    assert pq.isEmpty();

    return ret;
  }
}
