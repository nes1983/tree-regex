package automaton.core;

/**
 * Class representing an range of character to be used in automaton
 * 
 * @author Fabien Dubosson
 */
public class InputRange implements Comparable<InputRange>
{
	/**
	 * First {@link Character} of the range
	 */
	private final char	from;

	/**
	 * Last {@link Character} or the range
	 */
	private final char	to;


	/**
	 * Constructor class which take the first and last character
	 * 
	 * @param from
	 *            The first {@link Character} of the range
	 * @param to
	 *            The last {@link Character} of the range
	 */
	public InputRange(char from, char to)
	{
		this.from = from;
		this.to = to;
	}


	/**
	 * Return the first {@link Character} of the range
	 * 
	 * @return the first {@link Character} of the range
	 */
	public char getFrom()
	{
		return from;
	}


	/**
	 * Return the last {@link Character} of the range
	 * 
	 * @return the last {@link Character} of the range
	 */
	public char getTo()
	{
		return to;
	}


	@Override
	public int compareTo(InputRange o)
	{
		return o.getFrom() - this.getFrom();
	}


	public boolean contains(Character character)
	{
		return (from <= character && character <= to);
	}
}
