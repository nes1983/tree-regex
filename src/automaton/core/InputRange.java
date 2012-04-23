package automaton.core;

public class InputRange
{
	private final char	from;
	private final char	to;


	public InputRange(char from, char to)
	{
		this.from = from;
		this.to = to;
	}


	public char getFrom()
	{
		return from;
	}


	public char getTo()
	{
		return to;
	}
}
