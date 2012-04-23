package automaton.core;

import java.util.Set;


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
	private Set<State>		states;

	/**
	 * {@link Set} of valid {@link Character}
	 */
	private Set<Character>	alphabet;

	/**
	 * {@link TransitionTable} representing all possible transition
	 */
	private TransitionTable	transitionTable;

	/**
	 * Initial {@link State}
	 */
	private State			initialState;

	/**
	 * {@link Set} of final {@link State}
	 */
	private Set<State>		finalStates;

	/**
	 * Specify if the {@link TDFA} is deterministic
	 */
	private Boolean			isDeterministic;

}
