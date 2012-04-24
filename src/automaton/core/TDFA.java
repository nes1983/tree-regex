package automaton.core;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import automaton.instructions.SequenceOfInstructions;


/**
 * Class representing a simple {@link TDFA}
 * 
 * @author Fabien Dubosson
 */
public class TDFA
{
	/**
	 * {@link Set} of {@link State}s
	 */
	private Set<State>				states;

	/**
	 * {@link Set} of valid {@link Character}s
	 */
	private SortedSet<InputRange>	alphabet;

	/**
	 * {@link TransitionTable} representing all possible transition in
	 * {@link TDFA}
	 */
	private TransitionTable			transitionTable;

	/**
	 * Initial {@link State}
	 */
	private State					initialState;

	/**
	 * {@link Set} of final {@link State}s
	 */
	private Set<State>				finalStates;


	/**
	 * Initialize a {@link TDFA}
	 */
	public TDFA()
	{
		this.states = new LinkedHashSet<>();
		this.alphabet = new TreeSet<>();
		this.transitionTable = new TransitionTable();
		this.finalStates = new LinkedHashSet<>();
	}


	/**
	 * Construct {@link TDFA} by adding new transition.
	 * 
	 * @param startingState
	 *            Starting {@link State} of the transition
	 * @param range
	 *            Assigned {@link Character}s of the transition
	 * @param endingState
	 *            Ending {@link State} of the transition
	 * @param instruction
	 *            Assigned {@link SequenceOfInstructions} to execute when using the
	 *            transition
	 */
	public void addTransition(State startingState, InputRange range,
			State endingState, SequenceOfInstructions instruction)
	{
		this.states.add(startingState);
		this.states.add(endingState);
		this.addAlphabet(range);
		assert invariant();
	}


	/**
	 * Verify the consistency of the {@link TDFA}
	 * 
	 * @return <code>true</code> if the {@link TDFA} is consistent,
	 *         <code>false</code> otherwise
	 */
	protected boolean invariant()
	{
		Set<InputRange> alphabetOfTransitions =
				getAlphabetFrom(transitionTable);
		return alphabetOfTransitions.equals(alphabet);
	}


	/**
	 * Add a {@link Character} into the alphabet
	 * 
	 * @param input
	 *            the {@link Character} to add
	 */
	private void addAlphabet(InputRange input)
	{
		// TODO write method
	}


	/**
	 * Define the initial {@link State} of the {@link TDFA}
	 * 
	 * @param initialState
	 *            The new initial {@link State} of {@link TDFA}
	 */
	public void setInitialState(State initialState)
	{
		this.initialState = initialState;
	}


	/**
	 * Define a new {@link State} as a final {@link State} of {@link TDFA}
	 * 
	 * @param finalState
	 *            the {@link State} to define as final {@link State}
	 */
	public void addFinalState(State finalState)
	{
		this.finalStates.add(finalState);
		assert invariant();
	}


	/**
	 * Return an unmodifiable {@link Set} of all {@link State}s of {@link TDFA}
	 * 
	 * @return an unmodifiable {@link Set} of {@link State}s of {@link TDFA}
	 */
	public Set<State> getStates()
	{
		return Collections.unmodifiableSet(states);
	}


	/**
	 * Return an unmodifiable {@link Set} of all {@link Character}s of
	 * {@link TDFA}
	 * 
	 * @return an unmodifiable {@link Set} of {@link Character}s of {@link TDFA}
	 */
	public Set<InputRange> getAlphabet()
	{
		return Collections.unmodifiableSet(alphabet);
	}


	/**
	 * The initial {@link State} of {@link TDFA}
	 * 
	 * @return the initial {@link State} of {@link TDFA}
	 */
	public State getInitialState()
	{
		return initialState;
	}


	/**
	 * Return an unmodifiable {@link Set} of all final {@link State}s of
	 * {@link TDFA}
	 * 
	 * @return an unmodifiable {@link Set} of final {@link State}s of
	 *         {@link TDFA}
	 */
	public Set<State> getFinalStates()
	{
		return Collections.unmodifiableSet(finalStates);
	}


	/**
	 * Calculate the alphabet of a {@link TransitionTable}
	 * 
	 * @param transitionTable2
	 * @return the {@link Set} of {@link Character}s representing the alphabet
	 */
	private Set<InputRange> getAlphabetFrom(TransitionTable transitionTable2)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
