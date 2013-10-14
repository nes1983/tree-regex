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
        is("0:(0 )\n1:(1 )\n2:(2 )\n3:(3 )\n4:(4 )\n5:(5 )\n6:(6 )\n7:(7 )\n8:(8 )\n9:(9 )\n"
            + "10:(10 )\n11:(11 )\n12:(12 )\n13:(13 )\n14:(14 )\n15:(15 )\n16:(16 )\n17:(17 )\n"
            + "18:null\n19:null\n20:null\n21:null\n22:null\n23:null\n24:null\n25:null\n"
            + "26:null\n27:null\n28:null\n29:null\n30:null\n31:null\n"));
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
