/**
 * Copyright (C) 2006  Bull S. A. S.
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
package org.ow2.bonita.connector.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConnectorAPITests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("Connectors Widget Tests");
    //suite.addTestSuite(ConnectorAPITest.class);

    //Internationalization
//    suite.addTestSuite(EmptyBaseNameLanguageConnectorTest.class);
//    suite.addTestSuite(NoPagesConnectorTest.class);
//    suite.addTestSuite(PagesConnectorTest.class);
//    suite.addTestSuite(EnumerationLabelsConnectorTest.class);
//    suite.addTestSuite(GroupConnectorTest.class);
    return suite;
  }
}
