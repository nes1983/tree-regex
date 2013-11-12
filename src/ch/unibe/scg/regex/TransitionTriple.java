package ch.unibe.scg.regex;

import java.util.Objects;

class TransitionTriple {
  static enum Priority {
    LOW, NORMAL;
  }

  final Priority priority;
  final State state;
  final Tag tag;

  public TransitionTriple(final State state, final Priority priority, final Tag tag) {
    this.state = state;
    this.priority = priority;
    this.tag = tag;
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
    final TransitionTriple other = (TransitionTriple) obj;
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
    if (tag == null) {
      if (other.tag != null) {
        return false;
      }
    } else if (!tag.equals(other.tag)) {
      return false;
    }
    return true;
  }

  public Priority getPriority() {
    return priority;
  }

  public State getState() {
    return state;
  }

  public Tag getTag() {
    return tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(priority, state, tag);
  }

  @Override
  public String toString() {
    return "" + getState() + ", " + getPriority() + ", " + getTag();
  }
}
