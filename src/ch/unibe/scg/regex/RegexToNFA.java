package ch.unibe.scg.regex;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.ParserProvider.Node;
import ch.unibe.scg.regex.ParserProvider.Node.Basic;
import ch.unibe.scg.regex.ParserProvider.Node.Group;
import ch.unibe.scg.regex.ParserProvider.Node.Optional;
import ch.unibe.scg.regex.ParserProvider.Node.Plus;
import ch.unibe.scg.regex.ParserProvider.Node.PositiveSet;
import ch.unibe.scg.regex.ParserProvider.Node.SetItem;
import ch.unibe.scg.regex.ParserProvider.Node.Simple;
import ch.unibe.scg.regex.ParserProvider.Node.Star;
import ch.unibe.scg.regex.ParserProvider.Node.Union;
import ch.unibe.scg.regex.TNFA.RealNFA.Builder;
import ch.unibe.scg.regex.TransitionTriple.Priority;


/**
 * Not thread-safe! Use only from one thread at a time!
 *
 * @author nes
 */
class RegexToNFA {
  final InputRangeCleanup inputRangeCleanup = new InputRangeCleanup();

  public TNFA convert(final Node node) {
    requireNonNull(node);
    final Builder builder = new Builder();

    builder.registerCaptureGroup(builder.captureGroupMaker.entireMatch);

    final MiniAutomaton m =
        makeInitialMiniAutomaton(builder, builder.captureGroupMaker.entireMatch);

    final MiniAutomaton a = make(m, builder, node, builder.captureGroupMaker.entireMatch);

    final State endTagger = builder.makeState();
    builder.addEndTagTransition(a.getFinishing(), endTagger, builder.captureGroupMaker.entireMatch,
        Priority.NORMAL);

    builder.setAsAccepting(endTagger);
    return builder.build();
  }

  static class MiniAutomaton {
    final Collection<State> finishing;
    final Collection<State> initial;

    public MiniAutomaton(final Collection<State> initial, final Collection<State> finishing) {
      this.initial = Collections.unmodifiableCollection(initial);
      this.finishing = Collections.unmodifiableCollection(finishing);
    }

    public MiniAutomaton(final Collection<State> initial, final State finishing) {
      this(initial, singleton(finishing));
    }

    public MiniAutomaton(final State initial, final Collection<State> finishing) {
      this(singleton(initial), finishing);
    }

    public MiniAutomaton(final State initial, final State finishing) {
      this(singleton(initial), singleton(finishing));
    }

    public Collection<State> getFinishing() {
      return finishing;
    }

    public Collection<State> getInitial() {
      return initial;
    }

    @Override
    public String toString() {
      return "" + initial + " -> " + finishing;
    }
  }

  InputRange inputRangeFor(final Node.Char character) {
    return InputRange.make(character.getCharacter());
  }

  InputRange inputRangeFor(final Node.Range range) {
    return InputRange.make(range.getFrom(), range.getTo());
  }

  InputRange inputRangeFor(final SetItem i) {
    if (i instanceof Node.Range) {
      return inputRangeFor((Node.Range) i);
    } else if (i instanceof Node.Char) {
      return inputRangeFor((Node.Char) i);
    } else {
      throw new AssertionError("Unknown set item: " + i + ".");
    }
  }

  MiniAutomaton make(final MiniAutomaton last, final Builder builder, final Node node,
      CaptureGroup captureGroup) {
    MiniAutomaton ret;
    if (node instanceof Node.Any) {
      ret = makeAny(last, builder);
    } else if (node instanceof Node.Char) {
      ret = makeChar(last, builder, (Node.Char) node);
    } else if (node instanceof Node.Simple) {
      ret = makeSimple(last, builder, (Node.Simple) node, captureGroup);
    } else if (node instanceof Node.Optional) {
      ret = makeOptional(last, builder, (Node.Optional) node, captureGroup);
    } else if (node instanceof Node.Star) {
      ret = makeStar(last, builder, (Star) node, captureGroup);
    } else if (node instanceof Node.Plus) {
      ret = makePlus(last, builder, (Node.Plus) node, captureGroup);
    } else if (node instanceof Node.Group) {
      ret = makeGroup(last, builder, (Node.Group) node, captureGroup);
    } else if (node instanceof Node.Eos) {
      ret = makeEos(last, builder);
    } else if (node instanceof Node.Char) {
      ret = makeChar(last, builder, (Node.Char) node);
    } else if (node instanceof Node.PositiveSet) {
      ret = makePositiveSet(last, builder, (Node.PositiveSet) node);
    } else if (node instanceof Node.Union) {
      ret = makeUnion(last, builder, (Node.Union) node, captureGroup);
    } else {
      throw new AssertionError("Unknown node type: " + node);
    }

    assert !ret.getInitial().contains(null);
    assert !ret.getFinishing().contains(null);
    return ret;
  }

  MiniAutomaton makeAny(final MiniAutomaton last, final Builder builder) {
    final State a = builder.makeState();

    builder.addUntaggedTransition(InputRange.ANY, last.getFinishing(), a, Priority.NORMAL);

    return new MiniAutomaton(last.getFinishing(), a);
  }

