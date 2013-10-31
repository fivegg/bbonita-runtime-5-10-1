package org.ow2.bonita.deployment;

import org.ow2.bonita.deployment.BARVersion;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BARVersionTest extends TestCase {

  public void testVersionEqualityExactlyTheSame() {
    BARVersion barVersion = new BARVersion("5.4.2");
    Assert.assertTrue("5.4.2 == 5.4.2", 0 == barVersion.compareTo("5.4.2"));
  }

  public void testVersionEqualityWithoutAFinalDigit() {
    BARVersion barVersion = new BARVersion("5.4");
    Assert.assertTrue("5.4 == 5.4.0", 0 == barVersion.compareTo("5.4.0"));
  }

  public void testVersionEqualityWithoutAFinalLetter() {
    BARVersion barVersion = new BARVersion("5.4a");
    Assert.assertTrue("5.4a == 5.4a", 0 == barVersion.compareTo("5.4a"));
  }

  public void testGreaterThanUsingDigitsOnly() {
    BARVersion barVersion = new BARVersion("5.4");
    Assert.assertTrue("5.4 > 5.3", barVersion.compareTo("5.3") > 0);
  }

  public void testSnapshotLessThan() {
    BARVersion barVersion = new BARVersion("5.4-SNAPSHOT");
    Assert.assertTrue("5.4-SNAPSHOT > 5.4", barVersion.compareTo("5.4") > 0);
  }

  public void testVersionLessThan() {
    BARVersion barVersion = new BARVersion("5.9");
    Assert.assertTrue("5.9 < 5.10", barVersion.compareTo("5.10") < 0);
  }

  public void testVersionLessThanWord() {
    BARVersion barVersion = new BARVersion("5.9");
    Assert.assertTrue("5.9 < tree", barVersion.compareTo("tree") < 0);
  }

  public void testVersionLessThanDigit() {
    BARVersion barVersion = new BARVersion("9");
    Assert.assertTrue("9 < 10", barVersion.compareTo("10") < 0);
  }

}
