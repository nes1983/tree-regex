package ch.unibe.scg.regex;

import static java.util.Collections.singleton;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

interface Node {
  static class Any implements Elementary {
    @Override
    public String toString() {
      return ".";
    }

    @Override
    public Collection<Node> getChildren() {
      return Collections.emptyList();
    }
  }

  static interface Basic extends Node {
    // <star> | <plus> | <elementary-RE>
  }

  static abstract class Char extends SetItem implements Elementary {
    final char c;
    public Char(final char c) {
      super(InputRange.make(c));
      this.c = c;
    }

    @Override
    public String toString() {
      return String.valueOf(inputRange.getFrom());
    }
  }

  static interface Elementary extends Basic {
    // <group> | <any> | <eos> | <char> | <set>
  }

  static class Eos implements Elementary {
    @Override
    public Collection<Node> getChildren() {
      return Collections.emptyList();
    }
  }

  static class EscapedChar extends Char {
    public EscapedChar(final char character) {
      super(character);
    }

    @Override
    public String toString() {
      return "\\" + String.valueOf(inputRange.getFrom());
    }
  }

  static final class Group implements Elementary {
    final Node body;

    public Group(final Node body) {
      this.body = body;
    }

    @Override
    public String toString() {
      return "(" + body.toString() + ")";
    }

    @Override
    public Collection<Node> getChildren() {
      return singleton(body);
    }
  }

  static final class NegativeSet extends Set {
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

    @Override
    public Collection<Node> getChildren() {
      throw new RuntimeException("Not implemented");
    }
  }

  static class Optional implements Basic {
    final Elementary elementary;

    public Optional(final Elementary elementary) {
      this.elementary = elementary;
    }

    @Override
    public String toString() {
      return elementary.toString() + "?";
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return singleton(elementary);
    }
  }

  static final class Plus implements Basic {
    final Elementary elementary;

    public Plus(final Elementary elementary) {
      this.elementary = elementary;
    }

    @Override
    public String toString() {
      return elementary.toString() + "+";
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return singleton(elementary);
    }
  }

  static final class PositiveSet extends Set {
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

  static final class Range extends SetItem {

    public Range(final char from, final char to) {
      super(InputRange.make(from, to));
    }

    @Override
    public String toString() {
      return String.valueOf(inputRange.getFrom()) + "-" + inputRange.getTo();
    }
  }

  static interface Regex extends Node {
    // <union> | <simple-RE>
  }

  static abstract class Set implements Elementary {
    final List<SetItem> items;

    public Set(final List<SetItem> items) {
      this.items = items;
    }

    @Override
    public String toString() {
      throw new RuntimeException("Overwrite me");
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return items;
    }
  }

  // Char || Range
  static abstract class SetItem implements Node {
    final InputRange inputRange;

    public SetItem(InputRange inputRange) {
      this.inputRange = inputRange;
    }

    @Override
    public String toString() {
      throw new RuntimeException("Overwrite me");
    }

    @Override
    public Collection<Node> getChildren() {
      return Collections.emptyList();
    }
  }

  class Simple implements Regex {
    final List<? extends Basic> basics;

    public Simple(final List<? extends Basic> basics) {
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

    @Override
    public Collection<? extends Node> getChildren() {
      return basics;
    }
  }

  static class SimpleChar extends Char {
    public SimpleChar(final char character) {
      super(character);
    }

    @Override
    public String toString() {
      return String.valueOf(inputRange.getFrom());
    }
  }

  static class NonGreedyStar implements Basic {
    final Elementary elementary;

    public NonGreedyStar(final Elementary elementary) {
      this.elementary = elementary;
    }

    @Override
    public String toString() {
      return elementary.toString() + "*?";
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return singleton(elementary);
    }
  }

  static class Star implements Basic {
    final Elementary elementary;

    public Star(final Elementary elementary) {
      this.elementary = elementary;
    }

    @Override
    public String toString() {
      return elementary.toString() + "*";
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return singleton(elementary);
    }
  }

  static final class Union implements Regex {
    final Simple left;
    final Regex right;

    public Union(final Simple left2, final Regex right2) {
      this.left = left2;
      this.right = right2;
    }

    @Override
    public String toString() {
      return left.toString() + "|" + right;
    }

    @Override
    public Collection<? extends Node> getChildren() {
      return Arrays.asList(left, right);
    }
  }

  Collection<? extends Node> getChildren();
}