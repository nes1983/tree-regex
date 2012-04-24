package automaton.instructions;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class Context
{

	Map<Integer, TagValueFunction>	memory;


	public Context()
	{
		this.memory = new TreeMap<>();
	}


	public int get(int address, int tag)
	{
		if (!this.memory.containsKey(address)) return -1;
		return this.memory.get(address).get(tag);
	}


	public TagValueFunction get(int address)
	{
		if (!this.memory.containsKey(address)) return new TagValueFunction();
		return this.memory.get(address);
	}


	public void set(int address, int tag, int pos)
	{
		if (!memory.containsKey(address))
			this.memory.put(address, new TagValueFunction());
		this.memory.get(address).set(tag, pos);
	}


	public void copy(int from, int to)
	{
		this.memory.put(to, memory.get(from).clone());
	}


	public class TagValueFunction implements Cloneable
	{
		Map<Integer, Integer>	values;


		public TagValueFunction()
		{
			this.values = new TreeMap<>();
		}


		public int get(int tag)
		{
			if (!this.values.containsKey(tag)) return -1;
			return values.get(Integer.valueOf(tag));
		}


		public void set(int tag, int pos)
		{
			this.values.put(tag, pos);
		}


		public TagValueFunction clone()
		{
			TagValueFunction o = new TagValueFunction();
			for (Entry<Integer, Integer> entry : this.values.entrySet())
			{
				o.set(entry.getKey(), entry.getValue());
			}
			return o;
		}


		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof TagValueFunction)
			{
				if (((TagValueFunction) obj).values.size() != values.size())
					return false;
				for (Entry<Integer, Integer> entry : values.entrySet())
				{
					if (!entry.getValue()
							.equals(((TagValueFunction) obj).values.get(entry
									.getKey()))) return false;
				}
				return true;
			}
			return false;
		}
	}
}
