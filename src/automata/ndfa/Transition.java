package automata.ndfa;

import automata.core.State;
import automata.core.Symbol;


public class Transition
{

	private State	start;
	private Symbol	symbol;
	private State	end;


	public Transition(State start, Symbol symbol, State end)
	{
		this.start = start;
		this.symbol = symbol;
		this.end = end;
	}


	public State getStart()
	{
		return start;
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
		return String.format("%s + %s -> %s", start, symbol, end);
	}
}
