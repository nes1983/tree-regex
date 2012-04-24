package automaton.core;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * A {@link State} of a {@link TDFA}.
 * 
 * @author Fabien Dubosson
 */
public final class State implements Comparable<State>
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
	 * Literal {@link String} which identify the {@link State} type
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


	@Override
	public String toString()
	{
		return String.format(State.LITERAL + "%d", this.id);
	}


	@Override
	public int compareTo(State o)
	{
		// TODO Verify if follow axiom of comparison (See compareTo javadoc)
		return o.getId() - this.getId();
	}
}
