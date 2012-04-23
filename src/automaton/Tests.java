package automaton;

import org.junit.Test;
import automaton.core.InputRange;
import automaton.core.State;
import automaton.core.TransitionTable;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;


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

		InputRange i1 = new InputRange('a', 'd');
		InputRange i2 = new InputRange('k', 'o');
		InputRange i3 = new InputRange('y', 'z');

		table.put(q2, i1, q4);
		table.put(q2, i3, q3);
		table.put(q3, i2, q3);
		table.put(q2, i2, q3);
		table.put(q1, i1, q3);
		table.put(q3, i1, q3);
		table.put(q1, i2, q6);
		table.put(q3, i3, q5);
		table.put(q1, i3, q3);

		assertThat(table.get(q2, 'c'), is(q4));
		assertThat(table.get(q3, 'z'), is(q5));
		assertThat(table.get(q1, 'k'), is(q6));
	}
}
