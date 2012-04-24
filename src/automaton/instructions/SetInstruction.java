package automaton.instructions;

public class SetInstruction implements Instruction
{
	private final int	address, tag;


	public SetInstruction(int address, int tag)
	{
		this.address = address;
		this.tag = tag;
	}


	@Override
	public void execute(Context context, int pos)
	{
		context.set(this.address, this.tag, pos);
	}


}
