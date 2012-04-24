package automaton.instructions;

import java.util.LinkedList;


public class SequenceOfInstructions
{

	private LinkedList<Instruction>	instructions;


	public SequenceOfInstructions()
	{
		this.instructions = new LinkedList<>();
	}


	public void execute(Context context, int pos)
	{
		for (Instruction instruction : this.instructions)
		{
			instruction.execute(context, pos);
		}
	}


	public void enqueue(Instruction instruction)
	{
		this.instructions.add(instruction);
	}

}
