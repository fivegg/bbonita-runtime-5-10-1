/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.ow2.bonita.util.Misc.NullCheckResult;

/**
 * @author Pierre Vigneras
 */
public class MiscTest extends TestCase {

  public static final String TMP = System.getProperty("java.io.tmpdir");
  
  public interface ClonedObject {

  }

  public static class ChainMock {

    private final List<Object> passedBy = new ArrayList<Object>();

    public List<Object> getPassedBy() {
      return this.passedBy;
    }
    void passedBy(final Object o) {
      this.passedBy.add(o);
    }
  }

  public static interface ChainTestOp {

    void trace(ChainMock mock);
  }

  public abstract static class AbstractChainTest implements ChainTestOp {

    public void trace(final ChainMock mock) {
      mock.passedBy(this);
    }
  }

  public static class ConcreteChainTestA extends AbstractChainTest {
  }

  public static class ConcreteChainTestB implements ChainTestOp {

    public void trace(final ChainMock mock) {
      mock.passedBy(this);
    }
  }

  public void testGetChainOf() {
    final List<ChainTestOp> l = new LinkedList<ChainTestOp>();
    final ChainTestOp a = new ConcreteChainTestA();
    final ChainTestOp b = new ConcreteChainTestB();
    l.add(a);
    l.add(b);
    final ChainTestOp p = Misc.getChainOf(l);
    final ChainMock mock = new ChainMock();
    p.trace(mock);
    assertTrue(mock.getPassedBy().containsAll(l));
  }

  public void testDeepToString() {
    System.out.println("#### Visual check of deepToString() -- Begin");
    System.out.println(Misc.deepToString(null));
    System.out.println(Misc.deepToString(true));
    final byte b = 127;
    System.out.println(Misc.deepToString(b));
    System.out.println(Misc.deepToString('a'));
    final short sh = 32000;
    System.out.println(Misc.deepToString(sh));
    final int i = 10240000;
    System.out.println(Misc.deepToString(i));
    final long l = i * i * i;
    System.out.println(Misc.deepToString(l));
    final float f = 3.14f;
    System.out.println(Misc.deepToString(f));
    final double d = Math.E;
    System.out.println(Misc.deepToString(d));
    System.out.println(Misc.deepToString(Void.TYPE));
    System.out.println(Misc.deepToString("Hello World"));
    System.out.println(Misc.deepToString(new Object()));
    System.out.println(Misc.deepToString(new Object() {

      @SuppressWarnings("unused")
      private final String s = "Dummy Field";
    }));
    System.out.println(Misc.deepToString(new Object() {

      class DummyClass {

        @SuppressWarnings("unused")
        String name = "DummyName";

        class SecondDummyClass extends HashMap<String, String> {

          /**
           * 
           */
          private static final long serialVersionUID = -6372383122603433634L;

        }

        @SuppressWarnings("unused")
        SecondDummyClass secondField = new SecondDummyClass();
      }

      @SuppressWarnings("unused")
      DummyClass dummyClass = new DummyClass();
    }));
    System.out.println(Misc.deepToString(""));
    System.out.println(Misc.deepToString(DummyEnum1.ONE));
    System.out.println(Misc.deepToString(new Object() {

      @SuppressWarnings("unused")
      DummyEnum1 e = DummyEnum1.ONE;
      @SuppressWarnings("unused")
      Inner i = new Inner();

      class Inner {

        boolean[] tbo = new boolean[5];
        byte[] tby = new byte[5];
        char[] tch = new char[5];
        short[] tsh = new short[5];
        int[] tin = new int[5];
        long[] tlo = new long[5];
        float[] tfl = new float[5];
        double[] tdo = new double[5];
        Object[] to = new Object[5];
        Object[] tn = new Object[5];

        Inner() {
          Arrays.fill(this.tbo, true);
          Arrays.fill(this.tby, (byte) 10);
          Arrays.fill(this.tch, 'a');
          Arrays.fill(this.tsh, (short) 32000);
          Arrays.fill(this.tin, 10000000);
          Arrays.fill(this.tlo, 10000000 * 1000000);
          Arrays.fill(this.tfl, (int) 1.0);
          Arrays.fill(this.tdo, Math.E);
          Arrays.fill(this.to, "Hey");
          Arrays.fill(this.tn, null);
        }
      }
    }));

    System.out.println("#### Visual check of deepToString() -- End");
  }

