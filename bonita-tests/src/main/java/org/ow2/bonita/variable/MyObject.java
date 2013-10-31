package org.ow2.bonita.variable;

import java.io.Serializable;

public class MyObject implements Serializable {
  private static final long serialVersionUID = 5891800839689748519L;
  private String str;
  public MyObject(String str) {
    this.str = str;
  }
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MyObject)) {
      return false;
    }
    MyObject other = (MyObject) obj;
    if (str == null) {
      return other.str == null;
    }
    return other.str.equals(str);
  }
}