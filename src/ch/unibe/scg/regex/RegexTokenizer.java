package ch.unibe.scg.regex;

import static ch.unibe.scg.regex.RegexTokenizer.TokenType.GREEDY_SPEC_CHAR;
import static ch.unibe.scg.regex.RegexTokenizer.TokenType.NONGREEDY_SPEC_CHAR;
import static ch.unibe.scg.regex.RegexTokenizer.TokenType.OPEN_BRACE;
import static ch.unibe.scg.regex.RegexTokenizer.TokenType.ORD_CHAR;
import static ch.unibe.scg.regex.RegexTokenizer.TokenType.QUOTED_CHAR;
import static ch.unibe.scg.regex.RegexTokenizer.TokenType.SPEC_CHAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;

final class RegexTokenizer {
  final List<String> SUPPORTED_CHAR_RANGES = Collections.unmodifiableList(Arrays.asList(":alnum:",
    ":cntrl:", ":lower:", ":space:", ":alpha:", ":digit:", ":print:", ":upper:", ":blank:",
    ":graph:", ":punct:", ":xdigit:"));
  static enum TokenType {
    ORD_CHAR, QUOTED_CHAR, SPEC_CHAR, GREEDY_SPEC_CHAR, NONGREEDY_SPEC_CHAR, OPEN_BRACE, CLOSE_BRACE, DUP_COUNT, DUP_COMMA, NONMATCHING_BRACKET, OPEN_BRACKET, START_RANGE, RANGE_HYPHEN, END_RANGE, COLL_ELEMN_SINGLE, CLOSE_BRACKET;
  }
  static class Token {
    final TokenType type;
    final int begin;
    final int end;
    final String input;

    Token(final TokenType type, final int begin, final int end, final String input) {
      this.type = type;
      this.begin = begin;
      this.end = end;
      this.input = input;
    }

    @Override
    public String toString() {
      return String.format("%s[%s]", this.type, input.substring(begin, end));
    }
  }

  static class StatefulTokenizer {

    int index = 0;
    final String regex;
    final List<Token> tokens = new ArrayList<>();

    StatefulTokenizer(final String regex) {
      this.regex = regex;
    }

    boolean hasNext() {
      return index < regex.length();
    }

    // All subtokenizer methods need to set index to next char to consume.
    void tokenize() {
      while (hasNext()) {
        final int readAt = index;
        final char c = regex.charAt(index);
        if (c == '\\') {
          parseQuotedChar();
        } else if (c == '{'){
          parseDup();
        } else if (c == '[') {
          parseBracketExpression();
        } else if ("^.$()|\\".indexOf(c) >= 0) {
          parseSpecChar();
        } else if ("+*?".indexOf(c) >= 0) {
          parseGreedySpecChar();
        } else {
          tokens.add(new Token(ORD_CHAR, readAt, readAt + 1, regex));
          index++;
        }
      }
    }

    private void parseBracketExpression() {
      assert regex.charAt(index) == '[';
      final int begin = index;
      index++;
      if (!hasNext()) {
        die("Unmatched [, bracket expression unclosed");
      }
      final char c = regex.charAt(index);
      if (c == '.') {
        parseCollatingSymbol();
        return;
      } else if (c == '=') {
        parseEquivalenceClass();
        return;
      }

      // []
      if (c == '^') {
        index++;
        tokens.add(new Token(TokenType.NONMATCHING_BRACKET, begin, index, regex));
      } else {
        tokens.add(new Token(TokenType.OPEN_BRACKET, begin, index, regex));
      }

      while (true) {
        if (index+1 >= regex.length()) {
          die("Unmatched [");
        }
        if (isRange()) {
          parseRange();
          continue;
        }
        // [:alpha:0-5]
        if (regex.charAt(index) == ':') {

        }
        tokens.add(new Token(TokenType.COLL_ELEMN_SINGLE, index, index+1, regex));
        index++;
        if (regex.charAt(index) == ']') {
          tokens.add(new Token(TokenType.CLOSE_BRACKET, index, index+1, regex));
          index++;
          return;
        }
      }
      // on exit, index points after ]
    }

    private void parseRange() {
      tokens.add(new Token(TokenType.START_RANGE, index, index+1, regex));
      index++;
      tokens.add(new Token(TokenType.RANGE_HYPHEN, index, index+1, regex));
      index++;
      tokens.add(new Token(TokenType.END_RANGE, index, index+1, regex));
      index++;
    }

    private boolean isRange() {
      return (index+1) < regex.length() && regex.charAt(index+1) == '-' && (index+2) < regex.length() && regex.charAt(index+2) != ']';
    }

    private void parseEquivalenceClass() {
      throw new UnsupportedOperationException("Equivalence classes are not implemented");
    }

    private void parseCollatingSymbol() {
      throw new UnsupportedOperationException("Collating symbols are not implemented");
    }

    private void parseDup() {
      assert regex.charAt(index) == '{';
      tokens.add(new Token(OPEN_BRACE, index, index+1, regex));
      index++;
      int begin = index;
      // index is char currently read.
      for (; true; index++) {
        if (!hasNext()) {
          die("Missing }");
        }
        final char c = regex.charAt(index);

        if (Character.isDigit(c)) {
          continue;
        } else if (",}".indexOf(c) >= 0) {
          if (index <= begin) {
            die("Missing dup count.");
          }
          tokens.add(new Token(TokenType.DUP_COUNT, begin, index, regex));
          if (c == ',') {
            tokens.add(new Token(TokenType.DUP_COMMA, index, index+1, regex));
            begin = index+1;
          } else if (c == '}') {
            tokens.add(new Token(TokenType.CLOSE_BRACE, index, index+1, regex));
            return;
          }
        } else {
          die(String.format("Expected digit, curly, or comma, got %s", c));
        }
      }
    }

    private void parseGreedySpecChar() {
      final int begin = index;
      index++;
      if (hasNext() && regex.charAt(index) == '?') {
        index++;
        tokens.add(new Token(NONGREEDY_SPEC_CHAR, begin, index, regex));
      } else {
        tokens.add(new Token(GREEDY_SPEC_CHAR,  begin, index, regex));
      }
    }

    private void parseSpecChar() {
      tokens.add(new Token(SPEC_CHAR,  index, index+1, regex));
      index++;
    }

    private void parseQuotedChar() {
      assert regex.charAt(index) == '\\';
      index++;
      final char c = regex.charAt(index);
      if ("^.[$()|*+?{\\".indexOf(c) < 0) {
        die(String.format("Escaped unescapable character %s", c));
      }
      tokens.add(new Token(QUOTED_CHAR, index, index+1, regex));
      index++;
    }

    private void die(final String reason) {
      throw new PatternSyntaxException(reason, regex, index);
    }
  }

  List<Token> tokenize(final String regex) {
    final StatefulTokenizer tokenizer = new StatefulTokenizer(regex);
    tokenizer.tokenize();
    return tokenizer.tokens;
  }
}
