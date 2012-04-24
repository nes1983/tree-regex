package automaton.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ StateTest.class, TransitionTableTest.class, ContextTest.class,
		SequenceOfInstructionsTest.class })
public class AllTests
{}
