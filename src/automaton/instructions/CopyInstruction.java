package automaton.instructions;

public class CopyInstruction implements Instruction
{
	private final int	from, to, tag;


	public CopyInstruction(int from, int to, int tag)
	{
		this.from = from;
		this.to = to;
		this.tag = tag;
	}


	@Override
	public void execute(Context context, int pos)
	{
		context.set(this.to, this.tag, context.get(this.from, this.tag));
	}


}