  static enum DummyEnum1 {
    ONE, TWO, THREE
  };

  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#findAllInterfaces(java.lang.Class)}.
   */
  public void testFindAllInterfaces() {
    assertTrue(Misc.findAllInterfaces(ChainTestOp.class).size() == 0);
    assertTrue(Misc.findAllInterfaces(AbstractChainTest.class).size() == 1);
    assertTrue(Misc.findAllInterfaces(ConcreteChainTestA.class).size() == 1);
    assertTrue(Misc.findAllInterfaces(ConcreteChainTestB.class).size() == 1);
  }

  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#getAllContentFrom(java.io.Reader)}.
   * 
   * @throws IOException
   */
  public void testGetAllContentFromReader() throws IOException {
    final byte[] src = "Hello World".getBytes();
    final ByteArrayInputStream bais = new ByteArrayInputStream(src);
    try {
      final byte[] result = Misc.getAllContentFrom(bais);
      assertTrue(Arrays.equals(src, result));
    } finally {
      bais.close();
    }    
  }

  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#findNull(java.lang.Object[])}.
   */
  public void testFindNull() {
    NullCheckResult result = Misc.findNull(new Object[] {
      null
    });
    assertTrue(result.hasNull());
    assertTrue(result.isNull(0));

    result = Misc.findNull("1");
    assertFalse(result.hasNull());

    result = Misc.findNull(null, null);
    assertTrue(result.hasNull());
    assertTrue(result.isNull(0));
    assertTrue(result.isNull(1));

    result = Misc.findNull("1", null);
    assertTrue(result.hasNull());
    assertFalse(result.isNull(0));
    assertTrue(result.isNull(1));

    result = Misc.findNull(null, "2");
    assertTrue(result.hasNull());
    assertTrue(result.isNull(0));
    assertFalse(result.isNull(1));

    result = Misc.findNull("1", "2");
    assertFalse(result.hasNull());

  }

  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#checkArgsNotNull(java.lang.Object[])}.
   */
  public void testCheckArgsNotNull() {
    try {
      this.dummyMethod(null, "notNull", null);
      fail("Should throw an IllegalArgumentException here!");
    } catch (final IllegalArgumentException iae) {
      // ok
      assertTrue(iae.getMessage(), iae.getMessage().contains("Some parameters are null in org.ow2.bonita.util.MiscTest.dummyMethod()"));
    } catch (final Exception e) {
      e.printStackTrace();
      fail("Should never throw any other exception than IllegalArgumentException");
    }
  }

  private void dummyMethod(final Object o, final String s, final Misc m) {
    Misc.checkArgsNotNull(o, s, m);
  }
  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#getStringFrom(java.util.BitSet, java.lang.String[])}
   * .
   */
  public void testGetStringFrom() {
    NullCheckResult result = Misc.findNull(new Object[] {
      null
    });
    List<String> list = Misc.getStringFrom(result, "1");
    assertTrue(list.size() == 1);
    assertTrue(list.contains("1"));

    result = Misc.findNull("1");
    list = Misc.getStringFrom(result, "1");
    assertTrue(list.size() == 0);

    result = Misc.findNull(null, null);
    list = Misc.getStringFrom(result, "1", "2");
    assertTrue(list.size() == 2);
    assertTrue(list.contains("1"));
    assertTrue(list.contains("2"));

    result = Misc.findNull("1", null);
    list = Misc.getStringFrom(result, "1", "2");
    assertTrue(list.size() == 1);
    assertFalse(list.contains("1"));
    assertTrue(list.contains("2"));

    result = Misc.findNull(null, "2");
    list = Misc.getStringFrom(result, "1", "2");
    assertTrue(list.size() == 1);
    assertTrue(list.contains("1"));
    assertFalse(list.contains("2"));
  }

