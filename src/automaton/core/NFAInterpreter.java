package automaton.core;

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

import automaton.core.Automaton.TNFA;
import automaton.core.TransitionTable.Tag;
import org.junit.Assert;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NFAInterpreter {
	final TNFA nfa;

	final Tag ENTIRE_MATCH_TAG;
	final Pair<State, Tag> SCAN; 
	
	NFAInterpreter(TNFA nfa) {
		this.nfa = nfa;
		ENTIRE_MATCH_TAG = new Tag() {

			public int compareTo(Tag o) {
				return 0;
			}

			public int getGroup() {
				return 0;
			}};
		SCAN = new Pair<>(new State(), null);
			
	}
	
	static class WholeMatchTag implements Tag {
		
		public int compareTo(Tag o) {
			return 0;
		}

		public int getGroup() {
			return 0;
		}
	}
	
	public MatchResult match(int j, String input) {
		Deque<Pair<State,Tag>> q = new ArrayDeque<>();
		q.add(SCAN);
		int beginning = j;
		Pair<State,Tag> transition = new Pair<>(nfa.getInitialState(), (Tag) new WholeMatchTag());
		RealMatchResult r = new RealMatchResult();
		do {
			if(transition.equals(SCAN)) {
				j++;
				q.add(SCAN);
			} else {
				Collection<Pair<State, Tag>> transitions = nfa.availableTransitionsFor(transition.getFirst(), input.charAt(j));
				assert transitions != null;
				for(Pair<State, Tag> t : transitions) {
					q.add(t);
				}
			}
			transition = q.pop();
			if(! transition.equals(SCAN)) {
				r.takeCaptureGroup(transition.getSecond(), j-1);
			}
		} while(j <= input.length() && ! nfa.isAccepting(transition.getFirst()) && ! q.isEmpty());
		
		if(nfa.isAccepting(transition.getFirst())) {
			r.takeCaptureGroup( transition.getSecond(), j-1);
			r.takeCaptureGroup(ENTIRE_MATCH_TAG, j-1);
			return r; //TODO: make immutable.
		} else {
			return NoMatchResult.SINGLETON;
		}
	}
	
	
	
	class RealMatchResult implements MatchResult {
		
		List<Pair<Integer, Integer>> captureGroups = new ArrayList<>();

		
		public int start() {
			return start(0);
		}
		
		void takeCaptureGroup(Tag tag, int match) {
			assert tag != null;
			captureGroups.add(tag.getGroup(), new Pair<>(0,match));
		}

		public int start(int group) {
			return captureGroups.get(group).getFirst();
		}

		public int end() {
			return end(0);
		}

		public int end(int group) {
			return captureGroups.get(group).getSecond();
		}

		public String group() {
			throw null;
		}

		public String group(int group) {
			throw null;
		}

		public int groupCount() {
			throw null;
		}
		
	}
	
	public MatchResult match(String input ) {
		return match(0, input);
	}

	
	public static final class NFAInterpreterTest {
		
		@Test
		public void testMocked() {
			final TNFA tnfa = mock(TNFA.class);
			final State a = mock(State.class);
			final State b = mock(State.class);
			final State c = mock(State.class);
			final Tag tag = mock(Tag.class);
			
			when(tag.getGroup()).thenReturn(0);
			when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('a'))).thenReturn(
					Arrays.asList(new Pair<State,Tag>(a, tag)));
			when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('b'))).thenReturn(
					Arrays.asList(new Pair<State,Tag>(b, tag)));
			when(tnfa.availableTransitionsFor(Mockito.eq(b), Mockito.any(Character.class))).thenReturn(
					(Collection) Collections.emptyList());
			when(tnfa.getInitialState()).thenReturn(a);
			when(tnfa.isAccepting(eq(a))).thenReturn(false);
			when(tnfa.isAccepting(eq(b))).thenReturn(true);

			
			NFAInterpreter n = new NFAInterpreter(tnfa);
			MatchResult match = n.match("aaab");
			
			assertThat(match.start(), is(0));
			assertThat(match.end(), is(3));
		}
	}
		
}


enum NoMatchResult implements MatchResult {
	SINGLETON;

	public int start() {
		return -1;
	}

	public int start(int group) {
		throw new NoSuchElementException();
	}

	public int end() {
		return -1;
	}

	public int end(int group) {
		if(group == 0)
			return end();
		throw new NoSuchElementException();
	}

	public String group() {
		throw new NoSuchElementException();
	}

	public String group(int group) {
		throw new NoSuchElementException();
	}

	public int groupCount() {
		return -1;
	}
	
}
