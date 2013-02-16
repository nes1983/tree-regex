package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
        is("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, "
            + "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]"));
  }
}
