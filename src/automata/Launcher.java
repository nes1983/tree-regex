package automata;
import automata.core.Automaton;
import automata.core.State;
import automata.core.Transition;
import automata.utils.AutomatonConverter;
import automata.utils.AutomatonDisplayer;


public class Launcher
{

	public static void main(String[] args)
	{
		State q1 = new State();
		State q2 = new State();
		State q3 = new State();
		State q4 = new State();

		Transition t1 = new Transition(q1, '0', q2);
		Transition t2 = new Transition(q2, '1', q4);
		Transition t3 = new Transition(q4, '0', q3);
		Transition t4 = new Transition(q3, '0', q4);
		Transition t5 = new Transition(q3, null, q2);
		Transition t6 = new Transition(q1, null, q3);
		Transition t7 = new Transition(q2, '1', q2);

		Automaton ndfa = new Automaton();
		ndfa.constructFromTransitions(t1, t2, t3, t4, t5, t6, t7);
		ndfa.setInitialState(q1);
		ndfa.setFinalStates(q3, q4);

		System.out.println(AutomatonDisplayer.display(ndfa));

		Automaton dfa = AutomatonConverter.ndfaToDfa(ndfa);

		System.out.println(AutomatonDisplayer.display(dfa));
	}
}
