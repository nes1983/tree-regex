package automaton;

import org.hamcrest.core.IsNull;
import org.junit.Test;
import automaton.core.InputRange;
import automaton.core.State;
import automaton.core.TransitionTable;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;


public class Tests
{

	@Test
	public void TransitionTableQuery()
	{
		TransitionTable table = new TransitionTable();

		State q1 = new State();
		State q2 = new State();
		State q3 = new State();

		State q4 = new State();
		State q5 = new State();
		State q6 = new State();
		State q7 = new State();
		State q8 = new State();
		State q9 = new State();

		InputRange i1 = new InputRange('a', 'd');
		InputRange i2 = new InputRange('k', 'o');
		InputRange i3 = new InputRange('y', 'z');

		table.put(q2, i1, q4, null);
		table.put(q2, i3, q8, null);
		table.put(q3, i2, q9, null);
		table.put(q2, i2, q3, null);
		table.put(q1, i1, q7, null);
		table.put(q3, i1, q3, null);
		table.put(q1, i2, q6, null);
		table.put(q3, i3, q5, null);
		table.put(q1, i3, q3, null);

		assertThat(table.getState(q2, 'c'), is(q4));
		assertThat(table.getState(q3, 'z'), is(q5));
		assertThat(table.getState(q1, 'k'), is(q6));
		assertThat(table.getState(q1, 'a'), is(q7));
		assertThat(table.getState(q2, 'y'), is(q8));
		assertThat(table.getState(q3, 'o'), is(q9));
	}


	@Test
	public void TransitionMissing()
	{
		TransitionTable table = new TransitionTable();
		
		State q1 = new State();
		State q8 = new State();
		State q9 = new State();

		InputRange i1 = new InputRange('a', 'd');
		InputRange i3 = new InputRange('y', 'z');

		table.put(q1, i1, q9, null);
		table.put(q8, i3, q1, null);
		
		assertThat(table.getState(q9, 'c'), is(nullValue()));
		assertThat(table.getState(q1, 'z'), is(nullValue()));
	}
}
