package automaton.core;

public class StateInputPair implements Comparable<StateInputPair>
{
	private final State			state;
	private final InputRange	inputRange;


	public StateInputPair(State state, InputRange inputRange)
	{
		this.state = state;
		this.inputRange = inputRange;
	}


	public State getState()
	{
		return state;
	}


	public InputRange getInputRange()
	{
		return inputRange;
	}


	@Override
	public int compareTo(StateInputPair o)
	{
		int result = this.state.getId() - o.state.getId();
		if (result != 0) return result;
		return o.inputRange.getFrom() - this.inputRange.getFrom();
	}
}
