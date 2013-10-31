/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.services.LargeDataRepository;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

public class FileLargeDataRepository implements LargeDataRepository {

  private static final Logger LOG = Logger.getLogger(FileLargeDataRepository.class.getName());
  private final File base;
  private static final Object CONSTRUCTOR_MUTEX = new Object();
  private static final String INDEX_NAME = "index.txt";

  @Override
  public void clean() {
    Misc.deleteDir(this.base, 10, 5);
    createRepository();
  }

  @Override
  public boolean isEmpty() {
    return checkIsEmpty(this.base);
  }

  private boolean checkIsEmpty(final File dir) {
    final File[] files = dir.listFiles();
    if (files == null) {
      return true;
    }
    for (final File file : files) {
      if (file.isFile() && !file.getName().equals(INDEX_NAME)) {
        return false;
      } else if (!checkIsEmpty(file)) {
        return false;
      }
    }
    return true;
  }

  public FileLargeDataRepository(final String value) {
    Misc.checkArgsNotNull(value);
    final String property = "property:";
    String path = value;
    if (value.startsWith(property)) {
      path = System.getProperty(value.substring(property.length()));
    } else if (value.startsWith("${" + BonitaConstants.HOME + "}")) {
      path = path.replace("${" + BonitaConstants.HOME + "}", System.getProperty(BonitaConstants.HOME));
    }
    this.base = new File(path, "bonita-large-data-repository");
    if (LOG.isLoggable(Level.INFO)) {
      LOG.fine("Creating " + getClass().getName() + " with base: " + this.base);
    }
    createRepository();
  }