  /**
   * Test method for
   * {@link org.ow2.bonita.util.Misc#badStateIfNull(java.lang.Object, java.lang.String)}
   * .
   */
  public void testBadStateIf() {
    try {
      Misc.badStateIfTrue(true, "dummy");
      fail("Should throw an IllegalStateException");
    } catch (final IllegalStateException ise) {
      // ok
    }
    Misc.badStateIfTrue(false, "Dummy");
    // no problem
    Misc.badStateIfFalse(true, "dummy");
    // no problem
    try {
      Misc.badStateIfFalse(false, "dummy");
      fail("Should throw an IllegalStateException");
    } catch (final IllegalStateException ise) {
      // ok
    }
    try {
      Misc.badStateIfNull(null, "dummy");
      fail("Should throw an IllegalStateException");
    } catch (final IllegalStateException ise) {
      // ok
    }
    Misc.badStateIfNull("Ok", "dummy");
    // no problem
    Misc.badStateIfEquals("a", "b", "dummy");
    // no problem
    try {
      Misc.badStateIfEquals("a", "a", "dummy");
      fail("Should throw an IllegalStateException");
    } catch (final IllegalStateException ise) {
      // ok
    }
  }

  public void testDeleteDir() throws IOException {
    File dir = null;
    try {
      Misc.deleteDir(dir);
      fail("Check for null argument!");
    } catch (final IllegalArgumentException e) {
      // ok
    }
    try {
      dir = File.createTempFile("MiscTest", "tmp");
      dir.deleteOnExit();
      Misc.deleteDir(dir);
      fail("Check that the given file is a directory!");
    } catch (final IllegalArgumentException e) {
      assertTrue(e.getMessage().contains(dir.getName()));
    }

    System.err.println("#################### BONITA HOME PATH" + TMP);
    dir = new File(TMP, Misc.getUniqueId("MiscTest-") + Misc.getRandomString(5));
    dir.deleteOnExit();
    Misc.deleteDir(dir);
    // Should not fail!
    dir = new File(TMP, Misc.getUniqueId("MiscTest-") + Misc.getRandomString(5));
    dir.deleteOnExit();
    assertTrue(dir.mkdirs());
    this.populate(dir, Misc.random(5, 10));
    assertTrue(dir.exists() && dir.isDirectory());
    assertTrue(dir.listFiles().length > 0);
    Misc.deleteDir(dir);
    assertFalse(dir.exists());
  }
  
  public void testDeleteSubDir() throws IOException {
    File a = new File(TMP + File.separator + UUID.randomUUID().toString());
    a.mkdirs();
    File b = new File(a, "b");b.mkdirs();
    File c = new File(b, "c");c.mkdirs();
    File d = new File(c, "d");d.mkdirs();
    new File(d, "e");
    d.mkdirs();
    
    new File(b, "index").createNewFile();
    new File(c, "index").createNewFile();
    new File(d, "index").createNewFile();
    
    Misc.deleteDir(a);
  }

  /**
   * @param dir
   * @throws IOException
   */
  private void populate(final File dir, int n) throws IOException {
    while (n-- > 0) {
      final boolean newDir = Misc.RANDOM.nextBoolean();
      final String name = newDir ? n + "" : n + ".tmp";
      final File file = new File(dir, name);
      file.deleteOnExit();
      if (newDir) {
        Misc.badStateIfFalse(file.mkdir(), "Can't create new directory: " + file.getCanonicalPath());
        this.populate(file, n - 1);
      } else {
        Misc.badStateIfFalse(file.createNewFile(), "Can't create new file: " + file.getCanonicalPath());
      }
    }
  }

  public void testRandom() {
    final int n = Misc.RANDOM.nextInt(1000);
    for (int i = 0; i < n; i++) {
      int p;
      int k;
      do {
        p = Misc.RANDOM.nextInt(1000);
        k = Misc.RANDOM.nextInt(1000);
      } while (p == k);
      // System.out.println(p + ", " + k);
      final int min = Math.min(p, k);
      final int max = Math.max(p, k);
      final int r = Misc.random(min, max);
      assertTrue(r + ">" + min, r >= min);
      assertTrue(r + "<" + min, r <= max);
    }
  }

