package automata.composition;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import automata.core.Tag;


public class SetOfTags implements Iterable<Tag>
{

	private Set<Tag>	tags;


	public SetOfTags()
	{
		tags = new LinkedHashSet<>();
	}


	public void add(Tag tag)
	{
		tags.add(tag);
	}


	public void remove(Tag tag)
	{
		tags.remove(tag);
	}


	public boolean contains(Tag tag)
	{
		return tags.contains(tag);
	}


	@Override
	public String toString()
	{
		String result = "\uD835\uDCE3:\t{";
		for (Tag tag : this)
		{
			result += tag + ", ";
		}
		return result.substring(0, result.length() - 2) + "}";
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SetOfTags)
		{
			if (tags.equals(((SetOfTags) obj).tags)) return true;
		}
		return super.equals(obj);
	}


	@Override
	public Iterator<Tag> iterator()
	{
		return tags.iterator();
	}
}
