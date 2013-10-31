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
package org.ow2.bonita.facade.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Path;

import junit.framework.TestCase;

import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.internal.AbstractRemoteManagementAPI;
import org.ow2.bonita.facade.internal.AbstractRemoteRuntimeAPI;
import org.ow2.bonita.facade.internal.RESTRemoteIdentityAPI;
import org.ow2.bonita.facade.internal.RESTRemoteManagementAPI;
import org.ow2.bonita.facade.internal.RESTRemoteQueryRuntimeAPI;
import org.ow2.bonita.facade.internal.RESTRemoteRepairAPI;
import org.ow2.bonita.facade.internal.RESTRemoteRuntimeAPI;
import org.ow2.bonita.facade.internal.RESTRemoteWebAPI;
import org.ow2.bonita.facade.internal.RemoteBAMAPI;
import org.ow2.bonita.facade.internal.RemoteCommandAPI;
import org.ow2.bonita.facade.internal.RemoteQueryDefinitionAPI;
import org.ow2.bonita.facade.uuid.DocumentUUID;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;

/**
 * 
 * @author Elias Ricken de Medeiros
 *
 */
public class ConflictingPathsTest extends TestCase {
	
  // <method, type de parametre>
  private final Map<Method, Class<?>> allowedMissingAnnotations;

  //methodnames
  private final List<Method> allowByteArrayAnnotations;
  
  public ConflictingPathsTest() throws SecurityException, NoSuchMethodException {
    this.allowedMissingAnnotations = new HashMap<Method, Class<?>>();
    this.allowByteArrayAnnotations = new ArrayList<Method>();
    initiazeAllowedMissingAnnotations();
    initiazeAllowedByteArrayAnnotations();
  }

  private void initiazeAllowedMissingAnnotations() throws SecurityException, NoSuchMethodException {
    final Method deploy = AbstractRemoteManagementAPI.class.getMethod("deploy", BusinessArchive.class, Map.class);
    final Method executeConnectorWithClassLoader = AbstractRemoteRuntimeAPI.class.getMethod("executeConnector", 
        String.class, Map.class, ClassLoader.class, Map.class);
    final Method executeFilterWithClassLoader = AbstractRemoteRuntimeAPI.class.getMethod("executeFilter", 
        String.class, Map.class, Set.class, ClassLoader.class, Map.class);
    final Method executeRoleResolverWithClassLoader = AbstractRemoteRuntimeAPI.class.getMethod("executeRoleResolver", 
        String.class, Map.class, ClassLoader.class, Map.class);
    
    //allowed methods
    this.allowedMissingAnnotations.put(deploy, BusinessArchive.class);
    this.allowedMissingAnnotations.put(executeConnectorWithClassLoader, ClassLoader.class);
    this.allowedMissingAnnotations.put(executeFilterWithClassLoader, ClassLoader.class);
    this.allowedMissingAnnotations.put(executeRoleResolverWithClassLoader, ClassLoader.class);
  }
   
  private void initiazeAllowedByteArrayAnnotations() throws SecurityException, NoSuchMethodException {
    final Method addAttachment = AbstractRemoteRuntimeAPI.class.getMethod("addAttachment", ProcessInstanceUUID.class, String.class, String.class, byte[].class,Map.class);
    final Method addAttachmentWithDescr = AbstractRemoteRuntimeAPI.class.getMethod("addAttachment", ProcessInstanceUUID.class, String.class, 
        String.class, String.class, String.class, Map.class, byte[].class, Map.class);
    final Method createProcessDefDoc = AbstractRemoteRuntimeAPI.class.getMethod("createDocument", String.class, ProcessDefinitionUUID.class, String.class, String.class, byte[].class,Map.class);
    final Method createProcessInstDoc = AbstractRemoteRuntimeAPI.class.getMethod("createDocument", String.class, ProcessInstanceUUID.class, String.class, String.class, byte[].class, Map.class);
    final Method addDocumentVersion = AbstractRemoteRuntimeAPI.class.getMethod("addDocumentVersion", DocumentUUID.class, boolean.class, String.class, String.class, byte[].class, Map.class);
    
    //allowed methods
    this.allowByteArrayAnnotations.add(addAttachment);
    this.allowByteArrayAnnotations.add(addAttachmentWithDescr);
    this.allowByteArrayAnnotations.add(createProcessDefDoc);
    this.allowByteArrayAnnotations.add(createProcessInstDoc);
    this.allowByteArrayAnnotations.add(addDocumentVersion);
  }
  
