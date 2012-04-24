package automaton.tests;

import org.junit.Test;
import automaton.core.InputRange;
import automaton.core.State;
import automaton.core.TransitionTable;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class TransitionTableTest
{

	@Test
	public void getState()
	{
		// New TransitionTable
		TransitionTable table = new TransitionTable();

		// From states
		State q1 = new State();
		State q2 = new State();
		State q3 = new State();

		// To states
		State q4 = new State();
		State q5 = new State();
		State q6 = new State();
		State q7 = new State();
		State q8 = new State();
		State q9 = new State();

		// Some input ranges
		InputRange i1 = new InputRange('a', 'd');
		InputRange i2 = new InputRange('k', 'o');
		InputRange i3 = new InputRange('y', 'z');

		// Creating some transitions
		table.put(q2, i1, q4, null);
		table.put(q2, i3, q8, null);
		table.put(q3, i2, q9, null);
		table.put(q2, i2, q3, null);
		table.put(q4, i3, q2, null);
		table.put(q1, i1, q7, null);
		table.put(q3, i1, q3, null);
		table.put(q1, i2, q6, null);
		table.put(q3, i3, q5, null);
		table.put(q1, i3, q3, null);
		table.put(q4, i2, q2, null);

		// Verify existing transitions
		assertThat(table.getState(q2, 'c'), is(q4));
		assertThat(table.getState(q3, 'z'), is(q5));
		assertThat(table.getState(q1, 'k'), is(q6));
		assertThat(table.getState(q1, 'a'), is(q7));
		assertThat(table.getState(q2, 'y'), is(q8));
		assertThat(table.getState(q3, 'o'), is(q9));

		// Verify missing transitions
		assertThat(table.getState(q4, 'c'), is(nullValue()));
		assertThat(table.getState(q6, 'a'), is(nullValue()));
	}
}
