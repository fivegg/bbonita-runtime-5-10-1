package org.ow2.bonita.command;

import java.io.Serializable;
import java.util.List;

import org.ow2.bonita.env.Environment;
import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;

public class LDRStoreDataCommand implements Command<Object> {

  private static final long serialVersionUID = 6719963606454407470L;
  private List<String> categories;
  private String key;
  private Serializable value;
  private boolean overWrite;

  public LDRStoreDataCommand(List<String> categories, String key, Serializable value, boolean overWrite) {
    this.categories = categories;
    this.key = key;
    this.value = value;
    this.overWrite = overWrite;
  }

  public Object execute(Environment environment) throws Exception {
    LargeDataRepository ldr = EnvTool.getLargeDataRepository();
    ldr.storeData(categories, key, value, overWrite);
    return null;
  }

}
