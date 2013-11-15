package ch.unibe.scg.regex;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("javadoc")
public final class MappingInstructionTest {
  @Test
  public void simpleTest() {
    History.resetCount();
    Map<History, History> m = new LinkedHashMap<>();
    History[] h = new History[] { new History(), new History(), new History(),
                                  new History(), new History(), new History(),  };
    m.put(h[3], h[0]);
    m.put(h[5], h[3]);
    m.put(h[0], h[1]);
    m.put(h[1], h[4]);

    TNFAToTDFA converter = new TNFAToTDFA(null);
    List<Instruction> insts = converter.mappingInstructions(m);
    assertThat(insts.toString(), is("[1->4, 0->1, 3->0, 5->3]"));
  }
}
