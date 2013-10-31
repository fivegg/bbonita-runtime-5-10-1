package org.ow2.bonita.connector.examples.widget;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class SimpleListConnector extends Connector {

  private String a;
  private String b;
  private String c;
  private String d;

  public void setA(String a) {
    this.a = a;
  }

  public void setB(String b) {
    this.b = b;
  }

  public void setC(String c) {
    this.c = c;
  }

  public String getD() {
    return d;
  }

  @Override
  protected void executeConnector() throws Exception {
    d = a + b +c;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }

}
