/**
 * Copyright (C) 2009  Bull S. A. S.
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
package org.ow2.bonita.connector.examples.widget.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConnectorViewTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("Connectors View Tests");
    // General information tests
    suite.addTestSuite(EmptyIdConnectorTest.class);
    suite.addTestSuite(NullCategoryNameConnectorTest.class);
    suite.addTestSuite(EmptyCategoryNameConnectorTest.class);
    // Page tests
    suite.addTestSuite(PagesConnectorTest.class);
    // Widget tests
    suite.addTestSuite(TextConnectorTest.class);
    suite.addTestSuite(PasswordConnectorTest.class);
    suite.addTestSuite(TextareaConnectorTest.class);
    suite.addTestSuite(CheckboxConnectorTest.class);
    suite.addTestSuite(RadioConnectorTest.class);
    suite.addTestSuite(SelectConnectorTest.class);
    suite.addTestSuite(EnumerationConnectorTest.class);
    suite.addTestSuite(ArrayConnectorTest.class);
    suite.addTestSuite(SimpleListConnectorTest.class);
    // Group tests
    suite.addTestSuite(GroupsConnectorTest.class);
    // CompositeWidget tests
    return suite;
  }
}
