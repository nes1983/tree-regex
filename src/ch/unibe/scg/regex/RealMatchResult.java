package ch.unibe.scg.regex;

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

  final int[] captureGroupPositions;
  final CharSequence input;

  RealMatchResult(int[] captureGroupPositions, CharSequence input) {
    this.captureGroupPositions = captureGroupPositions;
    this.input = input;
  }

  public int end() {
    return end(0);
  }

  public int end(final int group) {
    return captureGroupPositions[group * 2 + 1];
  }

  public String group() {
    return group(0);
  }

  public String group(final int group) {
    return input.subSequence(start(group), end(group)).toString();
  }

  public int groupCount() {
    return captureGroupPositions.length / 2;
  }

  public int start() {
    return start(0);
  }

  public int start(final int group) {
    return captureGroupPositions[group * 2];
  }

  @Override
  public String toString() {
    return "" + start() + "-" + end();
  }
}
