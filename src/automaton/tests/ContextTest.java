package automaton.tests;

import org.junit.Test;
import automaton.instructions.Context;
import automaton.instructions.Context.TagValueFunction;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class ContextTest
{

	@Test
	public void TagValueFunctionEquality()
	{
		Context context = new Context();
		TagValueFunction t1 = context.new TagValueFunction();
		TagValueFunction t2 = context.new TagValueFunction();

		t1.set(0, 12);
		t1.set(1, 652);
		t1.set(6, 22);
		t1.set(567, -1);
		t1.set(-1, 2);
		t1.set(99, 3);
		t1.set(6, 56);

		t2.set(0, 12);
		t2.set(1, 652);
		t2.set(6, 22);
		t2.set(567, -1);
		t2.set(-1, 2);
		t2.set(99, 3);
		t2.set(6, 56);

		assertTrue(t1.equals(t2));

		t2.set(6, 57);
		assertFalse(t1.equals(t2));

		t2.set(6, 56);
		assertTrue(t1.equals(t2));

		t2.set(7, 57);
		assertFalse(t1.equals(t2));
	}


	@Test
	public void TagValueFunctionCloning()
	{
		Context context = new Context();
		TagValueFunction t1 = context.new TagValueFunction();
		t1.set(0, 12);
		t1.set(1, 652);
		t1.set(6, 22);
		t1.set(567, -1);

		TagValueFunction t2 = t1.clone();

		assertTrue(t1 != t2);
		assertTrue(t1.equals(t2));
	}


	@Test
	public void setGetAndCopy()
	{
		Context context = new Context();

		context.set(12, 3, 25);
		context.set(12, 7, 8);
		context.set(12, 5, 24);

		assertThat(context.get(12, 3), is(25));
		assertThat(context.get(12, 7), is(8));
		assertThat(context.get(12, 5), is(24));

		context.set(13, 5, 1);
		context.set(13, 6, 2);
		context.set(14, 5, 3);
		context.set(14, 6, 4);

		assertThat(context.get(13, 5), is(1));
		assertThat(context.get(13, 6), is(2));
		assertThat(context.get(14, 5), is(3));
		assertThat(context.get(14, 6), is(4));

		assertFalse(context.get(13).equals(context.get(11)));
		context.copy(13, 11);
		assertTrue(context.get(13).equals(context.get(11)));

		assertFalse(context.get(13).equals(context.get(14)));
		context.copy(13, 14);
		assertTrue(context.get(13).equals(context.get(14)));
	}
}
