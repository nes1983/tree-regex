package ch.unibe.scg.regex;

interface IntIterable {
    public IntIterator iterator();

    static interface IntIterator {
      public boolean hasNext();

      public int next();
    }
}