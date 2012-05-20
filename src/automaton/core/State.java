package automaton.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link State} of a {@link TDFA}. Immutable.
 * 
 * @author Fabien Dubosson
 */
class State implements Comparable<State> {
	/**
	 * Counter to assign next unique identifier
	 */
	private static AtomicInteger lastId = new AtomicInteger(0);

	public static State get() {
		return new State();
	}

	/**
	 * The unique identifier of the {@link State}
	 */
	private final int id;

	/**
	 * Constructs a {@link State} and assign it the next unique identifier
	 */
	State() {
		this.id = lastId.getAndIncrement();
	}

	@Override
	public int compareTo(final State o) {
		return o.getId() - this.getId();
	}

	/**
	 * Gets the identifier of the {@link State}
	 * 
	 * @return an {@link Integer} representing the {@link State} identifier
	 */
	public int getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "q" + id;
	}
}
