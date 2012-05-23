package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import automaton.core.ParserProvider.Node;
import automaton.core.ParserProvider.Node.Any;
import automaton.core.ParserProvider.Node.Basic;
import automaton.core.ParserProvider.Node.Char;
import automaton.core.ParserProvider.Node.EscapedChar;
import automaton.core.ParserProvider.Node.Group;
import automaton.core.ParserProvider.Node.Optional;
import automaton.core.ParserProvider.Node.Plus;
import automaton.core.ParserProvider.Node.Simple;
import automaton.core.ParserProvider.Node.SimpleChar;
import automaton.core.ParserProvider.Node.Star;
import automaton.core.TNFA.RealNFA.Builder;

/**
 * Not thread-safe! Use only from one thread at a time!
 * 
 * @author nes
 * 
 */
class RegexToNFA {
	static class MiniAutomaton {
		final Collection<State> finishing;
		final Collection<State> initial;

		public MiniAutomaton(final Collection<State> initial,
				final Collection<State> finishing) {
			this.initial = Collections.unmodifiableCollection(initial);
			this.finishing = Collections.unmodifiableCollection(finishing);
		}

		public MiniAutomaton(final Collection<State> initial,
				final State finishing) {
			this(initial, Arrays.asList(finishing));
		}

		public MiniAutomaton(final State initial,
				final Collection<State> finishing) {
			this(Arrays.asList(initial), finishing);
		}

		public MiniAutomaton(final State initial, final State finishing) {
			this(Arrays.asList(initial), Arrays.asList(finishing));
		}

		public Collection<State> getFinishing() {
			return finishing;
		}

		public Collection<State> getInitial() {
			return initial;
		}

		public Collection<State> repeatHandles() {
			return getFinishing();
		}

		@Override
		public String toString() {
			return "" + initial + " -> " + finishing;
		}
	}

	public static final class RegexToNFATest {

		@Test
		public void any() {
			final RegexToNFA r = new RegexToNFA();
			final Any any = mock(Any.class);
			final TNFA n = r.convert(any);
			assertThat(n.toString(), is("q0 -> [q1], {(q0, ANY)=[(q1, NONE)]}"));
			final NFAInterpreter i = new NFAInterpreter(n);
			final MatchResult o = i.match("");
			assertThat(o.toString(), is("NO_MATCH"));
		}

		@Test
		public void character() {
			final RegexToNFA r = new RegexToNFA();
			final Char character = mock(Char.class);
			when(character.getCharacter()).thenReturn('4');
			final TNFA n = r.convert(character);
			assertThat(n.toString(), is("q0 -> [q1], {(q0, 4-4)=[(q1, NONE)]}"));
			final NFAInterpreter i = new NFAInterpreter(n);
			final MatchResult o = i.match("5");
			assertThat(o.toString(), is("NO_MATCH"));
			final MatchResult oo = i.match("4");
			assertThat(oo.toString(), is("0-0"));
		}

		@Test
		public void escapedCharacter() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = (Simple) new ParserProvider().regexp()
					.parse("\\.");
			final Char character = (Char) s.basics.get(0);
			assertThat(character, is(EscapedChar.class));
			final TNFA n = r.convert(character);
			assertThat(n.toString(), is("q0 -> [q1], {(q0, .-.)=[(q1, NONE)]}"));
			final NFAInterpreter i = new NFAInterpreter(n);
			final MatchResult o = i.match("5");
			assertThat(o.toString(), is("NO_MATCH"));
			final MatchResult oo = i.match(".");
			assertThat(oo.toString(), is("0-0"));
		}

