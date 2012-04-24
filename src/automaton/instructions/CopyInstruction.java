package automaton.instructions;

public class CopyInstruction implements Instruction
{

	@Override
	public void execute(Context context, int... args)
	{
		int index = args[0];
		int tag = args[1];
		int currentPos = args[2];
		// TODO Auto-generated method stub
	}

}
