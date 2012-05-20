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
public class Pair<A extends Comparable<A>, B extends Comparable<B>> implements
		Comparable<Pair<A, B>> {
	/**
	 * The first {@link Object} of the {@link Pair}
	 */
	final private A first;
	/**
	 * The second {@link Object} of the {@link Pair}
	 */
	final private B second;

	/**
	 * Constructor taking the two {@link Object} as parameters
	 * 
	 * @param first
	 *            The first {@link Object} of the {@link Pair}
	 * @param second
	 *            The second {@link Object} of the {@link Pair}
	 */
	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?,?> other = (Pair<?,?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	/**
	 * Get the first {@link Object} of the {@link Pair}
	 * 
	 * @return the first {@link Object} of the {@link Pair}
	 */
	public A getFirst() {
		return first;
	}

	/**
	 * Get the second {@link Object} of the {@link Pair}
	 * 
	 * @return the second {@link Object} of the {@link Pair}
	 */
	public B getSecond() {
		return second;
	}

	public int compareTo(Pair<A, B> o) {
		int result = (this.getFirst()).compareTo(o.getFirst());
		if (result != 0)
			return result;
		return this.getSecond().compareTo(o.getSecond());
	}
}
