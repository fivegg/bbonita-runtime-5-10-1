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
package org.ow2.bonita.deadline;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Pascal Verdage
 */
public final class DeadlineDocument {

  public static String createDeadlineDateFromDelay(long delay) {
    return new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/SSS")
      .format(new Date(System.currentTimeMillis() + delay));
  }

  private String document;

  public String getDocument() {
    return document;
  }

  public DeadlineDocument(String packageId, String packageName) {
    this(packageId, packageName, "1.0");
  }
  
  public DeadlineDocument(String packageId, String packageName, String version) {
    document = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<Package xmlns=\"http://www.wfmc.org/2002/XPDL1.0\""
      + "  xmlns:xpdl=\"http://www.wfmc.org/2002/XPDL1.0\" "
      + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
      + "  xsi:schemaLocation=\"http://www.wfmc.org/2002/XPDL1.0"
      + "    http://wfmc.org/standards/docs/TC-1025_schema_10_xpdl.xsd\""
      + "  Id=\"" + packageId + "\" Name=\"" + packageName + "\">" + "\n"
      + "<PackageHeader>" + "\n"
      + "  <XPDLVersion>1.0</XPDLVersion>" + "\n"
      + "  <Vendor>Bonita Project Team</Vendor>" + "\n"
      + "  <Created>2008/04/17 14:07:11</Created>" + "\n"
      + "</PackageHeader>" + "\n"
      + "<RedefinableHeader>" + "\n"
      + "<Version>" + version + "</Version>" + "\n"
      + "</RedefinableHeader>" + "\n"
      + "<ConformanceClass GraphConformance=\"NON_BLOCKED\" />" + "\n"
      + "  <WorkflowProcesses>" + "\n";
  }

  public DeadlineDocument startProcess(String processId, String processName) {
    document += ""
      + "    <WorkflowProcess AccessLevel=\"PUBLIC\" Name=\"" + processName + "\" Id=\"" + processId + "\">" + "\n"
      + "      <ProcessHeader />" + "\n"
      + "      <RedefinableHeader>" + "\n"
      + "        <Version>1.0</Version>" + "\n"
      + "      </RedefinableHeader>" + "\n"
      + "      <DataFields>" + "\n";
    return this;
  }
  
  public DeadlineDocument addDataField(String id, String initialValue) {
    document += ""
      + "        <DataField Id=\"" + id + "\">" + "\n"
      + "          <DataType>" + "\n"
      + "            <BasicType Type=\"STRING\" />" + "\n"
      + "          </DataType>" + "\n"
      + "         <InitialValue>" + initialValue + "</InitialValue>" + "\n"
      + "        </DataField>" + "\n";
    return this;
  }

  public DeadlineDocument addDefaultParticipant() {
    document += ""
      + "      </DataFields>" + "\n"
      + "      <Participants>" + "\n"
      + "        <Participant Id=\"admin\" Name=\"admin\">" + "\n"
      + "          <ParticipantType Type=\"HUMAN\" />" + "\n"
      + "          <ExtendedAttributes>" + "\n"
      + "            <ExtendedAttribute Name=\"NewParticipant\" Value=\"true\" />" + "\n"
      + "          </ExtendedAttributes>" + "\n"
      + "        </Participant>" + "\n"
      + "      </Participants>" + "\n"
      + "      <Activities>" + "\n";
    return this;
  }

  public DeadlineDocument startActivity(String id, String name) {
    document += ""
      + "        <Activity Id=\"" + id + "\" Name=\"" + name + "\">" + "\n"
      + "          <Implementation>" + "\n"
      + "            <No />" + "\n"
      + "          </Implementation>" + "\n"
      + "          <Performer>admin</Performer>" + "\n"
      + "          <StartMode>" + "\n"
      + "            <Manual />" + "\n"
      + "          </StartMode>" + "\n";
    return this;
  }

  public DeadlineDocument addDeadline(String execution, String condition, String exception) {
    String executionAttribute = "";
    if (execution != null) {
      executionAttribute = "Execution=\"" + execution + "\"";
    }
    String deadlineCondition = "";
    if (condition != null) {
      deadlineCondition = "<DeadlineCondition>" + condition + "</DeadlineCondition>";
    }
    String exceptionName = "";
    if (exception != null) {
      exceptionName = "<ExceptionName>" + exception + "</ExceptionName>";
    }
    
    document += ""
      + "          <Deadline " + executionAttribute + ">" + "\n"
      + "            " + deadlineCondition + "\n"
      + "            " + exceptionName + "\n"
      + "          </Deadline>" + "\n";
    return this;
  }

  public DeadlineDocument endActivity() {
    document += ""
      + "          <TransitionRestrictions>" + "\n"
      + "            <TransitionRestriction>" + "\n"
      + "              <Join Type=\"AND\" />" + "\n"
      + "            </TransitionRestriction>" + "\n"
      + "          </TransitionRestrictions>" + "\n"
      + "          <ExtendedAttributes>" + "\n"
      + "            <ExtendedAttribute Name=\"XOffsetParticipantView\" Value=\"0\" />" + "\n"
      + "            <ExtendedAttribute Name=\"YOffsetParticipantView\" Value=\"0\" />" + "\n"
      + "            <ExtendedAttribute Name=\"XOffset\" Value=\"45\" />" + "\n"
      + "            <ExtendedAttribute Name=\"YOffset\" Value=\"34\" />" + "\n"
      + "          </ExtendedAttributes>" + "\n"
      + "        </Activity>" + "\n";
    return this;
  }

  public DeadlineDocument endActivities() {
    document += ""
      + "      </Activities>" + "\n"
      + "      <Transitions>" + "\n";
    return this;
  }

  public DeadlineDocument addTransition(String id, String name, String from, String to) {
    document += ""
      + "        <Transition Id=\"" + id + "\" Name=\"" + name
      + "\" From=\"" + from + "\" To=\"" + to + "\" />" + "\n";
    return this;  
  }

  public DeadlineDocument endProcess() {
    document += ""
      + "      </Transitions>" + "\n"
      + "    </WorkflowProcess>" + "\n";
    return this;
  }

  public DeadlineDocument endDocument() {
    document += ""
      + "  </WorkflowProcesses>" + "\n"
      + "  <ExtendedAttributes>" + "\n"
      + "    <ExtendedAttribute Name=\"MadeBy\" Value=\"ProEd\" />" + "\n"
      + "    <ExtendedAttribute Name=\"View\" Value=\"Activity\" />" + "\n"
      + "  </ExtendedAttributes>" + "\n"
      + "</Package>";
    return this;
  }

}
