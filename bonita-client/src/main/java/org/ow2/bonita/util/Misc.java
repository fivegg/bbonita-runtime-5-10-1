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
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import static org.ow2.bonita.util.GroovyExpression.START_DELIMITER;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.TypeVariable;
import java.net.InetAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.Permissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.xml.Problem;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Marc Blachon, Guillaume Porcher, Charles Souillard, Miguel Valdes, Pierre Vigneras
 */
public final class Misc {

  private static final Logger LOG = Logger.getLogger(Misc.class.getName());
  private static Object formatterLock = new Object();
  public static final String ATTACHMENT_INDEX_NAME_SEPARATOR = "_____";
  public static final int BASE64_BYTES_FRAGMENT_LENGTH = 65536;
  public static final String BASE64_BYTES_FRAGMENT_SEPARATOR = "::";

  private Misc() {
  }

  /**
   * A RANDOM instance. Prevent creation of many instances.
   */
  public static final Random RANDOM = new Random();
  /**
   * The line separator as defined by the property <code>line.separator</code>.
   */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  // Time formatter
  private static final DateFormat DELAY_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

  static {
    Misc.DELAY_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  /**
   * Number of milliseconds in a given day
   */
  public static final long DAY = 1000 * 60 * 60 * 24; // one day

  private static final AtomicLong SEQUENCE_NUMBER = new AtomicLong(0);

  public static boolean isOnWindows() {
    return System.getProperty("os.name").contains("Windows");
  }

  public static <T> List<T> subList(final Class<T> clazz, final List<T> list, final int fromIndex, final int toIndex) {
    if (list == null || list.isEmpty()) {
      return Collections.emptyList();
    }

    int validToIndex = toIndex;
    if (toIndex > list.size()) {
      validToIndex = list.size();
    }

    if (fromIndex >= validToIndex) {
      return Collections.emptyList();
    }

    return new ArrayList<T>(list.subList(fromIndex, validToIndex));
  }

  public static boolean isSetter(final String methodName) {
    return methodName.startsWith("set") && methodName.length() >= 4 && Character.isUpperCase(methodName.charAt(3));
  }

  public static Document generateDocument(final String s) throws ParserConfigurationException, SAXException,
      IOException, XPathExpressionException {
    final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final InputStream contentStream = new ByteArrayInputStream(s.getBytes());
    Document doc = null;
    try {
      doc = builder.parse(contentStream, null);
    } finally {
      if (contentStream != null) {
        contentStream.close();
      }
    }
    return doc;
  }

  public static Date getDate(final long time) {
    if (time == 0) {
      return null;
    }
    return new Date(time);
  }

  public static long getTime(final Date date) {
    if (date == null) {
      return 0;
    }
    return date.getTime();
  }

  public static List<Collection<Object>> splitCollection(final Collection<? extends Object> initial, final int size) {
    if (initial == null || initial.isEmpty()) {
      return Collections.emptyList();
    }
    final List<Collection<Object>> result = new ArrayList<Collection<Object>>();
    final Iterator<? extends Object> it = initial.iterator();
    int i = 0;
    Set<Object> set = null;
    while (it.hasNext()) {
      if (i == size) {
        i = 0;
      }
      if (i == 0) {
        set = new HashSet<Object>();
        result.add(set);
      }
      set.add(it.next());
      i++;
    }
    return result;
  }

  public static String getXPath(final String s) {
    if (s.contains(BonitaConstants.XPATH_VAR_SEPARATOR)) {
      final String[] segments = s.split("\\" + BonitaConstants.XPATH_VAR_SEPARATOR);
      return segments[1];
    } else {
      return null;
    }
  }

  public static String getVariableName(final String s) {
    if (s.contains(BonitaConstants.XPATH_VAR_SEPARATOR)) {
      final String[] segments = s.split("\\" + BonitaConstants.XPATH_VAR_SEPARATOR);
      return segments[0];
    } else if (s.contains(BonitaConstants.JAVA_VAR_SEPARATOR)) {
      final String[] segments = s.split(BonitaConstants.JAVA_VAR_SEPARATOR);
      return segments[0];
    } else {
      return s;
    }
  }

  public static boolean isXMLAppend(final String s) {
    final String[] segments = s.split("\\" + BonitaConstants.XPATH_VAR_SEPARATOR);
    return segments.length >= 3 && segments[2].equals(BonitaConstants.XPATH_APPEND_FLAG);
  }

  public static int getGroovyExpressionEndIndex(final String expression) {
    int open = 0;
    final char[] characters = expression.toCharArray();
    for (int i = 1; i < characters.length; i++) {
      if (characters[i] == '{') {
        open++;
      } else if (characters[i] == '}') {
        open--;
      }
      if (open == 0) {
        return i + 1;
      }
    }
    return -1;
  }

  public static boolean isJustAGroovyExpression(final String expression) {
    return expression.startsWith(START_DELIMITER) && getGroovyExpressionEndIndex(expression) == expression.length();
  }

  public static boolean containsAGroovyExpression(final String expression) {
    final int begin = expression.indexOf(START_DELIMITER);
    int end = -1;
    if (begin >= 0) {
      end = begin + getGroovyExpressionEndIndex(expression.substring(begin));
    }
    return begin < end;
  }

  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (final Exception e) {
      return "unknown";
    }
  }

  public static Map<String, byte[]> getJarEntries(final JarInputStream jis, final String jarFile) {
    final Map<String, byte[]> jarEntries = new HashMap<String, byte[]>();
    JarEntry jarEntry = null;
    try {
      while ((jarEntry = jis.getNextJarEntry()) != null) {
        String jarEntryName = jarEntry.getName();
        if (jarEntryName.startsWith("/")) {
          jarEntryName = jarEntryName.substring(1);
        }
        final byte[] content = Misc.getJarEntriesContent(jis);
        jarEntries.put(jarEntryName, content);
      }
    } catch (final IOException e) {
      throw new BonitaRuntimeException("Unable to load class: " + jarEntry.getName() + " from jar file: " + jarFile, e);
    }
    return jarEntries;
  }

