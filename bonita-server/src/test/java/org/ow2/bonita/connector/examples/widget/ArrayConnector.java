package org.ow2.bonita.connector.examples.widget;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class ArrayConnector extends Connector {

  private String a;
  private String b;
  private String c;
  private String d;
  private String e;
  private String f;

  public String getA() {
    return a;
  }

  public String getB() {
    return b;
  }

  public String getC() {
    return c;
  }

  public String getD() {
    return d;
  }

  public String getE() {
    return e;
  }
  
  public String getF() {
    return f;
  }

  public void setA(String a) {
    this.a = a;
  }

  public void setB(String b) {
    this.b = b;
  }

  public void setC(String c) {
    this.c = c;
  }

  public void setD(String d) {
    this.d = d;
  }

  public void setE(String e) {
    this.e = e;
  }

  public void setF(String f) {
    this.f = f;
  }
  
  @Override
  protected void executeConnector() throws Exception {
  }
  
  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
