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
//
//    if (false) {
//      assertThat(
//          tnfa.toString(),
//          is("q0 -> q14, {(q0, ANY)=[q0, NORMAL, NONE], "
//              + "(q0, ε)=[q1, NORMAL, ➀0], "
//              + "(q1, ε)=[q2, NORMAL, ➀1], "
//              + "(q2, ε)=[q3, NORMAL, ➀2], "
//              + "(q3, ε)=[q4, NORMAL, ➀3], "
//              + "(q4, a-a)=[q5, NORMAL, NONE], "
//              + "(q5, ε)=[q6, LOW, NONE, q4, NORMAL, NONE], "
//              + "(q6, ε)=[q7, NORMAL, ➁3], "
//              + "(q7, b-b)=[q8, NORMAL, NONE], "
//              + "(q8, ε)=[q9, NORMAL, ➁2], "
//              + "(q9, ε)=[q10, LOW, NONE, q2, NORMAL, NONE], "
//              + "(q10, c-c)=[q11, NORMAL, NONE], "
//              + "(q11, ε)=[q12, NORMAL, ➁1], "
//              + "(q12, ε)=[q13, LOW, NONE, q1, NORMAL, NONE], "
//              + "(q13, ε)=[q14, NORMAL, ➁0]}"));
//    }
//    tdfaInterpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
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
      is("[54(0 0 ), 55(10 10 ), 49(3 3 0 ), 50(10 10 2 ), 39(6 6 3 0 ), "
          + "40(9 9 5 1 ), 32(6 6 3 0 ), 33(8 8 4 0 )]"));
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
        is("[97(0 0 ), 98(28 28 ), 93(14 14 0 ), 94(28 28 13 ), "
            + "59(14 14 0 ), 60(24 24 9 ), 82(26 26 11 ), 83(27 27 12 )]"));
  }

  @Test
  public void testMatchRanges() {
    State.resetCount();
    History.resetCount();

    final Regex parsed = new ParserProvider().regexp().parse("[a-b][b-c]");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    assertThat(tnfa.toString(), is(
      "q0 -> q4, "
      + "{(q0, 0x0-0x60)=[q0, LOW, NONE], "
      + "(q0, a-a)=[q0, LOW, NONE], "
      + "(q0, b-b)=[q0, LOW, NONE], "
      + "(q0, c-c)=[q0, LOW, NONE], "
      + "(q0, d-0xffff)=[q0, LOW, NONE], "
      + "(q1, a-a)=[q2, NORMAL, NONE], "
      + "(q1, b-b)=[q2, NORMAL, NONE], "
      + "(q2, b-b)=[q3, NORMAL, NONE], "
      + "(q2, c-c)=[q3, NORMAL, NONE]}, "
      + "{q0=[q1, NORMAL, ➀0], "
      + "q3=[q4, NORMAL, ➁0]}"));


    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    MatchResultTree interpreted = interpreter.interpret("ab");
	assertThat(interpreted, is(instanceOf(RealMatchResult.class)));
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
      is("[27(0 0 ), 28(3 3 ), 23(2 2 0 ), 24(3 3 1 ), 21(2 2 0 ), 22(2 2 0 )]"));
  }

  @Test
  public void testOtherLehrer() {
    final Regex parsed = new ParserProvider().regexp().parse("((.*?),([0-9]+);)+");
    final TNFA tnfa = new RegexToNFA().convert(parsed);
    if (false) {
      assertThat(tnfa.toString(), is(
          "q15 -> q30, "
         + "{(q15, 0x0-0x2b)=[q15, LOW, NONE], "
          + "(q15, 0x2c-0x2c)=[q15, LOW, NONE], " // ,
          + "(q15, 0x2d-0x2f)=[q15, LOW, NONE], "
          + "(q15, 0-9)=[q15, LOW, NONE], "
          + "(q15, 0x3a-0x3a)=[q15, LOW, NONE], "
          + "(q15, 0x3b-0x3b)=[q15, LOW, NONE], " // ;
          + "(q15, 0x3c-0xffff)=[q15, LOW, NONE], "
          + "(q18, 0x0-0x2b)=[q19, NORMAL, NONE], "
          + "(q18, 0x2c-0x2c)=[q19, NORMAL, NONE], " // ,
          + "(q18, 0x2d-0x2f)=[q19, NORMAL, NONE], "
          + "(q18, 0-9)=[q19, NORMAL, NONE], "
          + "(q18, 0x3a-0x3a)=[q19, NORMAL, NONE], "
          + "(q18, 0x3b-0x3b)=[q19, NORMAL, NONE], "
          + "(q18, 0x3c-0xffff)=[q19, NORMAL, NONE], "
          + "(q21, 0x2c-0x2c)=[q22, NORMAL, NONE], " // ,
          + "(q23, 0-9)=[q24, NORMAL, NONE], "
          + "(q26, 0x3b-0x3b)=[q27, NORMAL, NONE]}, "
          + "{q15=[q16, NORMAL, ➀0], "
          + "q16=[q17, NORMAL, ➀1], "
          + "q17=[q18, NORMAL, ➀2], "
          + "q18=[q19, NORMAL, NONE], "
          + "q19=[q18, LOW, NONE, q20, NORMAL, NONE], "
          + "q20=[q21, NORMAL, ➁2], "
          + "q22=[q23, NORMAL, ➀3], "
          + "q24=[q25, LOW, NONE, q23, NORMAL, NONE], "
          + "q25=[q26, NORMAL, ➁3], "
          + "q27=[q28, NORMAL, ➁1], "
          + "q28=[q29, LOW, NONE, q16, NORMAL, NONE], "
          + "q29=[q30, NORMAL, ➁0]}"));
    }

    TDFAInterpreter interpreter = new TDFAInterpreter(TNFAToTDFA.make(tnfa));
    RealMatchResult res = (RealMatchResult) interpreter.interpret("Tom Lehrer,01;Alan Turing,23;");

    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[15(0 0 ), 16(3 3 ), 11(2 2 0 ), 12(3 3 1 ), 9(2 2 0 ), 10(2 2 0 )]"));
    assertThat(res.toString(), is(""));
  }

  @Test
  public void integrationTestWithUnion() {
    TDFAInterpreter interpreter = TDFAInterpreter.compile("((a+)(b|c|d))+");
    RealMatchResult res = (RealMatchResult) interpreter.interpret("abac");
    assertThat(Arrays.toString(res.captureGroupPositions),
      is("[36(0 0 ), 37(3 3 ), 34(2 2 0 ), 35(3 3 1 ), 26(2 2 0 ), 27(2 2 0 ), 32(3 3 1 ), 33(3 3 1 )]"));
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
