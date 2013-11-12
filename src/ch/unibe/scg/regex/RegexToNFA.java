package ch.unibe.scg.regex;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.ParserProvider.Node;
import ch.unibe.scg.regex.ParserProvider.Node.Basic;
import ch.unibe.scg.regex.ParserProvider.Node.Group;
import ch.unibe.scg.regex.ParserProvider.Node.NonGreedyStar;
import ch.unibe.scg.regex.ParserProvider.Node.Optional;
import ch.unibe.scg.regex.ParserProvider.Node.Plus;
import ch.unibe.scg.regex.ParserProvider.Node.PositiveSet;
import ch.unibe.scg.regex.ParserProvider.Node.SetItem;
import ch.unibe.scg.regex.ParserProvider.Node.Simple;
import ch.unibe.scg.regex.ParserProvider.Node.Star;
import ch.unibe.scg.regex.ParserProvider.Node.Union;
import ch.unibe.scg.regex.TNFA.Builder;
import ch.unibe.scg.regex.TransitionTriple.Priority;


/**
 * Not thread-safe! Use only from one thread at a time!
 *
 * @author nes
 */
class RegexToNFA {
  final InputRangeCleanup inputRangeCleanup = new InputRangeCleanup();

  TNFA convert(final Node node) {
    Collection<InputRange> allInputRanges = new ArrayList<>();
    allInputRanges.add(InputRange.ANY); // All regexes contain this implicitly.
    findRanges(node, allInputRanges);
    final Builder builder = Builder.make(allInputRanges);

    builder.registerCaptureGroup(builder.captureGroupMaker.entireMatch);

    final MiniAutomaton m =
        makeInitialMiniAutomaton(builder, builder.captureGroupMaker.entireMatch);

    final MiniAutomaton a = make(m, builder, node, builder.captureGroupMaker.entireMatch);

    final State endTagger = builder.makeState();
    builder.addEndTagTransition(a.finishing, endTagger, builder.captureGroupMaker.entireMatch,
        Priority.NORMAL);

    builder.setAsAccepting(endTagger);
    return builder.build();
  }

  private void findRanges(Node n, Collection<InputRange> out) {
    if (n instanceof Node.SetItem) {
      out.add(((SetItem) n).inputRange);
    }
    for (Node c : n.getChildren()) {
      findRanges(c, out);
    }
  }

  static class MiniAutomaton {
    final Collection<State> finishing;
    final Collection<State> initial;

    MiniAutomaton(final Collection<State> initial, final Collection<State> finishing) {
      if (initial.iterator().next() == null) {
        assert false;
      }
      this.initial = initial;
      this.finishing = finishing;
    }

    MiniAutomaton(final Collection<State> initial, final State finishing) {
      this(initial, singleton(finishing));
    }

    @Override
    public String toString() {
      return "" + initial + " -> " + finishing;
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
    } else if (node instanceof Node.NonGreedyStar) {
      ret = makeNonGreedyStar(last, builder, (Node.NonGreedyStar) node, captureGroup);
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

    assert !ret.initial.contains(null);
    assert !ret.finishing.contains(null);
    return ret;
  }

  MiniAutomaton makeAny(final MiniAutomaton last, final Builder builder) {
    final State a = builder.makeState();

    builder.addUntaggedTransition(InputRange.ANY, last.finishing, a, Priority.NORMAL);

    return new MiniAutomaton(last.finishing, a);
  }

  MiniAutomaton makeChar(final MiniAutomaton last, final Builder b, final Node.Char character) {
    final State a = b.makeState();
    final MiniAutomaton ret = new MiniAutomaton(last.finishing, a);

    b.addUntaggedTransition(character.inputRange, ret.initial, a, Priority.NORMAL);

    return ret;
  }

  MiniAutomaton makeEos(final MiniAutomaton last, final Builder builder) {
    final State a = builder.makeState();
    builder.addUntaggedTransition(InputRange.EOS, last.finishing, a, Priority.NORMAL);
    return new MiniAutomaton(last.finishing, a);
  }

