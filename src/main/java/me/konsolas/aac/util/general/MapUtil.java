package me.konsolas.aac.util.general;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Map;

public class MapUtil {
   public static <K, V extends Comparable<? super V>> Map<K, V> sortDescendingByValue(Map<K, V> map) {
      LinkedList<Entry<K, V>> linkedList = new LinkedList<>(map.entrySet());
      Collections.sort(linkedList, (entry, entry2) -> entry2.getValue().compareTo(entry.getValue()));
      LinkedHashMap<K, V> linkedHashMap = new LinkedHashMap<>();

      for (Entry<K, V> entry3 : linkedList) {
         linkedHashMap.put(entry3.getKey(), entry3.getValue());
      }

      return linkedHashMap;
   }
}
