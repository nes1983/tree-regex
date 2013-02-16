package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public final class CleanUpTest {
  InputRangeCleanup inputRangeCleanup;

  @Before
  public void setUp() {
    inputRangeCleanup = new InputRangeCleanup();
  }

  @Test
  public void splitAll() {
    final List<InputRange> l =
        Arrays.asList(InputRange.make('a', 'e'), InputRange.make('c', 'g'),
            InputRange.make('e', 'k'));
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
    final InputRange shrunk =
        inputRangeCleanup.shrink(InputRange.make('a', 'd'), InputRange.make('c', 'z'));
    assertThat(shrunk.toString(), is("a-b"));
  }
}
