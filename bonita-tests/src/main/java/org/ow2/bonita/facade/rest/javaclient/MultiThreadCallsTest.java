package org.ow2.bonita.facade.rest.javaclient;

import javax.security.auth.login.LoginException;

import org.ow2.bonita.APITestCase;

public class MultiThreadCallsTest extends APITestCase {

	class Toto extends Thread {
		
		private String userName;
		
		private Exception exception;
		
		public Toto(String userName) {
			this.userName = userName;
		}
		
		@Override
		public void run() {
			super.run();
			try {
				loginAs(userName, "bpm");
				getIdentityAPI().getAllUsers();
				getIdentityAPI().getAllRoles();
				getQueryRuntimeAPI().getProcessInstances();
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
		}

		public Exception getException() {
			return exception;
		}

		public String getMessage() {
			if (exception!= null) {
				return exception.getMessage();
			} else {
				return "";
			}
		}
		
	}
	
	
	public void testA() throws InterruptedException {
		Toto a = new Toto("john");
		Toto b = new Toto("james");
		a.start();
		b.start();
		a.join();
		b.join();
		assertNull(a.getMessage(), a.getException());
		assertNull(b.getMessage(), b.getException());
	}

}
