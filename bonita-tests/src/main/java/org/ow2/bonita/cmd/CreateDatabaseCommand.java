package org.ow2.bonita.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.hibernate.cfg.Configuration;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.runtime.event.EventExecutor;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.DbTool;
import org.ow2.bonita.util.EnvTool;

public class CreateDatabaseCommand implements Command<Void> {

  private static final long serialVersionUID = -297802302758846748L;
  protected static final Logger LOG = Logger.getLogger(CreateDatabaseCommand.class.getName());
  private final boolean recreate;

  public CreateDatabaseCommand(final boolean recreate) {
    this.recreate = recreate;
  }

  @Override
  public Void execute(final Environment environment) throws Exception {
    final EventExecutor eventExecutor = EnvTool.getEventExecutor();
    if (recreate) {
      if (eventExecutor != null && eventExecutor.isActive()) {
        eventExecutor.stop();
      }
      final Map<String, String> hibernateConfigs = new HashMap<String, String>();
      hibernateConfigs.put(EnvConstants.HB_CONFIG_CORE, EnvConstants.HB_SESSION_FACTORY_CORE);
      hibernateConfigs.put(EnvConstants.HB_CONFIG_HISTORY, EnvConstants.HB_SESSION_FACTORY_HISTORY);

      for (final Map.Entry<String, String> e : hibernateConfigs.entrySet()) {
        final Configuration config = (Configuration) environment.get(e.getKey());
        if (config != null) {
          LOG.info("Initializing DB for configuration: " + e.getKey());
          LOG.info("  - URL                 : " + DbTool.getDbUrl(config));
          LOG.info("  - DRIVER              : " + DbTool.getDbDriverClass(config));
          LOG.info("  - USE QUERY CACHE     : " + DbTool.getDbUseQueryCache(config));
          LOG.info("  - USE 2ND LEVEL CACHE : " + DbTool.getDbUseSecondLeveleCache(config));
          
          String domain = EnvTool.getDomain();
          if (domain == null || domain.isEmpty()) {
            domain = BonitaConstants.DEFAULT_DOMAIN;
          }

          // recreate db between tests
          DbTool.recreateDb(domain, e.getKey());
          if ("true".equals(DbTool.getDbUseQueryCache(config))) {
            DbTool.cleanCache(domain, e.getValue());
          }
        }
      }
    }
    if (eventExecutor != null && !eventExecutor.isActive()) {
      eventExecutor.start();
    }
    return null;
  }

}
