package org.ow2.bonita.integration.connector.resolver;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.ProcessBuilder;

public class NullSetRoleResolverTest extends APITestCase {

  public void testSimpleInstanceInitiatiorMapper() throws BonitaException, LoginException {
    ProcessDefinition definition =
      ProcessBuilder.createProcess("NullSet", "1.0")
      .addGroup("Users")
        .addGroupResolver(NullSetRoleResolver.class.getName())
      .addHumanTask("welcome", "Users")
    .done();
    ProcessDefinition process = getManagementAPI().deploy(getBusinessArchive(definition, null, NullSetRoleResolver.class));
    ProcessDefinitionUUID processUUID = process.getUUID();
    ProcessInstanceUUID instanceUUID = getRuntimeAPI().instantiateProcess(processUUID);

    getManagementAPI().deleteProcess(processUUID);
  }

}
