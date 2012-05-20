package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.functors.Tuple3;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import automaton.core.ParserProvider.RegexpASTNode.Any;
import automaton.core.ParserProvider.RegexpASTNode.Basic;
import automaton.core.ParserProvider.RegexpASTNode.Char;
import automaton.core.ParserProvider.RegexpASTNode.Elementary;
import automaton.core.ParserProvider.RegexpASTNode.Eos;
import automaton.core.ParserProvider.RegexpASTNode.EscapedChar;
import automaton.core.ParserProvider.RegexpASTNode.Group;
import automaton.core.ParserProvider.RegexpASTNode.Plus;
import automaton.core.ParserProvider.RegexpASTNode.Range;
import automaton.core.ParserProvider.RegexpASTNode.Regex;
import automaton.core.ParserProvider.RegexpASTNode.Set;
import automaton.core.ParserProvider.RegexpASTNode.SetItem;
import automaton.core.ParserProvider.RegexpASTNode.Simple;
import automaton.core.ParserProvider.RegexpASTNode.SimpleChar;
import automaton.core.ParserProvider.RegexpASTNode.Star;
import automaton.core.ParserProvider.RegexpASTNode.Union;

/**
 * Not threadsafe! Use from only one thread!
 * 
 * @author nes
 * 
 */
class ParserProvider {
	public static final class ParserTest {
		public static void main(final String... arg) {
			final ParserTest t = new ParserTest();
			t.setUp();
			t.testUnion2();
		}

		private ParserProvider pp;

		@Test
		public void niko() {
			Parser<Union> u = pp.union();
			final Parser<Regex> p = pp.regexp();
			final String s = "aaa|bbb";
			u = mock(Parser.class);
			final Parser uu = Parsers.or(p, u);
			verifyNoMoreInteractions(u);
			uu.parse(s);

			verifyZeroInteractions(u);
			final Regex rr = p.parse(s);
			assertThat(rr.toString(), is("aaa|bbb"));
		}

		@Test
		public void regexp4() {
			final Parser<Regex> s = pp.regexp();
			final Regex r = s.parse("aaa");
			assertThat(r.toString(), is("aaa"));
		}

		@Before
		public void setUp() {
			pp = new ParserProvider();
		}

		@After
		public void tearDown() {
			pp = null;
		}

		@Test
		public void testBasic() {
			final Basic s = pp.basic().parse(".*");
			assertThat(s, is(Star.class));
			final Star ss = (Star) s;
			assertThat(ss.elementary, is(Any.class));
			assertThat(s.toString(), is(".*"));
		}

		@Test(expected = ParserException.class)
		public void testDontParseRange() {
			final Range r = pp.range().parse("a--f");
			assertThat(r.toString(), is("a-f"));
		}

		@Test
		public void testEscaped() {
			final EscapedChar s = pp.escapedCharacter().parse("\\(");
			assertThat(s.character, is('('));
			assertThat(s.toString(), is("\\("));
		}

		@Test
		public void testGroup2() {
			pp.prepare();
			final Parser<Group> s = pp.group();
			final Group u = s.parse("(aaa|bbb)");
			assertThat(u.toString(), is("(aaa|bbb)"));
		}

		@Test(expected = ParserException.class)
		public void testNoSet() {
			pp.set().parse("[a-fgk-zA-B][");
		}

		@Test
		public void testParseRange() {
			final Range r = pp.range().parse("a-f");
			assertThat(r.toString(), is("a-f"));
		}

		@Test
		public void testPlus1() {
			final Plus s = pp.plus().parse("[a-fgk-zA-B]+");
			assertThat(s.elementary, is(Set.class));
			assertThat(s.toString(), is("[a-fgk-zA-B]+"));
		}

		@Test
		public void testPlus2() {
			final Plus s = pp.plus().parse(".+");
			assertThat(s.elementary, is(Any.class));
			assertThat(s.toString(), is(".+"));
		}

		@Test
		public void testRegexp() {
			final Parser<Regex> p = pp.regexp();
			final Regex rr = p.parse("aaa|bbb");
			assertThat(rr.toString(), is("aaa|bbb"));
		}

		@Test
		public void testSet() {
			final Set s = pp.set().parse("[a-fgk-zA-B]");

			assertThat(s.toString(), is("[a-fgk-zA-B]"));
		}

		@Test(expected = ParserException.class)
		public void testSimpleNotEmpty() {
			pp.simple().parse("");
		}

		@Test
		public void testStar1() {
			final Star s = pp.star().parse("[a-fgk-zA-B]*");
			assertThat(s.elementary, is(Set.class));
			assertThat(s.toString(), is("[a-fgk-zA-B]*"));
		}

		@Test
		public void testStar2() {
			final Star s = pp.star().parse(".*");
			assertThat(s.elementary, is(Any.class));
			assertThat(s.toString(), is(".*"));
		}

		@Test
		public void testUnion() {
			pp.prepare();
			final Parser<Union> s = pp.union();
			final Union u = s.parse("[a-fgk-zA-B]*|aaa");
			assertThat(u.toString(), is("[a-fgk-zA-B]*|aaa"));
			assertThat(u.left.getClass(), is((Object) Simple.class));
			assertThat(u.right, is(Simple.class));
		}

