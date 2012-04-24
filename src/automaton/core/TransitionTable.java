package automaton.core;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import automaton.instructions.Instruction;


/**
 * A {@link TransitionTable} is the set of all possible transition of a
 * {@link TDFA}
 * 
 * @author Fabien Dubosson
 */
public class TransitionTable
{
	/**
	 * The {@link Map} containing all possible transitions
	 */
	private final SortedMap<Pair<State, InputRange>, Pair<State, Instruction>>	transitions;


	/**
	 * Construct a new {@link TransitionTable}
	 */
	public TransitionTable()
	{
		this.transitions =
				new TreeMap<>(new PairComparator<State, InputRange>());
	}


	/**
	 * Put a new transition in the {@link TransitionTable}
	 * 
	 * @param startingState
	 *            The starting {@link State} of the transition
	 * @param range
	 *            The {@link Character}s representing the transition
	 * @param endingState
	 *            The ending {@link State} of the transition
	 * @param instruction
	 *            The {@link Instruction} to be executed when using the
	 *            transition
	 */
	public void put(State startingState, InputRange range, State endingState,
			Instruction instruction)
	{
		// TODO Some overlapping tests
		this.transitions.put(new Pair<>(startingState, range), new Pair<>(
				endingState, instruction));
	}


	/**
	 * Get the {@link State} reached from another {@link State} with a specific
	 * {@link Character}
	 * 
	 * @param state
	 *            The starting {@link State}
	 * @param character
	 *            The specified {@link Character}
	 * @return The {@link State} reached by the transition
	 */
	public State getState(State state, Character character)
	{
		Pair<State, Instruction> pair = getPair(state, character);
		return pair.getFirst();
	}


	/**
	 * Get the {@link Instruction} associated with the transition starting from
	 * a {@link State} with a specified {@link Character}.
	 * 
	 * @param state
	 *            The starting {@link State}
	 * @param character
	 *            The specified {@link Character}
	 * @return The {@link Instruction} associated with the transition
	 */
	public Instruction getInstruction(State state, Character character)
	{
		Pair<State, Instruction> pair = getPair(state, character);
		return pair.getSecond();
	}


	/**
	 * Get the {@link Pair} of {@link State} and {@link Instruction} assigned
	 * when starting from a {@link State} with a specified {@link Character}
	 * 
	 * @param state
	 *            The starting {@link State}
	 * @param character
	 *            The specified {@link Character}
	 * @return The {@link Pair} of {@link State} and {@link Instruction}
	 */
	private Pair<State, Instruction> getPair(State state, Character character)
	{
		// TODO Verify that the state is the same!
		InputRange searched = new InputRange(character, character);
		SortedMap<Pair<State, InputRange>, Pair<State, Instruction>> tail =
				transitions.tailMap(new Pair<>(state, searched));
		Pair<State, InputRange> pair = tail.firstKey();
		if (!pair.getFirst().equals(state)) return new Pair<>(null, null);
		if (!pair.getSecond().contains(character))
			return new Pair<>(null, null);
		return transitions.get(tail.firstKey());
	}

	/**
	 * A class to compare two {@link Pair}, first by comparing the first
	 * {@link Object} and then by comparing second {@link Object}
	 * 
	 * @author Fabien Dubosson
	 */
	private static class PairComparator<A extends Comparable<A>, B extends Comparable<B>>
			implements Comparator<Pair<A, B>>
	{
		@Override
		public int compare(Pair<A, B> o1, Pair<A, B> o2)
		{
			int result = o1.getFirst().compareTo(o2.getFirst());
			if (result != 0) return result;
			return o1.getSecond().compareTo(o2.getSecond());
		}
	}

}
