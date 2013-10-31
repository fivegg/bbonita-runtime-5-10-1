package org.ow2.bonita.variable;

import java.io.Serializable;
import java.util.Collection;

//@XmlRootElement(name="Employee")
public class Employee implements Serializable {
  private static final long serialVersionUID = 5891800839689748519L;
  private String firstName;
  private String lastName;
  private Collection<String> responsabilities;
  private Address address;

  public Employee(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Collection<String> getResponsabilities() {
    return responsabilities;
  }

  public void setResponsabilities(Collection<String> responsabilities) {
    this.responsabilities = responsabilities;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

}