  public void testUnreachableStatement() {
    try {
      Misc.unreachableStatement();
    } catch (final IllegalStateException ise) {
      // ok
    }
  }

  public static enum OOEnum {
    A("a"), B("b"), C("c");

    final String name;

    OOEnum(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }

  public static enum BadEnum {
    X, Y, Z;

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }

  public void testStringToEnum() {
    assertEquals(OOEnum.A, Misc.stringToEnum(OOEnum.class, OOEnum.A.toString()));
    assertEquals(OOEnum.B, Misc.stringToEnum(OOEnum.class, OOEnum.B.toString()));
    assertEquals(OOEnum.C, Misc.stringToEnum(OOEnum.class, OOEnum.C.toString()));
    assertEquals(BadEnum.X, Misc.stringToEnum(BadEnum.class, BadEnum.X.toString()));
    assertEquals(BadEnum.Y, Misc.stringToEnum(BadEnum.class, BadEnum.Y.toString()));
    assertEquals(BadEnum.Z, Misc.stringToEnum(BadEnum.class, BadEnum.Z.toString()));

    try {
      Misc.stringToEnum(BadEnum.class, OOEnum.A.toString());
    } catch (final IllegalArgumentException iae) {
      final String stack = Misc.getStackTraceFrom(iae);
      assertTrue("Stack trace: " + stack + " should contain: " + BadEnum.class.getName(), iae.getMessage().contains(BadEnum.class.getName()));
      assertTrue("Stack trace: " + stack + " should contain: " + OOEnum.A.toString(), iae.getMessage().contains(OOEnum.A.toString()));
    }
  }

  public void testWrite() throws IOException {
    final File dummy = File.createTempFile(Misc.class.getName(), ".testWrite");
    dummy.deleteOnExit();
    try {
      Misc.write(null, dummy);
      fail("Check null arguments");
    } catch (final IllegalArgumentException iae) {
      // ok
    }

    try {
      Misc.write(Misc.getRandomString(10), null);
      fail("Check null arguments");
    } catch (final IllegalArgumentException iae) {
      // ok
    }

    final int n = Misc.random(10, 20);
    final Map<File, String> map = new HashMap<File, String>(n);
    for (int i = 0; i < n; i++) {
      final File f = File.createTempFile(Misc.class.getName(), ".testWrite");
      f.deleteOnExit();
      final String s = Misc.getRandomString(i);
      Misc.write(s, f);
      map.put(f, s);
    }

    for (final Map.Entry<File, String> e : map.entrySet()) {
      final File f = e.getKey();
      final String expected = e.getValue();
      final String read = new String(Misc.getAllContentFrom(f));
      assertEquals(expected, read);
    }
  }

  public void testGetCurrentThreadStackTrace() {
    System.out.println("#### Visual check of getCurrentThreadStackTrace() -- Begin");
    System.out.println(Misc.getCurrentThreadStackTrace());
    System.out.println("#### Visual check of getCurrentThreadStackTrace() -- End");
  }

  // public void testAddAllDeep() {
  // final Collection<ClonedObject> src = new ArrayList<ClonedObject>();
  // Misc.addAllDeep(src, new HashSet<ClonedObject>(), new
  // Cloner<ClonedObject>() {
  //
  // public ClonedObject newClone(ClonedObject t) {
  // return null;
  // }
  //      
  // });
  // }

  public void testCheckReallySerializable() {
    assertNull("String instance is serializable", Misc.checkReallySerializable("Check"));
    assertNotNull("Object instance is not serializable", Misc.checkReallySerializable(new Serializable() {

      /**
       * 
       */
      private static final long serialVersionUID = -7902091241845366080L;
      @SuppressWarnings("unused")
      private final Object o = new Object();
    }));
  }

