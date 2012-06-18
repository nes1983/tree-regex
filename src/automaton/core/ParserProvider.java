package automaton.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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

import automaton.core.ParserProvider.Node.Any;
import automaton.core.ParserProvider.Node.Basic;
import automaton.core.ParserProvider.Node.Char;
import automaton.core.ParserProvider.Node.Elementary;
import automaton.core.ParserProvider.Node.Eos;
import automaton.core.ParserProvider.Node.EscapedChar;
import automaton.core.ParserProvider.Node.Group;
import automaton.core.ParserProvider.Node.Optional;
import automaton.core.ParserProvider.Node.Plus;
import automaton.core.ParserProvider.Node.Range;
import automaton.core.ParserProvider.Node.Regex;
import automaton.core.ParserProvider.Node.Set;
import automaton.core.ParserProvider.Node.SetItem;
import automaton.core.ParserProvider.Node.Simple;
import automaton.core.ParserProvider.Node.SimpleChar;
import automaton.core.ParserProvider.Node.Star;
import automaton.core.ParserProvider.Node.Union;

/**
 * Objects not threadsafe! Use from only one thread!
 * 
 * @author nes
 * 
 */
class ParserProvider {
	public static interface Node {
		public static class Any implements Elementary {
			Any() {
			}

			@Override
			public String toString() {
				return ".";
			}
		}

		public static interface Basic extends Node {
			// <star> | <plus> | <elementary-RE>
		}

		public static abstract class Char extends SetItem implements Elementary {
			final char character;

			Char(final char character) {
				this.character = character;
			}

			public char getCharacter() {
				return character;
			}
		}

		public static interface Elementary extends Basic {
			// <group> | <any> | <eos> | <char> | <set>
		}

		public static class Eos implements Elementary {
		}

		public static class EscapedChar extends Char {
			public EscapedChar(final char character) {
				super(character);
			}

			@Override
			public String toString() {
				return "\\" + String.valueOf(character);
			}
		}

		public static final class Group implements Elementary {
			final Node body;

			public Group(final Node body) {
				this.body = body;
			}