  private static byte[] getJarEntriesContent(final JarInputStream jis) throws IOException {
    int c;
    final byte[] buffer = new byte[512];
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      while ((c = jis.read(buffer)) != -1) {
        baos.write(buffer, 0, c);
      }
      baos.flush();
    } finally {
      baos.close();
    }
    return baos.toByteArray();
  }

  public static Map<String, byte[]> getJarEntries(final String jarFile, final byte[] jar) {
    JarInputStream jis = null;
    try {
      jis = new JarInputStream(new ByteArrayInputStream(jar));
      return getJarEntries(jis, jarFile);
    } catch (final IOException e) {
      throw new BonitaRuntimeException("Unable to load jar entries from jar file: " + jarFile, e);
    } finally {
      Misc.close(jis);
    }
  }

  /**
   * Generate a unique identifier prefixed by the given String.
   * 
   * @param prefix
   *          the prefix String
   * @return an UUID String prefixed by <code>prefix</code>
   * @see UUID
   */
  public static String getUniqueId(final String prefix) {
    return prefix + java.util.UUID.randomUUID();
  }

  public static boolean isJavaIdentifier(final String s) {
    if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String convertToJavaIdentifier(final String name) {
    final StringBuilder tmp = new StringBuilder();
    char car;
    int i = 0;
    if (name != null) {
      while (i < name.length()) {
        car = name.charAt(i);
        if (i == 0 && Character.isJavaIdentifierStart(car)) {
          tmp.append(car);
        } else if (i != 0 && Character.isJavaIdentifierPart(car)) {
          tmp.append(car);
        } else {
          tmp.append("_");
        }
        i++;
      }
      return tmp.toString();
    }
    return "";
  }

  /**
   * Generates a human readable id prefixed by the given String.
   * 
   * The generated identifier is unique only for the duration of the existence of this class.
   * 
   * @param prefix
   *          the string prefiy
   * @return a human readable id prefixed by <code>prefix</code>.
   */
  public static String getHumanReadableId(final String prefix) {
    return prefix + Misc.getHumanReadableId();
  }

  public static List<String> getBusinessArchiveCategories(final ProcessDefinitionUUID processUUID) {
    final List<String> categories = new ArrayList<String>();
    categories.add("BusinessArchives");
    categories.add(Misc.convertToJavaIdentifier(processUUID.getValue()));
    return categories;
  }

  public static List<String> getGlobalClassDataCategories() {
    final List<String> categories = new ArrayList<String>();
    categories.add("GlobalClasses");
    return categories;
  }

  public static List<String> getAttachmentCategories(final ProcessDefinitionUUID processUUID) {
    final List<String> categories = new ArrayList<String>();
    categories.add("Attachments");
    categories.add(Misc.convertToJavaIdentifier(processUUID.getValue()));
    return categories;
  }

  public static List<String> getAttachmentCategories(final ProcessInstanceUUID instanceUUID) {
    final List<String> categories = new ArrayList<String>();
    categories.add("Attachments");
    categories.add(Misc.convertToJavaIdentifier(instanceUUID.getValue()));
    return categories;
  }

  public static List<String> stringToList(final String s, final String separator) {
    if (s == null) {
      return Collections.emptyList();
    }
    final String[] array = s.split(separator);
    final List<String> result = new ArrayList<String>();
    for (final String st : array) {
      result.add(st);
    }
    return result;
  }

  public static String listToString(final List<String> list, final String separator) {
    if (list == null || list.isEmpty()) {
      return null;
    }
    final StringBuffer buf = new StringBuffer();
    for (final String s : list) {
      buf.append(separator).append(s);
    }
    return buf.toString().substring(separator.length());
  }

  public static Map<String, String> stringToMap(final String s) {
    if (s == null) {
      return Collections.emptyMap();
    }
    final String[] couples = s.split(";");
    final Map<String, String> result = new HashMap<String, String>();
    for (final String couple : couples) {
      final String[] st = couple.split(",");
      result.put(st[0], st[1]);
    }
    return result;
  }

  public static String mapToString(final Map<String, String> map) {
    if (map == null || map.isEmpty()) {
      return null;
    }

    final StringBuffer buf = new StringBuffer();
    for (final Map.Entry<String, String> entry : map.entrySet()) {
      buf.append(';').append(entry.getKey()).append(',').append(entry.getValue());
    }
    return buf.toString().substring(";".length());
  }

  /**
   * Generates a human readable id as a long.
   * 
   * The generated identifier is unique only for the duration of the existence of this class.
   * 
   * @return a unique id as a long.
   */
  public static long getHumanReadableId() {
    return Misc.SEQUENCE_NUMBER.getAndIncrement();
  }

  /**
   * Returns a random number between min and max value
   * 
   * @param min
   *          a positive integer
   * @param max
   *          a positive integer
   * @return a random number between min and max value
   * @throws IllegalArgumentException
   *           if <code>min >= max</code>
   */
  public static int random(final int min, final int max) {
    if (min >= max) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_1", min, max);
      throw new IllegalArgumentException(message);
    }
    final int n = max - min;
    if (n < 0) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_2", min, max);
      throw new IllegalArgumentException(message);
    }
    return Misc.RANDOM.nextInt(max - min) + min;
  }

  /**
   * Generate a RANDOM String of the given size.
   * 
   * @param size
   *          the size of the generated string
   * @return the RANDOM String
   */
  public static String getRandomString(final int size) {
    final char[] s = new char[size];
    int c = 'A';
    int r1 = 0;
    final Random r = new Random();
    for (int i = 0; i < size; i++) {
      r1 = r.nextInt(3);
      switch (r1) {
        case 0:
          c = '0' + (int) (Math.random() * 10);
          break;
        case 1:
          c = 'a' + (int) (Math.random() * 26);
          break;
        case 2:
          c = 'A' + (int) (Math.random() * 26);
          break;
      }
      s[i] = (char) c;
    }
    return new String(s);
  }

  /**
   * <p>
   * Format a delay.
   * </p>
   * 
   * <p>
   * This method returns a human readable string for delay such as the one used in benchmarks.
   * 
   * This method is thread safe.
   * </p>
   * 
   * @param delay
   *          a <code>long</code> value
   * @return a <code>String</code> value
   */
  public static final String formatDelay(final long delay) {
    // Don't use Math.abs() here !!
    // Math.abs(Long.MIN_VALUE) returns Long.MIN_VALUE !
    if (delay == Long.MAX_VALUE) {
      return "INFINITY";
    }
    if (delay == Long.MIN_VALUE) {
      return "-INFINITY";
    }

    if (Math.abs(delay) >= Misc.DAY) {
      final long days = delay / Misc.DAY;
      return days + " day" + (days > 1 ? "s" : "");
    }

    // Formatter are not thread safe. See Sun Bugs #6231579 and Sun Bug
    // #6178997.
    synchronized (Misc.formatterLock) {
      return delay < 0 ? "-" + Misc.DELAY_FORMATTER.format(new Date(-delay)) : Misc.DELAY_FORMATTER.format(new Date(
          delay));
    }
  }

  /**
   * <p>
   * Equivalent to {@link #formatDelay(long)}.
   * </p>
   * 
   * @param delay
   *          a <code>double</code> value
   * @return a <code>String</code> value
   */
  public static final String formatDelay(final double delay) {
    return Misc.formatDelay((long) delay);
  }

  /**
   * <p>
   * Return the list of <code>Class</code> objects representing every types a given class implements.
   * </p>
   * 
   * @param type
   *          a <code>Class</code> value
   * @return a <code>Set</code> value
   */
  public static Set<Class<?>> findAllTypes(final Class<?> type) {
    final Set<Class<?>> superTypes = Misc.findAllSuperTypes(type);
    final Set<Class<?>> result = new HashSet<Class<?>>(superTypes);

    for (final Class<?> i : superTypes) {
      result.addAll(Misc.findAllInterfaces(i));
    }

    result.addAll(Misc.findAllInterfaces(type));

    return result;
  }

  /**
   * Return the generic name of a given class.
   * 
   * For example, given java.util.Map.class, it returns string java.util.Map<K,V>
   * 
   * @param clazz
   *          the class
   * @return the generic name of a given class.
   */
  public static String getGenericFullName(final Class<?> clazz) {
    final StringBuilder sb = new StringBuilder(clazz.getCanonicalName());
    final TypeVariable<?>[] types = clazz.getTypeParameters();

    // This algorithm is weak! It only works for basic type. It will not work
    // for type such as: Type< A <B>> or Type<? extends B> ...
    // Please fix it!
    // TODO: Fix.
    if (types.length != 0) {
      sb.append('<');
      for (final TypeVariable<?> type : types) {
        sb.append(type.getName());
        sb.append(',');
      }
      sb.replace(sb.length() - 1, sb.length(), ">");
    }
    return new String(sb);
  }

  /**
   * <p>
   * Return the list of <code>Class</code> objects representing all super type a given class implements.
   * </p>
   * 
   * @param type
   *          a <code>Class</code> value
   * @return a <code>Set</code> value
   */
  public static Set<Class<?>> findAllSuperTypes(final Class<?> type) {
    final Set<Class<?>> classes = new HashSet<Class<?>>();
    for (Class<?> c = type; c != null; c = c.getSuperclass()) {
      classes.add(c);
    }
    return classes;
  }

  /**
   * <p>
   * Return the list of <code>Class</code> objects representing all interfaces a given class implements.
   * </p>
   * 
   * @param type
   *          a <code>Class</code> value
   * @return a <code>List</code> value
   */
  public static Set<Class<?>> findAllInterfaces(final Class<?> type) {
    final Set<Class<?>> classes = new HashSet<Class<?>>();

    final Class<?>[] interfaces = type.getInterfaces();
    for (final Class<?> i : interfaces) {
      classes.add(i);
      classes.addAll(Misc.findAllInterfaces(i));
    }
    final Class<?> superClass = type.getSuperclass();
    if (superClass != null) {
      classes.addAll(Misc.findAllInterfaces(superClass));
    }

    return classes;
  }

  /**
   * <p>
   * Return the <code>Class[]</code> array representing the types a given method take as parameters.
   * </p>
   * 
   * @param subClasses
   *          the classes which are to be subclasses of parameters
   * @param classToTest
   *          the class which declares the given method
   * @param methodName
   *          the method name
   * 
   * @return the formal parameters class array
   * @throws NoSuchMethodException
   *           if a method cannot be found.
   */
  @SuppressWarnings("unchecked")
  public static Class<?>[] findMethodClassArgs(final Class<?>[] subClasses, final Class<?> classToTest,
      final String methodName) throws NoSuchMethodException {

    final Set<Class<?>>[] classesList = new Set[subClasses.length];
    for (int i = 0; i < classesList.length; i++) {
      classesList[i] = Misc.findAllTypes(subClasses[i]);
    }

    final Method[] methods = classToTest.getDeclaredMethods();
    for (int i = methods.length - 1; i >= 0; i--) {
      // Same method name?
      if (!methods[i].getName().equals(methodName)) {
        continue;
      }

      // Same number of arguments?
      final Class<?>[] formal = methods[i].getParameterTypes();
      if (formal.length != subClasses.length) {
        continue;
      }

      if (Misc.checkFormal(formal, classesList)) {
        return formal;
      }
    }
    final String message = ExceptionManager.getInstance().getFullMessage("buc_M_3",
        Misc.componentsToString(subClasses, false), classToTest, methodName);
    throw new NoSuchMethodException(message);
  }

  /**
   * <p>
   * Return the <code>Class[]</code> array representing the types a constructor take as parameters.
   * </p>
   * 
   * @param subClasses
   *          the classes which are to be subclasses of parameters
   * @param classToTest
   *          the class which declares the constructor
   * 
   * @return the formal parameters class array
   * @throws NoSuchMethodException
   *           if a constructor cannot be found
   */
  @SuppressWarnings("unchecked")
  public static Class<?>[] findConstructorClassArgs(final Class<?>[] subClasses, final Class<?> classToTest)
      throws NoSuchMethodException {

    final Set<Class<?>>[] classesList = new Set[subClasses.length];
    for (int i = 0; i < classesList.length; i++) {
      classesList[i] = Misc.findAllTypes(subClasses[i]);
    }

    final Constructor<?>[] constructors = classToTest.getDeclaredConstructors();
    for (int i = constructors.length - 1; i >= 0; i--) {
      // Same number of arguments?
      final Class<?>[] formal = constructors[i].getParameterTypes();
      if (formal.length != subClasses.length) {
        continue;
      }

      if (Misc.checkFormal(formal, classesList)) {
        return formal;
      }
    }
    final String message = ExceptionManager.getInstance().getFullMessage("buc_M_4",
        Misc.componentsToString(subClasses, false), classToTest);
    throw new NoSuchMethodException(message);
  }

  private static boolean checkFormal(final Class<?>[] formal, final Set<Class<?>>[] types) {
    for (int i = 0; i < formal.length; i++) {
      final Iterator<Class<?>> iterator = types[i].iterator();
      boolean found = false;
      while (iterator.hasNext()) {
        final Class<?> type = iterator.next();
        if (type.equals(formal[i])) {
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }

    return true;
  }

  /**
   * <p>
   * Return an "identity string" for a given object.
   * </p>
   * 
   * <p>
   * The string returned is:
   * <ul>
   * <li><code>"null" if <code>o == null</code>
   * <li>Otherwise, <code>o.getClass().getName() + "#" + System.identityHashCode(o)</code>
   * </ul>
   * </p>
   * 
   * @param o
   *          the object to return the identity string of
   * @return the identity string as defined above
   */
  public static String identityToString(final Object o) {
    if (o == null) {
      return "null";
    }
    return o.getClass().getName() + "#" + System.identityHashCode(o);
  }

  /**
   * <p>
   * Smart toString() implementation of arrays.
   * </p>
   * 
   * @param args
   *          the array to return a smart string of.
   * @return the smart string of the given array.
   */
  public static String componentsToString(final Object[] args, final boolean deepToString) {
    if (args == null) {
      return "null";
    }

    Class<?> componentType = args.getClass().getComponentType();
    final StringBuilder string = new StringBuilder(componentType.getName());

    string.append("[");
    final int length = args.length;
    if (length != 0) {
      final int max = length - 1;
      for (int i = 0; i < max; i++) {
        if (args[i] == null) {
          string.append("null");
        } else if (args[i].getClass().isArray()) {
          componentType = args[i].getClass().getComponentType();
          if (componentType.isPrimitive()) {
            string.append(Misc.primitiveComponentsToString(args[i]));
          } else {
            string.append(Misc.componentsToString((Object[]) args[i], deepToString));
          }
        } else {
          string.append(deepToString ? Misc.deepToString(args[i]) : args[i].toString());
        }

        string.append("; ");
      }

      if (args[max] == null) {
        string.append("null");
      } else if (args[max].getClass().isArray()) {
        componentType = args[max].getClass().getComponentType();
        if (componentType.isPrimitive()) {
          string.append(Misc.primitiveComponentsToString(args[max]));
        } else {
          string.append(Misc.componentsToString((Object[]) args[max], deepToString));
        }
      } else {
        string.append(deepToString ? Misc.deepToString(args[max]) : args[max].toString());
      }
    }
    string.append("]");

    return new String(string);
  }

  /**
   * <p>
   * Smart toString() implementation of an array of primitive types.
   * </p>
   * 
   * @param array
   *          the array to return a smart string of.
   * @return the smart string of the given array.
   */
  public static String primitiveComponentsToString(final Object array) {
    if (array == null) {
      return "null";
    }

    final Class<?> c = array.getClass();
    if (!c.isArray() || !c.getComponentType().isPrimitive()) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_5");
      throw new IllegalArgumentException(message);
    }

    final StringBuilder string = new StringBuilder(c.getComponentType().getName());

    string.append("[");
    final int length = Array.getLength(array);
    if (length != 0) {
      final int max = length - 1;
      for (int i = 0; i < max; i++) {
        string.append(Array.get(array, i));
        string.append("; ");
      }

      string.append(Array.get(array, max));
    }
    string.append("]");

    return new String(string);
  }

  /**
   * Equivalent to {@link #getAllContentFrom(InputStream) getAllContentFrom(new FileInputStream(file))};
   * 
   * @param file
   *          the file to read
   * @return the whole content of the file in a single String.
   * @throws IOException
   *           If an I/O exception occurs
   */
  public static byte[] getAllContentFrom(final File file) throws IOException {
    InputStream in = null;
    try {
      in = new FileInputStream(file);
      return Misc.getAllContentFrom(in);
    } finally {
      Misc.close(in);
    }
  }

  /**
   * Equivalent to {@link #getAllContentFrom(InputStream) getAllContentFrom(source.getByteStream(source))};
   * 
   * @param source
   *          the file to read
   * @return the whole content of the file in a single String.
   * @throws IOException
   *           IOException If an I/O exception occurs
   */
  public static byte[] getAllContentFrom(final InputSource source) throws IOException {
    InputStream in = null;
    try {
      in = source.getByteStream();
      return Misc.getAllContentFrom(in);
    } finally {
      Misc.close(in);
    }
  }

  /**
   * Return the whole underlying stream content into a single String.
   * 
   * Warning: the whole content of stream will be kept in memory!! Use with care!
   * 
   * @param url
   *          the URL to read
   * @return the whole content of the stream in a single String.
   * @throws IOException
   *           if an I/O exception occurs
   */
  public static byte[] getAllContentFrom(final URL url) throws IOException {
    final InputStream in = url.openStream();
    try {
      return Misc.getAllContentFrom(in);
    } finally {
      in.close();
    }
  }

  /**
   * Return the whole underlying stream content into a single String.
   * 
   * Warning: the whole content of stream will be kept in memory!! Use with care!
   * 
   * @param in
   *          the stream to read
   * @return the whole content of the stream in a single String.
   * @throws IOException
   *           if an I/O exception occurs
   */
  public static byte[] getAllContentFrom(final InputStream in) throws IOException {
    if (in == null) {
      throw new IOException("The InputStream is null!");
    }
    final BufferedInputStream bis = new BufferedInputStream(in);
    final ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] resultArray = null;
    try {
      int c;
      while ((c = bis.read()) != -1) {
        result.write(c);
      }
      resultArray = result.toByteArray();
      result.flush();
    } finally {
      result.close();
      bis.close();
    }
    return resultArray;
  }

  public static void getFile(final File file, final byte[] fileAsByteArray) throws IOException {
    final FileOutputStream fos = new FileOutputStream(file);
    try {
      fos.write(fileAsByteArray);
      fos.flush();
    } finally {
      fos.close();
    }
  }

  /**
   * Equivalent to {@link #reflectClose(Object)}
   */
  public static Exception close(final Closeable closeable) {
    return Misc.reflectClose(closeable);
  }

  /**
   * Equivalent to {@link #reflectClose(Object)}
   */
  public static Exception close(final XMLEncoder encoder) {
    return Misc.reflectClose(encoder);
  }

  /**
   * Equivalent to {@link #reflectClose(Object)}
   */
  public static Exception close(final XMLDecoder decoder) {
    return Misc.reflectClose(decoder);
  }

  /**
   * Invoke the close() method on the given object.
   * 
   * This method uses the reflection API to find a close() method with no arguments. Any exception thrown (including
   * NoSuchMethodException) will be both logged using {@link #LOG} and returned. If the parameter is null, nothing is
   * done and null is returned.
   * 
   * @param o
   *          the object to call the close() method on.
   * @return the exception thrown if any, null otherwise.
   */
  public static Exception reflectClose(final Object o) {
    if (o == null) {
      return null;
    }
    try {
      final Method m = o.getClass().getMethod("close", new Class[0]);
      m.invoke(o, new Object[0]);
    } catch (final Exception e) {
      final StringBuilder sb = new StringBuilder("Exception thrown during close() on: ");
      sb.append(o.toString());
      sb.append(Misc.LINE_SEPARATOR);
      sb.append("Exception message is: ");
      sb.append(e.getMessage());
      sb.append(Misc.LINE_SEPARATOR);
      sb.append(Misc.getStackTraceFrom(e));
      Misc.LOG.warning(sb.toString());
      return e;
    }
    return null;
  }

  /**
   * @param permissions
   * @return the permissions size
   */
  public static int getPermissionsSize(final Permissions permissions) {
    int size = 0;
    final Enumeration<Permission> p = permissions.elements();
    while (p.hasMoreElements()) {
      size++;
    }
    return size;
  }

  /**
   * Return a proxy implementing all the interfaces specified that forward method invocations to the specified MBean.
   * 
   * @param <T>
   * @param mbeanInterface
   *          the interface the proxy should implement (the MBean should obviously also implement that interface).
   * @param jmxServiceUrl
   *          the JMX service URL
   * @param jmxObjectName
   *          the name the MBean has been registered to
   * @return a proxy implementing the specified interface and that forward method invocations to the specified MBean.
   * 
   * @throws IOException
   *           for any IO problem
   * @throws MalformedObjectNameException
   *           for any JMX Naming problem
   * @throws MBeanException
   *           for any MBean problem
   * @throws ReflectionException
   *           for any problem related to reflection
   */
  @SuppressWarnings("unchecked")
  public static <T> T getMBeanProxy(final Class<T> mbeanInterface, final String jmxServiceUrl,
      final String jmxObjectName) throws IOException, MalformedObjectNameException, InstanceNotFoundException,
      MBeanException, ReflectionException {
    return (T) Proxy.newProxyInstance(Misc.class.getClassLoader(), new Class[] { mbeanInterface },
        new MBeanInvocationHandler(jmxServiceUrl, jmxObjectName));
  }

  /**
   * Return a proxy that forward <strong>void-method</strong> invocations to each object specified in the list
   * <code>elements</code>.
   * 
   * The invocation order follows the given list order.
   * 
   * @param <T>
   *          the interface type of the returned proxy and of each elements.
   * @param elements
   *          the elements to forward method invocations to
   * @return the forwarding chaining proxy
   * @see Chainer
   * @see InvocationHandler
   * @see Proxy
   */
  @SuppressWarnings("unchecked")
  public static <T> T getChainOf(final List<T> elements) {
    Misc.checkArgsNotNull(elements);
    final Chainer<T> chain = new Chainer<T>();
    final Set<Class<?>> classes = new HashSet<Class<?>>();
    final Set<Class<?>> initial = Misc.findAllInterfaces(elements.get(0).getClass());
    classes.addAll(initial);
    for (final T element : elements) {
      chain.add(element);
      // We can't find the generic type T at runtime.
      // But this is required by Proxy.newProxyInstance().
      // So, we find the common interfaces implemented by all elements.
      final Set<Class<?>> interfaces = Misc.findAllInterfaces(element.getClass());
      classes.retainAll(interfaces);
    }
    if (classes.size() == 0) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_6", elements);
      throw new IllegalArgumentException(message);
    }
    return (T) Proxy.newProxyInstance(Misc.class.getClassLoader(), classes.toArray(new Class[classes.size()]), chain);
  }

  /**
   * Return a proxy that log method invocations through the provided logger.
   * 
   * @param <T>
   *          the target object type
   * @param target
   *          the target object method invocations should be forwarded to
   * @param logger
   *          the logger to use for logging
   * @return a proxy that log method invocations through the provided logger.
   * @see LoggingInvocationHandler
   * @see InvocationHandler
   * @see Proxy
   */
  @SuppressWarnings("unchecked")
  public static <T> T getLoggerProxyFor(final T target, final Logger logger) {
    final Set<Class<?>> classes = Misc.findAllInterfaces(target.getClass());
    return (T) Proxy.newProxyInstance(Misc.class.getClassLoader(), classes.toArray(new Class[classes.size()]),
        new LoggingInvocationHandler<T>(target, logger));
  }

  /**
   * Represents null value returned by {@link Misc#findNull(Object...)}.
   * 
   * @see Misc#findNull(Object...)
   * @author Pierre Vigneras
   */
  public static class NullCheckResult {

    private final int size;
    private final BitSet bitSet;

    NullCheckResult(final BitSet bitSet, final int size) {
      this.bitSet = bitSet;
      this.size = size;
    }

    /**
     * Returns true if some parameters given to {@link Misc#findNull(Object...)} were null.
     * 
     * @return true if some parameters given to {@link Misc#findNull(Object...)} were null.
     * @see Misc#findNull(Object...)
     */
    public boolean hasNull() {
      return bitSet.cardinality() != 0;
    }

    /**
     * Returns the number of parameters given to {@link Misc#findNull(Object...)}
     * 
     * @return the number of parameters given to {@link Misc#findNull(Object...)}
     * @see Misc#findNull(Object...)
     */
    public int getSize() {
      return size;
    }

    /**
     * Returns true if the i th parameter given to {@link Misc#findNull(Object...)} was null.
     * 
     * @param i
     *          the rank of the parameter given to {@link Misc#findNull(Object...)}.
     * @return true if the i th parameter given to {@link Misc#findNull(Object...)} was null.
     */
    public boolean isNull(final int i) {
      return bitSet.get(i);
    }
  }

  /**
   * Find null parameters in the given list.
   * 
   * This method returns a {@link NullCheckResult}.
   * 
   * 
   * @param params
   *          the parameters to check
   * @return a {@link NullCheckResult} representing null parameters.
   * @see NullCheckResult
   */
  public static NullCheckResult findNull(final Object... params) {
    if (params == null) {
      final BitSet bitSet = new BitSet(1);
      bitSet.set(0);
      return new NullCheckResult(bitSet, 1);
    }
    final BitSet bitSet = new BitSet(params.length);
    for (int i = 0; i < params.length; i++) {
      if (params[i] == null) {
        bitSet.set(i, true);
      } else {
        bitSet.set(i, false);
      }
    }
    return new NullCheckResult(bitSet, params.length);
  }

  /**
   * Check that the given parameters are not null.
   * 
   * This method should only be used to check that some parameters given to a given method are not null. The exception
   * message tries its best to produce a helpful message by scanning the stack trace.
   * 
   * @param params
   *          the parameters to check
   * @throws an
   *           IllegalArgumentException if at least one of the parameters is null
   */
  public static void checkArgsNotNull(final Object... params) {
    Misc.checkArgsNotNull(1, params);
  }

  /**
   * Check that the given parameters are not null.
   * 
   * This method should only be used to check that some parameters given to a given method are not null. The exception
   * message tries its best to produce a helpful message by scanning the stack trace.
   * 
   * @param offset
   *          the offset to use in the stack trace to produce error message
   * @param params
   *          the parameters to check
   * @throws an
   *           IllegalArgumentException if at least one of the parameters is null
   */
  public static void checkArgsNotNull(final int offset, final Object... params) {
    final NullCheckResult result = Misc.findNull(params);
    if (result.hasNull()) {
      // Guess the signature of the caller
      final StackTraceElement callerSTE = Misc.getCaller(offset + 1);
      final String className = callerSTE.getClassName();
      final String methodName = callerSTE.getMethodName();
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < result.getSize(); i++) {
        if (result.isNull(i)) {
          sb.append("null");
        } else {
          sb.append(params[i].getClass().getName());
        }
        if (i < result.getSize() - 1) {
          sb.append(", ");
        }
      }
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_7", className, methodName,
          sb.toString());
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Return the StackTraceElement at the given offset from this method invocation.
   * 
   * @param offset
   * @return a StackTraceElement
   */
  public static StackTraceElement getCaller(final int offset) {
    final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
    StackTraceElement callerSTE = null;
    for (int i = 0; i < stes.length - offset - 1; i++) {
      if (stes[i].getClassName().equals(Misc.class.getName()) && stes[i].getMethodName().equals("getCaller")) {
        callerSTE = stes[i + 1 + offset];
        break;
      }
    }
    Misc.badStateIfNull(callerSTE, "Ouch! Can't get the stack trace back to the caller of this method!");
    return callerSTE;
  }

  /**
   * Return strings mapped to null values in a given @{link {@link NullCheckResult}. .
   * 
   * If the returned @{link List} of String is called <code>l</code> then, it verifies:
   * <code>l.contains(names[i])</code> if and only if <code>nullCheckResult.isNull(i)</code> returns <code>true</code>.
   * 
   * Note that the number of String names given should be of the same size that the one used to get the given
   * {@link NullCheckResult} using {@link #findNull(Object...)}. An {@link IllegalArgumentException} is thrown
   * otherwise.
   * 
   * @param nullCheckResult
   *          the result as returned by {@link #findNull(Object...)}
   * @param names
   *          the strings that should be mapped to null values
   * @return a List of string mapped to the given {@link NullCheckResult}.
   * @throws IllegalArgumentException
   *           if the number of given names is different from {@link NullCheckResult#getSize()}
   * 
   * @see #findNull(Object...)
   * @see NullCheckResult
   */
  public static List<String> getStringFrom(final NullCheckResult nullCheckResult, final String... names) {
    final int n = names.length;
    if (nullCheckResult.getSize() != n) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_8", n, nullCheckResult.getSize());
      throw new IllegalArgumentException(message);
    }
    final List<String> list = new ArrayList<String>();
    if (!nullCheckResult.hasNull()) {
      return list;
    }
    for (int i = 0; i < n; i++) {
      if (nullCheckResult.isNull(i)) {
        list.add(names[i]);
      }
    }
    return list;
  }

  /**
   * This method throw an IllegalStateException if the given parameter is null
   * 
   * @param valueToCheck
   *          the value to check
   * @param msg
   *          the message for the thrown exception
   * 
   * @see IllegalStateException
   */
  public static void badStateIfNull(final Object valueToCheck, final String msg) {
    Misc.badStateIfTrue(valueToCheck == null, msg);
  }

  /**
   * This method throw an IllegalStateException if the given parameter is not null
   * 
   * @param valueToCheck
   *          the value to check
   * @param msg
   *          the message for the thrown exception
   * 
   * @see IllegalStateException
   */
  public static void badStateIfNotNull(final Object valueToCheck, final String msg) {
    Misc.badStateIfTrue(valueToCheck != null, msg);
  }

  /**
   * This method throw an IllegalStateException if the given parameter is true
   * 
   * @param valueToCheck
   *          the value to check
   * @param msg
   *          the message for the thrown exception
   * 
   * @see IllegalStateException
   */
  public static void badStateIfTrue(final boolean valueToCheck, final String msg) {
    if (valueToCheck) {
      throw new IllegalStateException(msg);
    }
  }

  /**
   * This method throw an IllegalStateException if the given parameter is false
   * 
   * @param valueToCheck
   *          the value to check
   * @param msg
   *          the message for the thrown exception
   * 
   * @see IllegalStateException
   */
  public static void badStateIfFalse(final boolean valueToCheck, final String msg) {
    Misc.badStateIfTrue(!valueToCheck, msg);
  }

  /**
   * This method throw an IllegalStateException if the given parameters are equals (using {@link Object#equals(Object)}
   * 
   * @param a
   *          the first object
   * @param b
   *          the second object
   * @param msg
   *          the message for the thrown exception
   * 
   * @see IllegalStateException
   */
  public static void badStateIfEquals(final Object a, final Object b, final String msg) {
    Misc.badStateIfTrue(a.equals(b), msg);
  }

  /**
   * Log a message to the logger of the caller at the given offset in the stack trace.
   * 
   * If <code>A.f()</code> calls <code>B.g()</code> that finally calls <code>dynamicLog(1, msg)</code> then, the msg
   * will be logged with a code similar to: <br>
   * <code>getLogger(B.getClass().getName()).log(level, msg);</code><br>
   * 
   * If the call was <code>dynamicLog(2, msg)</code> then, the code would be:<br>
   * <code>getLogger(A.getClass().getName()).log(level, msg);</code><br>
   * 
   * @param offset
   *          the offset in the stack trace
   * @param level
   *          the level to log the message to
   * @param msg
   *          the message to log
   */
  public static void dynamicLog(final int offset, final Level level, final String msg) {
    final StackTraceElement callerSTE = Misc.getCaller(offset);
    final String className = callerSTE.getClassName();
    final String methodName = callerSTE.getMethodName();
    final Logger logger = Logger.getLogger(className);
    final LogRecord record = new LogRecord(level, msg);
    record.setSourceClassName(className);
    record.setSourceMethodName(methodName);
    record.setLoggerName(logger.getName());
    logger.log(record);
  }

  /**
   * Log the given message at the given level using caller's Logger.
   * 
   * @param level
   *          a level
   * @param msg
   *          the message
   * @see #dynamicLog(int, Level, String)
   */
  public static void log(final Level level, final String msg) {
    Misc.dynamicLog(2, level, msg);
  }

  /**
   * This method logs at the given level a "warning message" if the given parameter is null
   * 
   * @param level
   *          a log level
   * @param valueToCheck
   *          the value to check
   * @param variableName
   *          the variable name holding valueToCheck. Can be null.
   * 
   */
  public static void warnIfNull(final Level level, final Object valueToCheck, final String variableName) {
    if (valueToCheck == null) {
      final String msg = "Warning: " + (variableName == null ? "a variable" : variableName) + " is null!";
      Misc.dynamicLog(1, level, msg);
    }
  }

  /**
   * This method logs at the given level a "warning" if the given parameter is not null
   * 
   * @param level
   *          a log level
   * @param valueToCheck
   *          the value to check
   * @param variableName
   *          the variable name holding valueToCheck. Can be null.
   */
  public static void warnIfNotNull(final Level level, final Object valueToCheck, final String variableName) {
    if (valueToCheck != null) {
      final String msg = "Warning: " + (variableName == null ? "a variable" : variableName) + " is not null!";
      Misc.dynamicLog(1, level, msg);
    }
  }

  /**
   * This method logs at the given level a "warning" if the given parameter is true
   * 
   * @param level
   *          a log level
   * @param valueToCheck
   *          the value to check
   * @param variableName
   *          the variable name holding valueToCheck. Can be null.
   */
  public static void warnIfTrue(final Level level, final boolean valueToCheck, final String variableName) {
    if (valueToCheck) {
      final String msg = "Warning: " + (variableName == null ? "a variable" : variableName) + " is true!";
      Misc.dynamicLog(1, level, msg);
    }
  }

  /**
   * This method logs at the given level a "warning" if the given parameter is false
   * 
   * @param level
   *          a log level
   * @param valueToCheck
   *          the value to check
   * @param variableName
   *          the variable name holding valueToCheck. Can be null.
   */
  public static void warnIfFalse(final Level level, final boolean valueToCheck, final String variableName) {
    if (!valueToCheck) {
      final String msg = "Warning: " + (variableName == null ? "a variable" : variableName) + " is false!";
      Misc.dynamicLog(1, level, msg);
    }
  }

  /**
   * This method logs at the given level a "warning" if the given parameter are equals
   * 
   * @param level
   *          a log level
   * @param a
   *          an object
   * @param b
   *          another object
   */
  public static void warnIfEquals(final Level level, final Object a, final Object b) {
    if (a.equals(b)) {
      Misc.dynamicLog(1, level, "Warning: equals objects: " + Misc.LINE_SEPARATOR + Misc.details(a, b));
    }
  }

  private static String details(final Object a, final Object b) {
    return "a.toString(): " + a.toString() + Misc.LINE_SEPARATOR + "b.toString(): " + b.toString()
        + Misc.LINE_SEPARATOR + "a.idendityToString(): " + Misc.identityToString(a) + Misc.LINE_SEPARATOR
        + "b.identityToString(): " + Misc.identityToString(b);
  }

  /**
   * This method logs at the given level a "warning" if the given parameter are not equals
   * 
   * @param level
   *          a log level
   * @param a
   *          an object
   * @param b
   *          another object
   */
  public static void warnIfNotEquals(final Level level, final Object a, final Object b) {
    if (!a.equals(b)) {
      Misc.dynamicLog(1, level, "Warning: non-equals objects: " + Misc.LINE_SEPARATOR + Misc.details(a, b));
    }
  }

  public static String getCurrentThreadStackTrace() {
    final StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    final StringBuilder stringBuilder = new StringBuilder();
    for (final StackTraceElement element : elements) {
      stringBuilder.append(element.toString());
      stringBuilder.append(Misc.LINE_SEPARATOR);
    }
    return stringBuilder.toString();
  }

  public static String getStackTraceFrom(final Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  public static String deepToString(final Object o) {
    return Misc.recursiveDeepToString(o, new HashMap<Object, String>());
  }

  private static String recursiveDeepToString(final Object o, final Map<Object, String> cache) {
    String result = cache.get(o);
    final String id = "@" + Integer.toHexString(System.identityHashCode(o));
    if (result != null && result != id) {
      return result;
    }
    if (o == null) {
      result = "null";
    } else {
      cache.put(o, id);
      final Class<?> clazz = o.getClass();
      final Package pack = clazz.getPackage();
      if (clazz.isPrimitive() || pack != null && pack.getName().startsWith("java.") || clazz.isEnum()) {
        result = o.toString();
      } else if (clazz.isArray()) {
        final Class<?> componentType = clazz.getComponentType();
        if (componentType.isPrimitive()) {
          result = Misc.primitiveComponentsToString(o);
        } else {
          result = Misc.componentsToString((Object[]) o, true);
        }
      } else {
        final StringBuilder sb = new StringBuilder(clazz.getName());
        sb.append('(').append(id).append(')').append("[");
        final Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
          try {
            field.setAccessible(true);
            sb.append(field.getName()).append(": ");
            final Object f = field.get(o);
            final String v = cache.get(f);
            if (v == id) {
              sb.append(id);
            } else {
              sb.append(Misc.recursiveDeepToString(f, cache));
            }
          } catch (final IllegalAccessException e) {
            Misc.LOG.warning("An exception occured during information fetching on field: " + field
                + Misc.LINE_SEPARATOR + "Stack trace is: " + Misc.getStackTraceFrom(e)
                + "Fallbacking to non-intrusive algorithm for toString().");
            sb.append("(*").append(field.toGenericString()).append("*)");
          }
          sb.append(", ");
        }
        if (fields.length != 0) {
          sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("]");
        result = new String(sb);

      }
    }
    cache.put(o, result);
    return result;
  }

  public static boolean deleteDir(final File dir) {
    return deleteDir(dir, 1, 0);
  }

  public static boolean deleteDir(final File dir, final int attempts, final long sleepTime) {
    Misc.checkArgsNotNull(dir);
    boolean result = true;
    if (!dir.exists()) {
      return false;
    }
    if (!dir.isDirectory()) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_9", dir.getAbsolutePath());
      throw new IllegalArgumentException(message);
    }
    final File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDir(files[i], attempts, sleepTime);
      } else {
        result = result && deleteFile(files[i], attempts, sleepTime);
      }
    }
    result = result && deleteFile(dir, attempts, sleepTime);
    return result;
  }

  public static boolean deleteFile(final File f, final int attempts, final long sleepTime) {
    int retries = attempts;
    while (retries > 0) {
      if (f.delete()) {
        break;
      }
      retries--;
      try {
        Thread.sleep(sleepTime);
      } catch (final InterruptedException e) {
      }
    }
    return retries > 0;
  }

  public static void unreachableStatement() {
    final String message = ExceptionManager.getInstance().getFullMessage("buc_M_10");
    Misc.unreachableStatement(message);
  }

  public static void unreachableStatement(final String reason) {
    throw new IllegalStateException(reason);
  }

  // This code has been taken from:
  // http://forum.java.sun.com/thread.jspa?threadID=760266&messageID=4340490
  // It has been slightly adapted to our needs.
  public static <E extends Enum<E>> E stringToEnum(final Class<E> c, final String s) {
    final EnumSet<E> set = EnumSet.allOf(c);
    for (final E e : set) {
      if (e.toString().equals(s)) {
        return e;
      }
    }
    final String message = ExceptionManager.getInstance().getFullMessage("buc_M_11", c.getName(), s, set.toString());
    throw new IllegalArgumentException(message);
  }

  public static byte[] serialize(final Serializable object) throws IOException, ClassNotFoundException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(bos);
    out.writeObject(object);
    out.flush();
    Misc.close(out);
    // Get the bytes of the serialized object
    return bos.toByteArray();
  }

  public static Serializable deserialize(final byte[] buf) throws IOException, ClassNotFoundException {
    final ByteArrayInputStream bais = new ByteArrayInputStream(buf);
    final ObjectInputStream ois = new ObjectInputStream(bais) {

      @Override
      protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        final String className = desc.getName();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Class.forName(className, true, classLoader);
      }
    };
    final Serializable newObject = (Serializable) ois.readObject();
    ois.close();
    bais.close();
    return newObject;
  }

  /**
   * Check that the given object is actually serializable.
   * 
   * Implementation tries to serialize and deserialize the given object (in memory). Note that <b>null</b> is returned
   * when the given object is serializable. It returns the throwable thrown by either the serialization or the
   * deserialization process. This means, that the object is not really serializable. Therefore the pattern for checking
   * should be written like: <br>
   * <code>
   * final Throwable error = checkReallySerializable(myObject); 
   * if (error == null) {
   *    // success
   * } else {
   *    // failure, do something with error...
   *    error.printStackTrace();
   * }
   * </code>
   * 
   * @param <T>
   *          A type (usually extending {@link Serializable}
   * @param object
   *          the object to check
   * @return null on success, the exception thrown on error
   */
  public static <T extends Serializable> Throwable checkReallySerializable(final T object) {
    try {
      final byte[] buf = Misc.serialize(object);
      Misc.deserialize(buf);
      return null;
    } catch (final Throwable t) {
      return t;
    }
  }

  /**
   * Write the given String to the given file using the default encoding.
   * 
   * @param s
   *          the string to be written
   * @param f
   *          the file to write the given string to
   * @throws IOException
   *           if an IO error is encountered (file not found, read-only file, and so on).
   */
  public static void write(final String s, final File f) throws IOException {
    Misc.checkArgsNotNull(s, f);
    final FileWriter writer = new FileWriter(f);
    try {
      writer.write(s);
      writer.flush();
    } finally {
      Misc.close(writer);
    }
  }

  public static void write(final File file, final byte[] fileContent) throws IOException {
    Misc.checkArgsNotNull(file, fileContent);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      os.write(fileContent);
      os.flush();
    } finally {
      Misc.close(os);
    }
  }

  /**
   * Perform java.io.File.createTempFile with retries when it fail (limit of 10 retries) (Use to by-pass bug #6325169 on
   * SUN JDK 1.5 on windows) Same parameters as {@link java.io.File#createTempFile(String, String, File)} method
   * 
   * @param prefix
   *          Prefix of the file
   * @param suffix
   *          Suffix of the file
   * @param directory
   *          Target directory
   * @return An abstract pathname denoting a newly-created empty file
   * @throws IOException
   *           If a file could not be created
   */
  public static File createTempFile(final String prefix, final String suffix, final File directory) throws IOException {
    // By-pass for the bug #6325169 on SUN JDK 1.5 on windows
    // The createTempFile could fail while creating a file with the same name of
    // an existing directory
    // So if the file creation fail, it retry (with a limit of 10 retry)
    // Rethrow the IOException if all retries failed
    File tmpDir = null;
    final int retryNumber = 10;
    int j = 0;
    boolean succeded = false;
    do {
      try {
        /*
         * If the prefix contained file separator we need to create the parent directories if missing
         */
        final int lastIndexOfSeparatorChar = prefix.lastIndexOf('/');
        String fileName = prefix;
        if (lastIndexOfSeparatorChar > -1) {
          final String dirToCreate = prefix.substring(0, lastIndexOfSeparatorChar);
          new File(directory.getAbsolutePath() + File.separator + dirToCreate).mkdirs();
          fileName = prefix.substring(lastIndexOfSeparatorChar, prefix.length());
        }
        if (!directory.exists()) {
          directory.mkdirs();
        }
        /* Create the file */
        tmpDir = File.createTempFile(fileName, suffix, directory);
        succeded = true;
      } catch (final IOException e) {
        if (j == retryNumber) {
          throw e;
        }
        try {
          Thread.sleep(100);
        } catch (final InterruptedException e1) {
        }
        j++;
      }
    } while (!succeded);
    return tmpDir;
  }

  public static byte[] generateJar(final Class<?>... classes) throws IOException {
    return Misc.generateJar(Misc.getResources(classes));
  }

  public static Map<String, byte[]> getResources(final Class<?>... classes) throws IOException {
    if (classes == null || classes.length == 0) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_13");
      throw new IOException(message);
    }
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    for (final Class<?> clazz : classes) {
      resources.put(clazz.getName().replace(".", "/") + ".class", ClassDataTool.getClassData(clazz));
    }
    return resources;
  }

  public static byte[] generateJar(final Map<String, byte[]> resources) throws IOException {
    if (resources == null || resources.size() == 0) {
      final String message = ExceptionManager.getInstance().getFullMessage("buc_M_14");
      throw new IOException(message);
    }
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final JarOutputStream out = new JarOutputStream(new BufferedOutputStream(baos));
    for (final Map.Entry<String, byte[]> resource : resources.entrySet()) {
      out.putNextEntry(new JarEntry(resource.getKey()));
      out.write(resource.getValue());
    }
    out.flush();
    Misc.close(out);
    final byte[] jar = baos.toByteArray();
    Misc.close(baos);
    return jar;
  }

  public static Map<String, byte[]> getResourcesFromZip(final byte[] barContent) throws IOException {
    final Map<String, byte[]> resources = new HashMap<String, byte[]>();
    final InputStream in = new ByteArrayInputStream(barContent);
    final ZipInputStream zis = new ZipInputStream(in);

    ZipEntry zipEntry = null;
    while ((zipEntry = zis.getNextEntry()) != null) {
      if (!zipEntry.isDirectory()) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        final byte[] buffer = new byte[512];
        while ((c = zis.read(buffer)) != -1) {
          baos.write(buffer, 0, c);
        }
        baos.flush();
        resources.put(zipEntry.getName(), baos.toByteArray());
        Misc.close(baos);
      }
    }
    Misc.close(zis);
    Misc.close(in);
    return resources;
  }

  @SuppressWarnings("unchecked")
  public static <T> T lookup(final String name, final Hashtable<String, String> environment) throws NamingException {
    InitialContext initialContext = null;
    if (environment != null) {
      initialContext = new InitialContext(environment);
    } else {
      initialContext = new InitialContext();
    }
    return (T) initialContext.lookup(name);
  }

  /**
   * Return a new string based on the given message string where all lines are prefixed by the given prefix.
   * 
   * 
   * @param message
   *          the message to transform
   * @param prefix
   *          the prefix to use
   * @return the prefixed string
   */
  public static String prefixAllLines(final String message, final String prefix) {
    Misc.checkArgsNotNull(message, prefix);
    if (message.length() == 0) {
      return prefix;
    }
    final BufferedReader reader = new BufferedReader(new StringReader(message));
    final StringBuilder builder = new StringBuilder();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (builder.length() != 0) {
          builder.append(Misc.LINE_SEPARATOR);
        }
        builder.append(prefix).append(line);
      }
    } catch (final IOException e) {
      final String msg = ExceptionManager.getInstance().getFullMessage("buc_M_15");
      Misc.unreachableStatement(msg);
    } finally {
      Misc.close(reader);
    }
    return builder.toString();
  }

  public static void showProblems(final Collection<Problem> problems, final String description) {
    if (problems != null) {
      StringBuffer errorMsg = null;
      for (final Problem p : problems) {
        if (p.getSeverity().equals(Problem.SEVERITY_ERROR) || p.getSeverity().equals(Problem.SEVERITY_FATALERROR)) {
          if (errorMsg == null) {
            errorMsg = new StringBuffer();
          }
          errorMsg.append(Misc.LINE_SEPARATOR);
          errorMsg.append("  ");
          errorMsg.append(p.toString());
          if (p.getCause() != null) {
            LOG.severe(p.toString() + ". Cause: " + p.getCause());
          } else {
            LOG.severe(p.toString());
          }
        } else {
          LOG.info("WARNING: " + p.toString());
        }
      }
      if (errorMsg != null) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_Pa_1", description, errorMsg);
        throw new BonitaRuntimeException(message);
      }
    }
  }

  public static Object convertIfPossible(final String variableId, final Object variableValue,
      final String dataTypeClassName) {
    if (variableValue == null) {
      return null;
    }
    try {
      final Class<?> destTypeClass = Class.forName(dataTypeClassName, true, Thread.currentThread()
          .getContextClassLoader());
      final Class<?> valueClass = variableValue.getClass();
      final boolean assignmentOK = destTypeClass.isAssignableFrom(valueClass);
      // manage inner classes
      if (assignmentOK || destTypeClass.getName().equals(valueClass.getName())) {
        return variableValue;
      } else {
        // try to convert it
        if ("java.lang".equals(valueClass.getPackage().getName())
            && "java.lang".equals(destTypeClass.getPackage().getName())) {
          // we can convert it
          final String varValueAsString = variableValue.toString();
          if (destTypeClass.equals(String.class)) {
            return varValueAsString;
          } else if (destTypeClass.equals(Boolean.class)) {
            if ("true".equals(varValueAsString) || "false".equals(varValueAsString)) {
              return Boolean.valueOf(varValueAsString);
            }
          } else if (destTypeClass.equals(Character.class)) {
            if (varValueAsString != null && varValueAsString.length() == 1) {
              return Character.valueOf(variableValue.toString().charAt(0));
            }
          } else {
            // Short, Long, Double, Float, Integer : all of them have a
            // valueOf(String) method
            Method valueOf;
            try {
              valueOf = destTypeClass.getMethod("valueOf", new Class<?>[] { String.class });
              return valueOf.invoke(destTypeClass, new Object[] { varValueAsString });
            } catch (final Exception e) {
            }
          }
        }
        return variableValue;
      }
    } catch (final ClassNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  public static String getAttachmentIndexName(final String name, final Date versionDate) {
    Misc.checkArgsNotNull(name, versionDate);
    return name + ATTACHMENT_INDEX_NAME_SEPARATOR + versionDate.getTime();
  }

  public static String getAttachmentName(final String indexName) {
    Misc.checkArgsNotNull(indexName);
    return indexName.substring(0, indexName.lastIndexOf(ATTACHMENT_INDEX_NAME_SEPARATOR));
  }

  public static String getActivityPriority(final int priority) {
    return getActivityPriority(priority, Locale.getDefault());
  }

  public static String getActivityPriority(final int priority, final Locale locale) {
    final ResourceBundle bundle = ResourceBundle.getBundle("org.ow2.bonita.util.Priority", locale);
    try {
      if (priority < 0 || priority > 2) {
        return bundle.getString(String.valueOf(0));
      }
      return bundle.getString(String.valueOf(priority));
    } catch (final MissingResourceException e) {
      throw new IllegalArgumentException("Priority: " + priority + " has no label", e);
    }
  }

  /**
   * @param decodeAndGather
   * @param contextProperties
   * @return
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public static Object deserialize(final byte[] buf, final Properties contextProperties) throws IOException,
      ClassNotFoundException {
    final Object res = deserialize(buf);
    if (res == null) {
      return null;
    } else if (res instanceof String && contextProperties != null) {
      return ProcessBuilder.resolveWithContext((String) res, contextProperties);
    } else if (res.getClass().isArray() && contextProperties != null) {
      final Object[] array = (Object[]) res;
      for (int i = 0; i < array.length; i++) {
        if (array[i] instanceof String) {
          array[i] = ProcessBuilder.resolveWithContext((String) array[i], contextProperties);
        }
      }
      return array;
    } else {
      return res;
    }
  }

  public static String fragmentAndBase64Encode(final byte[] bytes) throws IOException {

    final StringBuilder encodedString = new StringBuilder();

    final int bytesFragmentLength = BASE64_BYTES_FRAGMENT_LENGTH;

    int nbFragment = bytes.length / bytesFragmentLength;
    final int remainingBytes = bytes.length % bytesFragmentLength;
    if (remainingBytes > 0) {
      nbFragment++;
    }

    int bytesPosition = 0;
    for (int nbFragmentsProcessed = 0; nbFragmentsProcessed < nbFragment; nbFragmentsProcessed++) {

      ByteArrayOutputStream byteArrayOutputStream = null;
      if (nbFragmentsProcessed + 1 < nbFragment) {
        byteArrayOutputStream = new ByteArrayOutputStream(bytesFragmentLength);
        byteArrayOutputStream.write(bytes, bytesPosition, bytesFragmentLength);
      } else {
        byteArrayOutputStream = new ByteArrayOutputStream(remainingBytes);
        byteArrayOutputStream.write(bytes, bytesPosition, remainingBytes);
      }

      final String stringFragment = Base64.encodeBytes(byteArrayOutputStream.toByteArray());
      if (bytesPosition != 0) {
        encodedString.append(BASE64_BYTES_FRAGMENT_SEPARATOR);
      }
      encodedString.append(stringFragment);
      byteArrayOutputStream.close();

      bytesPosition += bytesFragmentLength;
    }

    return encodedString.toString();
  }

  public static byte[] base64DecodeAndGather(final String encodedString) throws IOException {

    final String[] encodedFragments = encodedString.split(BASE64_BYTES_FRAGMENT_SEPARATOR);

    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    for (final String encodedFragment : encodedFragments) {
      final byte[] decodedFragment = Base64.decode(encodedFragment);
      byteArrayOutputStream.write(decodedFragment);
    }

    final byte[] decodedBytes = byteArrayOutputStream.toByteArray();
    byteArrayOutputStream.close();

    return decodedBytes;
  }

  public static String hash(final String text) {
    if (text == null) {
      return null;
    } else {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance("SHA-1");
      } catch (final NoSuchAlgorithmException e) {
        throw new BonitaRuntimeException(e);
      }
      byte[] hash = null;
      try {
        hash = md.digest(text.getBytes("UTF-8"));
      } catch (final UnsupportedEncodingException e) {
        throw new BonitaRuntimeException(e);
      }
      final StringBuilder sb = new StringBuilder(hash.length * 2);
      for (final byte b : hash) {
        sb.append(String.format("%x", b));
      }
      return sb.toString();
    }
  }

  /**
   * @param variableId
   * @return
   */
  public static String getGroovyPlaceholderAccessExpression(final String variableId) {
    final String[] segments = variableId.split(BonitaConstants.JAVA_VAR_SEPARATOR);
    return segments[1];
  }

  /**
   * @return
   */
  public static String getSetterName(final String variableId) {
    final String[] segments = variableId.split(BonitaConstants.JAVA_VAR_SEPARATOR);
    return segments[2];
  }

  public static File createDirectories(final String path) {
    final File file = new File(path);
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

}
