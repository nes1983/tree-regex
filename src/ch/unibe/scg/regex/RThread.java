package ch.unibe.scg.regex;

class RThread {
  final State state;
  final Arraylike histories;

  RThread(State state, Arraylike histories) {
    this.state = state;
    this.histories = histories;
  }

  @Override
  public String toString() {
    return String.format("(%s %s)", state, histories.toString());
  }
}