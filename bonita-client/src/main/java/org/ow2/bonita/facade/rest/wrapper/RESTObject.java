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
package org.ow2.bonita.facade.rest.wrapper;

import java.io.IOException;
import java.io.Serializable;

import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.xml.XStreamUtil;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class RESTObject implements Serializable{
	private static final long serialVersionUID = -8454151825681540728L;
	private byte[] value;
	
	public RESTObject (Serializable clientObject) throws IOException, ClassNotFoundException{
		this.value = Misc.serialize(clientObject);
	}
	
	public Serializable getObject() throws IOException, ClassNotFoundException{
		return Misc.deserialize(value);
	}
	
	@Override
	public String toString() {
		XStream xstream = XStreamUtil.getDefaultXstream();
	  return xstream.toXML(this);
	}
	
	public static RESTObject valueOf(String str){
		XStream xstream = XStreamUtil.getDefaultXstream();
		return (RESTObject)xstream.fromXML(str);
	}
}
