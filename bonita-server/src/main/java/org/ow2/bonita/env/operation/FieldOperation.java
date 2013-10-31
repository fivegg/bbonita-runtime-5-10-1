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
package org.ow2.bonita.env.operation;

import java.lang.reflect.Field;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;

/**
 * injects another object into a field.
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 */
public class FieldOperation extends AbstractOperation {

  private static final long serialVersionUID = 1L;

  String fieldName = null;
  Descriptor descriptor = null;

  transient Field field = null;

  public FieldOperation() {
    super();
  }

  @Override
  public void apply(final Object target, final WireContext wireContext) {
    if (target != null) {

      // Get field
      synchronized (this) {
        if (field == null) {
          final Class<?> clazz = target.getClass();
          field = ReflectUtil.getField(clazz, fieldName);
        }
      }

      // Create value
      final Object value = wireContext.create(descriptor, true);
      // Set the field value
      try {
        ReflectUtil.set(field, target, value);
      } catch (final Exception e) {
        final String message = ExceptionManager.getInstance().getFullMessage("bp_FO_1", fieldName, value);
        throw new WireException(message, e);
      }
    }
  }

  /**
   * Gets the name of the field that should be updated by this operation.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Sets the name of the field that should be updated by this operation.
   * 
   * @param fieldName
   */
  public synchronized void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * Gets the descriptor used to create the field's value.
   */
  public Descriptor getDescriptor() {
    return descriptor;
  }

  /**
   * Sets the descriptor used to create the field's value
   * 
   * @param valueDescriptor
   */
  public synchronized void setDescriptor(final Descriptor valueDescriptor) {
    descriptor = valueDescriptor;
  }

}
