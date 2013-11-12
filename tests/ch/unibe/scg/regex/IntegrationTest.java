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
        is("[110(0 0 ), 111(28 28 ), 106(14 14 0 ), 107(28 28 13 ), "
            + "69(14 14 0 ), 70(24 24 9 ), 95(26 26 11 ), 96(27 27 12 )]"));
  }

  @Test
  public void testMatchRanges() {
    State.resetCount();
    History.resetCount();

    final Regex parsed = new ParserProvider().regexp().parse("[a-b][b-c]");
    final TNFA tnfa = new RegexToNFA().convert(parsed);

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
//      assertThat(tnfa.toString(), is(
//        "q0 -> q15, {"
//        + "(q0, 0x0-0x2b)=[q0, LOW, NONE], "
//        + "(q0, 0x2c-0x2c)=[q0, LOW, NONE], "
//        + "(q0, 0x2d-0x2f)=[q0, LOW, NONE], "
//        + "(q0, 0-9)=[q0, LOW, NONE], "
//        + "(q0, 0x3a-0x3a)=[q0, LOW, NONE], "
//        + "(q0, 0x3b-0x3b)=[q0, LOW, NONE], "
//        + "(q0, 0x3c-0xffff)=[q0, LOW, NONE], "
//        + "(q3, 0x0-0x2b)=[q4, NORMAL, NONE], "
//        + "(q3, 0x2c-0x2c)=[q4, NORMAL, NONE], "
//        + "(q3, 0x2d-0x2f)=[q4, NORMAL, NONE], "
//        + "(q3, 0-9)=[q4, NORMAL, NONE], "
//        + "(q3, 0x3a-0x3a)=[q4, NORMAL, NONE], "
//        + "(q3, 0x3b-0x3b)=[q4, NORMAL, NONE], "
//        + "(q3, 0x3c-0xffff)=[q4, NORMAL, NONE], "
//        + "(q6, 0x2c-0x2c)=[q7, NORMAL, NONE], "
//        + "(q8, 0-9)=[q9, NORMAL, NONE], "
//        + "(q11, 0x3b-0x3b)=[q12, NORMAL, NONE]}, "
//        + "{q0=[q1, NORMAL, ➀0], "
//        + "q1=[q2, NORMAL, ➀1], "
//        + "q2=[q3, NORMAL, ➀2], "
//        + "q3=[q4, NORMAL, NONE], "
//        + "q4=[q3, LOW, NONE, q5, NORMAL, NONE], "
//        + "q5=[q6, NORMAL, ➁2], "
//        + "q7=[q8, NORMAL, ➀3], "
//        + "q9=[q10, LOW, NONE, q8, NORMAL, NONE], "
//        + "q10=[q11, NORMAL, ➁3], "
//        + "q12=[q13, NORMAL, ➁1], "
//        + "q13=[q14, LOW, NONE, q1, NORMAL, NONE], "
//        + "q14=[q15, NORMAL, ➁0]}"));

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
