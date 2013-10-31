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
package org.ow2.bonita.integration.cycle;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.env.Environment;
import org.ow2.bonita.facade.def.element.impl.IterationDescriptor;
import org.ow2.bonita.facade.def.majorElement.ActivityDefinition;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.util.BonitaRuntimeException;
import org.ow2.bonita.util.Command;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.Misc;


public class CycleDetectionTest extends APITestCase {

	private static class CycleDetectionCommand implements Command<Object> {

		/**
		 *
		 */
		private static final long serialVersionUID = -7489124258263985239L;
		private final ProcessDefinitionUUID processUUID;

		public CycleDetectionCommand(final ProcessDefinitionUUID processUUID) {
			this.processUUID = processUUID;
		}

		public Object execute(final Environment environment) throws Exception {
			final ProcessDefinition process = EnvTool.getJournalQueriers().getProcess(processUUID);
			if (process == null) {
				throw new BonitaRuntimeException("Process is null");
			}
			Set<IterationDescriptor> iterationDescriptors = process.getIterationDescriptors();
			// A1 is an entry node for the  cycle a1 -> a2 -> a3
			ActivityDefinition n = process.getActivity("a1");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			// a1 reinit join for cycle a1->a2->a3
			List<String> l = new ArrayList<String>();
			l.add("a2");
			l.add("a3");
			l.add("a1");
			CycleDetectionCommand.checkContains(process, n, l);

			// A2 is not an entry node
			n = process.getActivity("a2");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a2")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}


			// A3 is an entry node for the  cycle a1 -> a2 -> a3
			n = process.getActivity("a3");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			// a3 reinit join for transition a2->a3
			l = new ArrayList<String>();
			l.add("a2");
			l.add("a3");
			l.add("a1");
			CycleDetectionCommand.checkContains(process, n, l);


			// A4 is not an entry node
			n = process.getActivity("a4");
			if (n.isInCycle()) {
				throw new BonitaRuntimeException("a4 is not in a cycle");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a4")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}// A5 is not an entry node
			n = process.getActivity("a5");
			if (n.isInCycle()) {
				throw new BonitaRuntimeException("a5 is not in a cycle");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a5")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}
			// A6 is not an entry node
			n = process.getActivity("a6");
			if (n.isInCycle()) {
				throw new BonitaRuntimeException("a6 is not in a cycle");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a6")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}

			// A7 is an entry node for the  cycle a7 -> a7
			n = process.getActivity("a7");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			l = new ArrayList<String>();
			l.add("a7");
			CycleDetectionCommand.checkContains(process, n, l);


			// A8 is an entry node for the cycle a8 -> a9
			// a8 is an entry node for the cycle a8 -> a9 -> a10
			n = process.getActivity("a8");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			l = new ArrayList<String>();
			l.add("a8");
			l.add("a9");
			l.add("a10");
			CycleDetectionCommand.checkContains(process, n, l);

			// A9 is not an entry node
			n = process.getActivity("a9");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a9")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}
			// A10 is not an entry node
			n = process.getActivity("a10");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a10")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}

			// A11 is an entry node for the  cycle a11 -> a12 -> a13
			n = process.getActivity("a11");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			l = new ArrayList<String>();
			l.add("a12");
			l.add("a13");
			l.add("a11");
			CycleDetectionCommand.checkContains(process, n, l);

			// A12 is an entry node for the  cycle a12 -> a13 -> a14 and a11 -> a12 -> a13
			n = process.getActivity("a12");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			l = new ArrayList<String>();
			l.add("a12");
			l.add("a13");
			l.add("a11");
			l.add("a14");
			CycleDetectionCommand.checkContains(process, n, l);

			// A13 is not an entry node
			n = process.getActivity("a13");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a13")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}
			// A14 is not an entry node
			n = process.getActivity("a14");
			if (!n.isInCycle()) {
				throw new BonitaRuntimeException("iterationDescriptors is null");
			}
			for (final IterationDescriptor it : iterationDescriptors) {
				if (CycleDetectionCommand.isEntryNode(it, "a14")) {
					throw new BonitaRuntimeException("iterationDescriptors not empty : " + iterationDescriptors.toString());
				}
			}
			return null;
		}

		private static void checkContains(final ProcessDefinition processDef, final ActivityDefinition n, final List<String> nodes) {
			Set<IterationDescriptor> iterations = processDef.getIterationDescriptors(n.getName());
			final List<String> remainingNodes = new ArrayList<String>(nodes);
			for (final IterationDescriptor it : iterations) {
				if (!nodes.containsAll(CycleDetectionCommand.getNodeNames(it.getCycleNodes()))) {
					Assert.fail("Some extra nodes are contained in iteration " + it);
				}
				remainingNodes.removeAll(CycleDetectionCommand.getNodeNames(it.getCycleNodes()));
			}
			if (!remainingNodes.isEmpty()) {
				Assert.fail(remainingNodes + " are not detected as being in a cycle");
			}
		}

		private static boolean isEntryNode(final IterationDescriptor it, final String nodeName) {
			for (final String node : it.getEntryNodes()) {
				if (node.equals(nodeName)) {
					return true;
				}
			}
			return false;
		}

		private static Set<String> getNodeNames(final Set<String> descriptors) {
			final Set<String> names = new HashSet<String>();
			for (final String node : descriptors) {
				names.add(node);
			}
			return names;
		}
	}

	public void testSimpleCycle() throws Exception {
		final URL xpdlUrl = this.getClass().getResource("cycleDetection.xpdl");
		final ProcessDefinition process = this.getManagementAPI().deploy(getBusinessArchiveFromXpdl(xpdlUrl,
				HookBeforeTerminateUpdateVariable.class));
		this.getManagementAPI().deployJar("cycle.jar", Misc.generateJar(CycleDetectionCommand.class));
		final ProcessDefinitionUUID processUUID = process.getUUID();

		try {
			this.getCommandAPI().execute(new CycleDetectionCommand(processUUID));
		} finally {
			this.getManagementAPI().removeJar("cycle.jar");
			this.getManagementAPI().disable(processUUID);
			this.getManagementAPI().deleteProcess(processUUID);
		}
	}

}
