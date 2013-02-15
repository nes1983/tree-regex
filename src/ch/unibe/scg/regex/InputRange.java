package ch.unibe.scg.regex;

/**
 * {@link InputRange} represent a range of {@link Character} which can be used in
 * {@link TransitionTable} of {@link TDFA}.
 * 
 * @author Fabien Dubosson
 */
abstract class InputRange implements Comparable<InputRange> {
  private static class Any extends SpecialInputRange {
    @Override
    public String toString() {
      return "ANY";
    }
  }

  private static class Eos extends SpecialInputRange {
    @Override
    public String toString() {
      return "$";
    }
  }

  private static class Epsilon extends SpecialInputRange {
    @Override
    public String toString() {
      return "ε";
    }
  }

  static class SpecialInputRange extends InputRange {
    @Override
    public boolean contains(char character) {
      return false;
    }

    @Override
    public char getFrom() {
      return Character.MIN_VALUE;
    }

    @Override
    public char getTo() {
      return Character.MIN_VALUE;
    }
  }

  public static final InputRange ANY = new Any();

  public static final InputRange EOS = new Eos();

  public static final InputRange EPSILON = new Epsilon();

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
      return "" + from + "-" + to;
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
    return Character.compare(getFrom(), o.getFrom());
  };

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
}
