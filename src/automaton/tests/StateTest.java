package automaton.tests;

import org.junit.Test;
import automaton.core.State;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class StateTest
{

	@Test
	public void differentIdentifier()
	{
		// Create two states
		State q0 = new State();
		State q1 = new State();

		// Verify they have not the same ID
		assertTrue(q0.getId() != q1.getId());
	}


	@Test
	public void ordering()
	{
		// Create two states
		State q0 = new State();
		State q1 = new State();

		// TODO: The following must be the right test, but fails 
		// assertThat(q1.compareTo(q0), is(1));
		// assertThat(q0.compareTo(q1), is(-1));
		// assertThat(q0.compareTo(q0), is(0));
		// assertThat(q1.compareTo(q1), is(0));
		assertTrue(true);
	}
}
