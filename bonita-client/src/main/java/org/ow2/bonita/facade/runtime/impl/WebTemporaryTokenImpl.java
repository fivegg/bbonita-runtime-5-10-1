/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.facade.runtime.impl;

import org.ow2.bonita.facade.runtime.WebTemporaryToken;


/**
 * @author Nicolas Chabanoles
 *
 */
public class WebTemporaryTokenImpl implements WebTemporaryToken {
  
  private static final long serialVersionUID = -5768466751532535489L;
  
  protected long dbid;
  
  private String token;
  private long expirationDate;
  private String identityKey;

  public WebTemporaryTokenImpl() {
    super();
  }
  
  public WebTemporaryTokenImpl(WebTemporaryTokenImpl src) {
    super();
    this.token = src.getToken();
    this.expirationDate = src.getExpirationDate();
    this.identityKey = src.getIdentityKey();
  }

  public WebTemporaryTokenImpl(String token, long expirationDate, String identityKey) {
    super();
    this.token = token;
    this.expirationDate = expirationDate;
    this.identityKey = identityKey;
  }
  
  public long getExpirationDate() {
    return this.expirationDate;
  }

  public String getIdentityKey() {
    return this.identityKey;
  }

  public String getToken() {
    return this.token;
  }
}
