package ch.unibe.scg.regex;

import java.util.ArrayList;
import java.util.List;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.api.Macrobenchmark;
import com.google.caliper.runner.CaliperMain;

public final class ArraylikeBenchmarks extends Benchmark {
  @Param
  Strategy strategy;
  
  @Param(value={"1", "2", "4", "8", "16", "32", "64", "96", "128", "144", "160", "176", "192", "208", "224", "240", "256"})
  public String length;
  
  int len;
  
  Arraylike array;
  History h = new History();
  
  enum Strategy {
    ARRAY {
      @Override
      Arraylike setup(int len) {
        return new Arraylike.HistoryArray(len);
      }
    }, TREE {
      @Override
      Arraylike setup(int len) {
        return new Arraylike.TreeArray(len);
      }
    };
    
    abstract Arraylike setup(int len);
  }
  
  @Override
  protected void setUp() {
    len = Integer.parseInt(length);
    array = strategy.setup(len);
  }
  
  @Macrobenchmark
  public int timeMatch(int rep) {
    List<Arraylike> l = new ArrayList<>(rep);
    int x = 0;
    for (int i = 0; i < rep; i++) {
      Arraylike a = array.set(len/4, h);
      x += a.size();
      l.add(a);
    }
    return x;
  }
  
  public static void main(String[] args) throws Exception {
    CaliperMain.main(ArraylikeBenchmarks.class, args);
  }
}
