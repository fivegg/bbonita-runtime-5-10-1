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
package org.ow2.bonita.connector.examples;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ow2.bonita.connector.examples.test.GetterConnectorTest;
import org.ow2.bonita.connector.examples.test.InputFieldsConnectorTest;
import org.ow2.bonita.connector.examples.test.InputFieldsOnCascadeConnectorTest;
import org.ow2.bonita.connector.examples.test.InputsOutputConnectorTest;
import org.ow2.bonita.connector.examples.test.MetadataOnGetterTest;
import org.ow2.bonita.connector.examples.test.NoDescriptorConnectorTest;
import org.ow2.bonita.connector.examples.test.NoFieldsConnectorTest;
import org.ow2.bonita.connector.examples.test.ObjectConnectorTest;
import org.ow2.bonita.connector.examples.test.PrimitiveDataTypesArrayConnectorTest;
import org.ow2.bonita.connector.examples.test.PrimitiveDataTypesConnectorTest;
import org.ow2.bonita.connector.examples.test.SetterConnectorTest;
import org.ow2.bonita.connector.examples.test.SimpleCalcualtorConnectorTest;
import org.ow2.bonita.connector.examples.test.SimpleRequiredInputFieldsConnectorTest;
import org.ow2.bonita.connector.examples.test.SingleInputFieldConnectorTest;
import org.ow2.bonita.connector.examples.test.UpperCaseVariableConnectorTest;
import org.ow2.bonita.connector.examples.test.WrappedPrimitiveDataTypesConnectorTest;
import org.ow2.bonita.connector.examples.test.WrongAnnotationPositionConnectorTest;
import org.ow2.bonita.connector.examples.test.XMLSetterTest;

public class ConnectorRuntimeTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("Connector Runtime Tests");
    suite.addTestSuite(NoFieldsConnectorTest.class);
    suite.addTestSuite(SingleInputFieldConnectorTest.class);
    suite.addTestSuite(InputFieldsConnectorTest.class);
    suite.addTestSuite(InputFieldsOnCascadeConnectorTest.class);
    suite.addTestSuite(SimpleRequiredInputFieldsConnectorTest.class);
    suite.addTestSuite(WrongAnnotationPositionConnectorTest.class);
    suite.addTestSuite(SetterConnectorTest.class);
    suite.addTestSuite(GetterConnectorTest.class);
    suite.addTestSuite(InputsOutputConnectorTest.class);
    suite.addTestSuite(PrimitiveDataTypesConnectorTest.class);
    suite.addTestSuite(PrimitiveDataTypesArrayConnectorTest.class);
    suite.addTestSuite(WrappedPrimitiveDataTypesConnectorTest.class);
    suite.addTestSuite(UpperCaseVariableConnectorTest.class);
    suite.addTestSuite(NoDescriptorConnectorTest.class);
    suite.addTestSuite(ObjectConnectorTest.class);
    suite.addTestSuite(MetadataOnGetterTest.class);
    suite.addTestSuite(XMLSetterTest.class);
    //Real example
    suite.addTestSuite(SimpleCalcualtorConnectorTest.class);
    return suite;
  }
}
