package automata.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import automata.core.Automaton;
import automata.core.State;
import automata.core.Transition;


/**
 * Class to apply some transformation to an {@link Automaton}
 * 
 * @author Fabien Dubosson
 */
public class NFAToDFAConverter
{
	/**
	 * Convert an nondeterministic finite {@link Automaton} into a deterministic
	 * finite automaton using powerset construction algorithm
	 * 
	 * @param oldAutomaton
	 *            The NDFA {@link Automaton}
	 * @return the equivalent DFA {@link Automaton}
	 */
	public static Automaton convert(Automaton oldAutomaton)
	{
		/*
		 * Variable which will contain the new State representing a subset of
		 * NDFA states
		 */
		Map<State, Set<State>> newStates = new HashMap<State, Set<State>>();

		/*
		 * New set representing initial state, final states and transitions of
		 * the new automaton
		 */
		State newInitialState = new State();
		Set<State> newFinalStates = new HashSet<State>();
		List<Transition> newTransitions = new LinkedList<Transition>();

		/*
		 * Variable which will contains a list of remaining states to treat
		 */
		Queue<State> workingQueue = new LinkedList<>();

		/*
		 * Variable to store temporary objects during algorithm
		 */
		Set<State> tempSet;
		State tempState;

		/*
		 * Start by generating the first new State
		 */
		tempState = new State();
		tempSet = new HashSet<State>();
		tempSet.add(oldAutomaton.getInitialState());
		tempSet.addAll(oldAutomaton.nfaStep(oldAutomaton.getInitialState(),
				null));

		/*
		 * Store the first state
		 */
		newStates.put(tempState, tempSet);
		workingQueue.offer(tempState);
		newInitialState = tempState;
		for (State state : tempSet)
		{
			if (oldAutomaton.getFinalStates().contains(state))
			{
				newFinalStates.add(tempState);
				break;
			}
		}

		/*
		 * Begin loop over workingQueue
		 */
		State current;
		while ((current = workingQueue.poll()) != null)
		{
			for (Character character : oldAutomaton.getAlphabet())
			{
				if (character == null) continue;

				tempSet = new HashSet<State>();
				for (State state : newStates.get(current))
				{
					tempSet.addAll(oldAutomaton.nfaStep(state, character));
				}

				boolean isNew = true;
				for (Entry<State, Set<State>> entry : newStates.entrySet())
				{
					if (entry.getValue().equals(tempSet))
					{
						tempState = entry.getKey();
						isNew = false;
					}
				}

				if (isNew)
				{
					tempState = new State();
					newStates.put(tempState, tempSet);
					workingQueue.offer(tempState);

					for (State state : tempSet)
					{
						if (oldAutomaton.getFinalStates().contains(state))
						{
							newFinalStates.add(tempState);
							break;
						}
					}
				}

				//TODO: Just added null to avoid code error
				newTransitions.add(new Transition(current, character, null,
						tempState));

			}
		}

		/*
		 * Construct the new automaton
		 */
		Automaton newAutomaton = new Automaton();
		newAutomaton.constructFromTransitions(newTransitions);
		newAutomaton.setInitialState(newInitialState);
		newAutomaton.setFinalStates(newFinalStates);

		/*
		 * Returns the new automaton
		 */
		return newAutomaton;
	}
}
