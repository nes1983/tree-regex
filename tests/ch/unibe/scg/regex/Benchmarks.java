package ch.unibe.scg.regex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.Macrobenchmark;
import com.google.caliper.runner.CaliperMain;

@SuppressWarnings("javadoc")
public final class Benchmarks {
  public static final class NonBackTracking extends Benchmark {
    private final static int INPUT_SIZE = 2000;
    String input;

    @Param
    private Strategy strategy;

    public enum Strategy {
      JPARSEC {
        @Override
        public int match(String input) {
          Parser<List<List<String>>> p = ((Scanners.isChar('a').many1().followedBy(Scanners.isChar('b')).source()).many1()
              .followedBy(Scanners.isChar('c'))).many1();
          List<List<String>> out = p.parse(input);
          return out.size();
        }
      },
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

    @Macrobenchmark
    public int timeMatch() {
      return strategy.match(input);
    }

    @Override
    protected void setUp() throws Exception {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < INPUT_SIZE; i++) {
        for (int j = 0; j < 200; j++) {
          sb.append('a');
        }
        sb.append("bc");
      }

      input = sb.toString();
    }
  }

  public static final class ClassNameBenchmark extends Benchmark {
    final private static String REGEX = "(.*?([a-z]+\\.)*([A-Z][a-zA-Z]*))*.*?";
    String input;

    @Param
    private Strategy strategy;

    public enum Strategy {
      ORACLE {
        @Override
        public int match(String input) {
          Matcher res = Pattern.compile("^" + REGEX + "$", Pattern.MULTILINE).matcher(input);
          int dummy = 0;
          while (res.find()) {
            dummy += res.group().length();
          }
          return dummy;
        }
      },
      OURS {

        @Override
        public int match(String input) {
          MatchResultTree res = TDFAInterpreter.compile(REGEX).interpret(input);
          return res.group().length();
        }
      };

      public abstract int match(String input);
    }

    @Override
    protected void setUp() throws Exception {
      final StringBuilder b = new StringBuilder();
      final Path path = Paths.get("src");

      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!file.toString().endsWith(".java")) {
            return super.visitFile(file, attrs);
          }
          b.append(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(file))).toString());
          return super.visitFile(file, attrs);
        }
      });

      input = b.toString();
      input = input.substring(input.length() * 3 / 4);
    }

    @Macrobenchmark
    public int timeMatch2() {
      return strategy.match(input);
    }
  }

  public static class PathologicalBenchmark extends Benchmark {
    static String regex;
    String input;

    @Param
    private Strategy strategy;

    @Param
    private InputSize inputSize;

    public enum InputSize {
      B(11), BB(12), BBB(13),
      A(14), THREE(15), AA(16), AAA(17), AAAA(18), AAAAA(19), FOUR(20);

      final int size;

      InputSize(int size) {
        this.size = size;
      }
    }

    public enum Strategy {
      ORACLE {
        @Override
        public int match(String input) {
          Matcher res = Pattern.compile("^" + regex + "$", Pattern.MULTILINE).matcher(input);
          int dummy = 0;
          res.find();
          dummy += res.group().length();
          return dummy;
        }
      },
      OURS {
        @Override
        public int match(String input) {
          MatchResultTree res = TDFAInterpreter.compile(regex).interpret(input);
          return res.group().length();
        }
      };

      public abstract int match(String input);
    }

    @Override
    protected void setUp() throws Exception {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < inputSize.size; i++) {
        b.append('a');
      }
      input = b.toString();

      // Build the regex
      b = new StringBuilder();
      for (int i = 0; i < inputSize.size; i++) {
        b.append("a?");
      }
      for (int i = 0; i < inputSize.size; i++) {
        b.append("a");
      }
      regex = b.toString();
      System.out.println(input);
      System.out.println(regex);
    }

    @Macrobenchmark
    public int timeMatch2() {
      return strategy.match(input);
    }
  }

  public static void main(String[] args) throws Exception {
    CaliperMain.main(ClassNameBenchmark.class, args);
  }
}