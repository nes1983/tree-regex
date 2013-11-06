package ch.unibe.scg.regex;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("javadoc")
public final class InputRangeTest {
	@Test
	public void testSingle() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'b'));
		assertThat(TNFAToTDFA.allInputRanges(start), is(start));
	}
	
	@Test
	public void testEmpty() {
		List<InputRange> start = Arrays.asList();
		assertThat(TNFAToTDFA.allInputRanges(start), is(start));
	}

	@Test
	public void testNonIntersecting() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'b'),
				InputRange.make('c', 'd'));
		assertThat(TNFAToTDFA.allInputRanges(start), is(start));
	}

	@Test
	public void testSimpleIntersecting() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'c'),
				InputRange.make('c', 'd'));
		assertThat(TNFAToTDFA.allInputRanges(start).toString(), is("[a-b, c-d]"));
	}
	
	@Test
	public void testEnclosing() {
		List<InputRange> start = Arrays.asList(
				InputRange.make('a', 'g'),
				InputRange.make('b', 'd'));
		assertThat(TNFAToTDFA.allInputRanges(start).toString(), is("[a-a, b-d, e-g]"));
	}
}
