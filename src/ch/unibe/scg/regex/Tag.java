package ch.unibe.scg.regex;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;


/**
 * Get one from {@link CaptureGroupMaker}.
 *
 * @author nes
 *
 */
interface Tag {
  static class NoTag implements Tag {
    private static final CaptureGroup none;
    static {
      final CaptureGroup myNone = new CaptureGroup(-1, null);
      myNone.parent = myNone;
      none = myNone;
    }

    @Override
    public CaptureGroup getGroup() {
      return none;
    }

    @Override
    public boolean isEndTag() {
      return false;
    }

    @Override
    public boolean isStartTag() {
      return false;
    }

    @Override
    public String toString() {
      return "NONE";
    }
  }

  /**
   * Don't instantiate directly. Leave it to {@link CaptureGroupMaker}.
   */
  static abstract class RealTag implements Tag {
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
        return "➁" + getGroup().number;
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
        return "➀" + getGroup().number;
      }
    }

    public static Tag makeEndTag(final CaptureGroup cg) {
      return new EndTag(cg);
    }

    public static Tag makeStartTag(final CaptureGroup cg) {
      return new StartTag(cg);
    }

    final CaptureGroup captureGroup;

    RealTag(final CaptureGroup captureGroup) {
      this.captureGroup = captureGroup;
    }

    @Override
    public CaptureGroup getGroup() {
      return captureGroup;
    }

    @Override
    public abstract boolean isEndTag();

    @Override
    public abstract boolean isStartTag();
  }

  public final Tag NONE = new NoTag();

  public CaptureGroup getGroup();

  public boolean isEndTag();

  public boolean isStartTag();
}
