package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

class TNFAToTDFA {

	static class DFAState {
		final Collection<Pair<State, Tag>> states;

		public DFAState(final Collection<Pair<State, Tag>> states) {
			super();
			this.states = states;
		}

		public Collection<Pair<State, Tag>> getStates() {
			return states;
		}

	}

	public static class NFA2DFATest {

		TNFA tnfa;
		TNFAToTDFA tnfa2tdfa;

		@Before
		public void setUp() {
			tnfa = mock(TNFA.class);
			tnfa2tdfa = new TNFAToTDFA(tnfa);

		}

		@Test
		public void testReach() {
			final Pair<State, Tag> pr = mock(Pair.class);
			final DFAState tState = new DFAState(Arrays.asList(pr));
			final State s = mock(State.class);
			final Tag t = mock(Tag.class);
			when(
					tnfa.availableTransitionsFor(eq(s),
							Mockito.any(Character.class))).thenReturn(
					Arrays.asList(pr));
			when(pr.getFirst()).thenReturn(s);
			when(pr.getSecond()).thenReturn(t);

			final List<Pair<State, Tag>> states = tnfa2tdfa.reach(tState, 'b');

			assertThat(states.size(), is(1));
			assertThat(states.get(0).getFirst(), is(s));
			assertThat(states.get(0).getSecond(), is(t));

		}
	}

	static class Triple {
		public final int priority;
		public final State state;
		public final Tag tag;

		public Triple(final State state, final int priority, final Tag tag) {
			this.state = state;
			this.priority = priority;
			this.tag = tag;
		}
	}

	TNFA tnfa;

	public TNFAToTDFA(final TNFA tnfa) {
		this.tnfa = tnfa;
	}

	public void closure(final Collection<Pair<State, Tag>> S) {
		final Deque<Triple> stack = new ArrayDeque<>();
		for (final Pair<State, Tag> pr : S) {
			stack.push(new Triple(pr.getFirst(), 0, pr.getSecond()));
		}
		final Collection<Pair<State, Tag>> closure = new ArrayList<>(S);
		while (!stack.isEmpty()) {
			State s;
			int p;
			Tag k;
			{
				final Triple t = stack.pop();
				s = t.state;
				p = t.priority;
				k = t.tag;
			}
			for (final Pair<State, Tag> transition : tnfa
					.availableTransitionsFor(s, null)) {
				if (!transition.getSecond().equals(Tag.NONE)) {
					// XXX continue here.
				}
			}
		}
	}

	/**
	 * tε closure(S) for each (u, k) ∈ S do push (u, 0, k) to stack initialize
	 * closure to S while stack is not empty pop (s, p, k), the top element, off
	 * of stack for each ε-transition from s to some state u do if the
	 * ε-transition was tagged with tn then if∃m:mmn ∈kthen remove mmn from k
	 * add mxn to k, where x is the smallest nonnegative integer such that mxn
	 * does not occur in S if ∃p′,k′ : (u,p′,k′) ∈ closure and p
	 * < p
	 * ′ then remove (u, p′, k′) from closure if (u, p, k) ∈/ closure then add
	 * (u, p, k) to closure push (u, p, k) onto stack remove the middle element,
	 * the priority, from all triples in closure return closure
	 * 
	 * @param t
	 * @param a
	 * @return
	 */

	public List<Pair<State, Tag>> reach(final DFAState t, final char a) {
		final List<Pair<State, Tag>> ret = new ArrayList<>();
		for (final Pair<State, Tag> pr : t.getStates()) {
			final Collection<Pair<State, Tag>> ts = tnfa
					.availableTransitionsFor(pr.getFirst(), a); // TODO switch
																// to
																// InputRange.
			for (final Pair<State, Tag> pr2 : ts) {
				ret.add(new Pair<>(pr2.getFirst(), pr.getSecond()));
			}
		}
		return Collections.unmodifiableList(ret);
	}
}
