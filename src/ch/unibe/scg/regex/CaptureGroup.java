package ch.unibe.scg.regex;

interface CaptureGroup {
  /**
   * Returns increasing capture groups, including their tags. The first returned capture group is 1.
   *
   * <p>
   * Immutable.
   */

  static class CaptureGroupMaker {
    final CaptureGroup entireMatch;
    CaptureGroup last;

    CaptureGroupMaker() {
      final RealCaptureGroup castEntireMatch = make(0, null);
      castEntireMatch.parent = castEntireMatch;
      entireMatch = castEntireMatch;

      last = entireMatch;
    }

    /**
     * Don't instantiate directly. Use {@link CaptureGroupMaker} instead.
     */
    static class RealCaptureGroup implements CaptureGroup {
      Tag startTag, endTag;
      final int number;
      CaptureGroup parent;

      /** Call {@link RealCaptureGroup#make(int)} instead */
      RealCaptureGroup(final int number, CaptureGroup parent) {
        this.number = number;
        this.parent = parent;
      }

      public CaptureGroup getParent() {
        return parent;
      }

      @Override
      public Tag getEndTag() {
        assert endTag != null;
        return endTag;
      }

      @Override
      public int getNumber() {
        return number;
      }

      @Override
      public Tag getStartTag() {
        assert startTag != null;
        return startTag;
      }

      @Override
      public String toString() {
        return "g" + number;
      }
    }

    private static RealCaptureGroup make(final int number, CaptureGroup parent) {
      final RealCaptureGroup cg = new RealCaptureGroup(number, parent);
      cg.startTag = Tag.RealTag.makeStartTag(cg);
      cg.endTag = Tag.RealTag.makeEndTag(cg);
      return cg;
    }

    public synchronized CaptureGroup next(CaptureGroup parent) {
      last = make(last.getNumber() + 1, parent);
      return last;
    }
  }

  public Tag getEndTag();

  public int getNumber();

  public Tag getStartTag();
}