		@Test
		public void testUnion2() {
			pp.prepare();
			final Parser<Union> s = pp.union();
			final Union u = s.parse("[a-fgk-zA-B]*|(aaa|bbb)");
			assertThat(u.toString(), is("[a-fgk-zA-B]*|(aaa|bbb)"));
			assertThat(u.left.getClass(), is((Object) Simple.class));
			assertThat(u.right, is(Simple.class));
		}

		@Test
		public void testUnion3() {
			pp.prepare();
			final Parser<Union> s = pp.union();
			final Union u = s.parse("bbb|aaa");
			assertThat(u.toString(), is("bbb|aaa"));
		}
	}

	public static interface RegexpASTNode {
		public static final class Any implements Elementary {
			@Override
			public String toString() {
				return ".";
			}
		}

		public static interface Basic extends RegexpASTNode {
			// <star> | <plus> | <elementary-RE>
		}

		public static abstract class Char extends SetItem implements Elementary {
			final char character;

			Char(final char character) {
				this.character = character;
			}
		}

		public static interface Elementary extends Basic {
			// <group> | <any> | <eos> | <char> | <set>
		}

		public static final class Eos implements Elementary {
		}

		public static final class EscapedChar extends Char {
			public EscapedChar(final char character) {
				super(character);
			}

			@Override
			public String toString() {
				return "\\" + String.valueOf(character);
			}
		}

		public static final class Group implements Elementary {
			final RegexpASTNode body;

			public Group(final RegexpASTNode body) {
				this.body = body;
			}

			@Override
			public String toString() {
				return "(" + body.toString() + ")";
			}
		}

		public static final class NegativeSet extends Set {

			public NegativeSet(final List<SetItem> items) {
				super(items);
			}

			@Override
			public String toString() {
				final StringBuilder s = new StringBuilder();
				s.append("[^");
				for (final SetItem i : items) {
					s.append(i);
				}
				s.append("]");
				return s.toString();
			}
		}

		public static final class Plus implements Basic {
			final Elementary elementary;

			public Plus(final Elementary elementary) {
				this.elementary = elementary;
			}

			@Override
			public String toString() {
				return elementary.toString() + "+";
			}
		}

		public static final class PositiveSet extends Set {

			public PositiveSet(final List<SetItem> items) {
				super(items);
			}

			@Override
			public String toString() {
				final StringBuilder s = new StringBuilder();
				s.append("[");
				for (final SetItem i : items) {
					s.append(i);
				}
				s.append("]");
				return s.toString();
			}

		}

		public static final class Range extends SetItem {
			final char from, to;

			Range(final char from, final char to) {
				this.from = from;
				this.to = to;
			}

			@Override
			public String toString() {
				return String.valueOf(from) + "-" + to;
			}
		}

		public static interface Regex extends RegexpASTNode {
			// <union> | <simple-RE>
		}

		public static abstract class Set implements Elementary {
			final List<SetItem> items;

			Set(final List<SetItem> items) {
				super();
				this.items = Collections.unmodifiableList(items);
			}

			@Override
			public String toString() {
				throw new RuntimeException("Overwrite me");
			}

		}

		public static class SetItem implements RegexpASTNode {
			SetItem() {
			}
		}

		public class Simple implements Regex {
			final List<Basic> basics;

			public Simple(final List<Basic> basics) {
				super();
				this.basics = Collections.unmodifiableList(basics);
			}

			@Override
			public String toString() {
				final StringBuilder s = new StringBuilder();
				for (final Basic b : basics) {
					s.append(b.toString());
				}
				return s.toString();
			}
		}

		public static final class SimpleChar extends Char {
			public SimpleChar(final char character) {
				super(character);
			}

			@Override
			public String toString() {
				return String.valueOf(character);
			}
		}

		public static final class Star implements Basic {
			final Elementary elementary;

			public Star(final Elementary elementary) {
				this.elementary = elementary;
			}

			@Override
			public String toString() {
				return elementary.toString() + "*";
			}
		}

		public static final class Union implements Regex {
			final Simple left;
			final Regex right;

			public Union(final Simple left, final Regex right) {
				super();
				this.left = left;
				this.right = right;
			}

			@Override
			public String toString() {
				return left.toString() + "|" + right;
			}
		}
	}

	/**
	 * Find only one constructor.
	 * 
	 * @param clazz
	 * @param cs
	 * @return
	 */
	static <T> Constructor<T> findConstructor(final Class<T> clazz,
			final Constructor<T>[] cs) {
		Constructor<T> c = null;
		for (final Constructor<T> eachC : cs) {
			if (eachC.getParameterTypes().length == 1) {
				if (c != null) {
					throw new IllegalArgumentException(
							"Expected only one constructor in " + clazz);
				}
				c = eachC;
			}
		}

		if (c == null) {
			throw new IllegalArgumentException("Expected only constructor in "
					+ clazz + ". List of constructors: " + cs);
		}
		return c;
	}

