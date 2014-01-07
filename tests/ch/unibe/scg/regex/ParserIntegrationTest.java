package ch.unibe.scg.regex;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import ch.unibe.scg.regex.Node.Regex;

public class ParserIntegrationTest {

  @Test
  public void testDot() {
    final Regex r = RegexParser.parse(".");
    assertThat(r.toString(), is("."));
  }
  
  @Test
  public void testGroup() {
    final Regex r = RegexParser.parse("[a]");
    assertThat(r.toString(), is("[a]"));
  }
  
  @Test
  public void testGroupRange() {
    final Regex r = RegexParser.parse("[a-z]");
    assertThat(r.toString(), is("[a-z]"));
  }
  
  @Test
  public void testGroupRangePlus() {
    final Regex r = RegexParser.parse("[a-z]+");
    assertThat(r.toString(), is("[a-z]+"));
  }
  
  @Test
  public void testGroupRanges() {
    final Regex r = RegexParser.parse("[a-zA-Z]");
    assertThat(r.toString(), is("[a-zA-Z]"));
  }
  
  @Test
  public void testGreedies() {
    final Regex r = RegexParser.parse("a?a+a*");
    assertThat(r.toString(), is("a?a+a*"));
  }
  
  @Test
  public void testNonGreedies() {
    final Regex r = RegexParser.parse("a+?a*?");
    assertThat(r.toString(), is("aa*?a*?"));
  }
  
  @Test
  public void testCaptureGroup() {
    final Regex r = RegexParser.parse("(a)");
    assertThat(r.toString(), is("(a)"));
  }
  
  @Test
  public void testOrGroup() {
    final Regex r = RegexParser.parse("(a|b)");
    assertThat(r.toString(), is("(a|b)"));
  }
  
  @Test
  public void testOr() {
    final Regex r = RegexParser.parse("a|b");
    assertThat(r.toString(), is("a|b"));
  }
  
  @Test
  public void testChainedOr() {
    final Regex r = RegexParser.parse("a|b|c|d");
    assertThat(r.toString(), is("a|b|c|d"));
  }
}
