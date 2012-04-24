package automaton.core;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;


/**
 * Class representing a simple {@link TDFA}
 * 
 * @author Fabien Dubosson
 */
public class TDFA
{
	/**
	 * {@link Set} of {@link State}
	 */
	private Set<State>				states;

	/**
	 * {@link Set} of valid {@link Character}
	 */
	private SortedSet<InputRange>	alphabet;

	/**
	 * {@link TransitionTable} representing all possible transition
	 */
	private TransitionTable			transitionTable;

	/**
	 * Initial {@link State}
	 */
	private State					initialState;

	/**
	 * {@link Set} of final {@link State}
	 */
	private Set<State>				finalStates;

	/**
	 * Specify if the {@link TDFA} is deterministic
	 */
	private Boolean					isDeterministic;
	
	public void addTransition(State oldState, InputRange range, State newState)
	{
		this.states.add(oldState);
		this.states.add(newState);
		this.addAlphabet(range);
		assert invariant();
	}
	
	public Set<InputRange> getAlphabet()
	{
		return Collections.unmodifiableSet(alphabet);
	}
	
	
	protected boolean invariant() 
	{
		SortedSet<InputRange> alphabetOfTransitions = getAlphabetFrom(transitionTable);
		return alphabetOfTransitions.equals(alphabet);
	}

	private SortedSet<InputRange> getAlphabetFrom(TransitionTable transitionTable2)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private void addAlphabet(InputRange input)
	{
		// TODO write method
	}
}
