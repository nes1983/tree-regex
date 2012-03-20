import automata.composition.Alphabet;
import automata.core.State;
import automata.core.Symbol;
import automata.core.Transition;
import automata.dfa.DFA;
import automata.ndfa.NDFA;


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
		DFA dfa = tnfa.toTDFA();
		System.out.println(dfa);


		//		State q1 = new State();
		//		State q2 = new State();
		//		State q3 = new State();
		//		State q4 = new State();
		//
		//		new Alphabet();
		//		Symbol se = Alphabet.getEmptySymbol();
		//		Symbol s0 = new Symbol("0");
		//		Symbol s1 = new Symbol("1");
		//
		//		Transition t1 = new Transition(q1, s0, q2);
		//		Transition t2 = new Transition(q2, s1, q4);
		//		Transition t3 = new Transition(q4, s0, q3);
		//		Transition t4 = new Transition(q3, s0, q4);
		//		Transition t5 = new Transition(q3, se, q2);
		//		Transition t6 = new Transition(q1, se, q3);
		//		Transition t7 = new Transition(q2, s1, q2);
		//
		//		NDFA ndfa = new NDFA();
		//		ndfa.addConstruct(t1, t2, t3, t4, t5, t6, t7);
		//		ndfa.setInitial(q1);
		//		ndfa.setFinal(q3, q4);
		//		
		//		System.out.println(ndfa);
		//		DFA dfa = ndfa.toTDFA();
		//		System.out.println(dfa);
	}
}
