package ch.unibe.scg.regex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.unibe.scg.regex.FixedPriorityQueue.Priorizable;
import ch.unibe.scg.regex.Tag.MarkerTag;
import ch.unibe.scg.regex.TransitionTriple.Priority;


class TNFAToTDFA {
  static class DFAState implements Comparable<DFAState> {
    final static DFAState NO_STATE;
    static {
      final Map<State, int[]> e = Collections.emptyMap();
      NO_STATE = new DFAState(e);
    }

    public static String toString(final Map<State, int[]> states) {
      final StringBuilder sb = new StringBuilder();
      for (final Map.Entry<State, int[]> el : states.entrySet()) {
        sb.append(el.getKey());
        sb.append("->");
        sb.append(Arrays.toString(el.getValue()));
        sb.append(", ");
      }
      sb.delete(sb.length() - 2, sb.length());
      return sb.toString();
    }

    final Map<State, int[]> innerStates;

    public DFAState(final Map<State, int[]> innerStates) {
      this.innerStates = Collections.unmodifiableMap(innerStates);
    }

    public int compareTo(final DFAState o) {
      return DFAStateComparator.SINGLETON.compare(this.innerStates, o.innerStates);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final DFAState other = (DFAState) obj;
      if (innerStates == null) {
        if (other.innerStates != null) {
          return false;
        }
      } else if (!innerStates.keySet().equals(other.innerStates.keySet())) {
        return false;
      }

      return true;
    }

    Map<State, int[]> getData() {
      return innerStates;
    }

    @Override
    public int hashCode() {
      if (innerStates == null) {
        return 1;
      }

      return innerStates.keySet().hashCode();
    }

    boolean isMappable(final DFAState other, final int[] mapping) {
      if (!this.innerStates.keySet().equals(other.innerStates.keySet())) {
        return false;
      }
      Arrays.fill(mapping, -1);

      for (final Map.Entry<State, int[]> entry : innerStates.entrySet()) {
        final int[] mine = entry.getValue();
        final int[] theirs = other.innerStates.get(entry.getKey());
        final boolean success = updateMap(mapping, mine, theirs);
        if (!success) {
          return false;
        }
      }

      return true;
    }

    /**
     * @return a mapping from this state to another, if there is one. Otherwise, return null.
     */
    int[] mappingIfAny(final DFAState other, final int maxLoc) {
      final int[] mapping = new int[maxLoc];
      if (this.isMappable(other, mapping)) {
        return mapping;
      } else {
        return null;
      }
    }

    @Override
    public String toString() {
      return toString(innerStates);
    }

    /**
     * Destructively update <code>map</code> until it maps from to to. A -1 entry in map means that
     * the value can still be changed. Other values are left untouched.
     * 
     * @param map Must be at least as big as the biggest values in both from and to. Elements must
     *        be >= -1. -1 stands for unassigned.
     * @param from same length as to.
     * @param to same length as from.
     * @return True if the mapping was successful; false otherwise.
     */
    private boolean updateMap(final int[] map, final int[] from, final int[] to) {
      assert from.length == to.length;
      for (int i = 0; i < from.length; i++) {
        if (from[i] < 0 && to[i] < 0) {
          continue; // Both leave i unspecified: that's fine.
        } else if (from[i] < 0 && to[i] >= 0) {
          return false; // Only from specifies the mapping, that won't do.
        } else if (map[from[i]] == -1) {
          map[from[i]] = to[i];
        } else if (map[from[i]] != to[i]) {
          return false;
        } // Else everything is fine.
      }
      return true;
    }
  }

  static enum DFAStateComparator implements Comparator<Map<State, int[]>> {
    SINGLETON;

