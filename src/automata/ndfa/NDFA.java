package automata.ndfa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import automata.composition.Alphabet;
import automata.composition.SetOfStates;
import automata.core.State;
import automata.core.Symbol;


public class NDFA
{
	private SetOfStates			states;
	private Alphabet			alphabet;
	private TransitionRelation	relations;
	private State				initialState;
	private SetOfStates			finalStates;


	public NDFA()
	{
		this.states = new SetOfStates(SetOfStates.Q_LETTER);
		this.alphabet = new Alphabet();
		this.relations = new TransitionRelation();
		this.finalStates = new SetOfStates(SetOfStates.F_LETTER);
	}


	public void addConstruct(Transition... transitions)
	{
		State start;
		Symbol symbol;
		State end;

		for (Transition transition : transitions)
		{
			start = transition.getStart();
			symbol = transition.getSymbol();
			end = transition.getEnd();

			if (!states.contains(start)) states.add(start);
			if (!states.contains(end)) states.add(end);
			if (!alphabet.contains(symbol)) alphabet.add(symbol);

			if (!relations.contains(transition)) relations.add(transition);
		}
	}


	public void setInitial(State initialState)
	{
		this.initialState = initialState;
	}


	public void setFinal(State... finals)
	{
		for (State state : finals)
		{
			if (!finalStates.contains(state)) finalStates.add(state);
		}
	}


	public void toTDFA()
	{
		Map<State, SetOfStates> newStates = new HashMap<State, SetOfStates>();
		Queue<SetOfStates> stack = new LinkedList<SetOfStates>();

		State q0 = new State();
		SetOfStates first = new SetOfStates();
		first.add(initialState);

		for (State state : relations.canReach(initialState, Alphabet.EMPTY))
		{
			first.add(state);
		}

		newStates.put(q0, first);
		stack.add(first);

		// ----

		SetOfStates current, found;

		while ((current = stack.poll()) != null)
		{
			for (Symbol symbol : alphabet)
			{
				if (symbol == Alphabet.EMPTY) continue;

				found = new SetOfStates();
				for (State substate : current)
				{
					Set<State> temp = relations.canReach(substate, symbol);
					if (temp != null)
					{
						found.addAll(temp);
					} else
					{
						found.add(substate);
					}

				}

				boolean isNew = true;
				for (SetOfStates sos : newStates.values())
				{
					if (sos.equals(found)) isNew = false;
				}
				if (isNew)
				{
					State q1 = new State();
					newStates.put(q1, found);
					stack.add(found);
				}
			}
		}


		for (Entry<State, SetOfStates> entry : newStates.entrySet())
		{
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
			System.out.println();
		}
	}


	@Override
	public String toString()
	{
		String result = "========================\n";
		result += "TNFA\n";
		result += "========================\n";
		result += states + "\n";
		result += alphabet + "\n";
		result += relations + "\n";
		result += "s:\t" + initialState + "\n";
		result += finalStates + "\n";
		return result + "========================";
	}
}