	public static <From, To> Map<From, To> fromConstructor(final Class<To> clazz) {
		final Constructor<To>[] cs = (Constructor<To>[]) clazz
				.getConstructors();

		final Constructor<To> c = findConstructor(clazz, cs);
		return new Map<From, To>() {
			public To map(final From arg0) {
				try {
					return c.newInstance(arg0);
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static <T> T notNull(final T o) {
		assert o != null;
		return o;
	}

	Parser.Reference<Regex> regexRef;

	ParserProvider() {
		reInitialize();
	}

	Parser<Any> any() {
		return Scanners.isChar('.').map(Maps.constant(new Any()));
	}

	Parser<Basic> basic() {
		return Parsers.or(plus(), star(), elementary());
	}

	Parser<Char> character() {
		return Parsers.or(simpleCharacter(), escapedCharacter());
	}

	public Parser<? extends Elementary> elementary() {
		return Parsers.or(any(), character(), eos(), set(), group());
	}

	Parser<Eos> eos() {
		final Parser<Void> p = Scanners.isChar('$');
		return p.map(Maps.constant(new RegexpASTNode.Eos()));
	}

	Parser<RegexpASTNode.EscapedChar> escapedCharacter() {
		final Parser<String> p = Scanners.isChar('\\').next(
				Scanners.ANY_CHAR.source());

		return p.map(new Map<String, EscapedChar>() {
			public EscapedChar map(final String arg) {
				assert arg.length() == 1;
				return new RegexpASTNode.EscapedChar(arg.charAt(0));
			}
		});
	}

	public Parser<Group> group() {
		final Parser<Regex> p = Parsers.between(Scanners.isChar('('),
				regexRef.lazy(), Scanners.isChar(')'));
		return p.map(fromConstructor(Group.class));
	}

	Parser<RegexpASTNode.NegativeSet> negativeSet() {
		final Parser<List<SetItem>> p = Parsers.between(Scanners.string("[^"),
				setItems(), Scanners.isChar(']'));
		return p.map(fromConstructor(RegexpASTNode.NegativeSet.class));
	}

	Parser<RegexpASTNode.Plus> plus() {
		final Parser<? extends Elementary> p = elementary().followedBy(
				Scanners.isChar('+'));
		return p.map(fromConstructor(RegexpASTNode.Plus.class));
	}

	Parser<RegexpASTNode.PositiveSet> positiveSet() {
		final Parser<List<SetItem>> p = Parsers.between(Scanners.isChar('['),
				setItems(), Scanners.isChar(']'));
		return p.map(fromConstructor(RegexpASTNode.PositiveSet.class));
	}

	// TestingOnly
	void prepare() {
		regexRef.set(regexp());
	}

	private Parser<RegexpASTNode.Range> range() {
		final Parser<Tuple3<Char, Void, Char>> p = Parsers.tuple(character(),
				Scanners.isChar('-'), character());
		return p.map(new Map<Tuple3<Char, Void, Char>, RegexpASTNode.Range>() {
			public Range map(final Tuple3<Char, Void, Char> arg0) {
				assert arg0 != null;
				return new RegexpASTNode.Range(arg0.a.character,
						arg0.c.character);
			}
		});
	}

	public Parser<Regex> regexp() {
		final Parser<Regex> p = Parsers.or(union(), simple());
		regexRef.set(p);
		return p;
	}

	public void reInitialize() {
		regexRef = Parser.newReference();
	}

	Parser<RegexpASTNode.Set> set() {
		return Parsers.or(positiveSet(), negativeSet());
	}

	Parser<RegexpASTNode.SetItem> setItem() {
		return Parsers.or(range(), character());
	}

	Parser<List<RegexpASTNode.SetItem>> setItems() {
		return setItem().many1();
	}

	Parser<RegexpASTNode.Simple> simple() {
		final Parser<List<Basic>> bs = basic().many1();
		return bs.map(fromConstructor(Simple.class));
	}

	Parser<RegexpASTNode.SimpleChar> simpleCharacter() {
		final Parser<String> p = Scanners.notAmong("[]*+()^.|\\-$").source();

		return p.map(new Map<String, SimpleChar>() {
			public SimpleChar map(final String arg) {
				assert arg != null && arg.length() == 1;
				return new RegexpASTNode.SimpleChar(arg.charAt(0));
			}
		});
	}

	Parser<RegexpASTNode.Star> star() {
		final Parser<? extends Elementary> p = elementary().followedBy(
				Scanners.isChar('*'));
		return p.map(fromConstructor(RegexpASTNode.Star.class));
	}

	Parser<Union> union() {

		final Parser<Tuple3<Simple, Void, Regex>> p = Parsers.tuple(simple(),
				Scanners.isChar('|'), regexRef.lazy());

		return p.map(new Map<Tuple3<Simple, Void, Regex>, Union>() {
			public Union map(final Tuple3<Simple, Void, Regex> a) {
				assert a != null;
				return new Union(notNull(a.a), notNull(a.c));
			}
		});

	}
}
