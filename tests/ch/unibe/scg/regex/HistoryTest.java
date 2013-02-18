package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.Memory.History;

/** @author Niko Schwarz */
@SuppressWarnings("javadoc")
public class HistoryTest {

  History history;

  @Before
  public void setUp() throws Exception {
    history = new History();
  }

  @Test
  public void testEmpty() {
    assertThat(history.toString(), is("()"));
  }

  @Test
  public void testPush() {
    history.push(1);
    history.push(2);
    history.push(3);
    history.push(4);
    // The extra space is unfortunate. But I don't want to hand-code it, or depend on
    // Guava Joiner.
    assertThat(history.toString(), is("(1 2 3 4 )"));
  }
}
