package ch.unibe.scg.regex;

interface CaptureGroup {
  /**
   * Returns increasing capture groups, including their tags. The first returned capture group is 1.
   */
  static class CaptureGroupMaker {
    /**
     * Don't instantiate directly. Use {@link CaptureGroupMaker} instead.
     */
    static class RealCaptureGroup implements CaptureGroup {
      static RealCaptureGroup make(final int number) {
        final RealCaptureGroup cg = new RealCaptureGroup(number);
        cg.startTag = Tag.RealTag.makeStartTag(cg);
        cg.endTag = Tag.RealTag.makeEndTag(cg);
        return cg;
      }

      Tag endTag;
      final int number;

      Tag startTag;

      private RealCaptureGroup(final int number) {
        this.number = number;
      }

      public Tag getEndTag() {
        assert endTag != null;
        return endTag;
      }

      public int getNumber() {
        return number;
      }

      public Tag getStartTag() {
        assert startTag != null;
        return startTag;
      }

      @Override
      public String toString() {
        return "g" + number;
      }
    }

    CaptureGroup last = new RealCaptureGroup(0);

    synchronized CaptureGroup next() {
      last = RealCaptureGroup.make(last.getNumber() + 1);
      return last;
    }
  }

  public Tag getEndTag();

  public int getNumber();

  public Tag getStartTag();
}
