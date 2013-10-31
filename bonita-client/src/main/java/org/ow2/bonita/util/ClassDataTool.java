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
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.ow2.util.asm.AnnotationVisitor;
import org.ow2.util.asm.Attribute;
import org.ow2.util.asm.ClassReader;
import org.ow2.util.asm.ClassVisitor;
import org.ow2.util.asm.FieldVisitor;
import org.ow2.util.asm.MethodVisitor;

/**
 * @author Pierre Vigneras
 */
public final class ClassDataTool {

  private ClassDataTool() { }

  public static class MyVisitor implements ClassVisitor {
    private String className = null;
    private String superClassName = null;
    private String[] interfaces = null;

    public void visit(final int version, final int access, final String name, final String signature,
        final String superName, final String[] interfaces) {
      this.className = name;
      this.interfaces = interfaces;
      this.superClassName = superName;
    }

    public void visitSource(final String source, final String debug) { }
    public void visitOuterClass(final String owner, final String name, final String desc) { }
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) { return null; }
    public void visitAttribute(final Attribute attr) { }
    public void visitInnerClass(final String name, final String outerName, final String innerName,
        final int access) { }
    public FieldVisitor visitField(final int access, final String name, final String desc,
        final String signature, final Object value) { return null; }
    public MethodVisitor visitMethod(final int access, final String name, final String desc,
        final String signature, final String[] exceptions) { return null; }
    public void visitEnd() { }

    public String getClassName() {
      return this.className.replace("/", ".");
    }

    public String getSuperClassName() {
      return this.superClassName;
    }

    public String[] getInterfaces() {
      return this.interfaces;
    }
  }

  public static MyVisitor visitClass(final byte[] data) {
    final MyVisitor mv = new MyVisitor();
    final ClassReader cr = new ClassReader(data);
    cr.accept(mv, 1);
    return mv;
  }

  public static Set<byte[]> getClasses(final Class< ? >... classes) throws IOException {
    final Set<byte[]> classesSet = new HashSet<byte[]>();
    if (classes != null) {
      for (final Class< ? > clazz : classes) {
        if (clazz == null) {
        	String message = ExceptionManager.getInstance().getFullMessage("buc_CDT_1");
          throw new IOException(message);
        }
        classesSet.add(getClassData(clazz));
      }
    }
    return classesSet;
  }

  public static byte[] getClassData(final Class< ? > clazz) throws IOException {
    if (clazz == null) {
    	String message = ExceptionManager.getInstance().getFullMessage("buc_CDT_2");
      throw new IOException(message);
    }
    final String resource = clazz.getName().replace('.', '/') + ".class";
    InputStream inputStream = clazz.getClassLoader().getResourceAsStream(resource);
    byte[] data = null;
    try {
      if (inputStream == null) {
        throw new IOException("Impossible to get stream from class: " + clazz.getName() + ", className= " + resource);
      }
      data = Misc.getAllContentFrom(inputStream);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return data;
  }

}
