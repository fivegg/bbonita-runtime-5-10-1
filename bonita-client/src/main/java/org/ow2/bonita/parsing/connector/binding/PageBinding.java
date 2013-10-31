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
package org.ow2.bonita.parsing.connector.binding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.bonita.connector.core.desc.Array;
import org.ow2.bonita.connector.core.desc.Checkbox;
import org.ow2.bonita.connector.core.desc.Component;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Enumeration;
import org.ow2.bonita.connector.core.desc.Enumeration.Selection;
import org.ow2.bonita.connector.core.desc.Group;
import org.ow2.bonita.connector.core.desc.Option;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Password;
import org.ow2.bonita.connector.core.desc.Radio;
import org.ow2.bonita.connector.core.desc.Select;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.core.desc.SimpleList;
import org.ow2.bonita.connector.core.desc.Text;
import org.ow2.bonita.connector.core.desc.Textarea;
import org.ow2.bonita.connector.core.desc.WidgetComponent;
import org.ow2.bonita.parsing.def.binding.ElementBinding;
import org.ow2.bonita.util.xml.Parse;
import org.ow2.bonita.util.xml.Parser;
import org.ow2.bonita.util.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthieu Chaffotte
 * 
 */
public class PageBinding extends ElementBinding {

  private static final Set<String> ALLOWED_WIDGETS = new HashSet<String>(10);

  static {
    ALLOWED_WIDGETS.add("text");
    ALLOWED_WIDGETS.add("password");
    ALLOWED_WIDGETS.add("textarea");
    ALLOWED_WIDGETS.add("array");
    ALLOWED_WIDGETS.add("list");
    ALLOWED_WIDGETS.add("checkbox");
    ALLOWED_WIDGETS.add("radio");
    ALLOWED_WIDGETS.add("select");
    ALLOWED_WIDGETS.add("enumeration");
    ALLOWED_WIDGETS.add("group");
  }

  public PageBinding() {
    super("page");
  }

  @Override
  public Object parse(final Element pageElement, final Parse parse, final Parser parser) {
    final ConnectorDescriptor descriptor = parse.findObject(ConnectorDescriptor.class);
    final List<Setter> inputs = descriptor.getInputs();
    final String pageId = getChildTextContent(pageElement, "pageId");
    final List<Component> widgets = getWidgets(pageElement, inputs);
    final Page page = new Page(pageId, widgets);
    descriptor.addPage(page);
    return null;
  }

  private List<Component> getWidgets(final Element page, final List<Setter> inputs) {
    final Element widgetsElement = XmlUtil.element(page, "widgets");
    if (widgetsElement != null) {
      final List<Component> widgets = new ArrayList<Component>();
      final List<Element> elements = XmlUtil.elements(widgetsElement, ALLOWED_WIDGETS);
      for (final Element element : elements) {
        widgets.add(getComponent(element, inputs));
      }
      return widgets;
    } else {
      return null;
    }
  }

  private Component getComponent(final Element componentElement, final List<Setter> inputs) {
    if ("group".equals(componentElement.getNodeName())) {
      final String labelId = getChildTextContent(componentElement, "labelId");
      final String optionalValue = XmlUtil.attribute(componentElement, "optional");
      final boolean optional = Boolean.parseBoolean(optionalValue);
      final Element widgetsElement = XmlUtil.element(componentElement, "widgets");
      List<WidgetComponent> widgets = null;
      if (widgetsElement != null) {
        widgets = new ArrayList<WidgetComponent>();
        final List<Element> widgetsElem = XmlUtil.elements(widgetsElement, ALLOWED_WIDGETS);
        if (widgetsElem != null) {
          for (final Element widgetElement : widgetsElem) {
            widgets.add(getWidget(widgetElement, inputs));
          }
        }
      }
      return new Group(labelId, optional, widgets);
    } else {
      return getWidget(componentElement, inputs);
    }
  }

