package org.ow2.bonita.integration.connector.test;

import java.util.Date;

public class HelloWorld {

  private Integer position;
  
  public HelloWorld() {
    super();
  }

  @SuppressWarnings("unused")
  private HelloWorld(String same) {
    this.position = new Integer(same);
  }

  public void add(Integer one, Integer two) {
    position = one + two;
  }

  public void sayHello() {
    System.out.println("Hello World!");
  }

  public void saySomething(String message) {
    System.out.println(message);
  }

  public void sayCompositeSomething(String message, Long number) {
    System.out.println(number + message);
  }

  public Integer addition() {
    System.out.println(position);
    return position;
  }
  
  public void sayCompositeArray(String message, Integer[] numbers) {
    System.out.print(message + ": ");
    for (int i = 0; i < numbers.length; i++) {
      System.out.print(numbers[i] + ", ");
    }
    System.out.println();
  }

  public void sayTwoThings(String message, Long number, Date date) {
    System.out.println(message + " " + number + " " + date);
  }
  
  public void sayPosition() {
    System.out.println(position);
  }
}
