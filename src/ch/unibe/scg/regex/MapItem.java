package ch.unibe.scg.regex;

@Deprecated
class MapItem implements Comparable<MapItem> {
  private final int pos;
  private final Tag tag;

  public MapItem(final Tag tag, final int pos) {
    super();
    this.tag = tag;
    this.pos = pos;
  }

  public int compareTo(final MapItem o) {
    final int comp = this.tag.compareTo(o.tag);
    if (comp != 0) {
      return comp;
    }
    return Integer.compare(pos, o.pos);
  }

  public int getPos() {
    return pos;
  }

  public Tag getTag() {
    return tag;
  }

  @Override
  public String toString() {
    return "MapItem[" + pos + ", " + tag + "]";
  }
}
