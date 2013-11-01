package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Any;
import ch.unibe.scg.regex.ParserProvider.Node.Char;
import ch.unibe.scg.regex.ParserProvider.Node.Eos;
import ch.unibe.scg.regex.ParserProvider.Node.EscapedChar;
import ch.unibe.scg.regex.ParserProvider.Node.Regex;
import ch.unibe.scg.regex.ParserProvider.Node.Simple;
import ch.unibe.scg.regex.ParserProvider.Node.SimpleChar;
import ch.unibe.scg.regex.ParserProvider.Node.Star;

@SuppressWarnings("javadoc")
public final class RegexToNFATest {

  @Test
  public void any() {
    final RegexToNFA r = new RegexToNFA();
    final Any any = mock(Any.class);
    final TNFA n = r.convert(any);
    assertThat(n.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ANY)=[q2, NORMAL, NONE], (q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void character() {
    final RegexToNFA r = new RegexToNFA();
    final Char character = mock(Char.class);
    when(character.getCharacter()).thenReturn('4');
    final TNFA n = r.convert(character);
    assertThat(n.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, 4-4)=[q2, NORMAL, NONE], (q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void eos() {
    final RegexToNFA r = new RegexToNFA();
    final Eos eos = mock(Eos.class);
    final TNFA n = r.convert(eos);
    assertThat(n.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, $)=[q2, NORMAL, NONE], (q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void escapedCharacter() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("\\.");
    final Char character = (Char) s.basics.get(0);
    assertThat(character, instanceOf(EscapedChar.class));
    final TNFA n = r.convert(character);
    assertThat(n.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, .-.)=[q2, NORMAL, NONE], (q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void group1() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("(\\.)");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(), is("q0 -> [q7], {(q0, ANY)=[q0, NORMAL, NONE], "
        + "(q0, ε)=[q1, NORMAL, ➀0], (q1, ε)=[q2, NORMAL, ➀1], (q2, .-.)=[q3, NORMAL, NONE], "
        + "(q3, ε)=[q4, NORMAL, ➁1], (q4, ε)=[q5, NORMAL, C1], (q5, ε)=[q6, NORMAL, ➁0], "
        + "(q6, ε)=[q7, NORMAL, C0]}"));
  }

  @Test
  public void group2() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("(\\.)*");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
        is("q0 -> [q7], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ε)=[q2, NORMAL, ➀1, q6, NORMAL, ➁0], (q2, .-.)=[q3, NORMAL, NONE], "
            + "(q3, ε)=[q4, NORMAL, ➁1, q2, NORMAL, NONE], (q4, ε)=[q5, NORMAL, C1], "
            + "(q5, ε)=[q6, NORMAL, ➁0], (q6, ε)=[q7, NORMAL, C0]}"));
  }

  @Before
  public void setUp() {
    State.resetCount();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testMockSimple() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = mock(Simple.class);
    final EscapedChar c = mock(EscapedChar.class);
    when(c.getCharacter()).thenReturn('.');
    when(s.getBasics()).thenReturn((List) Arrays.asList(c));
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(), is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], "
        + "(q0, ε)=[q1, NORMAL, ➀0], (q1, .-.)=[q2, NORMAL, NONE], "
        + "(q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void testMockStar() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = mock(Simple.class);
    final Star c = mock(Star.class);
    final SimpleChar e = mock(SimpleChar.class);
    when(c.getElementary()).thenReturn(e);
    when(e.getCharacter()).thenReturn('a');
    when(s.getBasics()).thenReturn((List) Arrays.asList(c));
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ε)=[q3, NORMAL, ➁0], (q1, a-a)=[q2, NORMAL, NONE], "
            + "(q2, ε)=[q1, NORMAL, NONE, q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void testSimple() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("\\.");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(), is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], "
        + "(q0, ε)=[q1, NORMAL, ➀0], (q1, .-.)=[q2, NORMAL, NONE], "
        + "(q2, ε)=[q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void testStar() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("a*");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
        is("q0 -> [q4], {(q0, ANY)=[q0, NORMAL, NONE], (q0, ε)=[q1, NORMAL, ➀0], "
            + "(q1, ε)=[q3, NORMAL, ➁0], (q1, a-a)=[q2, NORMAL, NONE], "
            + "(q2, ε)=[q1, NORMAL, NONE, q3, NORMAL, ➁0], (q3, ε)=[q4, NORMAL, C0]}"));
  }

  @Test
  public void testUnion() {
    final RegexToNFA r = new RegexToNFA();
    final Regex s = new ParserProvider().regexp().parse("a|b");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
      is("q0 -> q5, {(q0, ANY)=[q0, NORMAL, NONE], "
          + "(q0, ε)=[q1, NORMAL, ➀0], "
          + "(q1, a-a)=[q2, NORMAL, NONE], "
          + "(q1, b-b)=[q3, NORMAL, NONE], "
          + "(q2, ε)=[q4, NORMAL, NONE], "
          + "(q3, ε)=[q4, LOW, NONE], "
          + "(q4, ε)=[q5, NORMAL, ➁0]}"));
  }
}
