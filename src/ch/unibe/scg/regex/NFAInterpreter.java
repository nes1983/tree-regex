package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.regex.MatchResult;

import org.junit.Test;
import org.mockito.Mockito;

import ch.unibe.scg.regex.ParserProvider.Node.Simple;
import ch.unibe.scg.regex.RealMatchResult.NoMatchResult;



class NFAInterpreter {
  public static final class NFAInterpreterTest {

    @Test
    public void group2() {
      final RegexToNFA r = new RegexToNFA();
      final Simple s = (Simple) new ParserProvider().regexp().parse("(\\.)*");
      final TNFA tnfa = r.convert(s);
      final NFAInterpreter n = new NFAInterpreter(tnfa);
      final RealMatchResult match = (RealMatchResult) n.match("...");
      assertThat(match.captureGroups.toString(), is(""));
    }

    @Test
    public void testMocked() {
      final TNFA tnfa = mock(TNFA.class);
      final State a = mock(State.class);
      final State b = mock(State.class);
      final State c = mock(State.class);
      final Tag tag = mock(Tag.class);

      when(tag.getGroup()).thenReturn(0);
      when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('a'))).thenReturn(
          Arrays.asList(new TransitionTriple(a, null, tag)));
      when(tnfa.availableTransitionsFor(Mockito.eq(a), eq('b'))).thenReturn(
          Arrays.asList(new TransitionTriple(b, null, tag)));
      when(tnfa.availableTransitionsFor(Mockito.eq(b), Mockito.any(Character.class))).thenReturn(
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

  final TNFA nfa;

  final TransitionTriple SCAN;

  NFAInterpreter(final TNFA nfa) {
    this.nfa = nfa;

    SCAN = new TransitionTriple(null, null, null);

  }

  Character charAt(final int j, final String input) {
    if (j >= input.length()) {
      return null;
    }
    return input.charAt(j);
  }

  public MatchResult match(int j, final String input) {
    final Deque<TransitionTriple> q = new ArrayDeque<>();
    q.add(SCAN);
    TransitionTriple transition =
        new TransitionTriple(nfa.getInitialState(), null, Tag.ENTIRE_MATCH);
    final RealMatchResult r = new RealMatchResult();
    do {
      if (transition.equals(SCAN)) {
        j++;
        q.add(SCAN);
      } else {
        final Collection<TransitionTriple> transitions =
            nfa.availableTransitionsFor(transition.getState(), charAt(j, input));
        assert transitions != null;
        for (final TransitionTriple t : transitions) {
          q.add(t);
        }
      }
      transition = q.pop();
      if (!transition.equals(SCAN)) {
        r.takeCaptureGroup(transition.getTag(), j - 1);
      }
    } while (j <= input.length() && !nfa.isAccepting(transition.getState()) && !q.isEmpty());

    if (nfa.isAccepting(transition.getState())) {
      r.takeCaptureGroup(transition.getTag(), j - 1);
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
