package me.konsolas.aac.util.general;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomPlayerNameGenerator {
   private static final List<Alphabet> LETTERS;
   private static Random RANDOM = new Random();

   public static String generateName() {
      int value = 2 + RANDOM.nextInt(9);
      StringBuilder sb = new StringBuilder();

      while (value > 0) {
         value--;
         sb.append(pickLetter());
      }

      sb.append(RANDOM.nextInt(999999));
      return sb.toString();
   }

   private static String pickLetter() {
      return LETTERS.get(RANDOM.nextInt(LETTERS.size())).name();
   }

   static {
      Alphabet[] alphabetArray = Alphabet.values();
      LETTERS = Collections.unmodifiableList(Arrays.asList(alphabetArray));
   }

   private enum Alphabet {
      VALUES,
      b,
      c,
      d,
      e,
      f,
      g,
      h,
      i,
      j,
      k,
      l,
      m,
      n,
      o,
      p,
      q,
      r,
      s,
      t,
      u,
      v,
      w,
      x,
      y,
      z;

      private static final Alphabet[] A = new Alphabet[]{
         VALUES, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
      };
   }
}
