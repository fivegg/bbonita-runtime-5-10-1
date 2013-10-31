package org.ow2.bonita.command;

import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRGetData implements Command<Object> {

  private static final long serialVersionUID = -276514290407085913L;
  private Class<String> javaClass;
  private List<String> categories;
  private Set<String> keys;
  private String key;

  public LDRGetData(Class<String> javaClass, List<String> categories) {
    this(javaClass, categories, null, null);
  }
  
  public LDRGetData(Class<String> javaClass, List<String> categories,
      Set<String> keys) {
    this(javaClass, categories, keys, null);
  }

  public LDRGetData(Class<String> javaClass, List<String> categories,
      String key) {
    this(javaClass, categories, null, key);
  }

  private LDRGetData(Class<String> javaClass, List<String> categories,
      Set<String> keys, String key) {
    super();
    this.javaClass = javaClass;
    this.categories = categories;
    this.keys = keys;
    this.key = key;
  }

  public Object execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    if (key != null) {
      return ldr.getData(javaClass, categories, key);
    } else if (keys != null) {
      return ldr.getData(javaClass, categories, keys);
    } else {
      return ldr.getData(javaClass, categories);
    }
  }

}
