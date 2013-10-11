package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public final class MemoryTest {
  Memory memory;

  @Before
  public void setUp() {
    memory = new Memory();
  }

  @Test
  public void testWriteItFull() {
    for (int i = 0; i < 18; i++) {
      memory.write(i, i);
    }
    assertThat(memory.toString(),
        is("[(0 ), (1 ), (2 ), (3 ), (4 ), (5 ), (6 ), (7 ), (8 ), (9 ), (10 ), (11 ), (12 ),"
            + " (13 ), (14 ), (15 ), (16 ), (17 ), null, null, null, null, null, "
            + "null, null, null, null, null, null, null, null, null]"));
  }

  @Test
  public void testCommiting() {
	  memory.write(0, 0);
	  memory.commit(0);
	  memory.write(0, 5);

	  assertThat(memory.readHistory(0).toString(), is("(5 0 )"));
  }

  @Test
  public void testHistorySharing() {
	  memory.write(0, 0);
	  memory.commit(0);
	  memory.write(0, 1);
	  memory.copyTo(1, 0);
	  memory.write(1, 2);

	  assertThat(memory.readHistory(0).toString(), is("(1 0 )"));
	  assertThat(memory.readHistory(1).toString(), is("(2 0 )"));
  }
}
