package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TDFATransitionTable.NextState;

@SuppressWarnings("javadoc")
public final class DFATableTest {
  static final int s1 = 0;
  static final int s2 = 1;
  static final int s3 = 2;
  static final int s4 = 3;
  TDFATransitionTable table;
  NextState n;

  @Before
  public void setUp() {
    table =
        new TDFATransitionTable(new char[] {'c', 'l'}, new char[] {'k', 'm'}, new int[] {s1, s2},
            new int[] {s3, s4}, new Instruction[][] {new Instruction[] {}, new Instruction[] {}});
    n = new NextState();
  }

  @Test
  public void testTable() {
    table.newStateAndInstructions(200, 'd', n);
    assertThat(n.found, is(Boolean.FALSE));
  }

  @Test
  public void testTable2() {
    table.newStateAndInstructions(s1, 'c', n);
    assertThat(n.found, is(Boolean.TRUE));
    assertThat(n.nextState, is(s3));
  }

  @Test
  public void testTable3() {
    table.newStateAndInstructions(s1, 'k', n);
    assertThat(n.found, is(Boolean.TRUE));
    assertThat(n.nextState, is(s3));
  }

  @Test
  public void testTable4() {
    table.newStateAndInstructions(s1, 'l', n);
    assertThat(n.found, is(Boolean.FALSE));
  }

  @Test
  public void testTable5() {
    table.newStateAndInstructions(s2, 'l', n);
    assertThat(n.found, is(Boolean.TRUE));
    assertThat(n.nextState, is(s4));
  }
}
