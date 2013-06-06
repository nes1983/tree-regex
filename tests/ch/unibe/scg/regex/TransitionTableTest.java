package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TransitionTriple.Priority;

@SuppressWarnings("javadoc")
public final class TransitionTableTest {
  @Test
  public void crapTest() {
    final TNFATransitionTable.Builder tb = TNFATransitionTable.builder();

    // From states
    final State q0 = State.get();
    final State q1 = State.get();
    final State q2 = State.get();

    // To states
    final State q3 = State.get();
    final State q4 = State.get();
    final State q5 = State.get();
    final State q6 = State.get();
    final State q7 = State.get();
    final State q8 = State.get();

    // Some input ranges
    final InputRange i1 = InputRange.make('a', 'd');
    final InputRange i2 = InputRange.make('k', 'o');
    final InputRange i3 = InputRange.make('y', 'z');

    // Creating some transitions

    tb.put(q1, i1, q3, Priority.NORMAL, null);
    tb.put(q1, i3, q7, Priority.NORMAL, null);
    tb.put(q2, i2, q8, Priority.NORMAL, null);
    tb.put(q1, i2, q2, Priority.NORMAL, null);
    tb.put(q3, i3, q1, Priority.NORMAL, null);
    tb.put(q0, i1, q6, Priority.NORMAL, null);
    tb.put(q2, i1, q2, Priority.NORMAL, null);
    tb.put(q0, i2, q5, Priority.NORMAL, null);
    tb.put(q2, i3, q4, Priority.NORMAL, null);
    tb.put(q0, i3, q2, Priority.NORMAL, null);
    tb.put(q3, i2, q1, Priority.NORMAL, null);
    final TNFATransitionTable t = tb.build();

    // Verify existing transitions
    assertThat(t.nextAvailableTransitions(q1, 'c').iterator().next().getState(), is(q3));

    assertThat(t.nextAvailableTransitions(q2, 'z').iterator().next().getState(), is(q4));

    assertThat(t.nextAvailableTransitions(q0, 'k').iterator().next().getState(), is(q5));
    assertThat(t.nextAvailableTransitions(q0, 'a').iterator().next().getState(), is(q6));
    assertThat(t.nextAvailableTransitions(q1, 'y').iterator().next().getState(), is(q7));
    assertThat(t.nextAvailableTransitions(q2, 'o').iterator().next().getState(), is(q8));

    // Verify missing transitions
    // assertThat(t.getState(q4, 'c'), is(nullValue()));
    // assertThat(t.getState(q6, 'a'), is(nullValue()));
  }

  @Test
  public void crapTest2() {

    final TNFATransitionTable.Builder tb = TNFATransitionTable.builder();

    // From states
    final State q0 = State.get();
    final State q1 = State.get();
    final State q2 = State.get();

    // To states
    final State q3 = State.get();
    final State q4 = State.get();
    final State q5 = State.get();
    final State q6 = State.get();
    final State q7 = State.get();
    final State q8 = State.get();

    // Some input ranges
    final InputRange ad = InputRange.make('a', 'd');
    final InputRange ko = InputRange.make('k', 'o');
    final InputRange yz = InputRange.make('y', 'z');

    // Creating some transitions

    tb.put(q1, ad, q3, Priority.NORMAL, null);
    tb.put(q1, yz, q7, Priority.NORMAL, null);
    tb.put(q2, ko, q8, Priority.NORMAL, null);
    tb.put(q1, ko, q2, Priority.NORMAL, null);
    tb.put(q3, yz, q1, Priority.NORMAL, null);
    tb.put(q0, ad, q6, Priority.NORMAL, null);
    tb.put(q2, ad, q2, Priority.NORMAL, null);
    tb.put(q0, ko, q5, Priority.NORMAL, null);
    tb.put(q2, yz, q4, Priority.NORMAL, null);
    tb.put(q0, yz, q2, Priority.NORMAL, null);
    tb.put(q3, ko, q1, Priority.NORMAL, null);
    final TNFATransitionTable t = tb.build();
    // Verify existing transitions

    assertThat(t.nextAvailableTransitions(q1, 'z').iterator().next().getState(), is(q7));
    assertThat(t.nextAvailableTransitions(q0, 'k').iterator().next().getState(), is(q5));
    assertThat(t.nextAvailableTransitions(q0, 'a').iterator().next().getState(), is(q6));
    assertThat(t.nextAvailableTransitions(q1, 'y').iterator().next().getState(), is(q7));
    assertThat(t.nextAvailableTransitions(q2, 'o').iterator().next().getState(), is(q8));

    // Verify missing transitions
    // assertThat(t.getState(q4, 'c'), is(nullValue()));
    // assertThat(t.getState(q6, 'a'), is(nullValue()));
  }

  @Before
  public void setUp() {
    State.resetCount();
  }

  @Test
  public void tableTest() {

    final TNFATransitionTable.Builder tb = TNFATransitionTable.builder();

    final State q0 = State.get();
    final State q1 = State.get();

    final InputRange ad = InputRange.make('a', 'd');
    tb.put(q0, ad, q1, Priority.NORMAL, null);

    final TNFATransitionTable t = tb.build();

    assertThat(t.nextAvailableTransitions(q0, 'c').iterator().next().getState(), is(q1));

  }

  @Test
  public void tableTest2() {

    final TNFATransitionTable.Builder tb = TNFATransitionTable.builder();

    final State q0 = State.get();
    final State q1 = State.get();

    final State q2 = State.get();
    final State q3 = State.get();

    final InputRange ad = InputRange.make('a', 'd');
    final InputRange ko = InputRange.make('k', 'o');

    tb.put(q0, ad, q2, Priority.NORMAL, null);
    tb.put(q1, ko, q3, Priority.NORMAL, null);
    tb.put(q0, ko, q2, Priority.NORMAL, null);

    final TNFATransitionTable t = tb.build();
    assertThat(
        t.toString(),
        is("{(q0, a-d)=[q2, NORMAL, null], (q0, k-o)=[q2, NORMAL, null], (q1, k-o)=[q3, NORMAL, null]}"));
  }

}
