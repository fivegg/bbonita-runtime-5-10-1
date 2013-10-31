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
 * 
 * Modified by Charles Souillard, Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.cfg.Configuration;
import org.ow2.bonita.activity.ActivityTest;
import org.ow2.bonita.activity.FailedStateThrowingExceptionPolicyTest;
import org.ow2.bonita.activity.LoopActivityTest;
import org.ow2.bonita.activity.PriorityActivityTest;
import org.ow2.bonita.activity.implementationNo.ImplNoAutoTest;
import org.ow2.bonita.activity.implementationNo.ImplNoManuTest;
import org.ow2.bonita.activity.instantiation.MultiInstantiationTest;
import org.ow2.bonita.activity.multipleinstances.MultipleInstancesTests;
import org.ow2.bonita.activity.route.CancelJoinXorTest;
import org.ow2.bonita.activity.route.InitialEndingNodeTest;
import org.ow2.bonita.activity.route.JoinAndTest;
import org.ow2.bonita.activity.route.JoinNoTest;
import org.ow2.bonita.activity.route.JoinSplitTest;
import org.ow2.bonita.activity.route.JoinXorTest;
import org.ow2.bonita.activity.route.SplitAndTest;
import org.ow2.bonita.activity.subflow.SubflowTest;
import org.ow2.bonita.async.AsyncLockTest;
import org.ow2.bonita.async.AsyncTest;
import org.ow2.bonita.attachment.DocumentTests;
import org.ow2.bonita.classloader.BonitaClassLoaderTest;
import org.ow2.bonita.command.WebExecuteTaskCommandTest;
import org.ow2.bonita.connector.ExecuteStandaloneConnectorTest;
import org.ow2.bonita.db.DbTest;
import org.ow2.bonita.deadline.DeadlineTestSuite;
import org.ow2.bonita.env.EnvConstants;
import org.ow2.bonita.env.GlobalEnvironmentFactory;
import org.ow2.bonita.event.EventTests;
import org.ow2.bonita.example.AWFromBarProcessTest;
import org.ow2.bonita.example.AWMultiInstProcessTest;
import org.ow2.bonita.example.AWMultiInstTest;
import org.ow2.bonita.example.AWProcessTest;
import org.ow2.bonita.example.AWTest;
import org.ow2.bonita.example.CarpoolProcessTest;
import org.ow2.bonita.example.CarpoolTest;
import org.ow2.bonita.example.WebSaleProcessTest;
import org.ow2.bonita.example.WebsaleTest;
import org.ow2.bonita.facade.AssignTest;
import org.ow2.bonita.facade.BAMAPITest;
import org.ow2.bonita.facade.CommandAPITest;
import org.ow2.bonita.facade.DefinitionAPITest;
import org.ow2.bonita.facade.FormsMigrationTest;
import org.ow2.bonita.facade.GroovyPerformanceTest;
import org.ow2.bonita.facade.ManagementAPITest;
import org.ow2.bonita.facade.MultiThreadCallActivityTest;
import org.ow2.bonita.facade.RemoteAPITest;
import org.ow2.bonita.facade.RepairAPITest;
import org.ow2.bonita.facade.RuntimeAPITest;
import org.ow2.bonita.facade.SuspendResumeTest;
import org.ow2.bonita.facade.businessArchive.BusinessArchiveTest;
import org.ow2.bonita.facade.identity.IdentityAPITest;
import org.ow2.bonita.facade.privilege.PrivilegeTest;
import org.ow2.bonita.facade.query.QueryDefinitionAPITest;
import org.ow2.bonita.facade.query.QueryListTest;
import org.ow2.bonita.facade.query.QueryRuntimeAPITest;
import org.ow2.bonita.facade.query.WebAPITest;
import org.ow2.bonita.facade.rest.ConflictingPathsTest;
import org.ow2.bonita.facade.uuid.UUIDTest;
import org.ow2.bonita.hook.HookTestSuite;
import org.ow2.bonita.identity.auth.AuthenticationTest;
import org.ow2.bonita.integration.IntegrationTests;
import org.ow2.bonita.integration.bigprocesses.BigProcessesTest;
import org.ow2.bonita.integration.connector.ConnectorsTests;
import org.ow2.bonita.participant.ParticipantTest;
import org.ow2.bonita.perf.PerfTest;
import org.ow2.bonita.process.CommentFeedTest;
import org.ow2.bonita.process.ImplicitEndingTest;
import org.ow2.bonita.process.ProcessDefinitionLifeCycleTest;
import org.ow2.bonita.process.ProcessIntegrationTest;
import org.ow2.bonita.search.SearchTests;
import org.ow2.bonita.services.impl.DbUUIDServiceTest;
import org.ow2.bonita.services.impl.LDRTest;
import org.ow2.bonita.services.record.QuerierAPITest;
import org.ow2.bonita.transition.TransitionTests;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.DbTool;
import org.ow2.bonita.util.EncodingTest;
import org.ow2.bonita.util.EnvToolTest;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.ProcessBuilderTest;
import org.ow2.bonita.util.ToolTest;
import org.ow2.bonita.variable.ActivityVariableTest;
import org.ow2.bonita.variable.ActivityVariableWithSplitJoinTest;
import org.ow2.bonita.variable.ProcessVariableTest;
import org.ow2.bonita.variable.TransientVariablesTest;
import org.ow2.bonita.variable.VariableBasicTypeTest;
import org.ow2.bonita.versioning.VersioningTest;

/**
 * @author Pierre Vigneras
 */