  MiniAutomaton makeGroup(final MiniAutomaton last, final Builder builder, final Group group,
      CaptureGroup parentCaptureGroup) {
    final CaptureGroup cg = builder.makeCaptureGroup(parentCaptureGroup);
    builder.registerCaptureGroup(cg);
    final State startGroup = builder.makeState();
    builder.addStartTagTransition(last.finishing, startGroup, cg, Priority.NORMAL);
    final MiniAutomaton startGroupAutomaton = new MiniAutomaton(singleton(startGroup), singleton(startGroup));
    final MiniAutomaton body = make(startGroupAutomaton, builder, group.body, cg);

    final State endTag = builder.makeState();
    builder.addEndTagTransition(body.finishing, endTag, cg, Priority.NORMAL);

    return new MiniAutomaton(last.finishing, endTag);
  }

  MiniAutomaton makeInitialMiniAutomaton(final Builder builder, CaptureGroup entireMatch) {
    final State init = builder.makeInitialState();
    // Eat prefix.
    builder.addUntaggedTransition(InputRange.ANY, singleton(init), init, Priority.LOW);

    final State startTagger = builder.makeState();
    builder.addStartTagTransition(singleton(init), startTagger, entireMatch, Priority.NORMAL);
    return new MiniAutomaton(singleton(init), singleton(startTagger));
  }

  MiniAutomaton makeOptional(final MiniAutomaton last, final Builder builder,
      final Optional optional, CaptureGroup captureGroup) {
    final MiniAutomaton ma = make(last, builder, optional.elementary, captureGroup);

    final List<State> f = new ArrayList<>(last.finishing);
    f.addAll(ma.finishing);

    return new MiniAutomaton(last.finishing, f);
  }

  MiniAutomaton makePlus(final MiniAutomaton last, final Builder builder, final Plus plus,
      CaptureGroup captureGroup) {
    final MiniAutomaton inner = make(last, builder, plus.elementary, captureGroup);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(inner.finishing, out, Priority.LOW);

    final MiniAutomaton ret = new MiniAutomaton(last.finishing, out);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.finishing,
        inner.initial, Priority.NORMAL);
    return ret;
  }

  MiniAutomaton makeUnion(MiniAutomaton last, Builder builder, Union union,
      CaptureGroup captureGroup) {
    MiniAutomaton left = make(last, builder, union.left, captureGroup);
    MiniAutomaton right = make(last, builder, union.right, captureGroup);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(left.finishing, out, Priority.NORMAL);
    builder.makeUntaggedEpsilonTransitionFromTo(right.finishing, out, Priority.LOW);

    return new MiniAutomaton(last.finishing, out);
  }

  MiniAutomaton makePositiveSet(final MiniAutomaton last, final Builder builder,
      final PositiveSet set) {
    final List<SetItem> is = set.items;
    final SortedSet<InputRange> ranges = new TreeSet<>();
    for (final SetItem i : is) {
      ranges.add(i.inputRange);
    }
    final List<InputRange> rangesList = new ArrayList<>(ranges);
    final List<InputRange> cleanedRanges = inputRangeCleanup.cleanUp(rangesList);
    final State a = builder.makeState();
    for (InputRange range : cleanedRanges) {
      builder.addUntaggedTransition(range, last.finishing, a, Priority.NORMAL);
    }
    return new MiniAutomaton(last.finishing, a);
  }

  MiniAutomaton makeSimple(final MiniAutomaton last, final Builder b, final Simple simple,
      CaptureGroup captureGroup) {
    final List<? extends Basic> bs = simple.basics;

    MiniAutomaton lm = last;
    for (final Basic e : bs) {
      lm = make(lm, b, e, captureGroup);
    }

    return new MiniAutomaton(last.finishing, lm.finishing);
  }

  MiniAutomaton makeNonGreedyStar(MiniAutomaton last, Builder builder, NonGreedyStar nonGreedyStar,
      CaptureGroup captureGroup) {
    final MiniAutomaton inner = make(last, builder, nonGreedyStar.elementary, captureGroup);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.initial, inner.finishing, Priority.NORMAL);
    builder.makeUntaggedEpsilonTransitionFromTo(inner.finishing, inner.initial, Priority.LOW);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(inner.finishing, out, Priority.NORMAL);

    return new MiniAutomaton(inner.finishing, out);
  }

  MiniAutomaton makeStar(final MiniAutomaton last, final Builder builder, final Star star,
      CaptureGroup captureGroup) {
    final MiniAutomaton inner = make(last, builder, star.elementary, captureGroup);

    Collection<State> out = singleton(builder.makeState());
    builder.makeUntaggedEpsilonTransitionFromTo(inner.initial, out, Priority.LOW);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.finishing,
        inner.initial, Priority.NORMAL);

    return new MiniAutomaton(last.finishing, out);
  }
}
