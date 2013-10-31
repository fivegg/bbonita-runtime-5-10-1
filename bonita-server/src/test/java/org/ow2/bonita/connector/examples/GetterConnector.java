package org.ow2.bonita.connector.examples;

import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class GetterConnector extends Connector {

  private String name;
  private String surname;
  private Integer age;
  private String address;
  
  public void getData() {
    name.concat(surname);
  }
  
  public String getName() {
    return name;
  }

  public String getSurname() {
    return surname;
  }

  public Integer getAge() {
    return age;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  @Override
  protected void executeConnector() throws Exception {
    surname = address;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
