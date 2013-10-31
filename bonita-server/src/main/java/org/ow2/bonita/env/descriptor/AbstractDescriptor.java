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
package org.ow2.bonita.env.descriptor;

import java.io.Serializable;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.util.DefaultObservable;

/**
 * base class for {@link Descriptor}s.
 * 
 * @author Tom Baeyens
 */
public abstract class AbstractDescriptor extends DefaultObservable implements
    Serializable, Descriptor {

  private static final long serialVersionUID = 1L;

  protected long dbid;
  protected int dbversion;
  protected int version;
  protected String name = null;

  /** lazy creation and delayed initialization */
  public static final char INIT_LAZY = 'L';

  /** eager creation and delayed initialization */
  public static final char INIT_EAGER = 'E';

  /** lazy creation and immediate initialization */
  public static final char INIT_REQUIRED = 'R';

  /** eager creation and immediate initialization */
  public static final char INIT_IMMEDIATE = 'I';

  protected char init = INIT_LAZY;

  public AbstractDescriptor() {
    super();
  }

  public AbstractDescriptor(String name) {
    super();
    this.name = name;
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    return null;
  }

  public boolean isEagerInit() {
    return (init == INIT_EAGER || init == INIT_IMMEDIATE);
  }

  public boolean isDelayable() {
    return (init == INIT_EAGER || init == INIT_LAZY);
  }

  public void initialize(Object object, WireContext wireContext) {
  }

  /** the db primary key. */
  public Long getDbid() {
    return dbid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** see section 'Initialization' of {@link WireContext} */
  public void setInit(char init) {
    this.init = init;
  }
}
