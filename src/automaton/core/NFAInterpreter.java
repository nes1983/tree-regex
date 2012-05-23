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
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

import org.junit.Test;
import org.mockito.Mockito;

class NFAInterpreter {
	public static final class NFAInterpreterTest {

		@Test
		public void testMocked() {
			final TNFA tnfa = mock(TNFA.class);
			final State a = mock(State.class);
			final State b = mock(State.class);
			final State c = mock(State.class);
			final Tag tag = mock(Tag.class);

			when(tag.getGroup()).thenReturn(0);
			when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('a')))
					.thenReturn(Arrays.asList(new Pair<State, Tag>(a, tag)));
			when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('b')))
					.thenReturn(Arrays.asList(new Pair<State, Tag>(b, tag)));
			when(
					tnfa.availableTransitionsFor(Mockito.eq(b),
							Mockito.any(Character.class))).thenReturn(
					(Collection) Collections.emptyList());
			when(tnfa.getInitialState()).thenReturn(a);
			when(tnfa.isAccepting(eq(a))).thenReturn(false);
			when(tnfa.isAccepting(eq(b))).thenReturn(true);

			final NFAInterpreter n = new NFAInterpreter(tnfa);
			final MatchResult match = n.match("aaab");

			assertThat(match.start(), is(0));
			assertThat(match.end(), is(3));
		}
	}

	class RealMatchResult implements MatchResult {

		List<Pair<Integer, Integer>> captureGroups = new ArrayList<>();

		public int end() {
			return end(0);
		}

		public int end(final int group) {
			return captureGroups.get(group).getSecond();
		}

		public String group() {
			throw null;
		}

		public String group(final int group) {
			throw null;
		}

		public int groupCount() {
			throw null;
		}

		public int start() {
			return start(0);
		}

		public int start(final int group) {
			return captureGroups.get(group).getFirst();
		}

		void takeCaptureGroup(final Tag tag, final int match) {
			assert tag != null;
			captureGroups.add(tag.getGroup(), new Pair<>(0, match));
		}

		@Override
		public String toString() {
			return "" + start() + "-" + end();
		}
	}

	final TNFA nfa;

	final Pair<State, Tag> SCAN;

	NFAInterpreter(final TNFA nfa) {
		this.nfa = nfa;

		SCAN = new Pair<>(new State(), null);

	}

	public MatchResult match(int j, final String input) {
		if (input.equals("")) {
			return NoMatchResult.SINGLETON;
		}
		final Deque<Pair<State, Tag>> q = new ArrayDeque<>();
		q.add(SCAN);
		final int beginning = j;
		Pair<State, Tag> transition = new Pair<State, Tag>(
				nfa.getInitialState(), Tag.ENTIRE_MATCH);
		final RealMatchResult r = new RealMatchResult();
		do {
			if (transition.equals(SCAN)) {
				j++;
				q.add(SCAN);
			} else {
				final Collection<Pair<State, Tag>> transitions = nfa
						.availableTransitionsFor(transition.getFirst(),
								input.charAt(j));
				assert transitions != null;
				for (final Pair<State, Tag> t : transitions) {
					q.add(t);
				}
			}
			transition = q.pop();
			if (!transition.equals(SCAN)) {
				r.takeCaptureGroup(transition.getSecond(), j - 1);
			}
		} while (j <= input.length() && !nfa.isAccepting(transition.getFirst())
				&& !q.isEmpty());

		if (nfa.isAccepting(transition.getFirst())) {
			r.takeCaptureGroup(transition.getSecond(), j - 1);
			r.takeCaptureGroup(Tag.ENTIRE_MATCH, j - 1);
			return r; // TODO: make immutable.
		} else {
			return NoMatchResult.SINGLETON;
		}
	}

	public MatchResult match(final String input) {
		return match(0, input);
	}

}

enum NoMatchResult implements MatchResult {
	SINGLETON;

	public int end() {
		return -1;
	}

	public int end(final int group) {
		if (group == 0) {
			return end();
		}
		throw new NoSuchElementException();
	}

	public String group() {
		throw new NoSuchElementException();
	}

	public String group(final int group) {
		throw new NoSuchElementException();
	}

	public int groupCount() {
		return -1;
	}

	public int start() {
		return -1;
	}

	public int start(final int group) {
		throw new NoSuchElementException();
	}

	@Override
	public String toString() {
		return "NO_MATCH";
	}
}
