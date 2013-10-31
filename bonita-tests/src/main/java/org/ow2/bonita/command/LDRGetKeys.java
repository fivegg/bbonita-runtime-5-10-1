package org.ow2.bonita.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRGetKeys implements Command<Set<String>>{

  private static final long serialVersionUID = 7800527665986823978L;
  private String regex;
  private List<String> categories;

  public LDRGetKeys(List<String> categories) {
    this(categories, null);
  }

  public LDRGetKeys(List<String> categories, String regex) {
    this.regex = regex;
    this.categories = categories;
  }

  public Set<String> execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    Set<String> keys = new HashSet<String>();
    if (regex == null) {
      keys = ldr.getKeys(categories);
    } else {
      keys = ldr.getKeys(categories, regex);
    }
    return keys;
  }

}
