package automaton.core;

/**
 * A generic class representing a {@link Pair} of two kind of {@link Object}
 * 
 * @author Fabien Dubosson
 * @param <A>
 *            The first {@link Object} {@link Class} type
 * @param <B>
 *            The second {@link Object} {@link Class} type
 */
public class Pair<A, B>
{
	/**
	 * The first {@link Object} of the {@link Pair}
	 */
	private A	first;
	/**
	 * The second {@link Object} of the {@link Pair}
	 */
	private B	second;


	/**
	 * Constructor taking the two {@link Object} as parameters
	 * 
	 * @param first
	 *            The first {@link Object} of the {@link Pair}
	 * @param second
	 *            The second {@link Object} of the {@link Pair}
	 */
	public Pair(A first, B second)
	{
		super();
		this.first = first;
		this.second = second;
	}


	@Override
	public int hashCode()
	{
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}


	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Pair)
		{
			Pair<?, ?> otherPair = (Pair<?, ?>) other;
			return ((this.first == otherPair.first || (this.first != null
					&& otherPair.first != null && this.first
						.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
					&& otherPair.second != null && this.second
						.equals(otherPair.second))));
		}

		return false;
	}


	@Override
	public String toString()
	{
		return "(" + first + ", " + second + ")";
	}


	/**
	 * Get the first {@link Object} of the {@link Pair}
	 * 
	 * @return the first {@link Object} of the {@link Pair}
	 */
	public A getFirst()
	{
		return first;
	}


	/**
	 * Set the first {@link Object} of the {@link Pair}
	 * 
	 * @param first
	 *            The new first {@link Object} of the {@link Pair}
	 */
	public void setFirst(A first)
	{
		this.first = first;
	}


	/**
	 * Get the second {@link Object} of the {@link Pair}
	 * 
	 * @return the second {@link Object} of the {@link Pair}
	 */
	public B getSecond()
	{
		return second;
	}


	/**
	 * Set the second {@link Object} of the {@link Pair}
	 * 
	 * @param second
	 *            The new second {@link Object} of the {@link Pair}
	 */
	public void setSecond(B second)
	{
		this.second = second;
	}
}
