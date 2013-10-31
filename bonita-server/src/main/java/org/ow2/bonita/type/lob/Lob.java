/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ow2.bonita.type.lob;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import org.ow2.bonita.util.EnvTool;


/**
 * @author Tom Baeyens
 */
public class Lob implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final BlobStrategy DEFAULT_BLOB_STRATEGY = new BlobStrategyBlob();
  public static final ClobStrategy DEFAULT_CLOB_STRATEGY = new ClobStrategyChopped();
  
  protected long dbid;
  protected int dbversion;
  
  // cachedBytes is used by the BlobStrategyBlob as hibernate doesn't allow 
  // blobs to be read in the same session as they are created in.
  // So cachedBytes should not be persisted 
  protected byte[] cachedBytes = null;
  protected java.sql.Blob blob = null;
  protected byte[] bytes = null;
  protected List<BytesChop> bytesChops = null;
  
  protected char[] cachedChars = null;
  protected java.sql.Clob clob = null;
  protected String text = null;
  protected List<CharChop> charChops = null;
  
  // Only for Hibernate. Do not use directly!
  public Lob() {
  }
  
  /**
   * @param bytes The actual bytes that will be stored.
   */
  public Lob(byte[] bytes) {
    cachedBytes = bytes;
    getBlobStrategy().set(bytes, this);
  }

  public Lob(char[] text) {
    cachedChars = text;
    getClobStrategy().set(text, this);
  }


  public char[] extractChars() {
    return getClobStrategy().get(this);
  }


  
  public byte[] extractBytes() {
    return getBlobStrategy().get(this);
  }

  protected ClobStrategy getClobStrategy() {
    return EnvTool.getClobStrategy();
  }

  protected BlobStrategy getBlobStrategy() {
    return EnvTool.getBlobStrategy();
  }
  
  protected Object readResolve() throws ObjectStreamException {
    if (cachedBytes!=null) {
      getBlobStrategy().set(cachedBytes, this);
    }
    if (cachedChars!=null) {
      getClobStrategy().set(cachedChars, this);
    }
    return this;
  }

  protected Object writeReplace() throws ObjectStreamException {
    blob = null;
    clob = null;
    return this;
  }

  
}
