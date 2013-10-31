package org.ow2.bonita.util;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class DbMigrationTest extends TestCase {

  private List<String> getCommands(final String database) {
    // final InputStream stream = DbMigration.findPreMigrationScript(database);
    // byte[] bytes = null;
    // try {
    // bytes = IoUtil.readBytes(stream);
    // } finally {
    // try {
    // stream.close();
    // } catch (final IOException e) {
    // e.printStackTrace();
    // }
    // }
    // final String scriptContent = new String(bytes);
    // return DbMigration.getCommands(scriptContent, database);
    return Collections.emptyList();
  }

  private void checkCommands(final String database, final int nbOfCommands, final String forbbidenExpression) {
    // FIXME
    // final List<String> commands = getCommands(database);
    // assertEquals(nbOfCommands, commands.size());
    // for (final String command : commands) {
    // assertTrue(command, !command.contains(forbbidenExpression));
    // }
  }

  public void testMySqlScriptMajor() {
    checkCommands("mysql", 11, ";");
  }

  public void testSqlServerScriptMajor() {
    checkCommands("sqlserver", 11, "go");
  }

  public void testOracleScriptMajor() {
    checkCommands("oracle", 11, ";");
  }

  public void testPostgreScriptMajor() {
    checkCommands("postgresql", 11, ";");
  }

  public void testH2ScriptMajor() {
    checkCommands("h2", 11, ";");
  }

}
