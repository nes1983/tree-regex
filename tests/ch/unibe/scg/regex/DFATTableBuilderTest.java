package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.TDFATransitionTable.NextState;

@SuppressWarnings("javadoc")
public final class DFATTableBuilderTest {
  ch.unibe.scg.regex.TDFATransitionTable.Builder builder;

  @Before
  public void setUp() {
    builder = new TDFATransitionTable.Builder();
  }

  @Test
  public void testBuilder() {
    final DFAState q0 = mock(DFAState.class);
    final DFAState q1 = mock(DFAState.class);
    final List<Instruction> empty = Collections.emptyList();
    builder.addTransition(q0, InputRange.make('a', 'c'), q1, empty);

    final TDFATransitionTable dfa = builder.build();
    assertThat(dfa.toString(), is("q0-a-c -> q1 []\n"));
    final NextState pr = dfa.newStateAndInstructions(0, 'b');
    assertThat(pr.nextState, is(1));
    assertThat(pr.instructions.length, is(0));
  }
}
