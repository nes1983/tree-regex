package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import ch.unibe.scg.regex.RegexTokenizer.AnyToken;
import ch.unibe.scg.regex.RegexTokenizer.CharToken;
import ch.unibe.scg.regex.RegexTokenizer.CloseGroup;
import ch.unibe.scg.regex.RegexTokenizer.CloseRange;
import ch.unibe.scg.regex.RegexTokenizer.EscapedCharToken;
import ch.unibe.scg.regex.RegexTokenizer.HyphenToken;
import ch.unibe.scg.regex.RegexTokenizer.NonGreedyPlusToken;
import ch.unibe.scg.regex.RegexTokenizer.NonGreedyQuestionToken;
import ch.unibe.scg.regex.RegexTokenizer.NonGreedyStarToken;
import ch.unibe.scg.regex.RegexTokenizer.OpenGroup;
import ch.unibe.scg.regex.RegexTokenizer.OpenNegativeRange;
import ch.unibe.scg.regex.RegexTokenizer.OpenRange;
import ch.unibe.scg.regex.RegexTokenizer.OrToken;
import ch.unibe.scg.regex.RegexTokenizer.PlusToken;
import ch.unibe.scg.regex.RegexTokenizer.QuestionToken;
import ch.unibe.scg.regex.RegexTokenizer.StarToken;
import ch.unibe.scg.regex.RegexTokenizer.Token;

/**
 * The parser taking a string and converting it into a {@link Node.Regex}.
 * 
 * @see #parse()
 * @see RegexParser#parse(String)
 */
final class RegexParser {
  /** Parser descents into levels for groups, this models that. */
  private final Deque<SubParser> stack = new ArrayDeque<>();

  /** Reference what string we are parsing to give useful error messages. */
  private final String regex;

  /** The output of the {@link RegexTokenizer}. */
  private final List<Token> tokens;

  /** Feed the string to the parser. */
  private RegexParser(final String r) {
    regex = r;
    tokens = new RegexTokenizer(r).tokenize();
    stack.addFirst(new OuterParser());
  }

  /** Building block for descending into deeper levels. */
  private interface SubParser {
    /** Add meaning of token to currently built subexpression. */
    void interpret(Token c);

    /** Allow child parsers to add content to the parent parser. */
    void add(Node.Basic n);
  }

  /** Dispatch on tokens. */
  private abstract class StandardSubParser implements SubParser {
    @Override
    public void interpret(final RegexTokenizer.Token c) {
      // Dispatch: Java doesn't allow switch on classes.
      if (c instanceof CloseGroup) {
        parseCloseGroup((CloseGroup) c);
      } else if (c instanceof AnyToken) {
        parseAny((AnyToken) c);
      } else if (c instanceof OpenGroup) {
        parseOpenGroup((OpenGroup) c);
      } else if (c instanceof PlusToken) {
        parsePlus((PlusToken) c);
      } else if (c instanceof StarToken) {
        parseStar((StarToken) c);
      } else if (c instanceof QuestionToken) {
        parseQuestion((QuestionToken) c);
      } else if (c instanceof NonGreedyPlusToken) {
        parseNonGreedyPlus((NonGreedyPlusToken) c);
      } else if (c instanceof NonGreedyStarToken) {
        parseNonGreedyStar((NonGreedyStarToken) c);
      } else if (c instanceof NonGreedyQuestionToken) {
        parseNonGreedyQuestion((NonGreedyQuestionToken) c);
      } else if (c instanceof CharToken) {
        parseChar((CharToken) c);
      } else if (c instanceof EscapedCharToken) {
        parseEscapedChar((EscapedCharToken) c);
      } else if (c instanceof OpenRange) {
        parseRange((OpenRange) c);
      } else if (c instanceof OpenNegativeRange) {
        parseNegativeRange((OpenNegativeRange) c);
      } else if (c instanceof CloseRange) {
        parseCloseRange((CloseRange) c);
      } else if (c instanceof HyphenToken) {
        parseHyphen((HyphenToken) c);
      }  else if (c instanceof OrToken) {
        parseOr((OrToken) c);
      } else {
        die(c, String.format("unexpected token: %s", c));
      }
    }