		@Test
		public void group1() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = (Simple) new ParserProvider().regexp().parse(
					"(\\.)");
			final TNFA tnfa = r.convert(s);
			assertThat(
					tnfa.toString(),
					is("q0 -> [q3], {(q2, ε)=[(q3, ➁0)], (q1, .-.)=[(q2, NONE)], (q0, ε)=[(q1, ➀0)]}"));
		}

		@Test
		public void group2() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = (Simple) new ParserProvider().regexp().parse(
					"(\\.)*");
			final TNFA tnfa = r.convert(s);
			assertThat(
					tnfa.toString(),
					is("q0 -> [q3, q0], {(q3, ε)=[(q0, NONE)], "
							+ "(q2, ε)=[(q3, ➁0)], (q1, .-.)=[(q2, NONE)], (q0, ε)=[(q1, ➀0)]}"));
		}

		@Before
		public void setUp() {
			State.resetCount();
		}

		@Test
		public void testMockSimple() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = mock(Simple.class);
			final EscapedChar c = mock(EscapedChar.class);
			when(c.getCharacter()).thenReturn('.');
			when(s.getBasics()).thenReturn((List) Arrays.asList(c));
			final TNFA tnfa = r.convert(s);
			assertThat(tnfa.toString(),
					is("q0 -> [q1], {(q0, .-.)=[(q1, NONE)]}"));
		}

		@Test
		public void testMockStar() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = mock(Simple.class);
			final Star c = mock(Star.class);
			final SimpleChar e = mock(SimpleChar.class);
			when(c.getElementary()).thenReturn(e);
			when(e.getCharacter()).thenReturn('a');
			when(s.getBasics()).thenReturn((List) Arrays.asList(c));
			final TNFA tnfa = r.convert(s);
			assertThat(
					tnfa.toString(),
					is("q0 -> [q1, q0], {(q1, ε)=[(q0, NONE)], (q0, a-a)=[(q1, NONE)]}"));
		}

		@Test
		public void testSimple() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = (Simple) new ParserProvider().regexp()
					.parse("\\.");
			final TNFA tnfa = r.convert(s);
			assertThat(tnfa.toString(),
					is("q0 -> [q1], {(q0, .-.)=[(q1, NONE)]}"));
		}

		@Test
		public void testStar() {
			final RegexToNFA r = new RegexToNFA();
			final Simple s = (Simple) new ParserProvider().regexp().parse("a*");
			final TNFA tnfa = r.convert(s);
			assertThat(
					tnfa.toString(),
					is("q0 -> [q1, q0], {(q1, ε)=[(q0, NONE)], (q0, a-a)=[(q1, NONE)]}"));
		}
	}

	static class TaggedMiniAutomaton extends MiniAutomaton {
		final Collection<State> repeatHandles;

		public TaggedMiniAutomaton(final Collection<State> initial,
				final State finishing, final Collection<State> repeatHandles) {
			super(initial, finishing);
			this.repeatHandles = repeatHandles;
		}

		public Collection<State> getRepeatHandles() {
			return repeatHandles;
		}

		@Override
		public String toString() {
			return "ȶ" + super.toString();
		}

	}

	public static <T> T assertNotNull(final T object) {
		assert object != null;
		return object;
	}

	public static <T> T checkNotNull(final T o) {
		if (o == null) {
			throw new NullPointerException();
		}
		return o;
	}

	public TNFA convert(final Node node) {
		checkNotNull(node);

		final Builder builder = TNFA.RealNFA.builder();
		final MiniAutomaton m = makeInitialMiniAutomaton(builder);

		final MiniAutomaton a = make(m, builder, node);

		for (final State s : a.getFinishing()) {
			builder.setAsAccepting(s);
		}

		return builder.build();
	}

	MiniAutomaton make(final MiniAutomaton last, final Builder builder,
			final Node node) {
		MiniAutomaton ret;
		if (node instanceof Node.Any) {
			ret = makeAny(last, builder);
		} else if (node instanceof Node.Char) {
			ret = makeChar(last, builder, (Node.Char) node);
		} else if (node instanceof Node.Simple) {
			ret = makeSimple(last, builder, (Node.Simple) node);
		} else if (node instanceof Node.Optional) {
			ret = makeOptional(last, builder, (Node.Optional) node);
		} else if (node instanceof Node.Star) {
			ret = makeStar(last, builder, (Star) node);
		} else if (node instanceof Node.Plus) {
			ret = makePlus(last, builder, (Node.Plus) node);
		} else if (node instanceof Node.Group) {
			ret = makeGroup(last, builder, (Node.Group) node);
		} else if (node instanceof Node.Any) {
			throw new AssertionError("Unknown node type: " + node);
		} else if (node instanceof Node.Eos) {
			throw new AssertionError("Unknown node type: " + node);
		} else if (node instanceof Node.Char) {
			ret = makeChar(last, builder, (Node.Char) node);
		} else if (node instanceof Node.Set) {
			throw new AssertionError("Unknown node type: " + node);
		} else {
			throw new AssertionError("Unknown node type: " + node);
		}

		assert !ret.getInitial().contains(null);
		assert !ret.getFinishing().contains(null);
		return assertNotNull(ret);
	}

	MiniAutomaton makeAny(final MiniAutomaton last, final Builder builder) {
		final State a = builder.makeState();

		builder.addUntaggedTransition(InputRange.ANY, last.getFinishing(), a);

		return new MiniAutomaton(last.getFinishing(), a);
	}

	MiniAutomaton makeChar(final MiniAutomaton last, final Builder b,
			final Node.Char character) {
		final State a = b.makeState();
		final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), a);

		b.addUntaggedTransition(InputRange.make(character.getCharacter()),
				ret.getInitial(), a);

		return ret;
	}

	MiniAutomaton makeGroup(final MiniAutomaton last, final Builder builder,
			final Group group) {
		final CaptureGroup cg = builder.makeCaptureGroup();
		final State startGroup = builder.makeState();
		builder.addStartTagTransition(last.getFinishing(), startGroup, cg);
		final MiniAutomaton startGroupAutomaton = new MiniAutomaton(
				(State) null, startGroup) {
			@Override
			public Collection<State> getInitial() {
				throw new IllegalStateException(
						"A group's inner elements cannot point to something outside of it.");
			}
		};
		final MiniAutomaton body = make(startGroupAutomaton, builder,
				group.getBody());

		final State endGroup = builder.makeState();
		builder.addEndTagTransition(body.getFinishing(), endGroup, cg);

		return new TaggedMiniAutomaton(last.getFinishing(), endGroup,
				body.getFinishing());
	}

	MiniAutomaton makeInitialMiniAutomaton(final Builder builder) {
		final State init = builder.makeInitialState();

		return new MiniAutomaton(init, init);
	}

	MiniAutomaton makeNode(final MiniAutomaton last, final Builder builder,
			final Plus node) {
		throw new RuntimeException("Not implemented");
	}

	MiniAutomaton makeOptional(final MiniAutomaton last, final Builder builder,
			final Optional optional) {
		final MiniAutomaton ma = make(last, builder, optional.getElementary());

		final List<State> f = new ArrayList<>(last.getFinishing());
		f.addAll(ma.getFinishing());

		return new MiniAutomaton(last.getFinishing(), f);
	}

	MiniAutomaton makePlus(final MiniAutomaton last, final Builder builder,
			final Plus node) {
		throw new RuntimeException("Not implemented");
	}

	MiniAutomaton makeSimple(final MiniAutomaton last, final Builder b,
			final Simple simple) {
		final List<? extends Basic> bs = simple.getBasics();

		MiniAutomaton lm = last;
		for (final Basic e : bs) {
			lm = make(lm, b, e);
		}

		return new MiniAutomaton(last.getFinishing(), lm.getFinishing());
	}

	MiniAutomaton makeStar(final MiniAutomaton last, final Builder builder,
			final Star star) {
		final MiniAutomaton inner = make(last, builder, star.getElementary());

		final List<State> f = new ArrayList<>(last.getFinishing());
		f.addAll(inner.getFinishing());

		final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), f);

		builder.makeUntaggedEpsilonTransitionFromTo(inner.repeatHandles(),
				ret.getInitial());

		return ret;
	}
}
