package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class SetterConnector extends Connector {

  private String name;
  private String surname;
  private int age;
  private String address;
  private boolean male;
  @SuppressWarnings("unused")
  private boolean a;
  
  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public int getAge() {
    return age;
  }

  public String getAddress() {
    return address;
  }

  public boolean isMale() {
    return male;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setMale(boolean male) {
    this.male = male;
  }
  
  public boolean setA(boolean a) {
    return a;
  }
  
  public void setB(boolean b) {

  }

  @Override
  protected void executeConnector() throws Exception {
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
