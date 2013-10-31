package org.ow2.bonita.search;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.ow2.bonita.type.Variable;

public class VariableMapFieldBridge implements FieldBridge {

  @SuppressWarnings("unchecked") 
  public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
    Map<String, Variable> variables = (Map<String, Variable>) value;
    if (variables != null) {
      for (Entry<String, Variable> variable : variables.entrySet()) {
        Variable variableValue = variable.getValue();
        if (variableValue != null) {
          luceneOptions.addFieldToDocument(name + "_name", variable.getKey(), document);
          luceneOptions.addFieldToDocument(name + "_value", variable.getValue().getValue().toString(), document);
        }
      }
    }
  }

}
