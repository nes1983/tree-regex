package ch.unibe.scg.regex;


final class Transition {
  final Priority priority;
  final State state;
  final Tag tag;

  Transition(final State state, final Priority priority, final Tag tag) {
    this.state = state;
    this.priority = priority;
    this.tag = tag;
  }

  static enum Priority {
    LOW, NORMAL;
  }

  @Override
  public String toString() {
    return String.format("%s, %s, %s", state, priority, tag);
  }
}