  @Override
  public boolean deleteData(final List<String> categories, final String key) {
    try {
      final File file = removeFromIndex(categories, key);
      if (file != null && file.exists()) {
        int counter = 0;
        while (!file.delete() && counter < 50) {
          counter++;
          try {
            Thread.sleep(10);
          } catch (final InterruptedException e) {
            // nothing
          }
        }
      } else {
        return false;
      }
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
    return true;
  }

  @Override
  public boolean deleteData(final List<String> categories) {
    final File dir = getPath(categories, false);
    if (dir != null && dir.exists()) {
      Misc.deleteDir(dir, 10, 5);
      if (!dir.exists()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public <T> T getData(final Class<T> clazz, final List<String> categories, final String key) {
    try {
      final File file = getFromIndex(categories, key);

      if (file == null || !file.exists()) {
        return null;
      }
      return getData(clazz, file);
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public Set<String> getKeys(final List<String> categories, final String regex) {
    final File dir = getPath(categories, false);
    if (dir == null || !dir.exists()) {
      return Collections.emptySet();
    }
    try {
      final File index = getIndex(categories);
      synchronized (index.getAbsolutePath()) {
        final Properties properties = loadIndex(index);
        final Enumeration<Object> keys = properties.keys();
        final Set<String> result = new HashSet<String>();
        while (keys.hasMoreElements()) {
          final String key = (String) keys.nextElement();
          if (regex == null || key.matches(regex)) {
            result.add(key);
          }
        }
        return result;
      }
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public Set<String> getKeys(final List<String> categories) {
    return getKeys(categories, null);
  }

  @Override
  public <T> Map<String, T> getDataFromRegex(final Class<T> clazz, final List<String> categories, final String regex) {
    final File dir = getPath(categories, false);
    if (dir == null || !dir.exists()) {
      return Collections.emptyMap();
    }
    try {
      final File index = getIndex(categories);
      synchronized (index.getAbsolutePath()) {
        final Properties properties = loadIndex(index);

        final Map<String, T> result = new HashMap<String, T>();
        for (final Entry<Object, Object> entry : properties.entrySet()) {
          final String key = (String) entry.getKey();
          final File f = new File(dir + File.separator + entry.getValue());
          if (f.isFile() && (regex == null || key.matches(regex))) {
            result.put(key, getData(clazz, f));
          }
        }
        return result;
      }
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public <T> Map<String, T> getData(final Class<T> clazz, final List<String> categories, final Collection<String> keys) {
    if (keys == null) {
      return Collections.emptyMap();
    }
    final File dir = getPath(categories, false);
    if (dir == null || !dir.exists()) {
      return Collections.emptyMap();
    }
    try {
      final File index = getIndex(categories);
      synchronized (index.getAbsolutePath()) {
        final Properties properties = loadIndex(index);

        final Map<String, T> result = new HashMap<String, T>();
        for (final Entry<Object, Object> entry : properties.entrySet()) {
          final String key = (String) entry.getKey();
          if (keys.contains(key)) {
            final File f = new File(dir + File.separator + entry.getValue());
            if (f.isFile()) {
              result.put(key, getData(clazz, f));

            }
          }
        }
        return result;
      }
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public <T> Map<String, T> getData(final Class<T> clazz, final List<String> categories) {
    return getDataFromRegex(clazz, categories, null);
  }

  @Override
  public void storeData(final List<String> categories, final String key, final Serializable value,
      final boolean overWrite) {
    try {
      File file = getFromIndex(categories, key);

      if (file != null && file.exists()) {
        if (!overWrite) {
          return;
        } else {
          file.delete();
        }
      }
      final int lastDotIndex = key.lastIndexOf('.');
      String extension = "";
      if (lastDotIndex > 0) {
        extension = key.substring(lastDotIndex);
      }
      final String fileName = UUID.randomUUID().toString() + extension;
      file = new File(getPath(categories, true) + File.separator + fileName);
      file.createNewFile();
      Misc.write(file, Misc.serialize(value));
      addEntryToIndex(categories, key, fileName);
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  @Override
  public Set<String> getKeys() {
    final Set<String> keys = new HashSet<String>();
    try {
      analysePath(this.base, keys);
    } catch (final Exception e) {
      throw new BonitaRuntimeException(e);
    }
    return keys;
  }

  private void analysePath(final File dir, final Set<String> keys) throws FileNotFoundException, IOException {
    final File[] files = dir.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.isFile() && !file.getName().equals(INDEX_NAME)) {
          final File index = new File(dir.getAbsolutePath() + File.separator + INDEX_NAME);
          synchronized (index.getAbsolutePath()) {
            final Properties properties = loadIndex(index);
            for (final Entry<Object, Object> entry : properties.entrySet()) {
              if (entry.getValue().equals(file.getName())) {
                keys.add((String) entry.getKey());
              }
            }
          }
        } else if (file.isDirectory()) {
          analysePath(file, keys);
        }
      }
    }
  }

  public String getDataPath(final List<String> categories, final String key) {
    try {
      final File file = getFromIndex(categories, key);
      return file.getPath();
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  /* PRIVATE METHODS */

  @SuppressWarnings("unchecked")
  private <T> T getData(final Class<T> clazz, final File file) {
    try {
      final byte[] value = Misc.getAllContentFrom(file);
      return (T) Misc.deserialize(value);
    } catch (final IOException e) {
      throw new BonitaRuntimeException(e);
    } catch (final ClassNotFoundException e) {
      throw new BonitaRuntimeException(e);
    }
  }

  private File getFromIndex(final List<String> categories, final String key) throws IOException {
    final File index = getIndex(categories);
    synchronized (index.getAbsolutePath()) {
      final Properties properties = loadIndex(index);
      final String file = (String) properties.get(key);
      if (file == null) {
        return null;
      }
      return new File(getPath(categories, false) + File.separator + file);
    }
  }

  private File removeFromIndex(final List<String> categories, final String key) throws IOException {
    final File index = getIndex(categories);
    synchronized (index.getAbsolutePath()) {
      final Properties properties = loadIndex(index);
      final String file = (String) properties.remove(key);
      storeIndex(index, properties);
      return new File(getPath(categories, false) + File.separator + file);
    }
  }

  private File getIndex(final List<String> categories) throws IOException {
    final File dir = getPath(categories, true);
    final File index = new File(dir.getAbsolutePath() + File.separator + INDEX_NAME);
    synchronized (dir.getAbsolutePath()) {
      if (!index.exists()) {
        index.createNewFile();
      }
    }
    return index;
  }

  private void addEntryToIndex(final List<String> categories, final String key, final String value)
      throws FileNotFoundException, IOException {
    final File index = getIndex(categories);
    synchronized (index.getAbsolutePath()) {
      final Properties properties = loadIndex(index);
      properties.setProperty(key, value);
      storeIndex(index, properties);
    }
  }

  private Properties loadIndex(final File index) throws FileNotFoundException, IOException {
    final Properties properties = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(index);
      properties.load(fis);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
    return properties;
  }

  private void storeIndex(final File index, final Properties properties) throws FileNotFoundException, IOException {
    final FileOutputStream fos = new FileOutputStream(index);
    try {
      properties.store(fos, null);
    } finally {
      fos.close();
    }
  }

  public File getPath(final List<String> categories, final boolean create) {
    final List<String> convertedCategories = convertCategories(categories);

    File dir = base;
    if (categories != null) {
      for (final String category : convertedCategories) {
        final File file = getFile(dir, category);
        if (file == null && create) {
          dir = new File(dir.getAbsolutePath() + File.separator + category);
          dir.mkdirs();
        } else if (file == null) {
          return null;
        } else {
          dir = file;
        }
      }
    }
    return dir;
  }

  private List<String> convertCategories(final List<String> categories) {
    final List<String> result = new ArrayList<String>();
    for (final String category : categories) {
      result.add(Misc.convertToJavaIdentifier(category));
    }
    return result;
  }

  private File getFile(final File currentDir, final String category) {
    final File[] files = currentDir.listFiles();
    if (files == null || files.length == 0) {
      return null;
    }
    for (final File file : files) {
      if (file.isDirectory() && file.getName().equals(category)) {
        return file;
      }
    }
    return null;
  }

  private void createRepository() {
    if (LOG.isLoggable(Level.CONFIG)) {
      LOG.config("Configuring Large Data Repository: " + FileLargeDataRepository.class.getName() + " with base: "
          + base);
    }
    // if repo directory does not exist, create it in mutual exclusion
    // all is done in synchronized blocks, because double-checked locking pattern is broken
    synchronized (CONSTRUCTOR_MUTEX) {
      if (this.base.exists()) {
        if (!this.base.isDirectory() || !this.base.canRead() || !this.base.canWrite()) {
          final String message = ExceptionManager.getInstance().getFullMessage("bai_QDAPII_20",
              this.base.getAbsolutePath());
          throw new BonitaRuntimeException(message);
        }
      } else if (!this.base.mkdirs()) {
        final String message = ExceptionManager.getInstance().getFullMessage("bai_QDAPII_21",
            this.base.getAbsolutePath());
        throw new BonitaRuntimeException(message);
      }
    }
  }

}
