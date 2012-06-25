package automaton.instructions;

import automaton.core.Instruction;

public class CopyAllInstruction implements Instruction
{
	private final int	from, to;


	public CopyAllInstruction(int from, int to)
	{
		this.from = from;
		this.to = to;
	}


	@Override
	public void execute(Context context, int pos)
	{
		context.copy(this.from, this.to);
	}

}
