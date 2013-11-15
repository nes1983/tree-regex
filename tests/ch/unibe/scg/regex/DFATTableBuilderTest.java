package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    final DFAState q0 = new DFAState(null, new byte[] {1}, null);
    final DFAState q1 = new DFAState(null, new byte[] {2}, null);
    final List<Instruction> empty = Collections.emptyList();
    builder.addTransition(q0, InputRange.make('a', 'c'), q1, empty);

    final TDFATransitionTable dfa = builder.build();
    assertThat(dfa.toString(), is("q0-a-c -> q1 []\n"));
    NextState n = new NextState();
    dfa.newStateAndInstructions(0, 'b', n);
    assertThat(n.nextState, is(1));
    assertThat(n.instructions.length, is(0));
  }
}
