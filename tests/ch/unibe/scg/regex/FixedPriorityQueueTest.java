package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.FixedPriorityQueue.Priorizable;
import ch.unibe.scg.regex.TransitionTriple.Priority;

/** @author nes */
@SuppressWarnings("javadoc")
public final class FixedPriorityQueueTest {

  FixedPriorityQueue fixedPriorityQueue;
  TransitionTriple tLow1, tLow2, tLow3, tNormal1, tNormal2;

  public FixedPriorityQueueTest() {
    tLow1 = new TransitionTriple(State.get(), Priority.LOW, Tag.NONE);
    tLow2 = new TransitionTriple(State.get(), Priority.LOW, Tag.NONE);
    tLow3 = new TransitionTriple(State.get(), Priority.LOW, Tag.NONE);
    tNormal1 = new TransitionTriple(State.get(), Priority.NORMAL, Tag.NONE);
    tNormal2 = new TransitionTriple(State.get(), Priority.NORMAL, Tag.NONE);
  }

  @Before
  public void setUp() throws Exception {
    fixedPriorityQueue = new FixedPriorityQueue();
  }

  @Test
  public void testEmpty() {
    assertThat(fixedPriorityQueue.isEmpty(), is(true));
    fixedPriorityQueue.offer(tLow1);
    assertThat(fixedPriorityQueue.isEmpty(), is(false));
    fixedPriorityQueue.poll();
    assertThat(fixedPriorityQueue.isEmpty(), is(true));
  }

  @Test
  public void testOffer() {
    fixedPriorityQueue.offer(tLow1);
    fixedPriorityQueue.offer(tLow2);
    fixedPriorityQueue.offer(tNormal1);
    fixedPriorityQueue.offer(tLow3);
    fixedPriorityQueue.offer(tNormal2);
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tNormal2));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tNormal1));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tLow3));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tLow2));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tLow1));
  }

  @Test
  public void testPeek() {
    fixedPriorityQueue.offer(tLow1);
    fixedPriorityQueue.offer(tLow2);

    assertThat(fixedPriorityQueue.peek(), is((Priorizable) tLow2));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tLow2));
    assertThat(fixedPriorityQueue.peek(), is((Priorizable) tLow1));
    assertThat(fixedPriorityQueue.poll(), is((Priorizable) tLow1));
    assertThat(fixedPriorityQueue.isEmpty(), is(true));
  }
}
