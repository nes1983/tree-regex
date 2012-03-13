package automata.core;

public class State
{
	private int			id;
	private static int	lastId	= 0;


	public State()
	{
		this.id = State.lastId++;
	}


	public int getId()
	{
		return id;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof State) { return this.id == ((State) obj).id; }
		return super.equals(obj);
	}


	@Override
	public String toString()
	{
		return String.format("\uA757%d", this.id);
	}
}
