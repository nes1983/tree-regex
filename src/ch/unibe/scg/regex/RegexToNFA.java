package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.Arrays;
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
import ch.unibe.scg.regex.TNFA.RealNFA.Builder;
import ch.unibe.scg.regex.TransitionTriple.Priority;


/**
 * Not thread-safe! Use only from one thread at a time!
 * 
 * @author nes
 */
class RegexToNFA {
  static class MiniAutomaton {
    final Collection<State> finishing;
    final Collection<State> initial;

    public MiniAutomaton(final Collection<State> initial, final Collection<State> finishing) {
      this.initial = Collections.unmodifiableCollection(initial);
      this.finishing = Collections.unmodifiableCollection(finishing);
    }

    public MiniAutomaton(final Collection<State> initial, final State finishing) {
      this(initial, Arrays.asList(finishing));
    }

    public MiniAutomaton(final State initial, final Collection<State> finishing) {
      this(Arrays.asList(initial), finishing);
    }

    public MiniAutomaton(final State initial, final State finishing) {
      this(Arrays.asList(initial), Arrays.asList(finishing));
    }

    public Collection<State> getBeginRepeatHandle() {
      return initial;
    }

    public Collection<State> getFinishing() {
      return finishing;
    }

    public Collection<State> getFinishingRepeatHandles() {
      return getFinishing();
    }

    public Collection<State> getInitial() {
      return initial;
    }

    @Override
    public String toString() {
      return "" + initial + " -> " + finishing;
    }
  }

  static class TaggedMiniAutomaton extends MiniAutomaton {
    final Collection<State> beginRepeatHandle;
    final Collection<State> finishRepeatHandles;

    public TaggedMiniAutomaton(final Collection<State> initial, final State finishing,
        final Collection<State> repeatBeginHandle, final Collection<State> repeatHandles) {
      super(initial, finishing);
      this.finishRepeatHandles = repeatHandles;
      this.beginRepeatHandle = repeatBeginHandle;
    }

    @Override
    public Collection<State> getBeginRepeatHandle() {
      return beginRepeatHandle;
    }

    @Override
    public Collection<State> getFinishingRepeatHandles() {
      return finishRepeatHandles;
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

  MiniAutomaton make(final MiniAutomaton last, final Builder builder, final Node node) {
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
    } else if (node instanceof Node.Eos) {
      ret = makeEos(last, builder);
    } else if (node instanceof Node.Char) {
      ret = makeChar(last, builder, (Node.Char) node);
    } else if (node instanceof Node.PositiveSet) {
      ret = makePositiveSet(last, builder, (Node.PositiveSet) node);
    } else if (node instanceof Node.NegativeSet) {
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

  MiniAutomaton makeGroup(final MiniAutomaton last, final Builder builder, final Group group) {
    final CaptureGroup cg = builder.makeCaptureGroup();
    final State startGroup = builder.makeState();
    builder.addStartTagTransition(last.getFinishing(), startGroup, cg, Priority.NORMAL);
    final MiniAutomaton startGroupAutomaton = new MiniAutomaton((State) null, startGroup) {
      @Override
      public Collection<State> getInitial() {
        throw new IllegalStateException(
            "A group's inner elements cannot point to something outside of it.");
      }
    };
    final MiniAutomaton body = make(startGroupAutomaton, builder, group.getBody());

    final State endGroup = builder.makeState();
    builder.addEndTagTransition(body.getFinishing(), endGroup, cg, Priority.NORMAL);

    final TaggedMiniAutomaton ret =
        new TaggedMiniAutomaton(last.getFinishing(), endGroup, body.getInitial(),
            body.getFinishing());
    return ret;
  }

  MiniAutomaton makeInitialMiniAutomaton(final Builder builder) {
    final State init = builder.makeInitialState();

    builder.addUntaggedTransition(InputRange.ANY, init, init, Priority.NORMAL);

    return new MiniAutomaton(init, init);
  }

  MiniAutomaton makeNode(final MiniAutomaton last, final Builder builder, final Plus node) {
    throw new RuntimeException("Not implemented");
  }

  MiniAutomaton makeOptional(final MiniAutomaton last, final Builder builder,
      final Optional optional) {
    final MiniAutomaton ma = make(last, builder, optional.getElementary());

    final List<State> f = new ArrayList<>(last.getFinishing());
    f.addAll(ma.getFinishing());

    return new MiniAutomaton(last.getFinishing(), f);
  }

  MiniAutomaton makePlus(final MiniAutomaton last, final Builder builder, final Plus plus) {
    // TODO(niko) priority guard is missing.
    final MiniAutomaton inner = make(last, builder, plus.getElementary());

    final Collection<State> f = inner.getFinishing();

    final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), f);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.getFinishingRepeatHandles(),
        inner.getBeginRepeatHandle(), Priority.NORMAL);
    return ret;
  }

  MiniAutomaton makePositiveSet(final MiniAutomaton last, final Builder builder,
      final PositiveSet set) {
    final List<SetItem> is = set.getItems();
    final SortedSet<InputRange> ranges = new TreeSet<>();
    for (final SetItem i : is) {
      final InputRange ir = inputRangeFor(i);
      ranges.add(ir);
    }
    throw null;
  }

  MiniAutomaton makeSimple(final MiniAutomaton last, final Builder b, final Simple simple) {
    final List<? extends Basic> bs = simple.getBasics();

    MiniAutomaton lm = last;
    for (final Basic e : bs) {
      lm = make(lm, b, e);
    }

    return new MiniAutomaton(last.getFinishing(), lm.getFinishing());
  }

  MiniAutomaton makeStar(final MiniAutomaton last, final Builder builder, final Star star) {
    final MiniAutomaton inner = make(last, builder, star.getElementary());

    final List<State> f = new ArrayList<>(last.getFinishing());
    f.addAll(inner.getFinishing());

    final MiniAutomaton ret = new MiniAutomaton(last.getFinishing(), f);

    builder.makeUntaggedEpsilonTransitionFromTo(inner.getFinishingRepeatHandles(),
        inner.getBeginRepeatHandle(), Priority.NORMAL);
    return ret;
  }
}
