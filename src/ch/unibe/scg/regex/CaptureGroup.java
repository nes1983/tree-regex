package ch.unibe.scg.regex;

interface CaptureGroup {
  /**
   * Returns increasing capture groups, including their tags. The first returned capture group is 1.
   * 
   * <p>
   * Immutable.
   */
  static class CaptureGroupMaker {
    CaptureGroup last = make(0);

    /**
     * Don't instantiate directly. Use {@link CaptureGroupMaker} instead.
     */
    static class RealCaptureGroup implements CaptureGroup {
      Tag startTag, endTag;
      final int number;

      /** Call {@link RealCaptureGroup#make(int)} instead */
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

    RealCaptureGroup make(final int number) {
      final RealCaptureGroup cg = new RealCaptureGroup(number);
      cg.startTag = Tag.RealTag.makeStartTag(cg);
      cg.endTag = Tag.RealTag.makeEndTag(cg);
      return cg;
    }

    public synchronized CaptureGroup next() {
      last = make(last.getNumber() + 1);
      return last;
    }
  }

  public Tag getEndTag();

  public int getNumber();

  public Tag getStartTag();
}
