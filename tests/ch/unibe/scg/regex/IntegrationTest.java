package ch.unibe.scg.regex;

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
  TDFAInterpreter tdfaInterpreter;

  @Before
  public void setUp() {
    State.resetCount();
    History.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse("(((a+)b)+c)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

      assertThat(
          tnfa.toString(),
          is("q0 -> q14, {(q0, ANY)=[q0, NORMAL, NONE], "
              + "(q0, ε)=[q1, NORMAL, ➀0], "
              + "(q1, ε)=[q2, NORMAL, ➀1], "
              + "(q2, ε)=[q3, NORMAL, ➀2], "
              + "(q3, ε)=[q4, NORMAL, ➀3], "
              + "(q4, a-a)=[q5, NORMAL, NONE], "
              + "(q5, ε)=[q6, LOW, NONE, q4, NORMAL, NONE], "
              + "(q6, ε)=[q7, NORMAL, ➁3], "
              + "(q7, b-b)=[q8, NORMAL, NONE], "
              + "(q8, ε)=[q9, NORMAL, ➁2], "
              + "(q9, ε)=[q10, LOW, NONE, q2, NORMAL, NONE], "
              + "(q10, c-c)=[q11, NORMAL, NONE], "
              + "(q11, ε)=[q12, NORMAL, ➁1], "
              + "(q12, ε)=[q13, LOW, NONE, q1, NORMAL, NONE], "
              + "(q13, ε)=[q14, NORMAL, ➁0]}"));
    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
  }

  @Test
  public void shouldNotMatch() {
    final MatchResult res = tdfaInterpreter.interpret("aabbccaaaa");
    assertThat(res.toString(), is("NO_MATCH"));
  }

  @Test
  public void shouldNotMatchTwoBs() {
    final MatchResult res = tdfaInterpreter.interpret("aabbc");
    assertThat(res.toString(), is("NO_MATCH"));
  }

  @Test
  public void testMemoryAfterExecution() {
    RealMatchResult res = (RealMatchResult) tdfaInterpreter.interpret("abcaabaaabc");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[23(0 0 ), 24(10 10 ), 18(3 3 0 ), 19(10 10 2 ), "
          + "14(6 6 3 0 ), 15(9 9 5 1 ), 12(6 6 3 0 ), 13(8 8 4 0 )]"));
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
  public void testMatchExampleFromPaper1() {
    final Regex parsed = new ParserProvider().regexp().parse("((b+)|a)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    assertThat(tnfa.toString(),
        is("q15 -> q26, {(q15, ANY)=[q15, NORMAL, NONE], "
          + "(q15, ε)=[q16, NORMAL, ➀0], "
          + "(q16, ε)=[q17, NORMAL, ➀1], "
          + "(q17, ε)=[q18, NORMAL, ➀2], "
          + "(q17, a-a)=[q22, NORMAL, NONE], "
          + "(q18, b-b)=[q19, NORMAL, NONE], "
          + "(q19, ε)=[q20, LOW, NONE, q18, NORMAL, NONE], "
          + "(q20, ε)=[q21, NORMAL, ➁2], "
          + "(q21, ε)=[q23, NORMAL, NONE], "
          + "(q22, ε)=[q23, LOW, NONE], "
          + "(q23, ε)=[q24, NORMAL, ➁1], "
          + "(q24, ε)=[q25, LOW, NONE, q16, NORMAL, NONE], "
          + "(q25, ε)=[q26, NORMAL, ➁0]}"));
  }

  @Test
  public void testMatchExampleFromPaperTomLehrer() {
    final Regex parsed = new ParserProvider().regexp().parse("(([a-zA-Z ]*),([0-9]+);)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    assertThat(tnfa.toString(),
        is("q15 -> q29, {(q15, ANY)=[q15, NORMAL, NONE], "
            + "(q15, ε)=[q16, NORMAL, ➀0], "
            + "(q16, ε)=[q17, NORMAL, ➀1], "
            + "(q17, ε)=[q18, NORMAL, ➀2], "
            + "(q18, ε)=[q20, NORMAL, ➁2], "
            + "(q18,  - )=[q19, NORMAL, NONE], "
            + "(q18, A-Z)=[q19, NORMAL, NONE], "
            + "(q18, a-z)=[q19, NORMAL, NONE], "
            + "(q19, ε)=[q18, NORMAL, NONE, q20, NORMAL, ➁2], "
            + "(q20, ,-,)=[q21, NORMAL, NONE], "
            + "(q21, ε)=[q22, NORMAL, ➀3], "
            + "(q22, 0-9)=[q23, NORMAL, NONE], "
            + "(q23, ε)=[q24, LOW, NONE, q22, NORMAL, NONE], "
            + "(q24, ε)=[q25, NORMAL, ➁3], "
            + "(q25, ;-;)=[q26, NORMAL, NONE], "
            + "(q26, ε)=[q27, NORMAL, ➁1], "
            + "(q27, ε)=[q28, LOW, NONE, q16, NORMAL, NONE], "
            + "(q28, ε)=[q29, NORMAL, ➁0]}"));


    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("Tom Lehrer,01;Alan Turing,23;");
    assertThat(Arrays.toString(res.captureGroupPositions),
        is("[40(0 0 ), 41(28 28 ), 34(14 14 0 ), 35(28 28 13 ), 13(14 14 0 ), 14(24 24 9 ), 30(26 26 11 ), 31(27 27 12 )]"));
  }

  @Test
  public void testMemoryAfterExecutionSimple() {
    State.resetCount();
    History.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse("((a+)b)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abab");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[15(0 0 ), 16(3 3 ), 11(2 2 0 ), 12(3 3 1 ), 9(2 2 0 ), 10(2 2 0 )]"));
  }

  @Test
  public void integrationTestWithUnion() {
    State.resetCount();
    History.resetCount();
    final Regex parsed = new ParserProvider().regexp().parse("((a+)(b|c|d))+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abac");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[31(0 0 ), 32(3 3 ), 27(2 2 0 ), 28(3 3 1 ), 11(2 2 0 ), 12(2 2 0 ), 25(3 3 1 ), 26(3 3 1 )]"));
  }

  @Test
  public void testGroupMatch() {
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
