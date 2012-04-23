package automaton.core;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Class representing a {@link State} in an {@link TDFA}
 * 
 * @author Fabien Dubosson
 */
public class State
{
	/**
	 * The unique identifier of the {@link State}
	 */
	private final int				id;

	/**
	 * Counter to assign next unique identifier
	 */
	private static AtomicInteger	lastId	= new AtomicInteger(0);

	/**
	 * Literal String which identify the {@link State} object
	 */
	private static String			LITERAL	= "\uA757";


	/**
	 * Constructs a {@link State} and assign it the next unique identifier
	 */
	public State()
	{
		this.id = lastId.getAndIncrement();
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
