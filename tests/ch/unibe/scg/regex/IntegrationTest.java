package ch.unibe.scg.regex;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

    assertThat(res.matchPositionsDebugString(),
      is("(0, ) (10, ) (3, 0, ) (10, 2, ) (6, 3, 0, ) (9, 5, 1, ) (6, 3, 0, ) (8, 4, 0, ) "));

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
    assertThat(res.matchPositionsDebugString(),
        is("(0, ) (28, ) (14, 0, ) (28, 13, ) (14, 0, ) (24, 9, ) (26, 11, ) (27, 12, ) "));
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
    assertThat(res.matchPositionsDebugString(), is("(0, ) (3, ) (2, 0, ) (3, 1, ) (2, 0, ) (2, 0, ) "));
  }

  @Test
  public void testNoInstructions() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("a+b+");
    interpreter.interpret("aab");
    assertThat(interpreter.tdfaBuilder.build().toString(),
      is("q0-a-a -> q1 []\nq1-a-a -> q1 []\nq1-b-b -> q2 [2->3, 1->4, 4<- pos, c↑(3), c↓(4)]\n"));
  }

  @Test
  public void testOtherLehrer() {
    final Regex parsed = new ParserProvider().regexp().parse("(.*?(.*?),([0-9]+);)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("Tom Lehrer,01;Alan Turing,23;");

    assertThat(res.matchPositionsDebugString(),
      is("(0, ) (28, ) (14, 0, ) (28, 13, ) (14, 0, ) (24, 9, ) (26, 11, ) (27, 12, ) "));
  }

  @Test
  public void testTwoGreedy() {
    final Regex parsed = new ParserProvider().regexp().parse(".*(.*)");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("aaaa");

    assertThat(res.matchPositionsDebugString(), is("(0, ) (3, ) (4, ) (3, ) "));
  }

  @Test
  public void testTwoNonGreedy() {
    final Regex parsed = new ParserProvider().regexp().parse("(.*?(.*?))+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("aaaa");

    assertThat(res.matchPositionsDebugString(),
      is("(0, ) (3, ) (3, 1, 0, ) (3, 2, 0, ) (4, 2, 0, ) (3, 2, 0, ) "));
  }

  @Test
  public void integrationTestWithUnion() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("((a+)(b|c|d))+");
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abac");
    assertThat(res.matchPositionsDebugString(),
      is("(0, ) (3, ) (2, 0, ) (3, 1, ) (2, 0, ) (2, 0, ) (3, 1, ) (3, 1, ) "));
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