    /** Parse '\&lt;char'. */
    abstract void parseEscapedChar(EscapedCharToken c);

    /** Parse ')'. */
    abstract void parseCloseGroup(CloseGroup c);

    /** Parse '.'. */
    abstract void parseAny(AnyToken c);

    /** Parse '('. */
    abstract void parseOpenGroup(OpenGroup c);

    /** Parse '+'. */
    abstract void parsePlus(PlusToken c);

    /** Parse '*'. */
    abstract void parseStar(StarToken c);

    /** Parse '?'. */
    abstract void parseQuestion(QuestionToken c);

    /** Parse '+?'. */
    abstract void parseNonGreedyPlus(NonGreedyPlusToken c);

    /** Parse '*?'. */
    abstract void parseNonGreedyStar(NonGreedyStarToken c);

    /** Parse '??'. */
    abstract void parseNonGreedyQuestion(NonGreedyQuestionToken c);

    /** Parse '<char>'. */
    abstract void parseChar(CharToken c);

    /** Parse '['. */
    abstract void parseRange(OpenRange c);

    /** Parse '[^'. */
    abstract void parseNegativeRange(OpenNegativeRange c);

    /** Parse ']'. */
    abstract void parseCloseRange(CloseRange c);

    /** Parse '-'. */
    abstract void parseHyphen(HyphenToken c);

    /** Parse '|'. */
    abstract void parseOr(OrToken c);
  }

  /** Interprets tokens as escaped characters. */
  private class LiteralParser extends StandardSubParser {
    final Deque<Node.SetItem> content = new ArrayDeque<>();

    @Override
    void parseCloseGroup(final CloseGroup c) {
      content.add(new Node.EscapedChar(')'));
    }

    @Override
    void parseAny(final AnyToken c) {
      content.add(new Node.EscapedChar('.'));
    }

    @Override
    void parseOpenGroup(final OpenGroup c) {
      content.add(new Node.EscapedChar('('));
    }

    @Override
    void parsePlus(final PlusToken c) {
      content.add(new Node.EscapedChar('+'));
    }

    @Override
    void parseStar(final StarToken c) {
      content.add(new Node.EscapedChar('*'));
    }

    @Override
    void parseQuestion(final QuestionToken c) {
      content.add(new Node.EscapedChar('?'));
    }

    @Override
    void parseNonGreedyPlus(final NonGreedyPlusToken c) {
      content.add(new Node.EscapedChar('+'));
      content.add(new Node.EscapedChar('?'));
    }

    @Override
    void parseNonGreedyStar(final NonGreedyStarToken c) {
      content.add(new Node.EscapedChar('*'));
      content.add(new Node.EscapedChar('?'));
    }

    @Override
    void parseNonGreedyQuestion(final NonGreedyQuestionToken c) {
      content.add(new Node.EscapedChar('?'));
    }

    @Override
    void parseChar(final CharToken c) {
      content.add(new Node.SimpleChar(c.c));
    }

    @Override
    void parseRange(final OpenRange c) {
      content.add(new Node.EscapedChar('['));
    }

    @Override
    void parseNegativeRange(final OpenNegativeRange c) {
      content.add(new Node.EscapedChar('['));
      content.add(new Node.EscapedChar('^'));
    }

    @Override
    void parseHyphen(final HyphenToken c) {
      content.add(new Node.EscapedChar('-'));
    }

    @Override
    public void add(final Node.Basic n) {
      throw new AssertionError("LiteralParser had real subparser?");
    }

    @Override
    void parseCloseRange(final CloseRange c) {
      content.add(new Node.EscapedChar(']'));
    }

    @Override
    void parseOr(final OrToken c) {
      content.add(new Node.EscapedChar('|'));
    }

    @Override
    void parseEscapedChar(final EscapedCharToken c) {
      content.add(new Node.EscapedChar(c.c));
    }
  }

  /** Parse everything from '[' or '[^' onwards to the next ']'. */
  private final class SetItemParser extends LiteralParser {
    /**
     * True iff the range starts with '['.
     *
     * Characters must match ranges, otherwise they must not math the ranges.
     */
    private final boolean positive;

    private SetItemParser(final boolean positive) {
      this.positive = positive;
    }


