import automata.composition.Alphabet;
import automata.core.State;
import automata.core.Symbol;
import automata.ndfa.NDFA;
import automata.ndfa.Transition;


public class Launcher
{

	public static void main(String[] args)
	{
		State q0 = new State();
		State q1 = new State();
		State q2 = new State();

		Symbol se = Alphabet.getEmptySymbol();
		Symbol sa = new Symbol("a");


		Transition t1 = new Transition(q0, sa, q0);
		Transition t2 = new Transition(q0, se, q1);
		Transition t3 = new Transition(q1, sa, q1);
		Transition t4 = new Transition(q1, sa, q2);

		NDFA tnfa = new NDFA();
		tnfa.addConstruct(t1, t2, t3, t4);
		tnfa.setInitial(q0);
		tnfa.setFinal(q2);
		System.out.println(tnfa);
		tnfa.toTDFA();
	}
}