  private WidgetComponent getWidget(final Element widgetElement, final List<Setter> inputs) {
    WidgetComponent component = null;
    Setter setter = null;
    final String labelId = getChildTextContent(widgetElement, "labelId");
    final Element setterElement = XmlUtil.element(widgetElement, "setter");
    if (setterElement != null) {
      String reference = XmlUtil.attribute(setterElement, "reference");
      if (reference != null) {
        reference = reference.replace("/connector/inputs/setter", "");
        reference = reference.replace("[", "");
        reference = reference.replace("]", "");
        if ("".equals(reference)) {
          setter = inputs.get(0);
        } else {
          setter = inputs.get(Integer.parseInt(reference) - 1);
        }
      }
    }

    if ("text".equals(widgetElement.getNodeName())) {
      final String sizeValue = getChildTextContent(widgetElement, "size");
      int size = 20;
      if (sizeValue != null) {
        size = Integer.parseInt(sizeValue);
      }
      final String maxCharValue = getChildTextContent(widgetElement, "maxChar");
      int maxChar = 100;
      if (maxCharValue != null) {
        maxChar = Integer.parseInt(maxCharValue);
      }
      component = new Text(labelId, setter, size, maxChar);
    } else if ("password".equals(widgetElement.getNodeName())) {
      final String sizeValue = getChildTextContent(widgetElement, "size");
      int size = 20;
      if (sizeValue != null) {
        size = Integer.parseInt(sizeValue);
      }
      final String maxCharValue = getChildTextContent(widgetElement, "maxChar");
      int maxChar = 100;
      if (maxCharValue != null) {
        maxChar = Integer.parseInt(maxCharValue);
      }
      component = new Password(labelId, setter, size, maxChar);
    } else if ("textarea".equals(widgetElement.getNodeName())) {
      int rows = 10;
      final String rowsValue = getChildTextContent(widgetElement, "rows");
      if (rowsValue != null) {
        rows = Integer.parseInt(rowsValue);
      }
      int columns = 60;
      final String columnsValue = getChildTextContent(widgetElement, "columns");
      if (columnsValue != null) {
        columns = Integer.parseInt(columnsValue);
      }
      int maxChar = 100;
      final String maxCharValue = getChildTextContent(widgetElement, "maxChar");
      if (maxCharValue != null) {
        maxChar = Integer.parseInt(maxCharValue);
      }
      int maxCharPerRow = 600;
      final String maxCharPerRowValue = getChildTextContent(widgetElement, "maxCharPerRow");
      if (maxCharPerRowValue != null) {
        maxCharPerRow = Integer.parseInt(maxCharPerRowValue);
      }
      component = new Textarea(labelId, setter, rows, columns, maxChar, maxCharPerRow);
    } else if ("checkbox".equals(widgetElement.getNodeName())) {
      final String name = getChildTextContent(widgetElement, "name");
      final String value = getChildTextContent(widgetElement, "value");
      component = new Checkbox(labelId, setter, name, value);
    } else if ("radio".equals(widgetElement.getNodeName())) {
      final String name = getChildTextContent(widgetElement, "name");
      final String value = getChildTextContent(widgetElement, "value");
      component = new Radio(labelId, setter, name, value);
    } else if ("select".equals(widgetElement.getNodeName())) {
      final String editableElement = getChildTextContent(widgetElement, "editable");
      boolean editable = false;
      if (editableElement != null) {
        editable = Boolean.parseBoolean(editableElement);
      }
      final Element valuesElement = XmlUtil.element(widgetElement, "values");
      Map<String, String> values = null;
      if (valuesElement != null) {
        values = new HashMap<String, String>();
        final List<Element> entries = XmlUtil.elements(valuesElement, "entry");
        if (entries != null) {
          for (final Element entryElement : entries) {
            final List<Element> entry = XmlUtil.elements(entryElement, "string");
            values.put(entry.get(0).getTextContent(), entry.get(1).getTextContent());
          }
        }
      }
      Option top = null;
      final Element topElement = XmlUtil.element(widgetElement, "top");
      if (topElement != null) {
        final String label = getChildTextContent(topElement, "label");
        final String value = getChildTextContent(topElement, "value");
        top = new Option(label, value);
      }
      component = new Select(labelId, setter, values, editable, top);
    } else if ("enumeration".equals(widgetElement.getNodeName())) {
      final Element valuesElement = XmlUtil.element(widgetElement, "values");
      Map<String, String> values = null;
      if (valuesElement != null) {
        values = new HashMap<String, String>();
        final List<Element> entries = XmlUtil.elements(valuesElement, "entry");
        if (entries != null) {
          for (final Element entryElement : entries) {
            final List<Element> entry = XmlUtil.elements(entryElement, "string");
            values.put(entry.get(0).getTextContent(), entry.get(1).getTextContent());
          }
        }
      }
      final String linesValue = getChildTextContent(widgetElement, "lines");
      int lines = 1;
      if (linesValue != null) {
        lines = Integer.parseInt(linesValue);
      }
      final String selectionValue = getChildTextContent(widgetElement, "selection");
      Selection selection = null;
      if (selectionValue != null) {
        if (selectionValue.equals(Selection.SINGLE.toString())) {
          selection = Selection.SINGLE;
        } else if (selectionValue.equals(Selection.MUTLI.toString())) {
          selection = Selection.MUTLI;
        }
      }
      int[] selectedIndices = null;
      final Element selectedIndicesElement = XmlUtil.element(widgetElement, "selectedIndices");
      if (selectedIndicesElement != null) {
        final List<Element> entries = XmlUtil.elements(selectedIndicesElement, "int");
        if (entries != null) {
          selectedIndices = new int[entries.size()];
          for (int i = 0; i < entries.size(); i++) {
            selectedIndices[i] = Integer.parseInt(entries.get(i).getTextContent());
          }
        }
      }
      component = new Enumeration(labelId, setter, values, selectedIndices, lines, selection);
    } else if ("array".equals(widgetElement.getNodeName())) {
      final String colsValue = getChildTextContent(widgetElement, "cols");
      int cols = 2;
      if (colsValue != null) {
        cols = Integer.parseInt(colsValue);
      }
      final String rowsValue = getChildTextContent(widgetElement, "rows");
      int rows = 0;
      if (rowsValue != null) {
        rows = Integer.parseInt(rowsValue);
      }
      final String fixedColsValue = getChildTextContent(widgetElement, "fixedCols");
      boolean fixedCols = true;
      if (fixedColsValue != null) {
        fixedCols = Boolean.parseBoolean(fixedColsValue);
      }
      final String fixedRowsValue = getChildTextContent(widgetElement, "fixedRows");
      boolean fixedRows = false;
      if (fixedRowsValue != null) {
        fixedRows = Boolean.parseBoolean(fixedRowsValue);
      }
      final Element colsCaptionsElement = XmlUtil.element(widgetElement, "colsCaptions");
      List<String> colsCaptions = null;
      if (colsCaptionsElement != null) {
        final List<Element> captionsElement = XmlUtil.elements(colsCaptionsElement, "string");
        if (captionsElement != null) {
          colsCaptions = new ArrayList<String>();
          for (final Element element : captionsElement) {
            colsCaptions.add(element.getTextContent());
          }
        }
      }
      component = new Array(labelId, setter, cols, rows, fixedCols, fixedRows, colsCaptions);
    } else if ("list".equals(widgetElement.getNodeName())) {
      final String maxRowsValue = getChildTextContent(widgetElement, "maxRows");
      int maxRows = 0;
      if (maxRowsValue != null) {
        maxRows = Integer.parseInt(maxRowsValue);
      }
      component = new SimpleList(labelId, setter, maxRows);
    }
    return component;
  }

}
