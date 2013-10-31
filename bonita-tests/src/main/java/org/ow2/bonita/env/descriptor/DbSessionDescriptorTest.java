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

import junit.framework.TestCase;

import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.env.EnvironmentFactory;
import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvEntry;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.persistence.db.HibernateDbSession;
import org.ow2.bonita.services.CommandService;
import org.ow2.bonita.util.Command;


/**
 * @author Guillaume Porcher
 *
 */
public class DbSessionDescriptorTest extends TestCase {

  public void testHibernateXpdlPersistenceServiceDescriptor() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();

    envGenerator.addBlockEntry(new EnvEntry("session", "test",
        "<hibernate-session name='default-session' />"
        + "<hibernate-session name='test-session' />", true));

    envGenerator.addBlockEntry(new EnvEntry("persistenceService", "test",
        "<" + EnvConstants.DB_SESSION_TAG + " session='test-session' name='h1'/>"
        + "<" + EnvConstants.DB_SESSION_TAG + " session='default-session' name='h2'/>", true));

    final EnvironmentFactory envFactory = envGenerator.createEnvironmentFactory();
    assertNotNull(envFactory);

    envFactory.get(CommandService.class).execute(new Command<Object>() {
      private static final long serialVersionUID = 4583464708967697023L;

      public Object execute(final Environment environment) throws Exception {
        final Environment current = Environment.getCurrent();

        assertEquals(current, environment);

        final HibernateDbSession querierDbSession1 = (HibernateDbSession) current.get("h1");

        final HibernateDbSession querierDbSession2 = (HibernateDbSession) current.get("h2");

        final Object testSession = current.get("test-session");
        final Object defaultSession = current.get("default-session");

        assertNotNull(querierDbSession1);
        assertNotNull(querierDbSession2);

        assertEquals(testSession, querierDbSession1.getSession());
        assertEquals(defaultSession, querierDbSession2.getSession());

        assertNotSame(defaultSession, querierDbSession1.getSession());
        assertNotSame(testSession, querierDbSession2.getSession());
        return null;
      }
    });

    envFactory.close();
  }
}