public final class DefaultEnvNonRegressionTests extends TestCase {
  private DefaultEnvNonRegressionTests() { }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(DefaultEnvNonRegressionTests.class.getName());
    // NonRegression Tests that use the default envGenerator.
    // These tests are used to create EJB test suite. 
    
    //this test must be launched first
    suite.addTestSuite(DbUUIDServiceTest.class);
    
    suite.addTestSuite(AWTest.class);
    suite.addTestSuite(CarpoolTest.class);
    suite.addTestSuite(WebsaleTest.class);
    suite.addTestSuite(AWMultiInstTest.class);

    suite.addTestSuite(AWFromBarProcessTest.class);
    suite.addTestSuite(AWProcessTest.class);
    suite.addTestSuite(CarpoolProcessTest.class);
    suite.addTestSuite(WebSaleProcessTest.class);
    suite.addTestSuite(AWMultiInstProcessTest.class);

    suite.addTestSuite(BonitaClassLoaderTest.class);

    suite.addTest(HookTestSuite.suite());
    suite.addTestSuite(RemoteAPITest.class);
    suite.addTestSuite(ConflictingPathsTest.class);
    suite.addTestSuite(BAMAPITest.class);
    suite.addTestSuite(QueryListTest.class);
    suite.addTestSuite(QueryRuntimeAPITest.class);

    suite.addTest(SearchTests.suite());
    suite.addTestSuite(PrivilegeTest.class);

    suite.addTestSuite(QueryDefinitionAPITest.class);
    suite.addTestSuite(WebAPITest.class);
    suite.addTestSuite(ManagementAPITest.class);
    suite.addTestSuite(FormsMigrationTest.class);
    suite.addTestSuite(ProcessDefinitionLifeCycleTest.class);
    suite.addTestSuite(BusinessArchiveTest.class);
    suite.addTestSuite(RuntimeAPITest.class);
    suite.addTestSuite(AssignTest.class);
    suite.addTestSuite(CommandAPITest.class);
    suite.addTestSuite(SuspendResumeTest.class);
    suite.addTestSuite(ParticipantTest.class);
    suite.addTestSuite(QuerierAPITest.class);
    suite.addTestSuite(SubflowTest.class);
    suite.addTestSuite(DefinitionAPITest.class);
    suite.addTestSuite(IdentityAPITest.class);
    suite.addTestSuite(AuthenticationTest.class);
    suite.addTestSuite(RepairAPITest.class);
    suite.addTest(EventTests.suite());
    suite.addTestSuite(ToolTest.class);
    suite.addTestSuite(EnvToolTest.class);
    suite.addTest(DocumentTests.suite());
    if (!Misc.isOnWindows()) {
      suite.addTestSuite(LDRTest.class);
    }

    suite.addTestSuite(ActivityTest.class);
    suite.addTestSuite(FailedStateThrowingExceptionPolicyTest.class);
    suite.addTestSuite(LoopActivityTest.class);
    suite.addTestSuite(PriorityActivityTest.class);
    suite.addTestSuite(CommentFeedTest.class);
    suite.addTest(DeadlineTestSuite.suite());
    suite.addTest(IntegrationTests.suite());
    suite.addTestSuite(VersioningTest.class);
    suite.addTestSuite(ImplicitEndingTest.class);
    suite.addTestSuite(MultiThreadCallActivityTest.class);

    // Connectors
    suite.addTest(ConnectorsTests.suite());
    suite.addTestSuite(ExecuteStandaloneConnectorTest.class);
    suite.addTestSuite(MultiInstantiationTest.class);
    suite.addTest(MultipleInstancesTests.suite());
    suite.addTestSuite(DbTest.class);
    suite.addTestSuite(ProcessBuilderTest.class);
    suite.addTestSuite(UUIDTest.class);
    suite.addTestSuite(AsyncTest.class);
    suite.addTestSuite(AsyncLockTest.class);
    suite.addTestSuite(ProcessIntegrationTest.class);

    // Route
    suite.addTestSuite(SplitAndTest.class);
    suite.addTestSuite(JoinAndTest.class);
    suite.addTestSuite(JoinXorTest.class);
    suite.addTestSuite(JoinNoTest.class);
    suite.addTestSuite(JoinSplitTest.class);
    suite.addTestSuite(InitialEndingNodeTest.class);
    suite.addTestSuite(CancelJoinXorTest.class);
    // Implementation No
    suite.addTestSuite(ImplNoAutoTest.class);
    suite.addTestSuite(ImplNoManuTest.class);
    //Variable
    suite.addTestSuite(ProcessVariableTest.class);
    suite.addTestSuite(ActivityVariableTest.class);
    suite.addTestSuite(ActivityVariableWithSplitJoinTest.class);
    suite.addTestSuite(VariableBasicTypeTest.class);
    suite.addTestSuite(TransientVariablesTest.class);
    // Transition
    suite.addTest(TransitionTests.suite());
    
    //web execute task
    suite.addTestSuite(WebExecuteTaskCommandTest.class);

    suite.addTestSuite(BigProcessesTest.class);
    
    //groovy perf
    suite.addTestSuite(GroovyPerformanceTest.class);
    
    suite.addTestSuite(PerfTest.class);

    final Configuration config = (Configuration) GlobalEnvironmentFactory.getEnvironmentFactory(BonitaConstants.DEFAULT_DOMAIN).get(EnvConstants.HB_CONFIG_CORE);
    if (DbTool.isOnDb("postgres", config) || DbTool.isOnDb("h2", config)) {
      suite.addTestSuite(EncodingTest.class);
    }
    
    return suite;
  }

}
