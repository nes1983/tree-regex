package ch.unibe.scg.regex;

import org.codehaus.jparsec.Parser;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

@SuppressWarnings("javadoc")
public final class ParserBenchmarks extends Benchmark {
  @Param
  private Strategy strategy;

  @Param
  StringSelect input;

  public static enum StringSelect {
    E1("abcd") , E2("ab|cd"), E3("a*b?|c*?d*?"), E4("(a+(a?b(c*?)))"), E5("(.*?(.*?\\.)*([A-Z][a-zA-Z]*))*.*?");

    private final String input;

    StringSelect(String s) {
      this.input = s;
    }
  }

  public static enum Strategy {
    JPARSEC {
      final Parser<ch.unibe.scg.regex.ParserProvider.Node.Regex> parser = new ParserProvider()
          .regexp();

      @Override
      public int parse(String input) {
        return parser.parse(input).toString().hashCode();
      }

    },
    HANDMADE {
      @Override
      public int parse(String input) {
        return RegexParser.parse(input).toString().hashCode();
      }
    };

    public abstract int parse(String input);
  }
  
  public int timeParse(int rep) {
    int dummy = 0;
    for (int i = 0; i < rep; i++) {
      dummy += strategy.parse(input.input);
    }
    return dummy;
  }
  
  public static void main(String[] args) throws Exception {
    CaliperMain.main(ParserBenchmarks.class, args);
  }
}
