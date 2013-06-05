package ch.unibe.scg.regex;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Maps;
import org.codehaus.jparsec.functors.Tuple3;

import ch.unibe.scg.regex.ParserProvider.Node.Any;
import ch.unibe.scg.regex.ParserProvider.Node.Basic;
import ch.unibe.scg.regex.ParserProvider.Node.Char;
import ch.unibe.scg.regex.ParserProvider.Node.Elementary;
import ch.unibe.scg.regex.ParserProvider.Node.Eos;
import ch.unibe.scg.regex.ParserProvider.Node.EscapedChar;
import ch.unibe.scg.regex.ParserProvider.Node.Group;
import ch.unibe.scg.regex.ParserProvider.Node.Range;
import ch.unibe.scg.regex.ParserProvider.Node.Regex;
import ch.unibe.scg.regex.ParserProvider.Node.SetItem;
import ch.unibe.scg.regex.ParserProvider.Node.Simple;
import ch.unibe.scg.regex.ParserProvider.Node.SimpleChar;
import ch.unibe.scg.regex.ParserProvider.Node.Union;


/**
 * Objects not threadsafe! Use from only one thread!
 *
 * @author nes
 *
 */
class ParserProvider {
  public static interface Node {
    public static class Any implements Elementary {
      Any() {}

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

    public static class Eos implements Elementary {}

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

      public Node getElementary() {
        return elementary;
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
      SetItem() {}
    }

    public class Simple implements Regex {
      final List<? extends Basic> basics;

      public Simple(final List<? extends Basic> basics) {
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

  /** Find only one constructor. */
  static <T> Constructor<T> findConstructor(final Class<T> clazz, final Constructor<T>[] cs) {
    Constructor<T> c = null;
    for (final Constructor<T> eachC : cs) {
      if (eachC.getParameterTypes().length == 1) {
        if (c != null) {
          throw new IllegalArgumentException("Expected only one constructor in " + clazz);
        }
        c = eachC;
      }
    }

    if (c == null) {
      throw new IllegalArgumentException("Expected only constructor in " + clazz
          + ". List of constructors: " + Arrays.toString(cs));
    }
    return c;
  }

  public static <From, To> Map<From, To> fromConstructor(final Class<To> clazz) {
    final Constructor<To>[] cs = (Constructor<To>[]) clazz.getConstructors();

    final Constructor<To> c = findConstructor(clazz, cs);
    return new Map<From, To>() {
      public To map(final From arg0) {
        try {
          return c.newInstance(arg0);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
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
    final Parser<String> p = Scanners.isChar('\\').next(Scanners.ANY_CHAR.source());

    return p.map(new Map<String, EscapedChar>() {
      public EscapedChar map(final String arg) {
        assert arg.length() == 1;
        return new Node.EscapedChar(arg.charAt(0));
      }
    });
  }

  public Parser<Group> group() {
    final Parser<Regex> p =
        Parsers.between(Scanners.isChar('('), regexRef.lazy(), Scanners.isChar(')'));
    return p.map(fromConstructor(Group.class));
  }

  Parser<Node.NegativeSet> negativeSet() {
    final Parser<List<SetItem>> p =
        Parsers.between(Scanners.string("[^"), setItems(), Scanners.isChar(']'));
    return p.map(fromConstructor(Node.NegativeSet.class));
  }

  Parser<Node.Optional> optional() {
    final Parser<? extends Elementary> p = elementary().followedBy(Scanners.isChar('?'));
    return p.map(fromConstructor(Node.Optional.class));
  }

  Parser<Node.Plus> plus() {
    final Parser<? extends Elementary> p = elementary().followedBy(Scanners.isChar('+'));
    return p.map(fromConstructor(Node.Plus.class));
  }

  Parser<Node.PositiveSet> positiveSet() {
    final Parser<List<SetItem>> p =
        Parsers.between(Scanners.isChar('['), setItems(), Scanners.isChar(']'));
    return p.map(fromConstructor(Node.PositiveSet.class));
  }

  // TestingOnly
  void prepare() {
    regexRef.set(regexp());
  }

  Parser<Node.Range> range() {
    final Parser<Tuple3<Char, Void, Char>> p =
        Parsers.tuple(character(), Scanners.isChar('-'), character());
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
    final Parser<? extends Elementary> p = elementary().followedBy(Scanners.isChar('*'));
    return p.map(fromConstructor(Node.Star.class));
  }

  Parser<Union> union() {

    final Parser<Tuple3<Simple, Void, Regex>> p =
        Parsers.tuple(simple(), Scanners.isChar('|'), regexRef.lazy());

    return p.map(new Map<Tuple3<Simple, Void, Regex>, Union>() {
      public Union map(final Tuple3<Simple, Void, Regex> a) {
        assert a != null;
        return new Union(notNull(a.a), notNull(a.c));
      }
    });

  }
}
