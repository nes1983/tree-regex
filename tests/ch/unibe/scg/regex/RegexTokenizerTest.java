package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ch.unibe.scg.regex.RegexTokenizer;
import ch.unibe.scg.regex.RegexTokenizer.Token;
import ch.unibe.scg.regex.RegexTokenizer.CharToken;
import ch.unibe.scg.regex.RegexTokenizer.EscapedCharToken;
import ch.unibe.scg.regex.RegexTokenizer.NonGreedyStarToken;
import ch.unibe.scg.regex.RegexTokenizer.PlusToken;
import ch.unibe.scg.regex.RegexTokenizer.OrToken;
import ch.unibe.scg.regex.RegexTokenizer.AnyToken;

public class RegexTokenizerTest {
  List<Token> tokenize(String s) {
    return new RegexTokenizer(s).tokenize();
  }

  @Test
  public void testChar() {
    assertThat(Arrays.asList((Token) new CharToken(0, 'c')), is(tokenize("c")));
  }

  
  @Test
  public void testDot() {
    assertThat(Arrays.asList((Token) new AnyToken(0)), is(tokenize(".")));
  }
  
  @Test
  public void testEcapedChar() {
    assertThat(tokenize("\\|"), is(Arrays.asList((Token) new EscapedCharToken(0, '|'))));
  }

  @Test
  public void testComplex() {
    assertThat(Arrays.asList(
      new EscapedCharToken(0, '|'), new NonGreedyStarToken(2), 
      new CharToken(4, 'a'), new PlusToken(5), 
      new OrToken(6), new CharToken(7, 'b')).toString(), is(tokenize("\\|*?a+|b").toString()));
  }
}
