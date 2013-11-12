package ch.unibe.scg.regex;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("javadoc")
public final class InputRangeTest {
	@Test
	public void testSingle() {
		Collection<InputRange> start = Arrays.asList(
				InputRange.make('a', 'b'));
		assertThat(new InputRangeCleanup().cleanUp(start), is(start));
	}

	@Test
	public void testEmpty() {
		List<InputRange> start = Arrays.asList();
		assertThat(new InputRangeCleanup().cleanUp(start), is(start));
	}

	@Test
	public void testNonIntersecting() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'b'),
				InputRange.make('c', 'd'));
		assertThat(new InputRangeCleanup().cleanUp(start), is(start));
	}

	@Test
	public void testSimpleIntersecting() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'c'),
				InputRange.make('c', 'd'));
		assertThat(new InputRangeCleanup().cleanUp(start).toString(), is("[a-b, c-c, d-d]"));
	}

	@Test
	public void testEnclosing() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'g'),
				InputRange.make('b', 'd'));
		assertThat(new InputRangeCleanup().cleanUp(start).toString(), is("[a-a, b-d, e-g]"));
	}


	@Test
	public void testAny() {
		List<InputRange> start = Arrays.asList(
				InputRange.ANY,
				InputRange.make('b', 'd'));
		assertThat(new InputRangeCleanup().cleanUp(start).toString(), is("[0x0-a, b-d, e-0xffff]"));
	}
}