    private int compare(final int[] a, final int[] b) {
      {
        final int cmp = Integer.compare(a.length, b.length);
        if (cmp != 0) {
          return cmp;
        }
      }
      for (int i = 0; i < a.length; i++) {
        final int cmp = Integer.compare(a[i], b[i]);
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }

    @Override
    public int compare(final Map<State, int[]> o1, final Map<State, int[]> o2) {
      {
        final int cmp = Integer.compare(o1.size(), o2.size());
        if (cmp != 0) {
          return cmp;
        }
      }

      final List<State> states1 = new ArrayList<>(o1.keySet());
      Collections.sort(states1);
      final List<State> states2 = new ArrayList<>(o2.keySet());
      Collections.sort(states2);

      assert states1.size() == states2.size();

      for (int i = 0; i < states1.size(); i++) {
        final int cmp = states1.get(i).compareTo(states2.get(i));
        if (cmp != 0) {
          return cmp;
        }
      }

      for (int i = 0; i < states1.size(); i++) {
        assert states1.get(i).compareTo(states2.get(i)) == 0;
        final State s = states1.get(i);
        final int cmp = compare(o1.get(s), o2.get(s));
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }
  }

  static class MapItems implements Iterable<MapItem> {

    final Map<Tag, BitSet> mapItems;

    public MapItems(final Map<Tag, BitSet> mapItems) {
      this.mapItems = mapItems;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final MapItems other = (MapItems) obj;
      if (mapItems == null) {
        if (other.mapItems != null) {
          return false;
        }
      } else if (!mapItems.equals(other.mapItems)) {
        return false;
      }
      return true;
    }

    public Map<Tag, BitSet> getMapItems() {
      return mapItems;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((mapItems == null) ? 0 : mapItems.hashCode());
      return result;
    }

    public Iterator<MapItem> iterator() {
      return new Iterator<MapItem>() {

        Entry<Tag, BitSet> currentEntry;
        final Iterator<Entry<Tag, BitSet>> iterator = mapItems.entrySet().iterator();
        int position = -1;

        private void ensureCurrentEntry() {
          if (currentEntry == null) {
            currentEntry = iterator.next();
          }
          assert currentEntry != null;
        }

        @Override
        public boolean hasNext() {
          if (iterator.hasNext()) {
            return true;
          }
          ensureCurrentEntry();
          return currentEntry.getValue().nextSetBit(position + 1) >= 0;
        }

        @Override
        public MapItem next() {
          ensureCurrentEntry();
          BitSet bs;
          restart: do {
            bs = currentEntry.getValue();
            position = bs.nextSetBit(position + 1);
            if (position < 0) {
              currentEntry = iterator.next();
              position = -1;
              continue restart;
            }
          } while (false);
          assert position >= 0 && bs != null;

          return new MapItem(currentEntry.getKey(), position);
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

  }

  static enum StatesComparator implements Comparator<Map<State, SortedSet<MapItem>>> {
    SINGLETON;

    @Override
    public int compare(final Map<State, SortedSet<MapItem>> o1,
        final Map<State, SortedSet<MapItem>> o2) {
      {
        final int cmp = Integer.compare(o1.size(), o2.size());
        if (cmp != 0) {
          return cmp;
        }
      }

      final List<State> states1 = new ArrayList<>(o1.keySet());
      Collections.sort(states1);
      final List<State> states2 = new ArrayList<>(o2.keySet());
      Collections.sort(states2);

      assert states1.size() == states2.size();

      for (int i = 0; i < states1.size(); i++) {
        final int cmp = states1.get(i).compareTo(states2.get(i));
        if (cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }
  }

  static class StateWithMemoryLocation implements Map.Entry<State, int[]> {
    final int[] memoryLocation;
    final State state;

    StateWithMemoryLocation(final State state, final int[] memoryLocation) {
      this.memoryLocation = memoryLocation;
      this.state = state;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final StateWithMemoryLocation other = (StateWithMemoryLocation) obj;
      if (state == null) {
        if (other.state != null) {
          return false;
        }
      } else if (!state.equals(other.state)) {
        return false;
      }
      return true;
    }

    public State getKey() {
      return state;
    }

    public int[] getMemoryLocation() {
      return memoryLocation;
    }

    public State getState() {
      return state;
    }

    public int[] getValue() {
      return memoryLocation;
    }

    @Override
    public int hashCode() {
      return ((state == null) ? 0 : state.hashCode());
    }

    public int[] setValue(final int[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "" + state + "" + Arrays.toString(memoryLocation);
    }
  }

  public static TNFAToTDFA make(final TNFA tnfa) {
    return new TNFAToTDFA(tnfa);
  }

  int highestMapping = 0;

  final Instruction.InstructionMaker instructionMaker = Instruction.InstructionMaker.get();

  final TransitionTable.TDFATransitionTable.Builder tdfaBuilder =
      new TransitionTable.TDFATransitionTable.Builder();

  final TNFA tnfa;

  public TNFAToTDFA(final TNFA tnfa) {
    this.tnfa = tnfa;
  }

  /** @return All input ranges, sorted */
  List<InputRange> allInputRanges() {
    final List<InputRange> ranges = new ArrayList<>(tnfa.allInputRanges());
    if (ranges.size() < 2) {
      return Collections.unmodifiableList(ranges);
    }

    final List<InputRange> ret = new ArrayList<>();
    Collections.sort(ranges);
    final Iterator<InputRange> iter = ranges.iterator();
    InputRange last = iter.next();
    InputRange cur = null;
    while (iter.hasNext()) {
      cur = iter.next();
      if (last.getTo() < cur.getFrom()) {
        ret.add(last);
      } else {
        last = InputRange.make((char) (last.getTo() + 1), cur.getTo());
      }
      last = cur;
    }
    assert cur != null;
    ret.add(cur);
    return new ArrayList<>(ret);
  }

  /**
   * Maps directly to section 4 of the paper. That's why it looks so ugly.
   * 
   * @param S
   * @deprecated
   */
  @Deprecated
  Map<State, SortedSet<MapItem>> closure(final Map<State, SortedSet<MapItem>> S) {
    final Deque<Triple> stack = new ArrayDeque<>();
    for (final Entry<State, SortedSet<MapItem>> pr : S.entrySet()) {
      stack.push(new Triple(pr.getKey(), Priority.NORMAL, pr.getValue()));
    }
    final NavigableSet<Triple> closure = initClosure(S);
    while (!stack.isEmpty()) {
      State s;
      SortedSet<MapItem> k;
      {
        final Triple t = stack.pop();
        s = t.state;
        k = t.mapItems;
      }
      for (final TransitionTriple transition : tnfa.availableTransitionsFor(s, null)) {
        final SortedSet<MapItem> kk = new TreeSet<>(k);
        final Tag tag = transition.getTag();

        if (!tag.equals(Tag.NONE)) {

          {
            final MapItem toBeRemoved = toBeRemoved(kk, tag);
            if (toBeRemoved != null) {
              k.remove(toBeRemoved);
            }
            final Set<MapItem> range = null;// getSame(closure,
            // tag);
            if (true) {
              throw new AssertionError("end of editing.");
            }
            final int x = minimumX(kk, tag, range);

            kk.add(new MapItem(tag, x));
          }
        }

        {
          final State u = transition.getState();
          final Triple fromElement = new Triple(u, Priority.NORMAL, null);
          final Triple toElement = new Triple(u, transition.getPriority(), null);
          final SortedSet<Triple> removeThem = closure.subSet(fromElement, toElement);
          closure.removeAll(removeThem);
        }

        {
          final State u = transition.getState();
          final Priority priority = transition.getPriority();
          final Triple t = new Triple(u, priority, Collections.unmodifiableSortedSet(kk));
          if (!closure.contains(t)) {
            closure.add(t);
            stack.push(t);
          }
        }

      }
    }
    return removePriorities(closure);
  }

  public TDFA convert() {
    final DFAState start = makeStartState();

    final List<Instruction> initializer = makeInitializer(start);

    final Deque<DFAState> unmarkedStates = new ArrayDeque<>();
    final NavigableSet<DFAState> states = new TreeSet<>();

    states.add(start);
    unmarkedStates.add(start);

    for (DFAState t; !unmarkedStates.isEmpty();) {
      t = unmarkedStates.pop();

      for (final InputRange inputRange : allInputRanges()) {
        final char a = inputRange.getFrom();

        final DFAState u = e(t.getData(), a);

        final BitSet newLocations = newMemoryLocations(t.getData(), u.getData());
        // TODO(niko): There's a smarter way. You can compute the stores on the fly.

        int[] mapping = new int[highestMapping];
        final DFAState mappedState = findMappableState(states, u, mapping);

        if (mappedState == null) {
          mapping = null;
        }

        final List<Instruction> c = new ArrayList<>();
        for (int i = newLocations.nextSetBit(0); i >= 0; i = newLocations.nextSetBit(i + 1)) {
          if (mapping != null) {
            c.add(instructionMaker.storePos(mapping[i]));
          } else {
            c.add(instructionMaker.storePos(i));
          }
        }

        DFAState newState;
        if (mappedState != null) {
          c.addAll(mappingInstructions(mapping, u, newLocations));
          newState = mappedState;
        } else {
          states.add(u);
          unmarkedStates.add(u);
          newState = u;
        }

        // Free up new slots that weren't really needed.
        if (mappedState != null) {
          highestMapping -= newLocations.cardinality();
        }

        assert newState != null;

        tdfaBuilder.addTransition(t, inputRange, newState, c);

        // final Entry<State, SortedSet<MapItem>> smallestFinishing =
        // smallestFinishing(newState);
        // TODO(niko): finishing stuff.
      }
    }
    return new TDFA(tdfaBuilder.build(), initializer);
  }

  /** Used to create the initial state of the DFA. */
  private DFAState convertToDfaState(final State s) {
    final Map<State, int[]> initState = new HashMap<>();
    final int numTags = tnfa.allTags().size();
    final int[] initialMemoryLocations = makeInitialMemoryLocations(numTags);
    initState.put(s, initialMemoryLocations);
    return new DFAState(Collections.unmodifiableMap(initState));
  }

  /**
   * Niko and Aaron's closure.
   * 
   * @return The next state after state, for input a. If a == null, return the start state.
   */
  DFAState e(final Map<State, int[]> state, final Character a) {
    final Map<State, int[]> R = new LinkedHashMap<>(); // Linked to simplify unit testing.

    final Deque<Map.Entry<State, int[]>> stack = new ArrayDeque<>(); // normal priority
    final Deque<Map.Entry<State, int[]>> lowStack = new ArrayDeque<>(); // low priority

    if (a == null) { // TODO(nikoschwarz): Beautify.
      stack.addAll(state.entrySet());
    } else {
      for (final Entry<State, int[]> pr : state.entrySet()) {
        final int[] k = pr.getValue();
        final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(pr.getKey(), a);
        for (final TransitionTriple t : ts) {
          switch (t.getPriority()) {
            case LOW:
              lowStack.add(new StateWithMemoryLocation(t.getState(), Arrays.copyOf(k, k.length)));
              break;
            case NORMAL: // Fall thru
            default:
              stack.add(new StateWithMemoryLocation(t.getState(), Arrays.copyOf(k, k.length)));
          }
        }
      }
    }

    if (lowStack.isEmpty() && stack.isEmpty()) {
      return DFAState.NO_STATE;
    }

    do {
      final Entry<State, int[]> s = stack.isEmpty() ? lowStack.pop() : stack.pop();
      assert s != null;

      if (R.containsKey(s.getKey())) {
        continue;
      }
      R.put(s.getKey(), s.getValue());

      final State q = s.getKey();
      final int[] l = s.getValue();

      nextTriple: for (final TransitionTriple triple : tnfa.availableTransitionsFor(q, null)) {
        final State qDash = triple.state;

        // Step 1.
        if ((R.containsKey(qDash) && triple.priority.equals(Priority.LOW)) || R.containsKey(qDash)) {
          continue nextTriple;
        }

        // Step 2.
        final Tag tau = triple.tag;
        int[] tdash;
        if (!tau.equals(Tag.NONE)) {
          final int pos = positionFor(tau);
          tdash = Arrays.copyOf(l, l.length);
          tdash[pos] = nextInt(); // TODO(niko): Produce store.
        } else {
          tdash = l;
        }

        // Step 3. (TODO)

        // Step 4.
        if (!R.containsKey(triple.getState())) {
          switch (triple.getPriority()) {
            case LOW:
              lowStack.add(new StateWithMemoryLocation(triple.getState(), tdash));
              break;
            case NORMAL: // fall thru
            default:
              stack.add(new StateWithMemoryLocation(triple.getState(), tdash));
          }
        }
      }
    } while (!(stack.isEmpty() && lowStack.isEmpty()));
    return new DFAState(R);
  }

  private BitSet extractLocs(final Map<State, int[]> oldState) {
    final BitSet ret = new BitSet();
    for (final int[] ary : oldState.values()) {
      for (final int val : ary) {
        if (val >= 0) { // TODO(nikoschwarz): Is this correct? Do we need negative indices or not?
          ret.set(val);
        }
      }
    }
    return ret;
  }

  DFAState findMappableState(NavigableSet<DFAState> states, DFAState u, int[] mapping) {
    final Map<State, int[]> fromElement = new LinkedHashMap<>(u.getData());
    {
      final int[] min = new int[0];
      for (final Entry<State, int[]> e : fromElement.entrySet()) {
        e.setValue(min);
      }
    }
    final Map<State, int[]> toElement = new LinkedHashMap<>(u.getData());
    {
      final int[] max = new int[highestMapping + 1];
      for (final Entry<State, int[]> e : toElement.entrySet()) {
        e.setValue(max);
      }
    }
    final NavigableSet<DFAState> range =
        states.subSet(new DFAState(fromElement), true, new DFAState(toElement), true);

    for (final DFAState candidate : range) {
      if (u.isMappable(candidate, mapping)) {
        return candidate;
      }
    }

    return null;
  }

  SortedSet<MapItem> getSame(final SortedSet<MapItem> k, final Tag tag) {
    final MapItem from = new MapItem(tag, 0);
    final MapItem to = new MapItem(new MarkerTag(tag.getGroup() + 1), 0);
    return k.subSet(from, to);
  }

  NavigableSet<Triple> initClosure(final Map<State, SortedSet<MapItem>> S) {
    final NavigableSet<Triple> closure = new TreeSet<>();
    for (final Entry<State, SortedSet<MapItem>> pr : S.entrySet()) {
      closure.add(new Triple(pr.getKey(), Priority.NORMAL, pr.getValue()));
    }
    return closure;
  }

  /** Return a mapping from an existing state to a mapped state, if one exists. Otherwise, null. */
  Collection<Instruction> isStateMappable(final Map<State, SortedSet<MapItem>> toBeMappedState,
      final Map<State, SortedSet<MapItem>> existingState) {
    final Map<MapItem, MapItem> mappings = mappingsForReuse(toBeMappedState, existingState);
    if (mappings == null) {
      return null;
    }
    final Collection<Instruction> ret = new ArrayList<>();
    for (final Entry<MapItem, MapItem> e : mappings.entrySet()) {
      instructionMaker.reorder(e.getKey(), e.getValue());
    }
    return Collections.unmodifiableCollection(ret);
  }

  private List<Instruction> makeInitializer(final DFAState start) {
    final List<Instruction> initializer = new ArrayList<>();
    final BitSet locs = extractLocs(start.getData());
    for (int i = locs.nextSetBit(0); i >= 0; i = locs.nextSetBit(i + 1)) {
      initializer.add(instructionMaker.storePos(i));
    }
    return initializer;
  }

  private int[] makeInitialMemoryLocations(final int numTags) {
    final int[] ret = new int[numTags * 2];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = -1 * i - 1;
    }
    return ret;
  }

  DFAState makeStartState() {
    DFAState start;
    {
      final State nfaStart = tnfa.getInitialState();
      start = convertToDfaState(nfaStart);
    }

    return e(start.getData(), null);
  }

  Collection<? extends Instruction> mappingInstructions(final int[] mapping, final DFAState to,
      BitSet newLocations) {
    final BitSet locs = extractLocs(to.getData());
    locs.andNot(newLocations); // New locations already led to stores.
    final List<Instruction> ret = new ArrayList<>();

    for (int i = locs.nextSetBit(0); i >= 0; i = locs.nextSetBit(i + 1)) {
      ret.add(instructionMaker.reorder(i, mapping[i]));
      // XXX Don't trust this.
    }

    return ret;
  }

  /** If there's a mapping that allows reuse, return it. Otherwise, return null. */
  private Map<MapItem, MapItem> mappingsForReuse(
      final Map<State, SortedSet<MapItem>> toBeMappedState,
      final Map<State, SortedSet<MapItem>> states) {
    final Map<MapItem, MapItem> mappings = new LinkedHashMap<>();
    for (final Entry<State, SortedSet<MapItem>> e : toBeMappedState.entrySet()) {
      final SortedSet<MapItem> toBeMappedItems = e.getValue();
      final SortedSet<MapItem> fixedItems = states.get(e.getKey());
      if (toBeMappedItems.size() != fixedItems.size()) {
        return null;
      }

      final Iterator<MapItem> toBeMapped = toBeMappedItems.iterator();
      final Iterator<MapItem> fixed = fixedItems.iterator();

      while (toBeMapped.hasNext()) {
        assert fixed.hasNext();

        final MapItem from = toBeMapped.next();
        final MapItem to = fixed.next();

        final MapItem currentlySetTo = mappings.get(from);
        if (currentlySetTo != null && !currentlySetTo.equals(to)) {
          return null;
        }
        mappings.put(from, to);
      }
      return mappings;

    }
    return Collections.unmodifiableMap(mappings);
  }

  int minimumX(final SortedSet<MapItem> k, final Tag tag, final Set<MapItem> range) {
    int last = -1;
    int x = -1;
    if (range.isEmpty()) {
      return 0;
    }
    for (final MapItem mi : range) { // XXX I think there's

      // an easier way.
      if (last + 1 < mi.getPos()) {
        x = last + 1;
        break;
      }
      last = mi.getPos();
    }
    if (x == -1) {
      x = last + 1;
    }
    assert !k.contains(new MapItem(tag, x));
    return x;
  }

  BitSet newMemoryLocations(final Map<State, int[]> oldState, final Map<State, int[]> newState) {
    final BitSet oldLocs = extractLocs(oldState);
    final BitSet newLocs = extractLocs(newState);
    newLocs.andNot(oldLocs);
    return newLocs;
  }

  int nextInt() {
    // XXX One could plug more logic into this method, to eliminate
    // post-processing.
    return highestMapping++;
  }

  private int positionFor(final Tag tau) {
    int r = 2 * tau.getGroup();
    if (tau.isEndTag()) {
      r += 1;
    }
    return r;
  }

  Map<State, int[]> reachable(final Map<State, int[]> state, final char a) {
    final Map<State, int[]> ret = new LinkedHashMap<>();

    for (final Entry<State, int[]> pr : state.entrySet()) {
      final int[] k = pr.getValue();

      final Collection<TransitionTriple> ts = tnfa.availableTransitionsFor(pr.getKey(), a);
      for (final TransitionTriple t : ts) {
        assert t.getPriority().equals(Priority.NORMAL);
        ret.put(t.getState(), Arrays.copyOf(k, k.length));
      }
    }
    return Collections.unmodifiableMap(ret);
  }

  Map<State, SortedSet<MapItem>> removePriorities(final NavigableSet<Triple> closure) {
    final Map<State, SortedSet<MapItem>> ret = new LinkedHashMap<>(closure.size());
    for (final Triple t : closure) {
      ret.put(t.state, t.mapItems);
    }
    return Collections.unmodifiableMap(ret);
  }

  Entry<State, SortedSet<MapItem>> smallestFinishing(Map<State, SortedSet<MapItem>> newState) {

    final List<Entry<State, SortedSet<MapItem>>> finishing = new ArrayList<>();
    for (final Entry<State, SortedSet<MapItem>> ss : newState.entrySet()) {
      if (tnfa.isAccepting(ss.getKey())) {
        finishing.add(ss);
      }
    }
    if (finishing.isEmpty()) {
      return null;
    }
    Collections.sort(finishing, new Comparator<Entry<State, SortedSet<MapItem>>>() {
      public int compare(final Entry<State, SortedSet<MapItem>> o1,
          final Entry<State, SortedSet<MapItem>> o2) {
        return Integer.compare(o1.getValue().size(), o2.getValue().size());
      }
    });

    return finishing.get(0);
  }

  MapItem toBeRemoved(final SortedSet<MapItem> k, final Tag tag) {
    final SortedSet<MapItem> sames = getSame(k, tag);
    if (sames.isEmpty()) {
      return null;
    }
    return sames.last();
  }
}


class Triple implements Comparable<Triple>, Priorizable {
  public final SortedSet<MapItem> mapItems;
  public final Priority priority;
  public final State state;

  public Triple(final State state, final Priority priority, final SortedSet<MapItem> mapItems) {
    this.state = state;
    this.priority = priority;
    this.mapItems = mapItems;
  }

  public int compareTo(final Triple o) {
    final int cmp = this.state.compareTo(o.state);
    if (cmp != 0) {
      return cmp;
    }
    return priority.compareTo(o.priority);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Triple other = (Triple) obj;
    if (mapItems == null) {
      if (other.mapItems != null) {
        return false;
      }
    } else if (!mapItems.equals(other.mapItems)) {
      return false;
    }
    if (priority != other.priority) {
      return false;
    }
    if (state == null) {
      if (other.state != null) {
        return false;
      }
    } else if (!state.equals(other.state)) {
      return false;
    }
    return true;
  }

  @Override
  public Enum<?> getPriority() {
    return priority;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mapItems == null) ? 0 : mapItems.hashCode());
    result = prime * result + ((priority == null) ? 0 : priority.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "[" + mapItems + ", " + priority + ", " + state + "]";
  }
}
