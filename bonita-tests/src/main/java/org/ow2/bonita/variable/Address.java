package org.ow2.bonita.variable;

import java.io.Serializable;

//@XmlRootElement
public class Address implements Serializable {
  private static final long serialVersionUID = 5891800839689748519L;
  private String streetNumber;
  private String streetName;
  private int zipCode;

  public Address(String streetNumber, String streetName, int zipCode) {
    super();
    this.streetNumber = streetNumber;
    this.streetName = streetName;
    this.zipCode = zipCode;
  }

  public String getStreetNumber() {
    return streetNumber;
  }
  public String getStreetName() {
    return streetName;
  }
  public int getZipCode() {
    return zipCode;
  }

  public void setZipCode(int zipCode) {
    this.zipCode = zipCode;
  }
  
}