  public void testDynamicLogMethod() throws InterruptedException {
    final ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    handler.setFormatter(new TraceFormatter());

    final Logger logger = Logger.getLogger(this.getClass().getName());
    logger.setLevel(Level.ALL);
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    final Runnable runnable = new Runnable() {

      public void run() {
        System.out.println("1.1 -> Should print using the standard Formatter");
        Misc.dynamicLog(0, Level.INFO, "1.1 -> Message from Runtime");
        System.out.println("1.2 -> Should print using the TraceFormatter");
        Misc.dynamicLog(1, Level.INFO, "1.2 -> Message from Runtime + 1");
        System.out.println("1.3 -> Should print using the standard Formatter");
        Misc.dynamicLog(2, Level.INFO, "1.3 -> Message from Runtime + 2");
      }
    };
    final Logger internalLogger = Logger.getLogger(runnable.getClass().getName());
    internalLogger.setLevel(Level.ALL);
    internalLogger.addHandler(handler);
    internalLogger.setUseParentHandlers(false);

    System.out.println("#### Visual check of testDynamicLogMethod() -- Begin");
    System.out.println("0.1 -> Should print using the standard Formatter");
    Misc.dynamicLog(0, Level.INFO, "0.1 -> Message from main");
    System.out.println("0.2 -> Should print using the Trace Formatter");
    Misc.dynamicLog(1, Level.INFO, "0.2 -> Message from main + 1");
    System.out.println("0.3 -> Should print using the standard Formatter");
    Misc.dynamicLog(2, Level.INFO, "0.3 -> Message from main + 2");
    final Thread thread = new Thread(runnable);
    thread.start();
    thread.join();
    logger.removeHandler(handler);
    internalLogger.removeHandler(handler);
    System.out.println("#### Visual check of testDynamicLogMethod() -- End");
  }

  public void testLogMethod() throws InterruptedException {
    final ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    handler.setFormatter(new TraceFormatter());

    final Logger logger = Logger.getLogger(this.getClass().getName());
    logger.setLevel(Level.ALL);
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    final Runnable runnable = new Runnable() {

      public void run() {
        System.out.println("1.1 -> Should print using the TraceFormatter");
        Misc.log(Level.INFO, "1.1 -> Message from Runtime");
      }
    };

    final Logger internalLogger = Logger.getLogger(runnable.getClass().getName());
    internalLogger.setLevel(Level.ALL);
    internalLogger.addHandler(handler);
    internalLogger.setUseParentHandlers(false);

    System.out.println("#### Visual check of testLogMethod() -- Begin");
    System.out.println("0.1 -> Should print using the TraceFormatter");
    Misc.log(Level.INFO, "0.1 -> Message from main");
    final Thread thread = new Thread(runnable);
    thread.start();
    thread.join();
    logger.removeHandler(handler);
    internalLogger.removeHandler(handler);
    System.out.println("#### Visual check of testLogMethod() -- End");
  }

  public void testWarnMethods() {
    final Logger logger = Logger.getLogger(this.getClass().getName());
    logger.setLevel(Level.ALL);
    final ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    handler.setFormatter(new TraceFormatter());
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    System.out.println("#### Visual check of testWarnMethods() -- Begin");

    logger.info("0 -> Nothing should appear!");
    Misc.warnIfTrue(Level.WARNING, false, null);
    logger.info("1 -> Something should appear!");
    Misc.warnIfTrue(Level.WARNING, true, null);

    logger.info("2 -> Nothing should appear!");
    Misc.warnIfFalse(Level.WARNING, true, null);
    logger.info("3 -> Something should appear!");
    Misc.warnIfFalse(Level.WARNING, false, null);

    logger.info("4 -> Nothing should appear!");
    Misc.warnIfNull(Level.WARNING, new Object(), null);
    logger.info("5 -> Something should appear!");
    Misc.warnIfNull(Level.WARNING, null, null);

    logger.info("6 -> Nothing should appear!");
    Misc.warnIfNotNull(Level.WARNING, null, null);
    logger.info("7 -> Something should appear!");
    Misc.warnIfNotNull(Level.WARNING, new Object(), null);

    logger.info("8 -> Nothing should appear!");
    Misc.warnIfEquals(Level.WARNING, new Object(), new Object());
    logger.info("9 -> Something should appear!");
    Misc.warnIfEquals(Level.WARNING, "Foo", "Foo");

    logger.info("10 -> Nothing should appear!");
    Misc.warnIfNotEquals(Level.WARNING, "Foo", "Foo");
    logger.info("11 -> Something should appear!");
    Misc.warnIfNotEquals(Level.WARNING, new Object(), new Object());
    logger.removeHandler(handler);
    System.out.println("#### Visual check of testWarnMethods() -- End");
  }

