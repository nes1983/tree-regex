package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

class RealMatchResult implements MatchResult {
  static enum NoMatchResult implements MatchResult {
    SINGLETON;

    public int end() {
      return -1;
    }

    public int end(final int group) {
      if (group == 0) {
        return end();
      }
      throw new NoSuchElementException();
    }

    public String group() {
      throw new NoSuchElementException();
    }

    public String group(final int group) {
      throw new NoSuchElementException();
    }

    public int groupCount() {
      return -1;
    }

    public int start() {
      return -1;
    }

    public int start(final int group) {
      throw new NoSuchElementException();
    }

    @Override
    public String toString() {
      return "NO_MATCH";
    }
  }

  List<Pair<Integer, Integer>> captureGroups = new ArrayList<>();

  public int end() {
    return end(0);
  }

  public int end(final int group) {
    return captureGroups.get(group).getSecond();
  }

  public String group() {
    throw null;
  }

  public String group(final int group) {
    throw null;
  }

  public int groupCount() {
    throw null;
  }

  public int start() {
    return start(0);
  }

  public int start(final int group) {
    return captureGroups.get(group).getFirst();
  }

  void takeCaptureGroup(final Tag tag, final int match) {
    assert tag != null;
    captureGroups.add(tag.getGroup(), new Pair<>(0, match));
  }

  @Override
  public String toString() {
    return "" + start() + "-" + end();
  }
}
