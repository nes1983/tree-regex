package automata.core;

/**
 * Class representing a {@link State} in an {@link Automaton}
 * 
 * @author Fabien Dubosson
 */
public class State
{
	/**
	 * The unique identifier of the {@link State}
	 */
	private int				id;

	/**
	 * Counter to assign next unique identifier
	 */
	private static int		lastId	= 1;

	/**
	 * Literal String which identify the {@link State} object
	 */
	private static String	LITERAL	= "\uA757";


	/**
	 * Constructs a {@link State} and assign it the next unique identifier
	 */
	public State()
	{
		this.id = State.lastId++;
	}


	/**
	 * Gets the identifier of the {@link State}
	 * 
	 * @return an {@link Integer} representing the {@link State} identifier
	 */
	public int getId()
	{
		return this.id;
	}


	/**
	 * Represents the {@link State} by the LITERAL and the identifier
	 */
	@Override
	public String toString()
	{
		return String.format(State.LITERAL + "%d", this.id);
	}
}
