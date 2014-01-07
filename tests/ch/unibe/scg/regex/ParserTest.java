package ch.unibe.scg.regex;

import java.util.regex.PatternSyntaxException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import ch.unibe.scg.regex.Node.Union;

@SuppressWarnings("javadoc")
public final class ParserTest {
  
  @SuppressWarnings("unchecked")
  private <A extends Node.Basic> A getFirst(String regex) {
    final Node.Regex r = RegexParser.parse(regex);
    assertThat(r, instanceOf(Node.Simple.class));
    final Node.Basic first = ((Node.Simple) RegexParser.parse(regex)).basics.get(0);
    return (A) first;
  }  
  
  @Test
  public void regexp4() {
    final Node.Regex r = RegexParser.parse("aaa");
    assertThat(r.toString(), is("aaa"));
  }

  @Test
  public void testBasic() {
    final Node.Basic s = getFirst(".*");
    assertThat(s, instanceOf(Node.Star.class));
    final Node.Star ss = (Node.Star) s;
    assertThat(ss.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".*"));
  }

  public void testWeirdParseRange() {
    final Node.Regex r = RegexParser.parse("[a--f]");
    assertThat(r.toString(), is("[a--f]"));
    final Node.Regex rr = RegexParser.parse("[a--]");
    assertThat(rr.toString(), is("[a--]"));
  }

  @Test
  public void testEscaped() {
    final Node.EscapedChar s = getFirst("\\(");
    assertThat(s.inputRange.getFrom(), is('('));
    assertThat(s.toString(), is("\\("));
  }

  @Test
  public void testGroup2() {
    final Node.Group u = getFirst("(aaa|bbb)");
    assertThat(u.toString(), is("(aaa|bbb)"));
  }

  @Test(expected = PatternSyntaxException.class)
  public void testNoSet() {
    RegexParser.parse("[a-fgk-zA-B][");
  }

  @Test
  public void testOptional1() {
    final Node.Optional s = getFirst("[a-fgk-zA-B]?");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]?"));
  }

  @Test
  public void testOptional2() {
    final Node.Optional s = getFirst(".?");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".?"));
  }

  @Test
  public void testParseRange() {
    final Node.Regex r = RegexParser.parse("[a-f]");
    assertThat(r.toString(), is("[a-f]"));
  }
  
  @Test
  public void testParseRangeEscape() {
    final Node.Regex r = RegexParser.parse("[a\\-]");
    assertThat(r.toString(), is("[a\\-]"));
  }

  @Test
  public void testPlus1() {
    final Node.Plus s = getFirst("[a-fgk-zA-B]+");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]+"));
  }

  @Test
  public void testNegativeSet() {
    final Node.Plus s = getFirst("[^a-fgk-zA-B]+");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[^a-fgk-zA-B]+"));
  }

  @Test
  public void testPlus2() {
    final Node.Plus s = getFirst(".+");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".+"));
  }

  @Test
  public void testRegexp() {
    final Node.Regex rr = RegexParser.parse("aaa|bbb");
    assertThat(rr.toString(), is("aaa|bbb"));
  }

  @Test
  public void testSet() {
    final Node.Set s = getFirst("[a-fgk-zA-B]");

    assertThat(s.toString(), is("[a-fgk-zA-B]"));
  }
  

  @Test
  public void testStar1() {
    final Node.Star s = getFirst("[a-fgk-zA-B]*");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]*"));
  }

  @Test
  public void testStar2() {
    final Node.Star s = getFirst(".*");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".*"));
  }

  @Test
  public void testUnion() {
    final Node.Union u = (Union) RegexParser.parse("[a-fgk-zA-B]*|aaa");
    assertThat(u.toString(), is("[a-fgk-zA-B]*|aaa"));
    assertThat(u.left.getClass(), is((Object) Node.Simple.class));
    assertThat(u.right, instanceOf(Node.Simple.class));
  }

  @Test
  public void testUnion2() {
    final Node.Union u = (Node.Union) RegexParser.parse("[a-fgk-zA-B]*|(aaa|bbb)");
    assertThat(u.toString(), is("[a-fgk-zA-B]*|(aaa|bbb)"));
    assertThat(u.left, instanceOf(Node.Simple.class));
    assertThat(u.right, instanceOf(Node.Simple.class));
  }

  @Test
  public void testUnion3() {
    final Node.Union u = (Node.Union) RegexParser.parse("bbb|aaa");
    assertThat(u.toString(), is("bbb|aaa"));
  }

  @Test
  public void unionBig2() {
    final Node.Regex rr = RegexParser.parse("aaa|(bbb)?");
    assertThat(rr, instanceOf(Node.Union.class));
    final Node.Union u = (Node.Union) rr;
    final Node.Regex right = u.right;
    assertThat(right, instanceOf(Node.Simple.class));
    final Node.Simple simple = (Node.Simple) right;
    final Node.Basic optional = simple.basics.get(0);
    assertThat(optional, instanceOf(Node.Optional.class));
    assertThat(rr.toString(), is("aaa|(bbb)?"));
  }
  
  @Ignore("Is not currently supported. Only stupid people would do it anyway.")
  @Test
  public void nestedPlus() {
    final Node.Regex rr = RegexParser.parse("a++");
    assertThat(rr, instanceOf(Node.Plus.class));
  }
}
