package org.ow2.bonita.util;

import java.util.Date;

import junit.framework.TestCase;

public class DateUtilTest extends TestCase {

  public void testFormat() {
    Date formattedDate = DateUtil.parseDate("2001/06/21/14/05/10");
    String actual = DateUtil.format(formattedDate);
    assertTrue(actual.contains("2001-06-21T14:05:10.000"));
  }

  public void testFormatThreadSafe() throws InterruptedException {    
    DateThread first = new DateThread();
    DateThread second = new DateThread();

    first.start();
    second.start();

    first.join();
    second.join();

    assertFalse(first.fail);
    assertFalse(second.fail);
  }

  private class DateThread extends Thread {

    final String[] tabDateString = new String[] {"2001/06/21/14/05/10", "1698-12-06T16:15:45.100+0100", "1998/07/17/22/30/10/450" };
    final String[] expectedFormattedDates = new String[] {"2001-06-21T14:05:10.000", "1698-12-06T16:15:45.100+0100", "1998-07-17T22:30:10.450"};
    boolean fail = false;

    public void run() {
      int kerta = 0;
      while (kerta < 1000 && !fail) {
        for (int i = 0; i < tabDateString.length; i++) {
          Date formattedDate = DateUtil.parseDate(tabDateString[i]);
          String actual = DateUtil.format(formattedDate);
          if (!actual.contains(expectedFormattedDates[i])) {
            System.err.println(expectedFormattedDates[i] + " =>" + actual);
            fail = true;
          }
          kerta ++;
        }
      }
    }
  }

}
