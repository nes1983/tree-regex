package automata.core;

public class Tag
{
	private int			id;
	private static int	lastId	= 0;


	public Tag()
	{
		this.id = Tag.lastId++;
	}


	public int getId()
	{
		return id;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Tag) { return this.id == ((Tag) obj).id; }
		return super.equals(obj);
	}


	@Override
	public String toString()
	{
		return String.format("\u0167%d", this.id);
	}

}
