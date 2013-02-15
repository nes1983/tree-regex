package ch.unibe.scg.regex;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker.RealCaptureGroup;


/**
 * Get one from {@link CaptureGroupMaker}.
 * 
 * @author nes
 * 
 */
interface Tag extends Comparable<Tag> {
  static abstract class AbstractTag implements Tag {
    @Override
    public int compareTo(final Tag o) {
      return Integer.valueOf(getGroup()).compareTo(o.getGroup());
    }
  }

  static class MarkerTag implements Tag {
    final int group;
    final String name;

    MarkerTag(final int group) {
      this.name = "MarkerTag";
      this.group = group;
    }

    MarkerTag(final String name) {
      this.name = name;
      this.group = 0;
    }

    public int compareTo(final Tag o) {
      return getGroup() - o.getGroup();
    }

    public int getGroup() {
      return group;
    }

    @Override
    public boolean isEndTag() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStartTag() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return name;
    }

  }

  /**
   * Don't instantiate directly. Leave it to {@link CaptureGroupMaker}.
   */
  static abstract class RealTag extends AbstractTag {
    static class EndTag extends RealTag {
      private EndTag(final CaptureGroup captureGroup) {
        super(captureGroup);
      }

      @Override
      public boolean isEndTag() {
        return true;
      }

      @Override
      public boolean isStartTag() {
        return false;
      }

      @Override
      public String toString() {
        return "➁" + getGroup();
      }
    }

    static class StartTag extends RealTag {
      /** Call {@link #makeStartTag(RealCaptureGroup)} instead. */
      private StartTag(final CaptureGroup captureGroup) {
        super(captureGroup);
      }

      @Override
      public boolean isEndTag() {
        return false;
      }

      @Override
      public boolean isStartTag() {
        return true;
      }

      @Override
      public String toString() {
        return "➀" + getGroup();
      }
    }

    public static Tag makeEndTag(final RealCaptureGroup cg) {
      return new EndTag(cg);
    }

    public static Tag makeStartTag(final RealCaptureGroup cg) {
      return new StartTag(cg);
    }

    final CaptureGroup captureGroup;

    RealTag(final CaptureGroup captureGroup) {
      super();
      this.captureGroup = captureGroup;
    }

    @Override
    public int getGroup() {
      return captureGroup.getNumber();
    }

    public abstract boolean isEndTag();

    public abstract boolean isStartTag();
  }

  public final Tag ENTIRE_MATCH = new MarkerTag("ENTIRE_MATCH");

  public final Tag NONE = new MarkerTag("NONE");

  public int getGroup();

  public boolean isEndTag();

  public boolean isStartTag();
}
