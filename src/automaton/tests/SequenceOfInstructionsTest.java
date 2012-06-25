package automaton.tests;

import org.junit.Test;

import automaton.core.Instruction;
import automaton.instructions.Context;
import automaton.instructions.CopyAllInstruction;
import automaton.instructions.SequenceOfInstructions;
import automaton.instructions.SetInstruction;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class SequenceOfInstructionsTest
{

	@Test
	public void sequenceTest()
	{
		SequenceOfInstructions sequence = new SequenceOfInstructions();

		Context context = new Context();
		context.set(1, 1, 375);

		Instruction s0 = new SetInstruction(0, 0);
		Instruction s1 = new SetInstruction(0, 1);
		Instruction s2 = new SetInstruction(2, 3);

		Instruction c1 = new CopyAllInstruction(0, 2);
		Instruction c2 = new CopyAllInstruction(0, 3);
		Instruction c3 = new CopyAllInstruction(1, 3);

		sequence.enqueue(s0);
		sequence.enqueue(s1);
		sequence.enqueue(c1);
		sequence.enqueue(s2);
		sequence.enqueue(c2);
		sequence.enqueue(c3);

		sequence.execute(context, 978);

		assertThat(context.get(2, 0), is(978));
		assertThat(context.get(2, 1), is(978));
		assertThat(context.get(2, 2), is(-1));
		assertThat(context.get(2, 3), is(978));

		assertThat(context.get(3, 0), is(-1));
		assertThat(context.get(3, 1), is(375));
	}

}
