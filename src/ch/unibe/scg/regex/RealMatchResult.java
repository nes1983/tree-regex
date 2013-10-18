package ch.unibe.scg.regex;

import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

class RealMatchResult implements MatchResult {
  static enum NoMatchResult implements MatchResult {
    SINGLETON;

    @Override
    public int end() {
      return -1;
    }

    @Override
    public int end(final int group) {
      if (group == 0) {
        return end();
      }
      throw new NoSuchElementException();
    }

    @Override
    public String group() {
      throw new NoSuchElementException();
    }

    @Override
    public String group(final int group) {
      throw new NoSuchElementException();
    }

    @Override
    public int groupCount() {
      return -1;
    }

    @Override
    public int start() {
      return -1;
    }

    @Override
    public int start(final int group) {
      throw new NoSuchElementException();
    }

    @Override
    public String toString() {
      return "NO_MATCH";
    }
  }

  final History[] captureGroupPositions;
  final CharSequence input;

  RealMatchResult(History[] fin, CharSequence input) {
    this.captureGroupPositions = fin;
    this.input = input;
  }

  @Override
  public int end() {
    return end(0);
  }

  @Override
  public int end(final int group) {
    return captureGroupPositions[group * 2 + 1].iterator().next();
  }

  @Override
  public String group() {
    return group(0);
  }

  @Override
  public String group(final int group) {
    return input.subSequence(start(group), end(group)).toString();
  }

  @Override
  public int groupCount() {
    return captureGroupPositions.length / 2;
  }

  @Override
  public int start() {
    return start(0);
  }

  @Override
  public int start(final int group) {
    return captureGroupPositions[group * 2].iterator().next();
  }

  @Override
  public String toString() {
    return "" + start() + "-" + end();
  }
}
