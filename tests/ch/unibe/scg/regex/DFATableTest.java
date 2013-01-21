package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TransitionTable.NextState;
import ch.unibe.scg.regex.TransitionTable.TDFATransitionTable;

public class DFATableTest {
  final int s1 = 0;
  final int s2 = 1;
  final int s3 = 2;
  final int s4 = 3;
  TDFATransitionTable table;

  @Before
  public void setUp() {
    table =
        new TDFATransitionTable(new char[] {'c', 'l'}, new char[] {'k', 'm'}, new int[] {s1, s2},
            new int[] {s3, s4}, new List[] {Collections.EMPTY_LIST, Collections.EMPTY_LIST});
  }

  @Test
  public void testTable() {
    final NextState pr = table.newStateAndInstructions(200, 'd');
    assertThat(pr, is(nullValue()));
  }

  @Test
  public void testTable2() {
    final NextState pr = table.newStateAndInstructions(s1, 'c');
    assertThat(pr.getNextState(), is(s3));

  }

  @Test
  public void testTable3() {
    final NextState pr = table.newStateAndInstructions(s1, 'k');
    assertThat(pr.getNextState(), is(s3));

  }

  @Test
  public void testTable4() {
    final NextState pr = table.newStateAndInstructions(s1, 'l');
    assertThat(pr, is(nullValue()));

  }

  @Test
  public void testTable5() {
    final NextState pr = table.newStateAndInstructions(s2, 'l');
    assertThat(pr.getNextState(), is(s4));
  }

}