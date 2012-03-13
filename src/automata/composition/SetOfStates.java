package automata.composition;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import automata.core.State;


public class SetOfStates implements Iterable<State>
{

	private Set<State>		states;
	private String			name;
	public static String	F_LETTER	= "\uD835\uDCD5";
	public static String	Q_LETTER	= "\uD835\uDCE0";


	public SetOfStates()
	{
		this(Q_LETTER);
	}


	public SetOfStates(String name)
	{
		states = new LinkedHashSet<>();
		this.name = name;
	}


	public void add(State state)
	{
		states.add(state);
	}


	public void addAll(Collection<State> values)
	{
		this.states.addAll(values);
	}


	public void remove(State state)
	{
		states.remove(state);
	}


	public LinkedHashSet<State> getAll()
	{
		return new LinkedHashSet<>(states);
	}


	public boolean contains(State state)
	{
		return states.contains(state);
	}


	@Override
	public String toString()
	{
		String result = name + ":\t{";
		for (State state : this)
		{
			result += state + ", ";
		}
		return result.substring(0, result.length() - 2) + "}";
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SetOfStates)
		{
			if (this.states.equals(((SetOfStates) obj).states)) return true;
		}
		return super.equals(obj);
	}


	@Override
	public Iterator<State> iterator()
	{
		return states.iterator();
	}
}
