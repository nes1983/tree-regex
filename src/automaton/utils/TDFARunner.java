package automaton.utils;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;
import automaton.core.InputRange;
import automaton.core.Pair;
import automaton.core.State;
import automaton.core.TDFA;
import automaton.instructions.Context;
import automaton.instructions.CopyInstruction;
import automaton.instructions.SequenceOfInstructions;
import automaton.instructions.SetInstruction;
import automaton.instructions.StoreInstruction;


public class TDFARunner
{
	public static void main(String[] args)
	{
		// TODO Remove this method when no longer needed

		TDFA tdfa = new TDFA();

		State q0 = new State();
		State q1 = new State();

		SequenceOfInstructions s0 = new SequenceOfInstructions();
		SequenceOfInstructions s1 = new SequenceOfInstructions();
		SequenceOfInstructions s2 = new SequenceOfInstructions();
		SequenceOfInstructions s3 = new SequenceOfInstructions();

		s0.enqueue(new SetInstruction(0, 0));
		s1.enqueue(new SetInstruction(1, 0));
		s2.enqueue(new CopyInstruction(1, 0, 0));
		s2.enqueue(new SetInstruction(1, 0));
		s3.enqueue(new StoreInstruction(0, 0, 0));

		tdfa.setInitialState(q0, s1);
		tdfa.addTransition(q0, new InputRange('a', 'a'), q1, s1);
		tdfa.addTransition(q1, new InputRange('a', 'a'), q1, s2);
		tdfa.addFinalState(q1, s3);

		Pair<Boolean, Map<Integer, Integer>> result =
				TDFARunner.evaluate(tdfa, "aa");

		if (result.getFirst()) System.out.println(result.getSecond());
	}


	public static Pair<Boolean, Map<Integer, Integer>> evaluate(TDFA automaton,
			String string)
	{
		// Create needed variables
		State current, last = null;
		StringCharacterIterator characters =
				new StringCharacterIterator(string);
		Context context = new Context();
		int pos = 1;

		// Initialize first state
		current = automaton.getInitialState();
		automaton.getInitialInstructions().execute(context, pos);

		// Do the evaluation
		while ((current =
				automaton.step(context, pos, current, characters.current())) != null)
		{
			if (characters.next() == CharacterIterator.DONE) break;
			last = current;
			pos++;
		}

		// Control the acceptance
		if (current == null) return new Pair<>(false, context.getResult());
		if (!automaton.getFinalStates().contains(current))
			return new Pair<>(false, context.getResult());
		if (characters.current() != CharacterIterator.DONE)
			return new Pair<>(false, context.getResult());

		automaton.getFinalInstructions(last).execute(context, pos);
		return new Pair<>(true, context.getResult());
	}
}
