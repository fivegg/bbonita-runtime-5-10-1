/**
 * Copyright (C) 2009  BonitaSoft S.A.
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
package org.ow2.bonita.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Matthieu Chaffotte
 *
 */
public final class ExceptionManager {

	private static ExceptionManager instance = new ExceptionManager();
	private ResourceBundle bundle;

	private ExceptionManager() {
	  Locale currentLocale = Locale.ENGLISH;
		bundle = ResourceBundle.getBundle(this.getClass().getName(), currentLocale);
	};

	public static ExceptionManager getInstance() {
		return instance;
	}

	public String getFullMessage(String id, Object... arguments) {
		StringBuilder message = new StringBuilder();
		if (!containsAlreadyAnId(arguments)) {
			message.append(getIdMessage(id));
		}
		message.append(getMessage(id, arguments));
		return message.toString();
	}
	
	public String getIdMessage(String id) {
		StringBuilder message = new StringBuilder("Bonita Error: ");
		message.append(id);
		message.append("\n");
		return message.toString();
	}

	public String getMessage(String id, Object... arguments) {
		StringBuilder message = new StringBuilder();
		String pattern = getValue(id);
		if (pattern == null) {
			message.append("Bonita Error: " + id + " does not exist.");
		} else {
			String msg = MessageFormat.format(pattern, arguments);
			message.append(msg);
		}
		message.append("\n");
		return message.toString();
	}
	
	private String getValue(String id) {
		try {
			return bundle.getString(id);
		} catch(MissingResourceException mre) {
			return null;
		}
	}

	private boolean containsAlreadyAnId(Object... arguments) {
		boolean contains = false;
		if (arguments !=null) {
			for (Object object : arguments) {
	      if (object instanceof String) {
	      	String temp = (String) object;
	      	if (temp.contains("Bonita Error")) {
	      		contains = true;
	      		break;
	      	}
	      }
      }
		}
		return contains;
	}

}
