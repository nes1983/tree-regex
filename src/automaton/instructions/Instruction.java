package automaton.instructions;

public interface Instruction
{

	public void execute(Context context, int... args);

}
