package automaton.instructions;

public class StoreInstruction implements Instruction
{
	private final int	address, tag, resultIndex;


	public StoreInstruction(int address, int tag, int resultIndex)
	{
		this.address = address;
		this.tag = tag;
		this.resultIndex = resultIndex;
	}


	@Override
	public void execute(Context context, int pos)
	{
		context.saveValue(this.address, this.tag, this.resultIndex);
	}


}