    @Override
    void parseHyphen(final HyphenToken c) {
      final Node.SetItem last = content.removeLast();
      stack.addFirst(new RangeParser(last));
    }

    @Override
    void parseCloseRange(final CloseRange c) {
      stack.removeFirst();
      final SubParser parent = stack.peekFirst();
      if (positive) {
        parent.add(new Node.PositiveSet(new ArrayList<>(content)));
      } else {
        parent.add(new Node.NegativeSet(new ArrayList<>(content)));
      }
    }

    /** Adds additional possibility to match the group. */
    public void add(final Node.Range range) {
      content.add(range);
    }
  }

  /** Parses between '[' and ']', yielding ranges like 'a-z'. */
  private final class RangeParser extends LiteralParser {
    RangeParser(final Node.SetItem last) {
      super();
      content.addLast(last);
    }

    @Override
    public void parseHyphen(final HyphenToken c) {
      content.add(new Node.SimpleChar('-'));
    }

    @Override
    public void interpret(final Token c) {
      super.interpret(c);
      // We have at least 2 chars now: beginning and end.
      // There are cases however where we get more
      // (if a token is multiple chars, e.g. [^).
      // We ignore this at the moment.
      // TODO(akarper) not ignore that.
      stack.removeFirst();
      final Node.Char beginItem = (Node.Char) content.removeFirst();
      final Node.Char endItem = (Node.Char) content.removeFirst();

      final SetItemParser parent = (SetItemParser) stack.peekFirst();
      parent.add(new Node.Range(beginItem.c, endItem.c));
    }
  }

  /** Interprets the special characters according to regex standard. */
  private abstract class SimpleParser extends StandardSubParser {
    Deque<Node.Basic> content = new ArrayDeque<>();
    Node.Union orContent = null;

    @Override
    void parseHyphen(final HyphenToken c) {
      content.addLast(new Node.SimpleChar('-'));
    }
    @Override
    void parseEscapedChar(final EscapedCharToken c) {
      content.addLast(new Node.EscapedChar(c.c));
    }
    @Override
    void parseRange(final OpenRange c) {
      stack.addFirst(new SetItemParser(true));
    }
    @Override
    void parseNegativeRange(final OpenNegativeRange c) {
      stack.addFirst(new SetItemParser(false));
    }
    @Override
    void parseCloseRange(final CloseRange c) {
      die(c, "Unescaped ] found without preceding [");
    }
    @Override
    void parseChar(final CharToken c) {
      content.addLast(new Node.SimpleChar(c.c));
    }
    @Override
    void parseQuestion(final QuestionToken c) {
      final Node.Basic last = content.removeLast();
      if (last instanceof Node.Elementary) {
        content.addLast(new Node.Optional((Node.Elementary) last));
      } else {
        throw new AssertionError(
            "Question mark can't be after repetition token.");
      }
    }
    @Override
    void parseNonGreedyQuestion(final NonGreedyQuestionToken c) {
      throw new UnsupportedOperationException();
    }
    @Override
    void parseStar(final StarToken c) {
      final Node.Basic last = content.removeLast();
      if (last instanceof Node.Elementary) {
        content.addLast(new Node.Star((Node.Elementary) last));
      } else {
        // +* = *  ?* = * ** = *
        // +?* = * ??* = * *?* = *
        throw new UnsupportedOperationException();
      }
    }
    @Override
    void parseNonGreedyStar(final NonGreedyStarToken c) {
      final Node.Basic last = content.removeLast();
      if (last instanceof Node.Elementary) {
        content.addLast(new Node.NonGreedyStar((Node.Elementary) last));
      } else {
        // +*? = *?  ?*? = *? **? = *?
        // +?*? = * ??*? = * *?*? = *?
        throw new UnsupportedOperationException();
      }
    }
    @Override
    void parsePlus(final PlusToken c) {
      final Node.Basic last = content.removeLast();
      if (last instanceof Node.Elementary) {
        content.addLast(new Node.Plus((Node.Elementary) last));
      } else if (last instanceof Node.Optional) {
        // ?+ = * *+ = *
        // ??+ = * *?+ = *
        content.addLast(new Node.Star(((Node.Optional) last).elementary));
      } else if (last instanceof Node.Plus) {
        // ++ = + +?+ = +
        throw new UnsupportedOperationException();
      }
    }
    @Override
    void parseNonGreedyPlus(final NonGreedyPlusToken c) {
      final Node.Basic last = content.removeLast();
      if (last instanceof Node.Elementary) {
        // TODO(akarper) make this atomic
        content.addLast(last);
        content.addLast(new Node.NonGreedyStar((Node.Elementary) last));
      } else {
        // +*? = *?  ?*? = *? **? = *?
        // +?*? = * ??*? = * *?*? = *?
        throw new UnsupportedOperationException();
      }
    }
    @Override
    void parseOpenGroup(final OpenGroup c) {
      stack.addFirst(new GroupParser());
    }
    @Override
    abstract void parseCloseGroup(final CloseGroup c);
    @Override
    void parseAny(final AnyToken c) {
      add(new Node.Any());
    }

