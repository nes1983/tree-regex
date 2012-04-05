package automata.core;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import automata.exceptions.NotDeterministicException;


/**
 * Class representing a simple {@link Automaton}
 * 
 * @author Fabien Dubosson
 */
public class Automaton
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
	 * {@link Set} of {@link Transition} between {@link State}
	 */
	private Set<Transition>	transitions;

	/**
	 * Initial {@link State}
	 */
	private State			initialState;

	/**
	 * {@link Set} of final {@link State}
	 */
	private Set<State>		finalStates;

	/**
	 * Specify if the {@link Automaton} is deterministic
	 */
	private Boolean			isDeterministic;


	/**
	 * Constructs an empty {@link Automaton}
	 */
	public Automaton()
	{
		resetAutomaton();
	}


	/**
	 * Gets the {@link Set} of all {@link State}
	 * 
	 * @return a {@link Set} of {@link State}
	 */
	public Set<State> getStates()
	{
		return new HashSet<State>(states);
	}


	/**
	 * Gets the {@link Set} of valid {@link Character}
	 * 
	 * @return a {@link Set} of {@link Character}
	 */
	public Set<Character> getAlphabet()
	{
		return new HashSet<Character>(alphabet);
	}


	/**
	 * Gets the {@link Set} of {@link Transition} between {@link State}
	 * 
	 * @return a {@link Set} of {@link Transition}
	 */
	public Set<Transition> getTransition()
	{
		return new HashSet<Transition>(transitions);
	}


	/**
	 * Gets the initial {@link State}
	 * 
	 * @return the initial {@link State}
	 */
	public State getInitialState()
	{
		return initialState;
	}


	/**
	 * Gets the {@link Set} of final {@link State}
	 * 
	 * @return a {@link Set} of final {@link State}
	 */
	public Set<State> getFinalStates()
	{
		return new HashSet<State>(finalStates);
	}


	/**
	 * Returns if the {@link Automaton} is deterministic
	 * 
	 * @return a boolean
	 */
	public Boolean isDeterministic()
	{
		return isDeterministic;
	}


	/**
	 * Defines the initial {@link State} and add it to the {@link Set} of
	 * {@link State} if it is not already present
	 * 
	 * @param initialState
	 *            The new initial {@link State}
	 */
	public void setInitialState(State initialState)
	{
		this.states.add(initialState);
		this.initialState = initialState;
	}


	/**
	 * Defines the final {@link State} and add them to the {@link Set} of
	 * {@link State} if they are not already present
	 * 
	 * @param finalState
	 *            An {@link Array} representing final {@link State}
	 */
	public void setFinalStates(State... finalStates)
	{
		for (int i = 0; i < finalStates.length; i++)
		{
			this.states.add(finalStates[i]);
			this.finalStates.add(finalStates[i]);
		}
	}


	/**
	 * Defines the final {@link State} and add them to the {@link Set} of
	 * {@link State} if they are not already present
	 * 
	 * @param finalStates
	 *            A {@link Set} containing all final {@link State}
	 */
	public void setFinalStates(Set<State> finalStates)
	{
		this.states.addAll(finalStates);
		this.finalStates.addAll(finalStates);
	}


	/**
	 * Construct the {@link Set} of {@link State}, the {@link Set} of
	 * {@link Character} and the {@link Set} of {@link Transition} of the
	 * {@link Automaton} based on the given {@link Transition}
	 * 
	 * @param transitions
	 *            An {@link Set} of {@link Transition}
	 */
	public void constructFromTransitions(List<Transition> transitions)
	{
		resetAutomaton();
		this.isDeterministic = true;

		Map<State, Set<Character>> existing =
				new HashMap<State, Set<Character>>();

		State start, end;
		Character character;

		for (Transition transition : transitions)
		{
			start = transition.getStart();
			end = transition.getEnd();
			character = transition.getCharacter();

			this.states.add(start);
			this.states.add(end);
			this.alphabet.add(character);
			this.transitions.add(transition);


			if (!existing.containsKey(start))
			{
				Set<Character> alphabet = new HashSet<Character>();
				alphabet.add(character);
				existing.put(start, alphabet);
				continue;
			}

			if (existing.get(start).contains(character))
			{
				isDeterministic = false;
			}

			existing.get(start).add(character);
		}
	}


	/**
	 * Method overloading to use {@link Set} instead of {@link Array}
	 * 
	 * @param transitions
	 *            {@link Set} of {@link Transition}
	 */
	public void constructFromTransitions(Transition... transitions)
	{
		constructFromTransitions(Arrays.asList(transitions));
	}


	/**
	 * Get the {@link State} which is reach in a deterministic finite automaton
	 * from a specified {@link State} and specific {@link Character}
	 * 
	 * @param state
	 *            The starting {@link State}
	 * @param character
	 *            The desired {@link Character}
	 * @return a {@link State}
	 */
	public State dfaStep(State state, Character character)
			throws NotDeterministicException
	{
		if (!isDeterministic()) throw new NotDeterministicException();

		State result = null;

		for (Transition transition : transitions)
		{
			if (transition.getStart() == state
					&& transition.getCharacter() == character)
				return transition.getEnd();
		}

		return result;
	}


	/**
	 * Gets the {@link Set} of all {@link State} which can be reached in a
	 * nondeterministic finite automaton from a specified {@link State} and
	 * specific {@link Character}.
	 * 
	 * @param state
	 *            The starting {@link State}
	 * @param character
	 *            The desired {@link Character}
	 * @return a {@link Set} of {@link State}
	 */
	public Set<State> nfaStep(State state, Character character)
	{
		Set<State> result = new HashSet<State>();

		for (Transition transition : transitions)
		{
			if (transition.getStart() == state
					&& transition.getCharacter() == character)
			{
				result.add(transition.getEnd());
				result.addAll(nfaStep(transition.getEnd(), null));
			}
		}

		return result;
	}


	/**
	 * Reset all the element composing the {@link Automaton}
	 */
	public void resetAutomaton()
	{
		this.states = new HashSet<State>();
		this.alphabet = new HashSet<Character>();
		this.transitions = new HashSet<Transition>();
		this.initialState = null;
		this.finalStates = new HashSet<State>();
		this.isDeterministic = null;
	}

}
