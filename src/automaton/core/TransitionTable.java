package automaton.core;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import automaton.operations.Instruction;


public class TransitionTable
{
	private final SortedMap<Pair<State, InputRange>, Pair<State, Instruction>>	transitions;


	public TransitionTable()
	{
		this.transitions = new TreeMap<>(new StateInputPairComparator());
	}


	public void put(State oldState, InputRange range, State newState,
			Instruction instruction)
	{
		// TODO Some overlapping tests
		this.transitions.put(new Pair<>(oldState, range), new Pair<>(newState,
				instruction));
	}


	public State getState(State state, Character character)
	{
		Pair<State, Instruction> pair = getPair(state, character);
		return pair.getFirst();
	}


	public Instruction getInstruction(State state, Character character)
	{
		Pair<State, Instruction> pair = getPair(state, character);
		return pair.getSecond();
	}


	private Pair<State, Instruction> getPair(State state, Character character)
	{
		// TODO Verify that the state is the same!
		InputRange searched = new InputRange(character, character);
		SortedMap<Pair<State, InputRange>, Pair<State, Instruction>> tail =
				transitions.tailMap(new Pair<>(state, searched));
		Pair<State, InputRange> pair = tail.firstKey();
		if (! pair.getFirst().equals(state)) return new Pair<>(null, null);
		if (! pair.getSecond().contains(character)) return new Pair<>(null, null);
		return transitions.get(tail.firstKey());
	}

	private static class StateInputPairComparator implements
			Comparator<Pair<State, InputRange>>
	{


		@Override
		public int compare(Pair<State, InputRange> o1,
				Pair<State, InputRange> o2)
		{
			int result = o1.getFirst().compareTo(o2.getFirst());
			if (result != 0) return result;
			return o1.getSecond().compareTo(o2.getSecond());
		}
	}

}
