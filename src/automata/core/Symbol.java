package automata.core;

public class Symbol
{
	private int			id;
	private static int	lastId	= 0;
	private String		representation;


	public Symbol(String representation)
	{
		this.id = Symbol.lastId++;
		this.representation = representation;
	}


	public int getId()
	{
		return id;
	}


	public String getRepresentation()
	{
		return representation;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Symbol) { return this.id == ((Symbol) obj).id; }
		return super.equals(obj);
	}


	@Override
	public String toString()
	{
		return representation;
	}
}
