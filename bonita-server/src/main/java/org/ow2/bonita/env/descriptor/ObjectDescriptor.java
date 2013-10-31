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
package org.ow2.bonita.env.descriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.env.Descriptor;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.WireContext;
import org.ow2.bonita.env.WireDefinition;
import org.ow2.bonita.env.WireException;
import org.ow2.bonita.env.operation.FieldOperation;
import org.ow2.bonita.env.operation.Operation;
import org.ow2.bonita.env.operation.PropertyOperation;
import org.ow2.bonita.util.ArrayUtil;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.ReflectUtil;
import org.ow2.bonita.util.ReflectUtilDescriptor;

/**
 * <p>
 * This {@link Descriptor} creates and initializes an object. Objects can be
 * instantiated from a constructor or from a method invocation.
 * </p>
 * 
 * <p>
 * The way to create an object is specified one of these methods (see <a
 * href='#create'>creating objects</a>):
 * <ul>
 * <li>className ({@link #setClassName(String)})</li>
 * <li>factoryObjectName ({@link #setFactoryObjectName(String)})</li>
 * <li>factoryDescriptor ({@link #setFactoryDescriptor(Descriptor)})</li>
 * </ul>
 * Only one of these methods can be used.
 * </p>
 * 
 * <h3 id='create'>Creating objects</h3> <h4>Creating object from a constructor</h4>
 * 
 * <p>
 * This method is used when
 * <code>{@link #getClassName()}!=null && {@link #getMethodName()}==null</code>.
 * </p>
 * 
 * <p>
 * The {@link #construct(WireContext)} method creates a new object from a
 * constructor matching the given arguments (specified with
 * {@link #setArgDescriptors(List)}).
 * </p>
 * 
 * 
 * <h4>Creating an object from a method invocation</h4>
 * 
 * <p>
 * The name of the method to call is specified by the method attribute.
 * </p>
 * <ul>
 * <li>If the method is <i>static</i>, the related class is
 * {@link #getClassName()}.</li>
 * <li>If the method is an object method, the object to which the method will be
 * applied is defined by:
 * <ul>
 * <li>If <code>{@link #getFactoryObjectName()}!=null</code>: the object with the name
 * factoryObjectName will be fetched from the context.</li>
 * <li>if <code>{@link #getFactoryDescriptor()}!=null</code>: the object will be
 * created from the factory descriptor.</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * The object returned by {@link #construct(WireContext)} is the object returned
 * by the method invocation.
 * </p>
 * 
 * 
 * <h3>Initializing Objects</h3> <h4>Auto Wiring</h4>
 * <p>
 * If the auto wiring is enabled for the object (
 * <code>{@link #isAutoWireEnabled()}==true</code>), the WireContext will try to
 * look for objects with the same name as the fields in the class. If it finds
 * an object with that name, and if it is assignable to the field's type, it is
 * automatically injected, without the need for explicit {@link FieldOperation}
 * that specifies the injection in the wiring xml.
 * </p>
 * <p>
 * If the auto wiring is enabled and the WireContext finds an object with the
 * name of a field, but not assignable to this field, a warning message is
 * generated.
 * </p>
 * 
 * <p>
 * Auto-wiring is disabled by default.
 * </p>
 * 
 * <h4>Operations</h4>
 * <p>
 * Field injection or property injection are done after the auto-wiring. For
 * more information, see {@link Operation}.
 * </p>
 * 
 * <p>
 * If a field was injected by auto-wiring, its value can be overridden by
 * specifying a {@link FieldOperation} or {@link PropertyOperation} operation.
 * </p>
 * 
 * @author Tom Baeyens
 * @author Guillaume Porcher (documentation)
 * 
 */
public class ObjectDescriptor extends AbstractDescriptor implements Descriptor {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(ObjectDescriptor.class.getName());

  String className = null;

  /**
   * specifies the object reference on which the method will be invoked. Either
   * className, objectName or a descriptor has to be specified.
   * 
   * TODO check if this member can be replaced by a RefDescriptor in the
   * factoryDescriptor member.
   * 
   * */
  String factoryObjectName = null;

  /**
   * specifies the object on which to invoke the method. Either className,
   * objectName or a descriptor has to be specified.
   */
  Descriptor factoryDescriptor = null;

  String methodName = null;

  /** map to db as a component */
  List<ArgDescriptor> argDescriptors = null;
  /** list of operations to perform during initialization. */
  List<Operation> operations = null;

