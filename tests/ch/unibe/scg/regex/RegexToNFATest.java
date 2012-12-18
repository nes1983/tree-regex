package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.ParserProvider.Node.Any;
import ch.unibe.scg.regex.ParserProvider.Node.Char;
import ch.unibe.scg.regex.ParserProvider.Node.Eos;
import ch.unibe.scg.regex.ParserProvider.Node.EscapedChar;
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
    assertThat(n.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE), (q1, NONE)]}"));
    final NFAInterpreter i = new NFAInterpreter(n);
    final MatchResult o = i.match("");
    assertThat(o.toString(), is("NO_MATCH"));
  }

  @Test
  public void character() {
    final RegexToNFA r = new RegexToNFA();
    final Char character = mock(Char.class);
    when(character.getCharacter()).thenReturn('4');
    final TNFA n = r.convert(character);
    assertThat(n.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE)], (q0, 4-4)=[(q1, NONE)]}"));
    final NFAInterpreter i = new NFAInterpreter(n);
    final MatchResult o = i.match("5");
    assertThat(o.toString(), is("NO_MATCH"));
    final MatchResult oo = i.match("4");
    assertThat(oo.toString(), is("0-0"));
  }

  @Test
  public void eos() {
    final RegexToNFA r = new RegexToNFA();
    final Eos eos = mock(Eos.class);
    final TNFA n = r.convert(eos);
    assertThat(n.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE)], (q0, $)=[(q1, NONE)]}"));
  }

  @Test
  public void escapedCharacter() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("\\.");
    final Char character = (Char) s.basics.get(0);
    assertThat(character, is(EscapedChar.class));
    final TNFA n = r.convert(character);
    assertThat(n.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE)], (q0, .-.)=[(q1, NONE)]}"));
    final NFAInterpreter i = new NFAInterpreter(n);
    final MatchResult o = i.match("5");
    assertThat(o.toString(), is("NO_MATCH"));
    final MatchResult oo = i.match(".");
    assertThat(oo.toString(), is("0-0"));
  }

  @Test
  public void group1() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("(\\.)");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(), is("q0 -> [q3], {(q2, ε)=[(q3, ➁0)], (q1, .-.)=[(q2, NONE)], "
        + "(q0, ANY)=[(q0, NONE), (q1, ➀0)]}"));
  }

  @Test
  public void group2() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("(\\.)*");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
        is("q0 -> [q3, q0], {(q2, ε)=[(q3, ➁0), (q1, NONE)], (q1, .-.)=[(q2, NONE)], "
            + "(q0, ANY)=[(q0, NONE), (q1, ➀0)]}"));
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
    assertThat(tnfa.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE)], "
        + "(q0, .-.)=[(q1, NONE)]}"));
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
        is("q0 -> [q1, q0], {(q1, ε)=[(q0, NONE)], (q0, ANY)=[(q0, NONE)], "
            + "(q0, a-a)=[(q1, NONE)]}"));
  }

  @Test
  public void testSimple() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("\\.");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(), is("q0 -> [q1], {(q0, ANY)=[(q0, NONE)], "
        + "(q0, .-.)=[(q1, NONE)]}"));
  }

  @Test
  public void testStar() {
    final RegexToNFA r = new RegexToNFA();
    final Simple s = (Simple) new ParserProvider().regexp().parse("a*");
    final TNFA tnfa = r.convert(s);
    assertThat(tnfa.toString(),
        is("q0 -> [q1, q0], {(q1, ε)=[(q0, NONE)], (q0, ANY)=[(q0, NONE)], "
            + "(q0, a-a)=[(q1, NONE)]}"));
  }
}
