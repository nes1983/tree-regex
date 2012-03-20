package automata.utils;

import java.util.Set;
import automata.core.Automaton;


/**
 * Class to display an represent an {@link Automaton} in a text format
 * 
 * @author fabien
 */
public class AutomatonDisplayer
{
	/**
	 * Static {@link String} representing Letters
	 */
	private static String	STATES		= "\uD835\uDCE0";
	private static String	ALPHABET	= "\u03A3";
	private static String	FUNCTION	= "\u0394";
	private static String	INITIAL		= "s";
	private static String	FINAL		= "\uD835\uDCD5";
	private static String	EMPTY_SET	= "\u2205";


	/**
	 * Display an {@link Automaton} in text version
	 * 
	 * @param automaton
	 *            The {@link Automaton} to display
	 * @return a {@link String}
	 */
	public static String display(Automaton automaton)
	{
		String deterministic, initial;

		if (automaton.isDeterministic() == null)
			deterministic = "Not defined";
		else if (automaton.isDeterministic() == true)
			deterministic = "True";
		else
			deterministic = "False";

		if (automaton.getInitialState() == null)
			initial = EMPTY_SET;
		else
			initial = automaton.getInitialState().toString();

		String result = "";
		result += "=============================\n";
		result += "Automata\n";
		result += "=============================\n";
		result += "Deterministic: " + deterministic + "\n";
		result += "-----------------------------\n";
		result += displaySet(automaton.getStates(), STATES) + "\n";
		result += displaySet(automaton.getAlphabet(), ALPHABET) + "\n";
		result += displayFunction(automaton.getTransition(), FUNCTION) + "\n";
		result += INITIAL + ":\t" + initial + "\n";
		result += displaySet(automaton.getFinalStates(), FINAL) + "\n";
		result += "=============================\n";
		return result;
	}


	/**
	 * Display a {@link Set} in the form: "<name>: {i1,i2,...,in}"
	 * 
	 * @param set
	 *            The {@link Set} to display
	 * @param name
	 *            the name of the {@link Set}
	 * @return a {@link String} representing the {@link Set}
	 */
	private static String displaySet(Set<?> set, String name)
	{
		if (set.size() == 0) return name + ":\t" + EMPTY_SET;

		String result = name + ":\t{";
		for (Object item : set)
		{
			result += item + ", ";
		}
		return result.substring(0, result.length() - 2) + "}";
	}


	/**
	 * Display a {@link Set} in the form of a function
	 * 
	 * @param set
	 *            The {@link Set} to display
	 * @param name
	 *            the name of the {@link Set}
	 * @return a {@link String} representing the {@link Set}
	 */
	private static String displayFunction(Set<?> set, String name)
	{
		if (set.size() == 0) return name + ":\t" + EMPTY_SET;

		String result = name + ":\t| ";
		for (Object item : set)
		{
			result += item + "\n\t| ";
		}
		return result.substring(0, result.length() - 4);
	}
}
