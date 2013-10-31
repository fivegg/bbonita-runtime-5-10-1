package org.ow2.bonita.env;

import junit.framework.TestCase;

import org.ow2.bonita.env.generator.DbHistoryEnvGenerator;
import org.ow2.bonita.env.generator.EnvEntry;
import org.ow2.bonita.env.generator.EnvGenerator;
import org.ow2.bonita.util.BonitaRuntimeException;

public class XpdlEnvironmentParserTest extends TestCase {

  public void testParseEnvironmentFactoryFromXmlStringBadEnvironment() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    envGenerator.addBlockEntry(new EnvEntry("session", "test session", "<hibernate-session-test name='default-session' />"
        + "<hibernate-session name='test-session' />", true));
    try {
      BonitaEnvironmentParser.parseEnvironmentFactoryFromXmlString(envGenerator.createEnvironmentXml());
      fail("Invalid environment");
    } catch (final BonitaRuntimeException e) {
      //ok
    }
  }

  public void testParseEnvironmentFactoryFromXmlStringGoodEnvironment() {
    final EnvGenerator envGenerator = new DbHistoryEnvGenerator();
    envGenerator.addBlockEntry(new EnvEntry("session", "test session", "<hibernate-session name='default-session' />"
        + "<hibernate-session name='test-session' />", true));
    BonitaEnvironmentParser.parseEnvironmentFactoryFromXmlString(envGenerator.createEnvironmentXml());
  }
}
