package automata.tnfa;

import automata.core.State;
import automata.core.Symbol;
import automata.core.Tag;


public class Transition
{

	private State	start;
	private Symbol	symbol;
	private Tag		tag;
	private State	end;


	public Transition(State start, Symbol symbol, Tag tag, State end)
	{
		this.start = start;
		this.symbol = symbol;
		this.tag = tag;
		this.end = end;
	}


	public Transition(State start, Symbol symbol, State end)
	{
		this(start, symbol, null, end);
	}


	public State getStart()
	{
		return start;
	}


	public Tag getTag()
	{
		return tag;
	}


	public State getEnd()
	{
		return end;
	}


	public Symbol getSymbol()
	{
		return symbol;
	}


	@Override
	public String toString()
	{
		if (tag == null)
			return String.format("%s + %s -> %s", start, symbol, end);
		else
			return String.format("%s + %s\\%s -> %s", start, symbol, tag, end);
	}
}
