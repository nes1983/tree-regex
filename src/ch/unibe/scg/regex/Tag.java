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
      return Integer.compare(getGroup().getNumber(), o.getGroup().getNumber());
    }
  }

  static class NoTag implements Tag {
    private static final CaptureGroup none;
    static {
      final RealCaptureGroup myNone = new RealCaptureGroup(-1, null);
      myNone.parent = myNone;
      none = myNone;
    }

    public int compareTo(final Tag o) {
      return Integer.compare(-1, o.getGroup().getNumber());
    }

    public CaptureGroup getGroup() {
      return none;
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
      return "NONE";
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
        return "➁" + getGroup().getNumber();
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
        return "➀" + getGroup().getNumber();
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
    public CaptureGroup getGroup() {
      return captureGroup;
    }

    public abstract boolean isEndTag();

    public abstract boolean isStartTag();
  }

  public final Tag NONE = new NoTag();

  public CaptureGroup getGroup();

  public boolean isEndTag();

  public boolean isStartTag();
}
