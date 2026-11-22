package me.konsolas.aac.heuristics.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class SampleData<T extends Number> {
   private Deque<T> samples;
   private int capacity;

   public SampleData(int i) {
      this.capacity = i;
      this.samples = new ArrayDeque<>(i);
   }

   public void addSample(T number) {
      if (this.samples.size() >= this.capacity) {
         this.samples.poll();
         this.samples.add((T)number);
      } else {
         this.samples.add((T)number);
      }
   }

   public Collection<T> getSamples() {
      return this.samples;
   }

   public double getMean() {
      double d = 0.0;

      for (Number number : this.samples) {
         d += number.doubleValue();
      }

      return d / this.samples.size();
   }

   public void clear() {
      this.samples.clear();
   }
}
