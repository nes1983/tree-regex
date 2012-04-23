package automaton.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import automaton.core.TDFA;
import automaton.core.State;
import automaton.exceptions.NotDeterministicException;


public class DFARunner
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
	public static boolean evaluate(TDFA automaton, String string)
			throws NotDeterministicException
	{
		// Create needed variables
		State current;
		StringCharacterIterator characters =
				new StringCharacterIterator(string);

		// Initialize first state
		current = automaton.getInitialState();

		// Do the evaluation
		while ((current = automaton.dfaStep(current, characters.current())) != null)
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
