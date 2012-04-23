package automaton.core;

import java.util.SortedMap;
import java.util.TreeMap;


public class TransitionTable
{
	private final SortedMap<StateInputPair, State>	transitions;


	public TransitionTable()
	{
		this.transitions = new TreeMap<>();
	}


	public void put(State oldState, InputRange range, State newState)
	{
		// TODO Some overlapping tests
		this.transitions.put(new StateInputPair(oldState, range), newState);
	}


	public State get(State state, Character character)
	{
		InputRange searched = new InputRange(character, character);
		SortedMap<StateInputPair, State> tail =
				transitions.tailMap(new StateInputPair(state, searched));
		// TODO Verify that the state is the same!
		return transitions.get(tail.firstKey());
	}

}
