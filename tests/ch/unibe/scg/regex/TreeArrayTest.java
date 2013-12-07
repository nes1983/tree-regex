package ch.unibe.scg.regex;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.Arraylike.TreeArray;

public class TreeArrayTest {
  static final int n = 1000;
  
  @Before
  public void setUp() {
    History.resetCount();
  }

  @Test
  public void testConstruction1() {
    TreeArray ta = new TreeArray(1);
    assertEquals(0, ta.payload.id);
  }

  @Test
  public void testConstruction3() {
    TreeArray ta = new TreeArray(3);
    assertEquals(0, ta.payload.id);
    assertEquals(1, ta.left.size);
    assertEquals(1, ta.left.payload.id);
    assertEquals(1, ta.right.size);
    assertEquals(2, ta.right.payload.id);
  }

  @Test
  public void testConstruction12() {
    TreeArray ta = new TreeArray(12);
    assertEquals(0, ta.payload.id);
    assertEquals(6, ta.left.size);
    assertEquals(5, ta.right.size);
    assertEquals(3, ta.left.left.size);
    assertEquals(2, ta.left.right.size);
    assertEquals(2, ta.right.left.size);
    assertEquals(2, ta.right.right.size);
  }

  @Test
  public void testGet() {
    TreeArray ta = new TreeArray(n);
    for (int i = 0; i < n; i++) {
      assertEquals(i, ta.get(i).id);
    }
  }

  @Test
  public void testIterator() {
    TreeArray ta = new TreeArray(n);
    int i = 0;
    for (History h : ta) {
      assertEquals(i, h.id);
      i++;
    }
  }

  @Test
  public void testUnique() {
    TreeArray ta = new TreeArray(n);
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        assertNotEquals(String.format("Duplicate elements %d and %d", i, j), ta.get(i), ta.get(j));
      }
    }
  }

  @Test
  public void testSet() {
    TreeArray ta = new TreeArray(12);
    TreeArray ta2 = (TreeArray) ta.set(5, new History());
    assertEquals(5, ta.get(5).id);
    assertNotEquals(5, ta2.get(5).id);
    assertEquals(4, ta2.get(4).id);
    assertEquals(6, ta2.get(6).id);
  }
}
