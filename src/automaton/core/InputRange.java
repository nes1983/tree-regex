package automaton.core;

/**
 * {@link InputRange} represent a range of {@link Character} which can be used
 * in {@link TransitionTable} of {@link TDFA}.
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
	 * Constructor which take the first and last character as parameter
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


	/**
	 * Tell if the {@link InputRange} contains a {@link Character} within its
	 * range
	 * 
	 * @param character
	 *            A specific {@link Character}
	 * @return if the {@link Character} is contained within the
	 *         {@link InputRange}
	 */
	public boolean contains(Character character)
	{
		return (from <= character && character <= to);
	}


	@Override
	public int compareTo(InputRange o)
	{
		return o.getFrom() - this.getFrom();
	}
}
