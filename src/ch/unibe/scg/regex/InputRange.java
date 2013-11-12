package ch.unibe.scg.regex;

/**
 * {@link InputRange} represent a range of {@link Character} which can be used in
 * {@link TransitionTable} of TDFA.
 *
 * @author Fabien Dubosson
 */
abstract class InputRange implements Comparable<InputRange> {
  private static class Any extends RealInputRange {
    Any() {
      // Everything.
      super(Character.MIN_VALUE, Character.MAX_VALUE);
    }

    @Override
    public String toString() {
      return "ANY";
    }
  }

  private static class Eos extends RealInputRange {
    Eos() {
      // Nothing.
      super((char) (Character.MIN_VALUE + 1), Character.MIN_VALUE);
    }

    @Override
    public String toString() {
      return "$";
    }
  }

  static class SpecialInputRange extends RealInputRange {
    private SpecialInputRange(char from, char to) {
      super(from, to);
    }
  }

  public static final InputRange ANY = new Any();

  public static final InputRange EOS = new Eos();

  public static InputRange make(final char character) {
    return make(character, character);
  }

  public static InputRange make(final char from, final char to) {
    return new RealInputRange(from, to);
  }

  static class RealInputRange extends InputRange {
    /**
     * First {@link Character} of the range
     */
    private final char from;

    /**
     * Last {@link Character} or the range
     */
    private final char to;

    /**
     * Constructor which take the first and last character as parameter
     *
     * @param from The first {@link Character} of the range
     * @param to The last {@link Character} of the range
     */
    RealInputRange(final char from, final char to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean contains(final char character) {
      return (from <= character && character <= to);
    }

    @Override
    public String toString() {
      String printedFrom = Character.toString(from);
      if (!Character.isLetterOrDigit(from)) {
    	  printedFrom = String.format("0x%x", (int) from);
      }
      String printedTo = Character.toString(to);
      if (!Character.isLetterOrDigit(to)) {
    	  printedTo = String.format("0x%x", (int) to);
      }
      return String.format("%s-%s", printedFrom, printedTo);
    }

    @Override
    public char getFrom() {
      return from;
    }

    @Override
    public char getTo() {
      return to;
    }
  }

  @Override
  public int compareTo(InputRange o) {
    int cmp = Character.compare(getFrom(), o.getFrom());
    if (cmp != 0) {
      return cmp;
    }

    return Character.compare(getTo(), o.getTo());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InputRange)) {
      return false;
    }

    InputRange that = (InputRange) o;

    return this.getFrom() == that.getFrom() && this.getTo() == that.getTo();
  }

  /**
   * Tell if the {@link InputRange} contains a {@link Character} within its range
   *
   * @param character A specific {@link Character}.
   * @return if the {@link Character} is contained within the {@link InputRange}.
   */
  public abstract boolean contains(final char character);

  /**
   * Return the first {@link Character} of the range
   *
   * @return the first {@link Character} of the range
   */
  public abstract char getFrom();

  /**
   * Return the last {@link Character} of the range
   *
   * @return the last {@link Character} of the range
   */
  public abstract char getTo();

  @Override
  public int hashCode() {
    return (getFrom() * 31) ^ getTo();
  }
}
