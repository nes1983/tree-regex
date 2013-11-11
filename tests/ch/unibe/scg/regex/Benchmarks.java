package ch.unibe.scg.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.Macrobenchmark;
import com.google.caliper.runner.CaliperMain;

@SuppressWarnings("javadoc")
public final class Benchmarks extends Benchmark {
  String input;

  @Param
  private InputSize inputSize;

  @Param
  private Strategy strategy;

  public enum Strategy {
    ORACLE {
      @Override
      public int match(String input) {
        Pattern interpreter = Pattern.compile(REGEX);
        Matcher res = interpreter.matcher(input);
        res.find();
        return res.group().length();
      }
    },
    OURS {
      @Override
      public int match(String input) {
        TDFAInterpreter interpreter = TDFAInterpreter.compile(REGEX);
        MatchResultTree res = interpreter.interpret(input);
        return res.group().length();
      }
    };

    private static final String REGEX = "((a+b)+c)+";

    public abstract int match(String input);
  }

  public enum InputSize {
    MEDIUM(200000);

    final int size;

    InputSize(int size) {
      this.size = size;
    }
  }

  public static void main(String[] args) throws Exception {
    if (false) {
      CaliperMain.main(Benchmarks.class, args);
    }

    long start;
    long duration;
    int dummy = 0;

    Benchmarks b = new Benchmarks();
    if (false) {
    b.strategy = Strategy.ORACLE;
    b.inputSize = InputSize.MEDIUM;
    b.setUp();
    b.timeMatch(); // warmup

    start = System.nanoTime();
    for (int i = 0; i < 30; i++) {
      dummy += b.timeMatch();
    }
    duration = System.nanoTime() - start;
    System.out.println(String.format("oracle %10d", duration));

    if (dummy == 0) { // unlikely.
      throw new RuntimeException();
    }
    }

    b = new Benchmarks();
    b.strategy = Strategy.OURS;
    b.inputSize = InputSize.MEDIUM;
    b.setUp();
    b.timeMatch(); // warmup
    start = System.nanoTime();
    for (int i = 0; i < 30; i++) {
      dummy += b.timeMatch();
    }
    duration = System.nanoTime() - start;
    System.out.println(String.format("ours   %10d", duration));

    if (dummy == 0) { // unlikely.
      throw new RuntimeException();
    }

  }

  @Macrobenchmark
  public int timeMatch() {
    return strategy.match(input);
  }

  @Override
  protected void setUp() throws Exception {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < inputSize.size; i++) {
      for (int j = 0; j < 200; j++) {
        sb.append('a');
      }
      sb.append("bc");
    }

    input = sb.toString();
  }
}
