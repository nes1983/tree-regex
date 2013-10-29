package ch.unibe.scg.regex;

class CaptureGroup {
  Tag startTag, endTag;
  final int number;
  CaptureGroup parent;

  /** Call {@link RealCaptureGroup#make(int)} instead */
  CaptureGroup(final int number, CaptureGroup parent) {
    this.number = number;
    this.parent = parent;
  }

  /** Returns increasing capture groups, including their tags. The first returned capture group is 1. */
  static class CaptureGroupMaker {
    final CaptureGroup entireMatch;
    CaptureGroup last;

    CaptureGroupMaker() {
      final CaptureGroup castEntireMatch = make(0, null);
      castEntireMatch.parent = castEntireMatch;
      entireMatch = castEntireMatch;

      last = entireMatch;
    }

    private static CaptureGroup make(final int number, CaptureGroup parent) {
      final CaptureGroup cg = new CaptureGroup(number, parent);
      cg.startTag = Tag.RealTag.makeStartTag(cg);
      cg.endTag = Tag.RealTag.makeEndTag(cg);
      return cg;
    }

    public synchronized CaptureGroup next(CaptureGroup parent) {
      last = make(last.number + 1, parent);
      return last;
    }
  }

  @Override
  public String toString() {
    return "g" + number;
  }
}
