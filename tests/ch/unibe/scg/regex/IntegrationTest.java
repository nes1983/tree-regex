package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;

import org.junit.Before;
import org.junit.Test;

import ch.unibe.scg.regex.MatchResultTree.TreeNode;
import ch.unibe.scg.regex.ParserProvider.Node.Regex;

@SuppressWarnings("javadoc")
public final class IntegrationTest {
  @Before
  public void setUp() {
    State.resetCount();
    History.resetCount();
  }

  @Test
  public void shouldNotMatch() {
    TDFAInterpreter tdfaInterpreter = TDFAInterpreter.compile("(((a+)b)+c)+");
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
  }

  @Test
  public void shouldNotMatchTwoBs() {
    TDFAInterpreter tdfaInterpreter = TDFAInterpreter.compile("(((a+)b)+c)+");
    final MatchResult res = tdfaInterpreter.interpret("aabbc");
    assertThat(res.toString(), is("NO_MATCH"));
  }

  @Test
  public void testMemoryAfterExecution() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("(((a+)b)+c)+");
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abcaabaaabc");

    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[61(0 0 ), 62(10 10 ), 56(3 3 0 ), 57(10 10 2 ), "
          + "44(6 6 3 0 ), 45(9 9 5 1 ), 32(6 6 3 0 ), 33(8 8 4 0 )]"));
    assertThat(res.getRoot().getChildren().toString(), is("[abc, aabaaabc]"));
    Iterator<TreeNode> iter = res.getRoot().getChildren().iterator();
    List<TreeNode> children = (List<TreeNode>) iter.next().getChildren();
    assertThat(children.toString(), is("[ab]"));
    assertThat(children.get(0).getChildren().toString(), is("[a]"));

    children = (List<TreeNode>) iter.next().getChildren();
    assertThat(children.toString(), is("[aab, aaab]"));
    assertThat(children.get(0).getChildren().toString(), is("[aa]"));
    assertThat(children.get(1).getChildren().toString(), is("[aaa]"));
  }

  @Test
  public void testMatchExampleFromPaperTomLehrer() {
    final Regex parsed = new ParserProvider().regexp().parse("(([a-zA-Z ]*),([0-9]+);)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("Tom Lehrer,01;Alan Turing,23;");
    assertThat(Arrays.toString(res.captureGroupPositions),
        is("[106(0 0 ), 107(28 28 ), 102(14 14 0 ), 103(28 28 13 ), "
            + "62(14 14 0 ), 63(24 24 9 ), 88(26 26 11 ), 89(27 27 12 )]"));
  }

  @Test
  public void testMatchRanges() {
    final Regex parsed = new ParserProvider().regexp().parse("[a-b][b-c]");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    MatchResultTree interpreted = interpreter.interpret("ab");
	assertThat(interpreted, is(instanceOf(RealMatchResult.class)));
  }

  @Test
  public void testMemoryAfterExecutionSimple() {
    final Regex parsed = new ParserProvider().regexp().parse("((a+)b)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abab");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[30(0 0 ), 31(3 3 ), 26(2 2 0 ), 27(3 3 1 ), 21(2 2 0 ), 22(2 2 0 )]"));
  }

  @Test
  public void testNoInstructions() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("a+b+");
    interpreter.interpret("aab");
    assertThat(interpreter.tdfaBuilder.build().toString(), is(""));
  }

  @Test
  public void testOtherLehrer() {
    final Regex parsed = new ParserProvider().regexp().parse("(.*?(.*?),([0-9]+);)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("Tom Lehrer,01;Alan Turing,23;");

    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[98(0 0 ), 99(28 28 ), 92(14 14 0 ), 93(28 28 13 ), 57(14 14 0 ), "
          + "58(24 24 9 ), 78(26 26 11 ), 79(27 27 12 )]"));
  }

  @Test
  public void testTwoGreedy() {
    final Regex parsed = new ParserProvider().regexp().parse(".*(.*)");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("aaaa");

    assertThat(Arrays.toString(res.captureGroupPositions),
      is(""));
  }

  @Test
  public void integrationTestWithUnion() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("((a+)(b|c|d))+");
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abac");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[39(0 0 ), 40(3 3 ), 34(2 2 0 ), 35(3 3 1 ), 26(2 2 0 ), 27(2 2 0 ), 32(3 3 1 ), 33(3 3 1 )]"));
  }

  @Test
  public void testGroupMatch() {
    TDFAInterpreter tdfaInterpreter = TDFAInterpreter.compile("(((a+)b)+c)+");
    MatchResult result = tdfaInterpreter.interpret("aaabcaaabcaabc");
    assertThat(result.start(),  is(0));
    assertThat(result.end(),    is(13));
    assertThat(result.start(1), is(10));
    assertThat(result.end(1),   is(13));
    assertThat(result.start(2), is(10));
    assertThat(result.end(2),   is(12));
    assertThat(result.start(3), is(10));
    assertThat(result.end(3),   is(11));
  }
}
