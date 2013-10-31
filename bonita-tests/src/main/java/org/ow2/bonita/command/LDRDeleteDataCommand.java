package org.ow2.bonita.command;

import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRDeleteDataCommand implements Command<Boolean> {

  private static final long serialVersionUID = -8483312205727081144L;
  private List<String> categories;
  private String key;

  public LDRDeleteDataCommand(List<String> categories) {
    this(categories, null);
  }

  public LDRDeleteDataCommand(List<String> categories, String key) {
    super();
    this.categories = categories;
    this.key = key;
  }

  public Boolean execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    Boolean deleted = Boolean.valueOf(false);
    if (key != null) {
      deleted = ldr.deleteData(categories, key);
    } else {
      deleted = ldr.deleteData(categories);
    }
    return deleted;
  }

}
