package automata.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import automata.core.Automaton;
import automata.core.State;
import automata.exceptions.NotDeterministicException;


public class AutomatonRunner
{
	/**
	 * Evaluate a string on an automaton.
	 * 
	 * @param automaton
	 *            The automaton which will evaluate the string
	 * @param string
	 *            The string which will be evaluated
	 * @return A boolean. True if the string finish on accepting state, False
	 *         otherwise.
	 */
	public static boolean evaluate(Automaton automaton, String string)
			throws NotDeterministicException
	{
		// Create needed variables
		State current;
		StringCharacterIterator characters =
				new StringCharacterIterator(string);

		// Initialize first state
		current = automaton.getInitialState();

		// Do the evaluation
		while ((current = automaton.reach(current, characters.current())) != null)
		{
			if (characters.next() == CharacterIterator.DONE) break;
		}

		// Control the acceptance
		if (current == null) return false;
		if (!automaton.getFinalStates().contains(current)) return false;
		if (characters.current() != CharacterIterator.DONE) return false;
		return true;
	}
}