  MiniAutomaton makeChar(final MiniAutomaton last, final Builder b, final Node.Char character) {
    final State a = b.makeState();
    final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), a);

    b.addUntaggedTransition(InputRange.make(character.getCharacter()), ret.getInitial(), a,
        Priority.NORMAL);

    return ret;
  }

  MiniAutomaton makeEos(final MiniAutomaton last, final Builder builder) {
    final State a = builder.makeState();
    builder.addUntaggedTransition(InputRange.EOS, last.getFinishing(), a, Priority.NORMAL);
    return new MiniAutomaton(last.getFinishing(), a);
  }

  MiniAutomaton makeGroup(final MiniAutomaton last, final Builder builder, final Group group,
      CaptureGroup parentCaptureGroup) {
    final CaptureGroup cg = builder.makeCaptureGroup(parentCaptureGroup);
    builder.registerCaptureGroup(cg);
    final State startGroup = builder.makeState();
    builder.addStartTagTransition(last.getFinishing(), startGroup, cg, Priority.NORMAL);
    final MiniAutomaton startGroupAutomaton = new MiniAutomaton((State) null, startGroup) {
      @Override
      public Collection<State> getInitial() {
        throw new IllegalStateException(
            "A group's inner elements cannot point to something outside of it.");
      }
    };
    final MiniAutomaton body = make(startGroupAutomaton, builder, group.getBody(), cg);

    final State endTag = builder.makeState();
    builder.addEndTagTransition(body.getFinishing(), endTag, cg, Priority.NORMAL);

    return new MiniAutomaton(last.getFinishing(), endTag);
  }

  MiniAutomaton makeInitialMiniAutomaton(final Builder builder, CaptureGroup entireMatch) {
    final State init = builder.makeInitialState();
    // Eat prefix.
    builder.addUntaggedTransition(InputRange.ANY, init, init, Priority.NORMAL);

    final State startTagger = builder.makeState();
    builder.addStartTagTransition(singleton(init), startTagger, entireMatch, Priority.NORMAL);
    return new MiniAutomaton(init, startTagger);
  }

  MiniAutomaton makeOptional(final MiniAutomaton last, final Builder builder,
      final Optional optional, CaptureGroup captureGroup) {
    final MiniAutomaton ma = make(last, builder, optional.getElementary(), captureGroup);

    final List<State> f = new ArrayList<>(last.getFinishing());
    f.addAll(ma.getFinishing());

    return new MiniAutomaton(last.getFinishing(), f);
  }

  MiniAutomaton makePlus(final MiniAutomaton last, final Builder builder, final Plus plus,
      CaptureGroup captureGroup) {
    final MiniAutomaton inner = make(last, builder, plus.getElementary(), captureGroup);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(inner.getFinishing(), out, Priority.LOW);

    final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), out);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.getFinishing(),
        inner.getInitial(), Priority.NORMAL);
    return ret;
  }

  MiniAutomaton makeUnion(MiniAutomaton last, Builder builder, Union union,
      CaptureGroup captureGroup) {
    MiniAutomaton left = make(last, builder, union.left, captureGroup);
    MiniAutomaton right = make(last, builder, union.right, captureGroup);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(left.getFinishing(), out, Priority.NORMAL);
    builder.makeUntaggedEpsilonTransitionFromTo(right.getFinishing(), out, Priority.LOW);

    return new MiniAutomaton(last.getFinishing(), out);
  }

  MiniAutomaton makePositiveSet(final MiniAutomaton last, final Builder builder,
      final PositiveSet set) {
    final List<SetItem> is = set.getItems();
    final SortedSet<InputRange> ranges = new TreeSet<>();
    for (final SetItem i : is) {
      final InputRange ir = inputRangeFor(i);
      ranges.add(ir);
    }
    final SortedSet<InputRange> cleanedRanges = inputRangeCleanup.cleanUp(ranges);
    final State a = builder.makeState();
    for (InputRange range : cleanedRanges) {
      builder.addUntaggedTransition(range, last.getFinishing(), a, Priority.NORMAL);
    }
    return new MiniAutomaton(last.getFinishing(), a);
  }

  MiniAutomaton makeSimple(final MiniAutomaton last, final Builder b, final Simple simple,
      CaptureGroup captureGroup) {
    final List<? extends Basic> bs = simple.getBasics();

    MiniAutomaton lm = last;
    for (final Basic e : bs) {
      lm = make(lm, b, e, captureGroup);
    }

    return new MiniAutomaton(last.getFinishing(), lm.getFinishing());
  }

  MiniAutomaton makeStar(final MiniAutomaton last, final Builder builder, final Star star,
      CaptureGroup captureGroup) {
    final MiniAutomaton inner = make(last, builder, star.getElementary(), captureGroup);

    final List<State> f = new ArrayList<>(last.getFinishing());
    f.addAll(inner.getFinishing());

    final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), f);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.getFinishing(), inner.getInitial(),
        Priority.NORMAL);
    return ret;
  }
}