    @Override
    void parseOr(final OrToken c) {
      final Node.Simple sofar = new Node.Simple(new ArrayList<>(content));
      stack.addFirst(new OrParser(sofar));
    }
    @Override
    public void add(final Node.Basic n) {
      content.addLast(n);
    }

    /** Add righthand side of '|'. */
    public void add(final Node.Union u) {
      orContent = u;
    }
  }

  /** Yields righthand side of '|' into previous parser. */
  private final class RighthandOrParser extends SimpleParser {
    void end() {
      // we're in the case /(a|b)c/ here.
      stack.removeFirst();
      final OrParser or = (OrParser) stack.removeFirst();
      final SubParser parent = stack.peekFirst();
      assert parent instanceof SimpleParser;
      final SimpleParser simpleParent = (SimpleParser) parent;
      if (orContent == null) {
        final Node.Simple right = new Node.Simple(new ArrayList<>(content));
        simpleParent.add(new Node.Union(or.left, right));
      } else {
        simpleParent.add(new Node.Union(or.left, orContent));
      }
    }
    @Override
    void parseCloseGroup(final CloseGroup c) {
      end();
      final SubParser parent = stack.peekFirst();
      parent.interpret(c);
    }
  }

  /** Reinterprets lefthand side as being before '|'. */
  private final class OrParser implements SubParser {
    final Node.Simple left;

    public OrParser(final Node.Simple sofar) {
      left = sofar;
    }

    @Override
    public void interpret(final Token c) {
      final RighthandOrParser child = new RighthandOrParser();
      stack.push(child);
      child.interpret(c);
    }

    @Override
    public void add(final Node.Basic n) {
      throw new AssertionError("Added to OrParser");
    }
  }

  /** Starts reading the inner part of a capture group on '('. */
  private final class GroupParser extends SimpleParser {
    @Override
    void parseCloseGroup(final CloseGroup c) {
      stack.removeFirst();
      final SubParser parent = stack.peekFirst();
      if (orContent == null) {
        parent.add(new Node.Group(new Node.Simple(new ArrayList<>(content))));
      } else {
        parent.add(new Node.Group(orContent));
      }
    }
  }

  /** Outmost parser, not in any capture group yet. */
  final class OuterParser extends SimpleParser {
    @Override
    void parseCloseGroup(final CloseGroup c) {
      die(c, "Found ) without matching (.");
    }
  }

  /** Declares given Token to be unexpected. */
  private void die(final Token t, final String reason) {
    throw new PatternSyntaxException(reason, regex, t.index);
  }

  /** @return parse of the whole regex. */
  public Node.Regex parse() {
    for (final Token t: tokens) {
      final SubParser current = stack.peekFirst();
      current.interpret(t);
    }
    SubParser current = stack.peekFirst();
    while (current instanceof RighthandOrParser) {
      final RighthandOrParser o = (RighthandOrParser) current;
      o.end();
      current = stack.peekFirst();
    }
    final OuterParser last = (OuterParser) stack.removeFirst();
    if (last.orContent == null) {
      return new Node.Simple(new ArrayList<>(last.content));
    }
    return last.orContent;
  }

  /** @return Parse of the regular expression given. */
  public static Node.Regex parse(final String s) {
    return new RegexParser(s).parse();
  }
}
