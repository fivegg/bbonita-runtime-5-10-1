package org.ow2.bonita.variable;

import java.io.Serializable;

public class CustomClass implements Serializable {
  private static final long serialVersionUID = 1L;
  private long id;
  private String name;

  public CustomClass(long id, String name) {
      this.id = id;
      this.name = name;
  }

  public long getId() {
      return id;
  }

  public void setId(long id) {
      this.id = id;
  }

  public String getName() {
      return name;
  }

  public void setName(String name) {
      this.name = name;
  }
}
