package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Gets a sorted set of possibly intersecting input ranges, and makes them
 * non-intersecting.
 * 
 * @author nes
 * 
 */
class InputRangeCleanup {

	public static final class CleanUpTest {

		InputRangeCleanup inputRangeCleanup;

		@Before
		public void setUp() {
			inputRangeCleanup = new InputRangeCleanup();
		}

		@Test
		public void splitAll() {
			final List<InputRange> l = Arrays.asList(InputRange.make('a', 'e'),
					InputRange.make('c', 'g'), InputRange.make('e', 'k'));
			final TreeSet<InputRange> s = new TreeSet<>(l);
			final SortedSet<InputRange> cleaned = inputRangeCleanup.cleanUp(s);
			assertThat(cleaned.toString(), is("[a-b, c-d, e-k]"));
		}

		@After
		public void tearDown() {
			inputRangeCleanup = null;
		}

		@Test
		public void testSplit() {
			final InputRange shrunk = inputRangeCleanup.shrink(
					InputRange.make('a', 'd'), InputRange.make('c', 'z'));
			assertThat(shrunk.toString(), is("a-b"));
		}
	}

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
		return InputRange.make(toBeShrunk.getFrom(),
				(char) (fixed.getFrom() - 1));
	}
}
