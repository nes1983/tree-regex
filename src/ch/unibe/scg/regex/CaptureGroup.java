package ch.unibe.scg.regex;

interface CaptureGroup {
  public Tag getEndTag();

  public int getNumber();

  public Tag getStartTag();
}
