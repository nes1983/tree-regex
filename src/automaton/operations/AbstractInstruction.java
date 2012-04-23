package automaton.operations;

public abstract class AbstractInstruction implements Instruction
{
	private final int	from, to;


	public AbstractInstruction(int from, int to)
	{
		this.from = from;
		this.to = to;
	}


	public int getFrom()
	{
		return from;
	}


	public int getTo()
	{
		return to;
	}
}
