package automata;

import automata.core.Automaton;
import automata.core.State;
import automata.core.Transition;
import automata.exceptions.NotDeterministicException;
import automata.utils.NFAToDFAConverter;
import automata.utils.AutomatonDisplayer;
import automata.utils.DFARunner;


public class Launcher
{

	public static void wikipedia(String[] args)
	{
		State q1 = new State();
		State q2 = new State();
		State q3 = new State();
		State q4 = new State();

		Transition t1 = new Transition(q1, '0', null, q2);
		Transition t2 = new Transition(q2, '1', null, q4);
		Transition t3 = new Transition(q4, '0', null, q3);
		Transition t4 = new Transition(q3, '0', null, q4);
		Transition t5 = new Transition(q3, null, null, q2);
		Transition t6 = new Transition(q1, null, null, q3);
		Transition t7 = new Transition(q2, '1', null, q2);

		Automaton ndfa = new Automaton();
		ndfa.constructFromTransitions(t1, t2, t3, t4, t5, t6, t7);
		ndfa.setInitialState(q1);
		ndfa.setFinalStates(q3, q4);

		System.out.println(AutomatonDisplayer.display(ndfa));

		Automaton dfa = NFAToDFAConverter.convert(ndfa);

		System.out.println(AutomatonDisplayer.display(dfa));

		try
		{
			System.out.println(DFARunner.evaluate(dfa, "001"));
		} catch (NotDeterministicException e)
		{
			e.printStackTrace();
		}
	}


	public static void main(String[] args)
	{
		wikipedia(args);
	}
}