			public Node getBody() {
				return body;
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

		public static class Optional implements Basic {
			final Elementary elementary;

			public Optional(final Elementary elementary) {
				this.elementary = elementary;
			}

			public Node getElementary() {
				return elementary;
			}

			@Override
			public String toString() {
				return elementary.toString() + "?";
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

			public char getFrom() {
				return from;
			}

			public char getTo() {
				return to;
			}

			@Override
			public String toString() {
				return String.valueOf(from) + "-" + to;
			}
		}

		public static interface Regex extends Node {
			// <union> | <simple-RE>
		}

		public static abstract class Set implements Elementary {
			final List<SetItem> items;

			Set(final List<SetItem> items) {
				super();
				this.items = Collections.unmodifiableList(items);
			}

			public List<SetItem> getItems() {
				return items;
			}

			@Override
			public String toString() {
				throw new RuntimeException("Overwrite me");
			}

		}

		public static class SetItem implements Node {
			SetItem() {
			}
		}

		public class Simple implements Regex {
			final List<? extends Basic> basics;

			public Simple(final List<? extends Basic> basics) {
				super();
				this.basics = Collections.unmodifiableList(basics);
			}

			public List<? extends Basic> getBasics() {
				return basics;
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

		public static class SimpleChar extends Char {
			public SimpleChar(final char character) {
				super(character);
			}

			@Override
			public String toString() {
				return String.valueOf(character);
			}
		}

		public static class Star implements Basic {
			final Elementary elementary;

			public Star(final Elementary elementary) {
				this.elementary = elementary;
			}

			public Node getElementary() {
				return elementary;
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

	public static final class ParserTest {
		public static void main(final String... arg) {
			final ParserTest t = new ParserTest();
			t.setUp();
			t.testUnion2();
		}

		private ParserProvider pp;

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
		public void testOptional1() {
			final Optional s = pp.optional().parse("[a-fgk-zA-B]?");
			assertThat(s.elementary, is(Set.class));
			assertThat(s.toString(), is("[a-fgk-zA-B]?"));
		}

		@Test
		public void testOptional2() {
			final Optional s = pp.optional().parse(".?");
			assertThat(s.elementary, is(Any.class));
			assertThat(s.toString(), is(".?"));
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

		@Test
		public void unionBig() {
			final Parser<Regex> p = pp.regexp();
			final String s = "aaa|bbb";
			final Regex rr = p.parse(s);
			assertThat(rr.toString(), is("aaa|bbb"));
		}

		@Test
		public void unionBig2() {
			final Parser<Regex> p = pp.regexp();
			final String s = "aaa|(bbb)?";
			final Regex rr = p.parse(s);
			assertThat(rr, is(Union.class));
			final Union u = (Union) rr;
			final Regex right = u.right;
			assertThat(right, is(Simple.class));
			final Simple simple = (Simple) right;
			final Basic optional = simple.getBasics().get(0);
			assertThat(optional, is(Optional.class));
			assertThat(rr.toString(), is("aaa|(bbb)?"));

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
		return Parsers.or(plus(), star(), optional(), elementary());
	}

	Parser<Char> character() {
		return Parsers.or(simpleCharacter(), escapedCharacter());
	}

	public Parser<? extends Elementary> elementary() {
		return Parsers.or(any(), character(), eos(), set(), group());
	}

	Parser<Eos> eos() {
		final Parser<Void> p = Scanners.isChar('$');
		return p.map(Maps.constant(new Node.Eos()));
	}

	Parser<Node.EscapedChar> escapedCharacter() {
		final Parser<String> p = Scanners.isChar('\\').next(
				Scanners.ANY_CHAR.source());

		return p.map(new Map<String, EscapedChar>() {
			public EscapedChar map(final String arg) {
				assert arg.length() == 1;
				return new Node.EscapedChar(arg.charAt(0));
			}
		});
	}

	public Parser<Group> group() {
		final Parser<Regex> p = Parsers.between(Scanners.isChar('('),
				regexRef.lazy(), Scanners.isChar(')'));
		return p.map(fromConstructor(Group.class));
	}

	Parser<Node.NegativeSet> negativeSet() {
		final Parser<List<SetItem>> p = Parsers.between(Scanners.string("[^"),
				setItems(), Scanners.isChar(']'));
		return p.map(fromConstructor(Node.NegativeSet.class));
	}

	Parser<Node.Optional> optional() {
		final Parser<? extends Elementary> p = elementary().followedBy(
				Scanners.isChar('?'));
		return p.map(fromConstructor(Node.Optional.class));
	}

	Parser<Node.Plus> plus() {
		final Parser<? extends Elementary> p = elementary().followedBy(
				Scanners.isChar('+'));
		return p.map(fromConstructor(Node.Plus.class));
	}

	Parser<Node.PositiveSet> positiveSet() {
		final Parser<List<SetItem>> p = Parsers.between(Scanners.isChar('['),
				setItems(), Scanners.isChar(']'));
		return p.map(fromConstructor(Node.PositiveSet.class));
	}

	// TestingOnly
	void prepare() {
		regexRef.set(regexp());
	}

	private Parser<Node.Range> range() {
		final Parser<Tuple3<Char, Void, Char>> p = Parsers.tuple(character(),
				Scanners.isChar('-'), character());
		return p.map(new Map<Tuple3<Char, Void, Char>, Node.Range>() {
			public Range map(final Tuple3<Char, Void, Char> arg0) {
				assert arg0 != null;
				return new Node.Range(arg0.a.character, arg0.c.character);
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

	Parser<Node.Set> set() {
		return Parsers.or(positiveSet(), negativeSet());
	}

	Parser<Node.SetItem> setItem() {
		return Parsers.or(range(), character());
	}

	Parser<List<Node.SetItem>> setItems() {
		return setItem().many1();
	}

	Parser<Node.Simple> simple() {
		final Parser<List<Basic>> bs = basic().many1();
		return bs.map(fromConstructor(Simple.class));
	}

	Parser<Node.SimpleChar> simpleCharacter() {
		final Parser<String> p = Scanners.notAmong("[]*+()^.|?\\-$").source();

		return p.map(new Map<String, SimpleChar>() {
			public SimpleChar map(final String arg) {
				assert arg != null && arg.length() == 1;
				return new Node.SimpleChar(arg.charAt(0));
			}
		});
	}

	Parser<Node.Star> star() {
		final Parser<? extends Elementary> p = elementary().followedBy(
				Scanners.isChar('*'));
		return p.map(fromConstructor(Node.Star.class));
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