  public void testGetCaller() {
    final StackTraceElement ste = Misc.getCaller(0);
    assertNotNull("Global Not null", ste);
    assertEquals("Global Caller class", this.getClass().getName(), ste.getClassName());
    assertEquals("Global Caller method", "testGetCaller", ste.getMethodName());
    new Runnable() {

      public void run() {
        StackTraceElement ste = Misc.getCaller(0);
        assertNotNull("Internal Not null", ste);
        assertEquals("Internal Caller class", this.getClass().getName(), ste.getClassName());
        assertEquals("Internal Caller method", "run", ste.getMethodName());
        ste = Misc.getCaller(1);
        assertNotNull("Internal+1 Not null", ste);
        assertEquals("Internal+1 Caller class", MiscTest.this.getClass().getName(), ste.getClassName());
        assertEquals("Internal+1 Caller method", "testGetCaller", ste.getMethodName());
      }
    }.run();
  }

  public void testClose() {
    assertNull(Misc.close((Closeable) null));
    assertNull(Misc.close((XMLEncoder) null));
    assertNull(Misc.close((XMLDecoder) null));
    assertNull(Misc.close(new ByteArrayOutputStream()));
    assertNull(Misc.close(new XMLEncoder(new ByteArrayOutputStream())));
    assertNull(Misc.close(new XMLDecoder(new ByteArrayInputStream(new byte[0]))));
    final Exception e = Misc.reflectClose(new Object());
    assertNotNull(e);
    assertTrue(e instanceof NoSuchMethodException);
  }

  public void testGetGenericFullName() {
    String s = Misc.getGenericFullName(Chainer.class);
    assertEquals(Chainer.class.getName() + "<T>", s);

    s = Misc.getGenericFullName(Map.class);
    assertEquals(Map.class.getName() + "<K,V>", s);
  }

  public void testPrefixAllLines() {
    try {
      Misc.prefixAllLines(null, "");
      fail("Check null arguments");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
    try {
      Misc.prefixAllLines("", null);
      fail("Check null arguments");
    } catch (final IllegalArgumentException iae) {
      // ok
    }
    final String prefix = "Prefix";
    assertEquals(prefix, Misc.prefixAllLines("", prefix));
    assertEquals(prefix + Misc.LINE_SEPARATOR + prefix, Misc.prefixAllLines(Misc.LINE_SEPARATOR + Misc.LINE_SEPARATOR, prefix));
    final String s = "First" + Misc.LINE_SEPARATOR + "Second" + Misc.LINE_SEPARATOR + "Three";
    assertEquals(prefix + "First" + Misc.LINE_SEPARATOR + prefix + "Second" + Misc.LINE_SEPARATOR + prefix + "Three", Misc.prefixAllLines(s, prefix));
  }

  public void testActivityPriorities() {
  	assertEquals("Urgent", Misc.getActivityPriority(2, Locale.ENGLISH));
  	assertEquals("High", Misc.getActivityPriority(1, Locale.ENGLISH));
  	assertEquals("Normal", Misc.getActivityPriority(0, Locale.ENGLISH));
  	assertEquals("Normal", Misc.getActivityPriority(3, Locale.ENGLISH));
  	assertEquals("Normal", Misc.getActivityPriority(-1, Locale.ENGLISH));
  	assertEquals("Normal", Misc.getActivityPriority(4, Locale.ENGLISH));
  	assertEquals("Normal", Misc.getActivityPriority(-2, Locale.ENGLISH));
  }

  public void testWriteToUnexistentFile() throws Exception {
	  File file = null;
	  try {
		  file = new File(TMP + File.separator + System.currentTimeMillis());
		  Misc.write(file, new byte[] { 1, 2, 3} );
		  assertTrue(file.exists());
		  assertEquals(3, file.length());
	  } finally {
		  file.delete();
	  }
  }

}
