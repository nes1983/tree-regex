package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node;

@SuppressWarnings("javadoc")
public final class ParserTest {
  public static void main(final String... arg) {
    final ParserTest t = new ParserTest();
    t.setUp();
    t.testUnion2();
  }

  private ParserProvider pp;

  @Test
  public void regexp4() {
    final Parser<Node.Regex> s = pp.regexp();
    final Node.Regex r = s.parse("aaa");
    assertThat(r.toString(), is("aaa"));
  }

  @Before
  public void setUp() {
    pp = new ParserProvider();
  }

  @Test
  public void testBasic() {
    final Node.Basic s = pp.basic().parse(".*");
    assertThat(s, instanceOf(Node.Star.class));
    final Node.Star ss = (Node.Star) s;
    assertThat(ss.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".*"));
  }

  @Test(expected = ParserException.class)
  public void testDontParseRange() {
    final Node.Range r = pp.range().parse("a--f");
    assertThat(r.toString(), is("a-f"));
  }

  @Test
  public void testEscaped() {
    final Node.EscapedChar s = pp.escapedCharacter().parse("\\(");
    assertThat(s.inputRange.getFrom(), is('('));
    assertThat(s.toString(), is("\\("));
  }

  @Test
  public void testGroup2() {
    pp.prepare();
    final Parser<Node.Group> s = pp.group();
    final Node.Group u = s.parse("(aaa|bbb)");
    assertThat(u.toString(), is("(aaa|bbb)"));
  }

  @Test(expected = ParserException.class)
  public void testNoSet() {
    pp.set().parse("[a-fgk-zA-B][");
  }

  @Test
  public void testOptional1() {
    final Node.Optional s = pp.optional().parse("[a-fgk-zA-B]?");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]?"));
  }

  @Test
  public void testOptional2() {
    final Node.Optional s = pp.optional().parse(".?");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".?"));
  }

  @Test
  public void testParseRange() {
    final Node.Range r = pp.range().parse("a-f");
    assertThat(r.toString(), is("a-f"));
  }

  @Test
  public void testPlus1() {
    final Node.Plus s = pp.plus().parse("[a-fgk-zA-B]+");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]+"));
  }

  @Test
  public void testPlus2() {
    final Node.Plus s = pp.plus().parse(".+");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".+"));
  }

  @Test
  public void testRegexp() {
    final Parser<Node.Regex> p = pp.regexp();
    final Node.Regex rr = p.parse("aaa|bbb");
    assertThat(rr.toString(), is("aaa|bbb"));
  }

  @Test
  public void testSet() {
    final Node.Set s = pp.set().parse("[a-fgk-zA-B]");

    assertThat(s.toString(), is("[a-fgk-zA-B]"));
  }

  @Test(expected = ParserException.class)
  public void testSimpleNotEmpty() {
    pp.simple().parse("");
  }

  @Test
  public void testStar1() {
    final Node.Star s = pp.star().parse("[a-fgk-zA-B]*");
    assertThat(s.elementary, instanceOf(Node.Set.class));
    assertThat(s.toString(), is("[a-fgk-zA-B]*"));
  }

  @Test
  public void testStar2() {
    final Node.Star s = pp.star().parse(".*");
    assertThat(s.elementary, instanceOf(Node.Any.class));
    assertThat(s.toString(), is(".*"));
  }

  @Test
  public void testUnion() {
    pp.prepare();
    final Parser<Node.Union> s = pp.union();
    final Node.Union u = s.parse("[a-fgk-zA-B]*|aaa");
    assertThat(u.toString(), is("[a-fgk-zA-B]*|aaa"));
    assertThat(u.left.getClass(), is((Object) Node.Simple.class));
    assertThat(u.right, instanceOf(Node.Simple.class));
  }

  @Test
  public void testUnion2() {
    pp.prepare();
    final Parser<Node.Union> s = pp.union();
    final Node.Union u = s.parse("[a-fgk-zA-B]*|(aaa|bbb)");
    assertThat(u.toString(), is("[a-fgk-zA-B]*|(aaa|bbb)"));
    assertThat(u.left.getClass(), is((Object) Node.Simple.class));
    assertThat(u.right, instanceOf(Node.Simple.class));
  }

  @Test
  public void testUnion3() {
    pp.prepare();
    final Parser<Node.Union> s = pp.union();
    final Node.Union u = s.parse("bbb|aaa");
    assertThat(u.toString(), is("bbb|aaa"));
  }

  @Test
  public void unionBig() {
    final Parser<Node.Regex> p = pp.regexp();
    final String s = "aaa|bbb";
    final Node.Regex rr = p.parse(s);
    assertThat(rr.toString(), is("aaa|bbb"));
  }

  @Test
  public void unionBig2() {
    final Parser<Node.Regex> p = pp.regexp();
    final String s = "aaa|(bbb)?";
    final Node.Regex rr = p.parse(s);
    assertThat(rr, instanceOf(Node.Union.class));
    final Node.Union u = (Node.Union) rr;
    final Node.Regex right = u.right;
    assertThat(right, instanceOf(Node.Simple.class));
    final Node.Simple simple = (Node.Simple) right;
    final Node.Basic optional = simple.basics.get(0);
    assertThat(optional, instanceOf(Node.Optional.class));
    assertThat(rr.toString(), is("aaa|(bbb)?"));
  }
}
