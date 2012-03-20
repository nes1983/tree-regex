package automata.core;

/**
 * Class which represent a {@link Transition} inside an {@link Automaton}
 * 
 * @author fabien
 */
public class Transition
{
	/**
	 * Starting {@link State} of the {@link Transition}
	 */
	private State		start;

	/**
	 * {@link Character} representing the {@link Transition}
	 */
	private Character	character;

	/**
	 * Ending {@link State} of the {@link Transition}
	 */
	private State		end;


	/**
	 * Construct a {@link Transition} from start {@link State} to end
	 * {@link State} with associated {@link Character}
	 * 
	 * @param start
	 *            The starting {@link State} (from)
	 * @param character
	 *            The associated {@link Character}
	 * @param end
	 *            The ending {@link State} (to)
	 */
	public Transition(State start, Character character, State end)
	{
		this.start = start;
		this.character = character;
		this.end = end;
	}


	/**
	 * Gets the starting {@link State}
	 * 
	 * @return the starting {@link State}
	 */
	public State getStart()
	{
		return this.start;
	}


	/**
	 * Gets the ending {@link State}
	 * 
	 * @return the ending {@link State}
	 */
	public State getEnd()
	{
		return this.end;
	}


	/**
	 * Gets the associated {@link Character}
	 * 
	 * @return the associated {@link Character}
	 */
	public Character getCharacter()
	{
		return this.character;
	}


	/**
	 * Represents the {@link Transition} by a {@link String}
	 */
	@Override
	public String toString()
	{
		return String.format("%s + %s -> %s", this.start, this.character, this.end);
	}
}
