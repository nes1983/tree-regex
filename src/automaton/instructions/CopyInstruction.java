package automaton.instructions;

public class CopyInstruction implements Instruction
{
	private final int	from, to;


	public CopyInstruction(int from, int to)
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
