package org.ow2.bonita.search;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

public class ObjectMapFieldBridge implements FieldBridge {

  @SuppressWarnings("unchecked")
  public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
    Map<Object, Object> variables = (Map<Object, Object>) value;
    if (variables != null) {
      for (Entry<Object, Object> variable : variables.entrySet()) {
        Object variableValue = variable.getValue();
        if (variableValue != null) {
          luceneOptions.addFieldToDocument(name + "_name", variable.getKey().toString(), document);
          luceneOptions.addFieldToDocument(name + "_value", variable.getValue().toString(), document);
        }
      }
    }
  }

}
