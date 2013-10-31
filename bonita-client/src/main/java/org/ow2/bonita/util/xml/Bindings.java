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
package org.ow2.bonita.util.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

/**
 * a set of {@link Binding}s divided into categories managed by a {@link Parser}
 * .
 * 
 * <a href="./Parser.html#bindings">See also 'Bindinds'</a>
 * 
 * @see Parser
 * @author Tom Baeyens
 */
public class Bindings {

  /** maps categories to a list of bindings */
  protected Map<String, List<Binding>> bindings = null;

  public Bindings() {
  }

  /**
   * to be used when you want to customize the binding behaviour of a
   * {@link Parser}.
   */
  public Bindings(Bindings other) {
    if (other.bindings != null) {
      bindings = new HashMap<String, List<Binding>>();
      Set<String> categorieNames = other.bindings.keySet();
      if (categorieNames != null) {
        for (String categoryName : categorieNames) {
          List<Binding> categoryBindings = other.bindings.get(categoryName);
          List<Binding> categoryBindingsCopy = new ArrayList<Binding>(
              categoryBindings);
          bindings.put(categoryName, categoryBindingsCopy);
        }
      }
    }
  }

  /**
   * get a binding for the given element and category. If the category is null,
   * then all the categories will be searched for a binding in random order.
   */
  public Binding getBinding(Element element) {
    return getBinding(element, (String) null);
  }

  /**
   * get a binding for the given element and category. If the category is null,
   * then all the categories will be searched for a binding in random order.
   */
  public Binding getBinding(Element element, String category) {
    if (bindings == null) {
      return null;
    }
    // if no category is specified
    if (category == null) {
      // search in all categories
      for (List<Binding> categoryBindings : bindings.values()) {
        Binding binding = getBinding(element, categoryBindings);
        if (binding != null) {
          return binding;
        }
      }
    } else {
      List<Binding> categoryBindings = bindings.get(category);
      if (categoryBindings != null) {
        return getBinding(element, categoryBindings);
      }
    }
    return null;
  }

  protected Binding getBinding(Element element, List<Binding> categoryBindings) {
    for (Binding binding : categoryBindings) {
      if (binding.matches(element)) {
        return binding;
      }
    }
    return null;
  }

  /**
   * add an elementParser to this parser that will handle parsing of elements of
   * the given tagName for the default category.
   */
  public void addBinding(Binding binding) {
    if (bindings == null) {
      bindings = new HashMap<String, List<Binding>>();
    }
    String category = binding.getCategory();
    List<Binding> categoryBindings = bindings.get(category);
    if (categoryBindings == null) {
      categoryBindings = new ArrayList<Binding>();
      bindings.put(category, categoryBindings);
    }
    categoryBindings.add(binding);
  }

  /**
   * the set of all tagNames for which there is a binding specified in the given
   * category
   */
  public Set<String> getTagNames(String category) {
    List<Binding> categoryBindings = null;
    if (bindings != null) {
      categoryBindings = bindings.get(category);
    } else {
      categoryBindings = Collections.emptyList();
    }
    Set<String> tagNames = new HashSet<String>();
    for (Binding binding : categoryBindings) {
      tagNames.add(binding.toString());
    }
    return tagNames;
  }
}