  /** True if autowiring is enabled. */
  boolean isAutoWireEnabled = false;

  public ObjectDescriptor() {
    super();
  }

  public ObjectDescriptor(String className) {
    super();
    this.className = className;
  }

  public ObjectDescriptor(Class<?> clazz) {
    super();
    this.className = clazz.getName();
  }

  /**
   * This method constructs a new Object from the ObjectDefinition. This object
   * will be created from a class constructor or from a method invocation.
   * 
   * @throws WireException
   *           one of the following exception occurred:
   *           <ul>
   *           <li>if the object cannot be created (unable to load the specified
   *           class or no matching constructor found)</li>
   *           <li>if the invocation of the specified method failed</li>
   *           </ul>
   * @see ObjectDescriptor
   */
  public Object construct(WireContext wireContext) {
    Object object = null;
    Class<?> clazz = null;

    if (className != null) {
      try {
        ClassLoader classLoader = wireContext.getClassLoader();
        clazz = ReflectUtil.loadClass(classLoader, className);
      } catch (Exception e) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_OD_1", (name != null ? " '" + name + "'" : ""), e.getMessage());
        throw new WireException(message, e);
      }

      if (methodName == null) {
        // plain instantiation
        try {
          Object[] args = getArgs(wireContext, argDescriptors);
          Constructor<?> constructor = ReflectUtilDescriptor.findConstructor(clazz,
              argDescriptors, args);
          if (constructor == null) {
          	String message = ExceptionManager.getInstance().getFullMessage(
          			"bp_OD_2", clazz.getName(), ArrayUtil.toString(args));
            throw new WireException(message);
          }
          object = constructor.newInstance(args);
        } catch (WireException e) {
          throw e;
        } catch (Exception e) {
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_OD_3", (name != null ? name : className), e.getMessage());
          throw new WireException(message, e);
        }
      }

    } else if (factoryObjectName != null) {
      // referenced factory object
      object = wireContext.get(factoryObjectName, false);
      if (object == null) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_OD_4", methodName, factoryObjectName);
        throw new WireException(message);
      }

    } else if (factoryDescriptor != null) {
      // factory object descriptor
      object = wireContext.create(factoryDescriptor, false);
      if (object == null) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_OD_5", methodName);
        throw new WireException(message);
      }
    }

    if (methodName != null) {
      // method invocation on object or static method invocation in case object
      // is null
      if (object != null) {
        clazz = object.getClass();
      }
      try {
        Object[] args = ObjectDescriptor.getArgs(wireContext, argDescriptors);
        Method method = ReflectUtilDescriptor.findMethod(clazz, methodName,
            argDescriptors, args);
        if (method == null) {
          // throw exception but first, generate decent message
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_OD_6", ReflectUtilDescriptor.getSignature(methodName, argDescriptors, args),
        			(object != null ? "object " + object + " (" + clazz.getName()
                  + ")" : "class " + clazz.getName()));
          throw new WireException(message);
        }
        if (object == null && (!Modifier.isStatic(method.getModifiers()))) {
          // A non static method is invoked on a null object
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_OD_7", clazz.getName(), ReflectUtilDescriptor.getSignature(methodName, argDescriptors, args));
          throw new WireException(message);
        }
        object = ReflectUtil.invoke(method, object, args);

      } catch (WireException e) {
        throw e;
      } catch (Exception e) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_OD_8", methodName, e.getMessage());
        throw new WireException(message, e);
      }
    }

    return object;
  }

  /**
   * Initializes the specified object. If auto-wiring was set to
   * <code>true</code>, auto-wiring is performed (see
   * {@link #autoWire(Object, WireContext)}). Fields and properties injections
   * are then performed.
   * 
   */
  public void initialize(Object object, WireContext wireContext) {
    try {
      // specified operations takes precedence over auto-wiring.
      // e.g. in case there is a collision between
      // a field or property injection and an autowired value,
      // the field or property injections should win.
      // That is why autowiring is done first
      if (isAutoWireEnabled) {
        autoWire(object, wireContext);
      }
      if (operations != null) {
        for (Operation operation : operations) {
          operation.apply(object, wireContext);
        }
      }
    } catch (Exception e) {
    	String message = ExceptionManager.getInstance().getMessage(
    			"bp_OD_9", (name != null ? name : className), e.getMessage());
      throw new WireException(message, e);
    }
  }

  public Class<?> getType(WireDefinition wireDefinition) {
    if (className != null) {
      try {
        return ReflectUtil
            .loadClass(wireDefinition.getClassLoader(), className);
      } catch (BonitaRuntimeException e) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bp_OD_10", (name != null ? name : className),  e.getMessage());
        throw new WireException(message, e
            .getCause());
      }
    }

    Descriptor descriptor = null;
    if (factoryDescriptor != null) {
      descriptor = factoryDescriptor;
    } else if (factoryObjectName != null) {
      descriptor = wireDefinition.getDescriptor(factoryObjectName);
    }

    if (descriptor != null) {
      Class<?> factoryClass = descriptor.getType(wireDefinition);
      if (factoryClass != null) {
        Method method = ReflectUtilDescriptor.findMethod(factoryClass, methodName,
            argDescriptors, null);
        if (method != null) {
          return method.getReturnType();
        }
      }
    }
    return null;
  }

  /**
   * Auto wire object present in the context and the specified object's fields.
   * 
   * @param object
   *          object on which auto-wiring is performed.
   * @param wireContext
   *          context in which the wiring objects are searched.
   */
  protected void autoWire(Object object, WireContext wireContext) {
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      Field[] declaredFields = clazz.getDeclaredFields();
      if (declaredFields != null) {
        for (Field field : declaredFields) {
          if (!Modifier.isStatic(field.getModifiers())) {
            String fieldName = field.getName();

            Object autoWireValue = null;
            if ("environment".equals(fieldName)) {
              autoWireValue = Environment.getCurrent();

            } else if (("context".equals(fieldName))
                || ("wireContext".equals(fieldName))) {
              autoWireValue = wireContext;

            } else if (wireContext.has(fieldName)) {
              autoWireValue = wireContext.get(fieldName);

            } else {
              autoWireValue = wireContext.get(field.getType());
            }
            // if auto wire value has not been found in current context,
            // search in environment
            if (autoWireValue == null) {
              Environment currentEnvironment = Environment.getCurrent();
              if (currentEnvironment != null) {
                autoWireValue = currentEnvironment.get(fieldName);
                if (autoWireValue == null) {
                  autoWireValue = currentEnvironment.get(field.getType());
                }
              }
            }

            if (autoWireValue != null) {
              try {
              	if (LOG.isLoggable(Level.FINE)) {
                  LOG.fine("auto wiring field " + fieldName + " in " + name);
              	}
                ReflectUtil.set(field, object, autoWireValue);
              } catch (BonitaRuntimeException e) {
                if (e.getCause() instanceof IllegalArgumentException) {
                	if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("WARNING: couldn't auto wire " + fieldName
                      + " (of type " + field.getType().getName() + ") "
                      + "with value " + autoWireValue + " (of type "
                      + autoWireValue.getClass().getName() + ")");
                	}
                } else {
                	if (LOG.isLoggable(Level.INFO)) {
                    LOG.info("WARNING: couldn't auto wire " + fieldName + " with value " + autoWireValue);
                	}
                }
              }
            }
          }
        }
      }
      clazz = clazz.getSuperclass();
    }
  }

  /**
   * Creates a list of arguments (objects) from a list of argument descriptors.
   * 
   * @param wireContext
   *          context used to create objects.
   * @param argDescriptors
   *          list of argument descriptors.
   * @return a list of object created from the descriptors.
   * @throws Exception
   */
  public static Object[] getArgs(WireContext wireContext,
      List<ArgDescriptor> argDescriptors) throws Exception {
    Object[] args = null;
    if (argDescriptors != null) {
      args = new Object[argDescriptors.size()];
      for (int i = 0; i < argDescriptors.size(); i++) {
        ArgDescriptor argDescriptor = argDescriptors.get(i);
        Object arg;
        try {
          arg = wireContext.create(argDescriptor.getDescriptor(), true);
          args[i] = arg;
        } catch (RuntimeException e) {
          // i have made sure that the runtime exception is caught everywhere
          // the getArgs method
          // is used so that a better descriptive exception can be rethrown
        	String message = ExceptionManager.getInstance().getFullMessage(
        			"bp_OD_11", i, e.getMessage());
          throw new Exception(message, e);
        }
      }
    }
    return args;
  }

  /**
   * Adds a argument descriptor to the list of arguments descriptor to used when
   * invoking the specified method.
   * 
   * @param argDescriptor
   *          argument descriptor to add.
   */
  public void addArgDescriptor(ArgDescriptor argDescriptor) {
    if (argDescriptors == null) {
      argDescriptors = new ArrayList<ArgDescriptor>();
    }
    argDescriptors.add(argDescriptor);
  }

  /**
   * Adds an operation to perform during initialization.
   * 
   * @param operation
   *          operation to add.
   */
  public void addOperation(Operation operation) {
    if (operations == null) {
      operations = new ArrayList<Operation>();
    }
    operations.add(operation);
  }

  /** convenience method to add a type based field injection */
  public void addTypedInjection(String fieldName, Class<?> type) {
    addInjection(fieldName, new EnvironmentTypeRefDescriptor(type));
  }

  /** add an injection based on a descriptor */
  public void addInjection(String fieldName, Descriptor descriptor) {
    FieldOperation injectionOperation = new FieldOperation();
    injectionOperation.setFieldName(fieldName);
    injectionOperation.setDescriptor(descriptor);
    addOperation(injectionOperation);
  }

  /**
   * Gets the class name of the object to create. This name is defined only when
   * creating objects from a constructor or when invoking static methods.
   * 
   * @return the name of the class of the object to create.
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets class name of the object to create. This name is defined only when
   * creating objects from a constructor or when invoking static methods. If
   * this name is set, the factoryObjectName and factoryDescriptor should not be
   * set.
   * 
   * @see #setFactoryDescriptor(Descriptor)
   * @see #setFactoryObjectName(String)
   * @param className
   *          name of the class to use.
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Gets the list of descriptors to use to create method arguments.
   * 
   * @return list of descriptors to use to create method arguments.
   */
  public List<ArgDescriptor> getArgDescriptors() {
    return argDescriptors;
  }

  /**
   * Sets the list of descriptors to use to create method arguments.
   * 
   * @param argDescriptors
   *          list of descriptors to use to create method arguments.
   */
  public void setArgDescriptors(List<ArgDescriptor> argDescriptors) {
    this.argDescriptors = argDescriptors;
  }

  /**
   * Gets the list of operations to perform during initialization.
   * 
   * @return list of operations to perform during initialization.
   */
  public List<Operation> getOperations() {
    return operations;
  }

  /**
   * Sets the list of operations to perform during initialization.
   * 
   * @param operations
   *          list of operations to perform during initialization.
   */
  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }

  /**
   * Gets the Descriptor from which the object should be created.
   * 
   * @return the Descriptor from which the object should be created.
   */
  public Descriptor getFactoryDescriptor() {
    return factoryDescriptor;
  }

  /**
   * Sets the Descriptor from which the object should be created. If this
   * Descriptor is set, the className and factoryObjectName should not be set.
   * 
   * @see #setClassName(String)
   * @see #setFactoryObjectName(String)
   * @param factoryDescriptor
   *          the Descriptor from which the object should be created.
   */
  public void setFactoryDescriptor(Descriptor factoryDescriptor) {
    this.factoryDescriptor = factoryDescriptor;
  }

  /**
   * Gets the name of the object to get from the WireContext.
   * 
   * @return name of the object to get from the WireContext.
   */
  public String getFactoryObjectName() {
    return factoryObjectName;
  }

  /**
   * Sets name of the object to get from the WireContext. If this name is set,
   * the className and factoryDescriptor should not be set.
   * 
   * @see #setClassName(String)
   * @see #setFactoryDescriptor(Descriptor)
   * @param factoryObjectName
   *          name of the object to get from the WireContext.
   */
  public void setFactoryObjectName(String factoryObjectName) {
    this.factoryObjectName = factoryObjectName;
  }

  /**
   * Gets the name of the method to invoke.
   * 
   * @return name of the method to invoke.
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Sets the name of the method to invoke.
   * 
   * @param methodName
   *          name of the method to invoke.
   */
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  /**
   * Checks if auto-wiring is enabled
   * 
   * @return <code>true</code> if auto-wiring is enabled.
   */
  public boolean isAutoWireEnabled() {
    return isAutoWireEnabled;
  }

  /**
   * Enables/Disables auto wiring mode.
   * 
   * @param isAutoWireEnabled
   *          <code>true</code> to enable auto-wiring.
   */
  public void setAutoWireEnabled(boolean isAutoWireEnabled) {
    this.isAutoWireEnabled = isAutoWireEnabled;
  }
}
