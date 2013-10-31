package org.ow2.bonita.integration.connector.test;

import java.util.Date;
import java.util.List;

import org.ow2.bonita.connector.core.Connector;
import org.ow2.bonita.connector.core.ConnectorError;

public class InputOutputConnector extends Connector {

  private int firstInput;
  private String secondInput;
  private double thirdInput;
  private Date fourthInput;
  private String output;

  public String getOutput() {
    return output;
  }

  public void setFirstInput(int firstInput) {
    this.firstInput = firstInput;
  }
  
  public void setFirstInput(Integer firstInput) {
    this.firstInput = firstInput.intValue();
  }

  public void setFirstInput(Long firstInput) {
    this.firstInput = firstInput.intValue();
  }

  public void setSecondInput(String secondInput) {
    this.secondInput = secondInput;
  }

  public void setThirdInput(double thirdInput) {
    this.thirdInput = thirdInput;
  }

  public void setThirdInput(Double thirdInput) {
    this.thirdInput = thirdInput;
  }

  public void setFourthInput(Date fourthInput) {
    this.fourthInput = fourthInput;
  }

  @Override
  protected void executeConnector() throws Exception {
    output = firstInput + " " + secondInput + " " + thirdInput + " " + fourthInput;
  }

  @Override
  protected List<ConnectorError> validateValues() {
    return null;
  }
}
