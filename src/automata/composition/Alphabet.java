package automata.composition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import automata.core.Symbol;


public class Alphabet implements Iterable<Symbol>
{
	public static Symbol		EMPTY	= new Symbol("\u03B5");

	private Map<String, Symbol>	symbols;


	public Alphabet()
	{
		this.symbols = new HashMap<>();
	}


	public void add(Symbol symbol)
	{
		this.symbols.put(symbol.getRepresentation(), symbol);
	}


	public boolean contains(String value)
	{
		return this.symbols.containsKey(value);
	}


	public boolean contains(Symbol symbol)
	{
		return this.symbols.containsValue(symbol);
	}


	public Symbol get(String value)
	{
		return this.symbols.get(value);
	}


	public static Symbol getEmptySymbol()
	{
		return EMPTY;
	}


	@Override
	public String toString()
	{
		String result = "\u03A3:\t{";
		for (Symbol symbol : this)
		{
			result += symbol + ", ";
		}
		return result.substring(0, result.length() - 2) + "}";
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Alphabet)
		{
			if (this.symbols.equals(((Alphabet) obj).symbols)) return true;
		}
		return super.equals(obj);
	}


	@Override
	public Iterator<Symbol> iterator()
	{
		return this.symbols.values().iterator();
	}


}
