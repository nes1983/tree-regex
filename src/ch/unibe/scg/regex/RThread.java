package ch.unibe.scg.regex;

import java.util.Arrays;

class RThread {
  final State state;
  final History[] histories;

  RThread(State state, History[] histories) {
    this.state = state;
    this.histories = Arrays.copyOf(histories, histories.length);
  }

  @Override
  public String toString() {
    return String.format("(%s %s)", state, Arrays.toString(histories));
  }
}