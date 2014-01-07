package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

final class RegexTokenizer {
  static class Token {
    final int index;
    
    public Token(int i) {
      index= i;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o.getClass() != this.getClass()) return false;
      Token t = (Token) o;
      if (t.index != index) return false;
      return true;
    }
    
    @Override
    public int hashCode() {
      return this.getClass().hashCode();
    }
    
    @Override
    public String toString() {
      return String.format("%s@%d", this.getClass().getSimpleName(), index);
    }
  }
  static final class AnyToken extends Token {
    public AnyToken(int i) {
      super(i);
    }
  }
  static final class OpenGroup extends Token {
    public OpenGroup(int i) {
      super(i);
    }
  }
  static final class CloseGroup extends Token {
    public CloseGroup(int i) {
      super(i);
    }
  }
  static final class OpenRange extends Token {
    public OpenRange(int i) {
      super(i);
    }
  }
  static final class OpenNegativeRange extends Token {
    public OpenNegativeRange(int i) {
      super(i);
    }
  }
  static final class CloseRange extends Token {
    public CloseRange(int i) {
      super(i);
    }
  }
  static final class HyphenToken extends Token {
    public HyphenToken(int i) {
      super(i);
    }
  }
  static final class OrToken extends Token {
    public OrToken(int i) {
      super(i);
    }
  }
  static final class PlusToken extends Token {
    public PlusToken(int i) {
      super(i);
    }
  }
  static final class NonGreedyPlusToken extends Token {
    public NonGreedyPlusToken(int i) {
      super(i);
    }
  }
  static final class StarToken extends Token {
    public StarToken(int i) {
      super(i);
    }
  }
  static final class NonGreedyStarToken extends Token {
    public NonGreedyStarToken(int i) {
      super(i);
    }
  }
  static final class QuestionToken extends Token {
    public QuestionToken(int i) {
      super(i);
    }
  }
  static final class NonGreedyQuestionToken extends Token {
    public NonGreedyQuestionToken(int i) {
      super(i);
    }
  }
  abstract static class CharTokenS extends Token {
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + c;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!super.equals(obj)) return false;
      if (getClass() != obj.getClass()) return false;
      CharTokenS other = (CharTokenS) obj;
      if (c != other.c) return false;
      return true;
    }

    final char c;

    CharTokenS(int i, final char c) {
      super(i);
      this.c = c;
    }
    
    @Override
    public String  toString() {
      return super.toString() + "('" + c + "')";
    }
  }
  
  final static class CharToken extends CharTokenS {
    CharToken(int i, char c) {
      super(i, c);
    }
  }
  final static class EscapedCharToken extends CharTokenS {
    EscapedCharToken(int i, char c) {
      super(i, c);
    }
  }

  int index = 0;
  final String regex;

  RegexTokenizer(final String regex) {
    this.regex = regex;
  }

  boolean hasNext() {
    return index < regex.length();
  }

  List<Token> tokenize() {
    List<Token> tokens = new ArrayList<>();
    while (hasNext()) {
      final int readAt = index;
      final char c = regex.charAt(index++);
      switch (c) {
        case '\\':
          if (!hasNext()) {
            die("Expected escaped char");
          }
          final char escaped = regex.charAt(index++);
          tokens.add(new EscapedCharToken(readAt, escaped));
          break;
        case '.':
          tokens.add(new AnyToken(readAt));
          break;
        case '(':
          tokens.add(new OpenGroup(readAt));
          break;
        case ')':
          tokens.add(new CloseGroup(readAt));
          break;
        case '[':
          if (!hasNext()) {
            die("Range ended abruptly");
          }
          final char first = regex.charAt(index);
          if (first == '^') {
            index++;
            tokens.add(new OpenNegativeRange(readAt));
          } else {
            tokens.add(new OpenRange(readAt));
          }
          break;
        case ']':
          tokens.add(new CloseRange(readAt));
          break;
        case '|':
          tokens.add(new OrToken(readAt));
          break;
        case '+':
          if (hasNext() && regex.charAt(index) == '?') {
            index++;
            tokens.add(new NonGreedyPlusToken(readAt));
          } else {
            tokens.add(new PlusToken(readAt));
          }
          break;
        case '*':
          if (hasNext() && regex.charAt(index) == '?') {
            index++;
            tokens.add(new NonGreedyStarToken(readAt));
          } else {
            tokens.add(new StarToken(readAt));
          }
          break;
        case '?':
          if (hasNext() && regex.charAt(index) == '?') {
            index++;
            tokens.add(new NonGreedyQuestionToken(readAt));
          } else {
            tokens.add(new QuestionToken(readAt));
          }
          break;
        case '-':
          tokens.add(new HyphenToken(readAt));
          break;
        default:
          tokens.add(new CharToken(readAt, c));
      }
    }
    return tokens;
  }

  private void die(String reason) {
    throw new PatternSyntaxException(reason, regex, index);
  }
}