	public void testVerifyConflictingPaths() {
		final List<Class<?>> jaxrsAnnotatedClasses = getJaxrsAnnotatedClasses();
		
		Map<String, Method> paths = new HashMap<String, Method>();
		Map<String, Method> genericPaths = new HashMap<String, Method>();
		for (Class<?> clazz : jaxrsAnnotatedClasses) {
			String rootPathValue = null;
			for (Annotation annotation : clazz.getAnnotations()) {
				if (annotation instanceof Path) {
					rootPathValue = ((Path) annotation).value();
				}
			}
			assertNotNull(rootPathValue);
			assertTrue(!rootPathValue.equals(""));
			
			for (Method method : clazz.getMethods()) {
				for (Annotation methodAnnotation : method.getAnnotations()) {
					if (methodAnnotation instanceof Path) {
						String pathValue = rootPathValue + ((Path) methodAnnotation).value();
						
						if (pathValue == null || pathValue.trim().equals("")) {
							fail("The method does not contains a path value.\nIn method: " + method);
						}
						
						if (pathValue.endsWith("/")) {
							fail("The path value ends with \"/\".\nIn method: " + method);
						}
						assertTrue(!pathValue.endsWith("/"));
						
						if (paths.containsKey(pathValue)) {
							fail("The path \"" + pathValue + "\" is already used by the method " 
									+ paths.get(pathValue) + ".\nIn method: " + method);
						}
						paths.put(pathValue, method);
						
						String genericPath = getGenericPath(pathValue);
						
						if (genericPaths.containsKey(genericPath)) {
							fail("Conflicting path with the method " 
									+ genericPaths.get(genericPath) + ".\nIn method: " + method);
						}
												
						genericPaths.put(genericPath, method);
					}
				}
			}
		}
	}

  private List<Class<?>> getJaxrsAnnotatedClasses() {
    final List<Class<?>> jaxrsAnnotatedClasses = new ArrayList<Class<?>>();
		jaxrsAnnotatedClasses.add(RemoteBAMAPI.class);
		jaxrsAnnotatedClasses.add(RemoteCommandAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteIdentityAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteManagementAPI.class);
		jaxrsAnnotatedClasses.add(RemoteQueryDefinitionAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteQueryRuntimeAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteRepairAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteRuntimeAPI.class);
		jaxrsAnnotatedClasses.add(RESTRemoteWebAPI.class);
    return jaxrsAnnotatedClasses;
  }

	/**
	 * @param pathValue
	 * @return
	 */
	private String getGenericPath(String pathValue) {
		StringTokenizer tokenizer = new StringTokenizer(pathValue, "{}", true);
		boolean replaceToken = false;
		StringBuilder strBuilder = new StringBuilder();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (replaceToken) {
				strBuilder.append("aPathParam");
			} else {
				strBuilder.append(token);
			}
			if (token.equals("{")) {
				replaceToken = true;
			} else if (token.equals("}")) {
				replaceToken = false;
				strBuilder.append(token);
			}
		}
		
		return strBuilder.toString();
	}
	
	public void testVerifyMissingAnnotations() {
	  final List<Class<?>> jaxrsAnnotatedClasses = getJaxrsAnnotatedClasses();
	  final List<Method> methodsWithProblems = new ArrayList<Method>();
	  for (final Class<?> clazz : jaxrsAnnotatedClasses) {
	    //annotations for all parameters. Each parameter is associated to one array even if doesn't contain annotations
      for (final Method m : clazz.getMethods()) {
        final Annotation[][] parametersAnnotations = m.getParameterAnnotations();
        final Class<?>[] parameterTypes = m.getParameterTypes();
        //anotations por each parameter
        for (int i = 0; i < parametersAnnotations.length; i++) {
          if (parametersAnnotations[i].length != 1 && !parameterTypes[i].equals(byte[].class) && !isMissingAnnotationAllowed(m, parameterTypes[i])) {
            methodsWithProblems.add(m);
          }
          
        }
      }
    }
	  
	  final StringBuilder stb = new StringBuilder("Some parameters don't have jaxrs annotations (or have more than one) in the following methods:");
	  for (Method method : methodsWithProblems) {
	    stb.append("\n\t-->");
      stb.append(method);
    }
    assertTrue(stb.toString(), methodsWithProblems.size() == 0);
	}

  private boolean isMissingAnnotationAllowed(final Method m, final Class<?> clazz) {
    final Class<?> type = this.allowedMissingAnnotations.get(m);
    boolean result = false;
    if (type != null) {
      result = type.equals(clazz);
    }
    return result;
  }
  
  public void testVerifyByteArrays() {
    final List<Class<?>> jaxrsAnnotatedClasses = getJaxrsAnnotatedClasses();
    final List<Method> methodsWithProblems = new ArrayList<Method>();
    for (final Class<?> clazz : jaxrsAnnotatedClasses) {
      //annotations for all parameters. Each parameter is associated to one array even if doesn't contain annotations
      for (final Method m : clazz.getMethods()) {
        final Annotation[][] parametersAnnotations = m.getParameterAnnotations();
        final Class<?>[] parameterTypes = m.getParameterTypes();
        //anotations por each parameter
        for (int i = 0; i < parametersAnnotations.length; i++) {
          //byte array with annotation
          if (parametersAnnotations[i].length == 1 && parameterTypes[i].equals(byte[].class) 
                && !this.allowByteArrayAnnotations.contains(m)) {
            methodsWithProblems.add(m);
          }
          
        }
      }
    }
    
    final StringBuilder stb = new StringBuilder("Some parameters don't have jaxrs annotations (or have more than one) in the following methods:");
    for (Method method : methodsWithProblems) {
      stb.append("\n\t-->");
      stb.append(method);
    }
    assertTrue(stb.toString(), methodsWithProblems.size() == 0);
  }
}
