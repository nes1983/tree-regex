package ch.unibe.scg.regex;

import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker;
import ch.unibe.scg.regex.CaptureGroup.CaptureGroupMaker.RealCaptureGroup;


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
      final RealCaptureGroup myNone = new RealCaptureGroup(-1, null);
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
