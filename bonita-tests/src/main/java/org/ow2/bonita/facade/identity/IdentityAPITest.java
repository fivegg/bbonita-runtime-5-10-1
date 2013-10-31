package org.ow2.bonita.facade.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.exception.GroupNotFoundException;
import org.ow2.bonita.facade.exception.RoleNotFoundException;
import org.ow2.bonita.facade.exception.UserNotFoundException;
import org.ow2.bonita.facade.paging.GroupCriterion;
import org.ow2.bonita.facade.paging.RoleCriterion;
import org.ow2.bonita.facade.paging.UserCriterion;
import org.ow2.bonita.util.BonitaException;
import org.ow2.bonita.util.Misc;

public class IdentityAPITest extends APITestCase {

  public void testDeletingARole() throws BonitaException {
    final Group group = getIdentityAPI().addGroup("group", null);
    final Role role1 = getIdentityAPI().addRole("role1");
    final Role role2 = getIdentityAPI().addRole("role2");

    final User user = getIdentityAPI().addUser("marja", "bpm");

    final Membership membership1 = getIdentityAPI().getMembershipForRoleAndGroup(role1.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(user.getUUID(), membership1.getUUID());
    final Membership membership2 = getIdentityAPI().getMembershipForRoleAndGroup(role2.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(user.getUUID(), membership2.getUUID());

    List<User> users = getIdentityAPI().getAllUsersInMembership(membership1.getUUID());
    assertEquals(1, users.size());
    assertEquals(user.getUsername(), users.get(0).getUsername());
    users = getIdentityAPI().getAllUsersInMembership(membership2.getUUID());
    assertEquals(1, users.size());
    assertEquals(user.getUsername(), users.get(0).getUsername());

    getIdentityAPI().removeRoles(Collections.singletonList(role1.getUUID()));
    users = getIdentityAPI().getAllUsersInMembership(membership2.getUUID());
    assertEquals(1, users.size());
    assertEquals(user.getUsername(), users.get(0).getUsername());

    getIdentityAPI().removeUserByUUID(user.getUUID());
    getIdentityAPI().removeRoles(Collections.singletonList(role2.getUUID()));
    getIdentityAPI().removeGroupByUUID(group.getUUID());
  }

  public void testGetUser() throws Exception {
    final User john = getIdentityAPI().getUser("john");
    assertNotNull(john);
    assertEquals("John", john.getFirstName());
  }

  public void testGetRole() throws Exception {
    final Role userRole = getIdentityAPI().getRole("user");
    assertNotNull(userRole);
    assertEquals("User", userRole.getLabel());
  }

  public void testAddUser() throws Exception {
    getIdentityAPI().addUser("ateam", "bpm", "hannibal", "smith", "hannibal.smith@a.team");
    final User newUser = getIdentityAPI().getUser("ateam");
    assertNotNull(newUser);
    assertEquals(Misc.hash("bpm"), newUser.getPassword());
    assertEquals("ateam", newUser.getUsername());
    assertEquals("hannibal", newUser.getFirstName());
    assertEquals("smith", newUser.getLastName());
    assertEquals("hannibal.smith@a.team", newUser.getEmail());

    getIdentityAPI().removeUser("ateam");
  }

  public void testUpdateUser() throws Exception {
    getIdentityAPI().addUser("johnny", "deadzone");
    User updatedUser = getIdentityAPI().updateUser("johnny", "johnny2", "deadzone", "Johnny", "Smith",
        "johnny.smith@dead.zone");
    updatedUser = getIdentityAPI().getUser("johnny2");
    assertNotNull(updatedUser);
    assertEquals(Misc.hash("deadzone"), updatedUser.getPassword());
    assertEquals("johnny2", updatedUser.getUsername());
    assertEquals("Johnny", updatedUser.getFirstName());
    assertEquals("Smith", updatedUser.getLastName());
    assertEquals("johnny.smith@dead.zone", updatedUser.getEmail());
    getIdentityAPI().removeUser("johnny2");
  }

  public void testUpdateUserManager() throws Exception {
    final User user = getIdentityAPI().addUser("nicolas", "bpm");
    assertNull(user.getManagerUUID());
    final User manager = getIdentityAPI().addUser("manager", "bpm");
    User updatedUser = getIdentityAPI().updateUserByUUID(user.getUUID(), "nicolas", null, null, null, null,
        manager.getUUID(), null);
    assertNotNull(updatedUser.getManagerUUID());
    assertEquals(manager.getUUID(), updatedUser.getManagerUUID());
    updatedUser = getIdentityAPI().updateUserByUUID(user.getUUID(), "nicolas", null, null, null, null, null, null);
    assertNull(updatedUser.getManagerUUID());

    getIdentityAPI().removeUserByUUID(user.getUUID());
    getIdentityAPI().removeUserByUUID(manager.getUUID());
  }

  public void testAddRole() throws Exception {
    getIdentityAPI().addRole("bonita", "Bonita", "bonita role");
    final Role newRole = getIdentityAPI().getRole("bonita");
    assertNotNull(newRole);
    assertEquals("bonita", newRole.getName());
    assertEquals("Bonita", newRole.getLabel());
    assertEquals("bonita role", newRole.getDescription());

    getIdentityAPI().removeRole("bonita");
  }

  public void testUpdateRole() throws Exception {
    getIdentityAPI().addRole("sales", "Sales team", "Dunder Mifflin Sales");
    Role updatedRole = getIdentityAPI().updateRole("sales", "sales team", "Sales team role",
        "Dunder Mifflin Sales team");
    updatedRole = getIdentityAPI().getRole("sales team");
    assertNotNull(updatedRole);
    assertEquals("Sales team role", updatedRole.getLabel());
    assertEquals("Dunder Mifflin Sales team", updatedRole.getDescription());
    assertEquals("sales team", updatedRole.getName());

    getIdentityAPI().removeRole("sales team");
  }

  public void testGetRolesOld() throws Exception {
    final Set<Role> roles = getIdentityAPI().getRoles();
    assertNotSame(0, roles.size());
  }

  public void testGetUsersOld() throws Exception {
    final Set<User> users = getIdentityAPI().getUsers();
    assertNotSame(0, users.size());
  }

  public void testRemoveUser() throws Exception {
    getIdentityAPI().removeUser("jack");
    try {
      getIdentityAPI().getUser("jack");
      fail("user jack should have been removed");
    } catch (final UserNotFoundException e) {
      getIdentityAPI().addUser("jack", "bpm", "Jack", "Doe", "");
      getIdentityAPI().addRoleToUser("user", "jack");
    }
  }

  public void testRemoveRole() throws Exception {
    getIdentityAPI().removeRole("user");
    try {
      getIdentityAPI().getRole("user");
      fail("role user should have been removed");
    } catch (final RoleNotFoundException e) {
      getIdentityAPI().addRole("user", "User", "The user role");
    }
  }

  public void testGetUsersInRole() throws Exception {
    final Set<User> users = getIdentityAPI().getUsersInRole("admin");
    assertNotSame(0, users.size());
  }

  public void testGetUserRoles() throws Exception {
    final Set<Role> roles = getIdentityAPI().getUserRoles("admin");
    assertNotSame(0, roles.size());
  }

  public void testAddRemoveRoleToUser() throws Exception {
    getIdentityAPI().addRoleToUser("admin", "john");
    Set<Role> roles = getIdentityAPI().getUserRoles("john");
    Role adminRole = null;
    for (final Role role : roles) {
      if ("admin".equals(role.getName())) {
        adminRole = role;
      }
    }
    assertNotNull(adminRole);

    getIdentityAPI().removeRoleFromUser("admin", "john");
    roles = getIdentityAPI().getUserRoles("john");
    adminRole = null;
    for (final Role role : roles) {
      if ("admin".equals(role.getName())) {
        adminRole = role;
      }
    }
    assertNull(adminRole);
  }

  public void testSetUserRoles() throws Exception {
    final Set<String> userRoles = new HashSet<String>();
    userRoles.add("admin");
    getIdentityAPI().setUserRoles("john", userRoles);
    Set<Role> userSetRoles = getIdentityAPI().getUserRoles("john");
    assertEquals(1, userSetRoles.size());

    userRoles.remove("admin");
    getIdentityAPI().setUserRoles("john", userRoles);
    userSetRoles = getIdentityAPI().getUserRoles("john");
    assertEquals(0, userSetRoles.size());

    userRoles.add("admin");
    userRoles.add("user");
    getIdentityAPI().setUserRoles("john", userRoles);
    userSetRoles = getIdentityAPI().getUserRoles("john");
    assertEquals(2, userSetRoles.size());

    userRoles.remove("admin");
    getIdentityAPI().setUserRoles("john", userRoles);
    userSetRoles = getIdentityAPI().getUserRoles("john");
    assertEquals(1, userSetRoles.size());
  }

  public void testAddRemoveUpdateUserWithMetadata() throws Exception {
    ProfileMetadata seniority = getIdentityAPI().addProfileMetadata("seniority", "years of service");
    final Map<String, String> userMetadata = new HashMap<String, String>();
    userMetadata.put("seniority", "10");
    User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "michael", "scott", "Mr", "Regional Manager",
        null, userMetadata);
    userMetadata.put("seniority", "8");
    User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "dwight", "schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), userMetadata);
    assertEquals(1, dwight.getMetadata().size());
    getIdentityAPI()
        .updateUserPersonalContactInfo(
            dwight.getUUID(),
            "dwight.schrute@schrutefarms.com",
            "000000000000",
            "666666666666",
            null,
            "Schrute Farms",
            null,
            "Main Street",
            "18431",
            "Honesdale",
            "Pennsylvania",
            "United States",
            "http://www.tripadvisor.fr/Hotel_Review-g52842-d730099-Reviews-Schrute_Farms-Honesdale_Pocono_Mountains_Region_Pennsylvania.html");
    getIdentityAPI().updateUserProfessionalContactInfo(dwight.getUUID(), "dwight.schrute@dunder-mifflin.com",
        "111111111111", "666666666666", "222222222222", null, "Dunder Mifflin Office", "address", "zipCode",
        "Scranton", "Pennsylvania", "United States", "http://www.dundermifflin.com");
    dwight = getIdentityAPI().getUserByUUID(dwight.getUUID());
    assertNotNull(dwight);
    assertEquals("dwight.schrute", dwight.getUsername());
    assertEquals(Misc.hash("battlestargalactica"), dwight.getPassword());
    assertEquals("dwight", dwight.getFirstName());
    assertEquals("schrute", dwight.getLastName());
    assertEquals("Mr", dwight.getTitle());
    assertEquals("Salesman - Assistant Regional Manager", dwight.getJobTitle());
    assertEquals(michael.getUUID(), dwight.getManagerUUID());
    assertEquals("dwight.schrute@schrutefarms.com", dwight.getPersonalContactInfo().getEmail());
    assertEquals("dwight.schrute@dunder-mifflin.com", dwight.getProfessionalContactInfo().getEmail());
    assertEquals("000000000000", dwight.getPersonalContactInfo().getPhoneNumber());
    assertEquals("111111111111", dwight.getProfessionalContactInfo().getPhoneNumber());
    assertEquals("666666666666", dwight.getPersonalContactInfo().getMobileNumber());
    assertEquals("666666666666", dwight.getProfessionalContactInfo().getMobileNumber());
    assertEquals(null, dwight.getPersonalContactInfo().getFaxNumber());
    assertEquals("222222222222", dwight.getProfessionalContactInfo().getFaxNumber());
    assertEquals("Schrute Farms", dwight.getPersonalContactInfo().getBuilding());
    assertEquals(null, dwight.getProfessionalContactInfo().getBuilding());
    assertEquals(null, dwight.getPersonalContactInfo().getRoom());
    assertEquals("Dunder Mifflin Office", dwight.getProfessionalContactInfo().getRoom());
    assertEquals("Main Street", dwight.getPersonalContactInfo().getAddress());
    assertEquals("address", dwight.getProfessionalContactInfo().getAddress());
    assertEquals("18431", dwight.getPersonalContactInfo().getZipCode());
    assertEquals("zipCode", dwight.getProfessionalContactInfo().getZipCode());
    assertEquals("Honesdale", dwight.getPersonalContactInfo().getCity());
    assertEquals("Scranton", dwight.getProfessionalContactInfo().getCity());
    assertEquals("Pennsylvania", dwight.getPersonalContactInfo().getState());
    assertEquals("Pennsylvania", dwight.getProfessionalContactInfo().getState());
    assertEquals("United States", dwight.getPersonalContactInfo().getCountry());
    assertEquals("United States", dwight.getProfessionalContactInfo().getCountry());
    assertEquals(
        "http://www.tripadvisor.fr/Hotel_Review-g52842-d730099-Reviews-Schrute_Farms-Honesdale_Pocono_Mountains_Region_Pennsylvania.html",
        dwight.getPersonalContactInfo().getWebsite());
    assertEquals("http://www.dundermifflin.com", dwight.getProfessionalContactInfo().getWebsite());
    final Map<ProfileMetadata, String> dwightsMetadata = dwight.getMetadata();
    assertEquals(1, dwightsMetadata.size());
    seniority = getIdentityAPI().findProfileMetadataByName("seniority");
    for (final Entry<ProfileMetadata, String> metadataEntry : dwightsMetadata.entrySet()) {
      if (metadataEntry.getKey().getUUID().equals(seniority.getUUID())) {
        assertEquals("8", metadataEntry.getValue());
      }
    }

    getIdentityAPI().updateUserDelegee(michael.getUUID(), dwight.getUUID());
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    assertEquals(dwight.getUUID(), michael.getDelegeeUUID());
    final User david = getIdentityAPI().addUser("david.wallace", "password");
    userMetadata.put("seniority", "12");

    getIdentityAPI().updateUserByUUID(michael.getUUID(), "michaelscott", "michael1", "scott1", "Mr1",
        "Regional Manager1", david.getUUID(), userMetadata);
    getIdentityAPI().updateUserPassword(michael.getUUID(), "bestboss1");
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    assertEquals("michaelscott", michael.getUsername());
    assertEquals(Misc.hash("bestboss1"), michael.getPassword());
    assertEquals("michael1", michael.getFirstName());
    assertEquals("scott1", michael.getLastName());
    assertEquals("Mr1", michael.getTitle());
    assertEquals("Regional Manager1", michael.getJobTitle());
    assertEquals(david.getUUID(), michael.getManagerUUID());
    final Map<ProfileMetadata, String> michaelsMetadata = michael.getMetadata();
    assertEquals(1, michaelsMetadata.size());
    seniority = getIdentityAPI().findProfileMetadataByName("seniority");
    for (final Entry<ProfileMetadata, String> metadataEntry : michaelsMetadata.entrySet()) {
      if (metadataEntry.getKey().getUUID().equals(seniority.getUUID())) {
        assertEquals("12", metadataEntry.getValue());
      }
    }

    getIdentityAPI().removeUserByUUID(david.getUUID());
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    assertNull(michael.getManagerUUID());
    getIdentityAPI().removeProfileMetadataByUUID(seniority.getUUID());
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    assertEquals(0, michael.getMetadata().size());
    getIdentityAPI().removeUserByUUID(dwight.getUUID());
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    assertNull(michael.getDelegeeUUID());
    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testAddUserWithEmptyValueOfManagerUUID() throws BonitaException {
    final User john = getIdentityAPI().addUser("john.doe", "bpm", "John", "Doe", "Mr", "Unknown", "", null);
    assertNotNull(john);
    getIdentityAPI().removeUserByUUID(john.getUUID());
  }

  public void testAddAUserWithEmptyUserName1() throws BonitaException {
    try {
      getIdentityAPI().addUser("", "bpm", "John", "Doe", "Mr", "Unknown", "", null);
      fail("It is not allowed to add a user with an empty user name");
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testAddAUserWithEmptyUserName2() throws BonitaException {
    try {
      getIdentityAPI().addUser("", "bpm");
      fail("It is not allowed to add a user with an empty user name");
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testGetUsers() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "michael", "scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "dwight", "schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "dwightisanidiot", "jim", "halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "dwightisanidiot", "pam", "halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2);
    assertEquals(2, users.size());
    assertEquals("admin", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(2, 2);
    assertEquals(2, users.size());
    assertEquals("jack", users.get(0).getUsername());
    assertEquals("james", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(4, 2);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("john", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(6, 2);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByFirstNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("jack", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("james", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("john", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("admin", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByFirstNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("admin", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("john", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("james", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jack", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByLastNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Doe", users.get(0).getLastName());
    assertEquals("Doe", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Doe", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Schrute", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Scott", users.get(0).getLastName());
    assertNull(users.get(1).getLastName());
    assertEquals("admin", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByLastNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertNull(users.get(0).getLastName());
    assertEquals("Scott", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Schrute", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Doe", users.get(1).getLastName());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Doe", users.get(0).getLastName());
    assertEquals("Doe", users.get(1).getLastName());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByUserNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("admin", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("jack", users.get(0).getUsername());
    assertEquals("james", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("john", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUsersOrderByUserNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final List<User> allUsers = getIdentityAPI().getAllUsers();
    assertEquals(8, allUsers.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsers();
    assertEquals(allUsers.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsers(0, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(2, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("john", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(4, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("james", users.get(0).getUsername());
    assertEquals("jack", users.get(1).getUsername());

    users = getIdentityAPI().getUsers(6, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("admin", users.get(1).getUsername());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetProfileMetadata() throws Exception {
    final ProfileMetadata seniority = getIdentityAPI().addProfileMetadata("seniority", "years of service");
    final ProfileMetadata hobbies = getIdentityAPI().addProfileMetadata("hobbies", "hobbies");
    final ProfileMetadata dateOfBirth = getIdentityAPI().addProfileMetadata("dateOfBirth", "date of birth");

    final List<ProfileMetadata> allMetadata = getIdentityAPI().getAllProfileMetadata();
    assertEquals(3, allMetadata.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfProfileMetadata();
    assertEquals(allMetadata.size(), numberOfMetadata);

    List<ProfileMetadata> metadata = getIdentityAPI().getProfileMetadata(0, 2);
    assertEquals(2, metadata.size());
    assertEquals("dateOfBirth", metadata.get(0).getName());
    assertEquals("hobbies", metadata.get(1).getName());
    metadata = getIdentityAPI().getProfileMetadata(2, 2);
    assertEquals(1, metadata.size());
    assertEquals("seniority", metadata.get(0).getName());

    final Collection<String> profileMetadataUUIDs = new HashSet<String>();
    profileMetadataUUIDs.add(seniority.getUUID());
    profileMetadataUUIDs.add(hobbies.getUUID());
    profileMetadataUUIDs.add(dateOfBirth.getUUID());
    getIdentityAPI().removeProfileMetadata(profileMetadataUUIDs);
  }

  public void testAddRemoveUpdateRole() throws Exception {
    Role managerRole = getIdentityAPI().addRole("regionalmanager", "regional manager", "regional branch manager");
    final Role salesmanRole = getIdentityAPI().addRole("salesmanRole", "salesman role", "Dunder Mifflin salesman");
    managerRole = getIdentityAPI().getRoleByUUID(managerRole.getUUID());
    assertEquals("regional manager", managerRole.getLabel());
    assertEquals("regional branch manager", managerRole.getDescription());
    assertEquals("regionalmanager", managerRole.getName());
    getIdentityAPI().updateRoleByUUID(managerRole.getUUID(), "manager", "regional branch manager",
        "Dunder Mifflin regional manager");
    managerRole = getIdentityAPI().getRoleByUUID(managerRole.getUUID());
    assertEquals("regional branch manager", managerRole.getLabel());
    assertEquals("Dunder Mifflin regional manager", managerRole.getDescription());
    assertEquals("manager", managerRole.getName());
    getIdentityAPI().removeRoleByUUID(managerRole.getUUID());
    final Set<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(salesmanRole.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testGetRoles() throws Exception {
    final Role regionalManager = getIdentityAPI().addRole("regionalmanager", "regional manager",
        "regional branch manager");
    final Role salesMan = getIdentityAPI().addRole("salesmanRole", "salesman role", "Dunder Mifflin salesman");

    final List<Role> allRoles = getIdentityAPI().getAllRoles();
    assertEquals(4, allRoles.size());

    final int numberOfRoles = getIdentityAPI().getNumberOfRoles();
    assertEquals(allRoles.size(), numberOfRoles);

    List<Role> roles = getIdentityAPI().getRoles(0, 2);
    assertEquals(2, roles.size());
    assertEquals("admin", roles.get(0).getName());
    assertEquals("regionalmanager", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(2, 2);
    assertEquals(2, roles.size());
    assertEquals("salesmanRole", roles.get(0).getName());
    assertEquals("user", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(4, 2);
    assertEquals(0, roles.size());

    final Collection<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(regionalManager.getUUID());
    roleUUIDs.add(salesMan.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testGetRolesOrderByNameAsc() throws Exception {
    final Role regionalManager = getIdentityAPI().addRole("regionalmanager", "Regional manager",
        "Regional branch manager");
    final Role salesMan = getIdentityAPI().addRole("salesmanRole", "Salesman role", "Dunder Mifflin salesman");

    final List<Role> allRoles = getIdentityAPI().getAllRoles();
    assertEquals(4, allRoles.size());

    final int numberOfRoles = getIdentityAPI().getNumberOfRoles();
    assertEquals(allRoles.size(), numberOfRoles);

    List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.NAME_ASC);
    assertEquals(2, roles.size());
    assertEquals("admin", roles.get(0).getName());
    assertEquals("regionalmanager", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(2, 2, RoleCriterion.NAME_ASC);
    assertEquals(2, roles.size());
    assertEquals("salesmanRole", roles.get(0).getName());
    assertEquals("user", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(4, 2);
    assertEquals(0, roles.size());

    final Collection<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(regionalManager.getUUID());
    roleUUIDs.add(salesMan.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testGetRolesOrderByLabelAsc() throws Exception {
    final Role regionalManager = getIdentityAPI().addRole("regionalmanager", "Regional manager",
        "Regional branch manager");
    final Role salesMan = getIdentityAPI().addRole("salesmanRole", "Salesman role", "Dunder Mifflin salesman");

    final List<Role> allRoles = getIdentityAPI().getAllRoles();
    assertEquals(4, allRoles.size());

    final int numberOfRoles = getIdentityAPI().getNumberOfRoles();
    assertEquals(allRoles.size(), numberOfRoles);

    List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.LABEL_ASC);
    assertEquals(2, roles.size());
    assertEquals("admin", roles.get(0).getName());
    assertEquals("regionalmanager", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(2, 2, RoleCriterion.LABEL_ASC);
    assertEquals(2, roles.size());
    assertEquals("salesmanRole", roles.get(0).getName());
    assertEquals("user", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(4, 2);
    assertEquals(0, roles.size());

    final Collection<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(regionalManager.getUUID());
    roleUUIDs.add(salesMan.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testGetRolesOrderByNameDesc() throws Exception {
    final Role regionalManager = getIdentityAPI().addRole("regionalmanager", "Regional manager",
        "Regional branch manager");
    final Role salesMan = getIdentityAPI().addRole("salesmanRole", "Salesman role", "Dunder Mifflin salesman");

    final List<Role> allRoles = getIdentityAPI().getAllRoles();
    assertEquals(4, allRoles.size());

    final int numberOfRoles = getIdentityAPI().getNumberOfRoles();
    assertEquals(allRoles.size(), numberOfRoles);

    List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.NAME_DESC);
    assertEquals(2, roles.size());
    assertEquals("user", roles.get(0).getName());
    assertEquals("salesmanRole", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(2, 2, RoleCriterion.NAME_DESC);
    assertEquals(2, roles.size());
    assertEquals("regionalmanager", roles.get(0).getName());
    assertEquals("admin", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(4, 2);
    assertEquals(0, roles.size());

    final Collection<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(regionalManager.getUUID());
    roleUUIDs.add(salesMan.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testGetRolesOrderByLabelDesc() throws Exception {
    final Role regionalManager = getIdentityAPI().addRole("regionalmanager", "Regional manager",
        "Regional branch manager");
    final Role salesMan = getIdentityAPI().addRole("salesmanRole", "Salesman role", "Dunder Mifflin salesman");

    final List<Role> allRoles = getIdentityAPI().getAllRoles();
    assertEquals(4, allRoles.size());

    final int numberOfRoles = getIdentityAPI().getNumberOfRoles();
    assertEquals(allRoles.size(), numberOfRoles);

    List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.LABEL_DESC);
    assertEquals(2, roles.size());
    assertEquals("user", roles.get(0).getName());
    assertEquals("salesmanRole", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(2, 2, RoleCriterion.LABEL_DESC);
    assertEquals(2, roles.size());
    assertEquals("regionalmanager", roles.get(0).getName());
    assertEquals("admin", roles.get(1).getName());

    roles = getIdentityAPI().getRoles(4, 2);
    assertEquals(0, roles.size());

    final Collection<String> roleUUIDs = new HashSet<String>();
    roleUUIDs.add(regionalManager.getUUID());
    roleUUIDs.add(salesMan.getUUID());
    getIdentityAPI().removeRoles(roleUUIDs);
  }

  public void testAddRemoveUpdateGroup() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton branch",
        "Dunder Mifflin Scranton regional branch", null);
    Group salesGroup = getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales",
        scrantonGroup.getUUID());
    salesGroup = getIdentityAPI().getGroupByUUID(salesGroup.getUUID());
    assertEquals("Scranton Sales team", salesGroup.getLabel());
    assertEquals("Dunder Mifflin Sales", salesGroup.getDescription());
    assertEquals("sales", salesGroup.getName());
    assertEquals(scrantonGroup.getUUID(), salesGroup.getParentGroup().getUUID());
    final Group dunderMifflinGroup = getIdentityAPI().addGroup("dundermifflin", null);
    getIdentityAPI().updateGroupByUUID(salesGroup.getUUID(), "salesteam", "Sales team group",
        "Dunder Mifflin Sales team", dunderMifflinGroup.getUUID());
    salesGroup = getIdentityAPI().getGroupByUUID(salesGroup.getUUID());
    assertEquals("Sales team group", salesGroup.getLabel());
    assertEquals("Dunder Mifflin Sales team", salesGroup.getDescription());
    assertEquals("salesteam", salesGroup.getName());
    assertEquals(dunderMifflinGroup.getUUID(), salesGroup.getParentGroup().getUUID());
    getIdentityAPI().removeGroupByUUID(salesGroup.getUUID());
    final Set<String> groupUUIDs = new HashSet<String>();
    groupUUIDs.add(dunderMifflinGroup.getUUID());
    groupUUIDs.add(scrantonGroup.getUUID());
    getIdentityAPI().removeGroups(groupUUIDs);
  }

  public void testGetGroups() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allGroups = getIdentityAPI().getAllGroups();
    assertEquals(5, allGroups.size());

    final List<Group> rootGroups = getIdentityAPI().getChildrenGroupsByUUID(null);
    assertEquals(2, rootGroups.size());

    final int numberOfGroups = getIdentityAPI().getNumberOfGroups();
    assertEquals(allGroups.size(), numberOfGroups);

    List<Group> groups = getIdentityAPI().getGroups(0, 2);
    assertEquals(2, groups.size());
    assertEquals("accountancy", groups.get(0).getName());
    assertEquals("hr", groups.get(1).getName());

    groups = getIdentityAPI().getGroups(2, 3);
    assertEquals(3, groups.size());
    assertEquals("platform", groups.get(0).getName());
    assertEquals("sales", groups.get(1).getName());
    assertEquals("scranton", groups.get(2).getName());

    final List<Group> allChildrenGroups = getIdentityAPI().getChildrenGroupsByUUID(scrantonGroup.getUUID());
    assertEquals(3, allChildrenGroups.size());

    final int numberOfChildrenGroups = getIdentityAPI().getNumberOfChildrenGroups(scrantonGroup.getUUID());
    assertEquals(allChildrenGroups.size(), numberOfChildrenGroups);

    List<Group> childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 0, 2);
    assertEquals(2, childrenGroups.size());
    assertEquals("accountancy", childrenGroups.get(0).getName());
    assertEquals("hr", childrenGroups.get(1).getName());
    childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 2, 2);
    assertEquals(1, childrenGroups.size());
    assertEquals("sales", childrenGroups.get(0).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetGroupsOrderByNameAsc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allGroups = getIdentityAPI().getAllGroups();
    assertEquals(5, allGroups.size());

    final int numberOfGroups = getIdentityAPI().getNumberOfGroups();
    assertEquals(allGroups.size(), numberOfGroups);

    List<Group> groups = getIdentityAPI().getGroups(0, 2, GroupCriterion.NAME_ASC);
    assertEquals(2, groups.size());
    assertEquals("accountancy", groups.get(0).getName());
    assertEquals("hr", groups.get(1).getName());

    groups = getIdentityAPI().getGroups(2, 3, GroupCriterion.NAME_ASC);
    assertEquals(3, groups.size());
    assertEquals("platform", groups.get(0).getName());
    assertEquals("sales", groups.get(1).getName());
    assertEquals("scranton", groups.get(2).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetGroupsOrderByLabelAsc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allGroups = getIdentityAPI().getAllGroups();
    assertEquals(5, allGroups.size());

    final int numberOfGroups = getIdentityAPI().getNumberOfGroups();
    assertEquals(allGroups.size(), numberOfGroups);

    List<Group> groups = getIdentityAPI().getGroups(0, 2, GroupCriterion.LABEL_ASC);
    assertEquals(2, groups.size());
    assertEquals("platform", groups.get(0).getName());
    assertEquals("accountancy", groups.get(1).getName());

    groups = getIdentityAPI().getGroups(2, 3, GroupCriterion.LABEL_ASC);
    assertEquals(3, groups.size());
    assertEquals("scranton", groups.get(0).getName());
    assertEquals("hr", groups.get(1).getName());
    assertEquals("sales", groups.get(2).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetGroupsOrderByNameDesc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allGroups = getIdentityAPI().getAllGroups();
    assertEquals(5, allGroups.size());

    final int numberOfGroups = getIdentityAPI().getNumberOfGroups();
    assertEquals(allGroups.size(), numberOfGroups);

    List<Group> groups = getIdentityAPI().getGroups(0, 2, GroupCriterion.NAME_DESC);
    assertEquals(2, groups.size());
    assertEquals("scranton", groups.get(0).getName());
    assertEquals("sales", groups.get(1).getName());

    groups = getIdentityAPI().getGroups(2, 3, GroupCriterion.NAME_DESC);
    assertEquals(3, groups.size());
    assertEquals("platform", groups.get(0).getName());
    assertEquals("hr", groups.get(1).getName());
    assertEquals("accountancy", groups.get(2).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetGroupsOrderByLabelDesc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allGroups = getIdentityAPI().getAllGroups();
    assertEquals(5, allGroups.size());

    final int numberOfGroups = getIdentityAPI().getNumberOfGroups();
    assertEquals(allGroups.size(), numberOfGroups);

    List<Group> groups = getIdentityAPI().getGroups(0, 2, GroupCriterion.LABEL_DESC);
    assertEquals(2, groups.size());
    assertEquals("sales", groups.get(0).getName());
    assertEquals("hr", groups.get(1).getName());

    groups = getIdentityAPI().getGroups(2, 3, GroupCriterion.LABEL_DESC);
    assertEquals(3, groups.size());
    assertEquals(3, groups.size());
    assertEquals("scranton", groups.get(0).getName());
    assertEquals("accountancy", groups.get(1).getName());
    assertEquals("platform", groups.get(2).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetChildrenGroupsOrderByNameAsc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allChildrenGroups = getIdentityAPI().getChildrenGroupsByUUID(scrantonGroup.getUUID());
    assertEquals(3, allChildrenGroups.size());

    final int numberOfChildrenGroups = getIdentityAPI().getNumberOfChildrenGroups(scrantonGroup.getUUID());
    assertEquals(allChildrenGroups.size(), numberOfChildrenGroups);

    List<Group> childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 0, 2,
        GroupCriterion.NAME_ASC);
    assertEquals(2, childrenGroups.size());
    assertEquals("accountancy", childrenGroups.get(0).getName());
    assertEquals("hr", childrenGroups.get(1).getName());
    childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 2, 2, GroupCriterion.NAME_ASC);
    assertEquals(1, childrenGroups.size());
    assertEquals("sales", childrenGroups.get(0).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetChildrenGroupsOrderByLabelAsc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allChildrenGroups = getIdentityAPI().getChildrenGroupsByUUID(scrantonGroup.getUUID());
    assertEquals(3, allChildrenGroups.size());

    final int numberOfChildrenGroups = getIdentityAPI().getNumberOfChildrenGroups(scrantonGroup.getUUID());
    assertEquals(allChildrenGroups.size(), numberOfChildrenGroups);

    List<Group> childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 0, 2,
        GroupCriterion.LABEL_ASC);
    assertEquals(2, childrenGroups.size());
    assertEquals("accountancy", childrenGroups.get(0).getName());
    assertEquals("hr", childrenGroups.get(1).getName());
    childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 2, 2, GroupCriterion.LABEL_ASC);
    assertEquals(1, childrenGroups.size());
    assertEquals("sales", childrenGroups.get(0).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetChildrenGroupsOrderByNameDesc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allChildrenGroups = getIdentityAPI().getChildrenGroupsByUUID(scrantonGroup.getUUID());
    assertEquals(3, allChildrenGroups.size());

    final int numberOfChildrenGroups = getIdentityAPI().getNumberOfChildrenGroups(scrantonGroup.getUUID());
    assertEquals(allChildrenGroups.size(), numberOfChildrenGroups);

    List<Group> childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 0, 2,
        GroupCriterion.NAME_DESC);
    assertEquals(2, childrenGroups.size());
    assertEquals("sales", childrenGroups.get(0).getName());
    assertEquals("hr", childrenGroups.get(1).getName());
    childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 2, 2, GroupCriterion.NAME_DESC);
    assertEquals(1, childrenGroups.size());
    assertEquals("accountancy", childrenGroups.get(0).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testGetChildrenGroupsOrderByLabelDesc() throws Exception {
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton Branch",
        "Dunder Mifflin Scranton regional branch", null);
    getIdentityAPI().addGroup("sales", "Scranton Sales team", "Dunder Mifflin Sales", scrantonGroup.getUUID());
    getIdentityAPI().addGroup("accountancy", "Scranton Accountancy", "Dunder Mifflin Scranton accountancy",
        scrantonGroup.getUUID());
    getIdentityAPI().addGroup("hr", "Scranton HR", "Dunder Mifflin Scranton HR", scrantonGroup.getUUID());

    final List<Group> allChildrenGroups = getIdentityAPI().getChildrenGroupsByUUID(scrantonGroup.getUUID());
    assertEquals(3, allChildrenGroups.size());

    final int numberOfChildrenGroups = getIdentityAPI().getNumberOfChildrenGroups(scrantonGroup.getUUID());
    assertEquals(allChildrenGroups.size(), numberOfChildrenGroups);

    List<Group> childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 0, 2,
        GroupCriterion.LABEL_DESC);
    assertEquals(2, childrenGroups.size());
    assertEquals("sales", childrenGroups.get(0).getName());
    assertEquals("hr", childrenGroups.get(1).getName());
    childrenGroups = getIdentityAPI().getChildrenGroups(scrantonGroup.getUUID(), 2, 2, GroupCriterion.LABEL_DESC);
    assertEquals(1, childrenGroups.size());
    assertEquals("accountancy", childrenGroups.get(0).getName());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
  }

  public void testAddRemoveMembershipsToUser() throws Exception {
    User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "michael", "scott", "Mr", "Regional Manager",
        null, null);
    User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "dwight", "schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final Role managerRole = getIdentityAPI().addRole("regionalmanager", "regional manager", "regional branch manager");
    final Role assistantRole = getIdentityAPI().addRole("assistantregionalmanager", "assistant regional manager",
        "assistant regional branch manager");
    final Role salesmanRole = getIdentityAPI().addRole("salesmanRole", "salesman role", "Dunder Mifflin salesman");
    final Group scrantonGroup = getIdentityAPI().addGroup("scranton", "Scranton branch",
        "Dunder Mifflin Scranton regional branch", null);
    final Membership managerMembership = getIdentityAPI().getMembershipForRoleAndGroup(managerRole.getUUID(),
        scrantonGroup.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), managerMembership.getUUID());
    final Membership salesmanMembership = getIdentityAPI().getMembershipForRoleAndGroup(salesmanRole.getUUID(),
        scrantonGroup.getUUID());
    final Membership assistantMembership = getIdentityAPI().getMembershipForRoleAndGroup(assistantRole.getUUID(),
        scrantonGroup.getUUID());
    final Set<String> dwightsMemberships = new HashSet<String>();
    dwightsMemberships.add(salesmanMembership.getUUID());
    dwightsMemberships.add(assistantMembership.getUUID());
    getIdentityAPI().addMembershipsToUser(dwight.getUUID(), dwightsMemberships);

    List<User> users = getIdentityAPI().getAllUsersInGroup(scrantonGroup.getUUID());
    assertEquals(2, users.size());
    if (users.get(0).getFirstName().equals("dwight")) {
      dwight = users.get(0);
      michael = users.get(1);
    } else {
      michael = users.get(0);
      dwight = users.get(1);
    }
    Set<Membership> dwightMemberships = dwight.getMemberships();
    assertEquals(2, dwightMemberships.size());
    Set<Membership> michaelsMemberships = michael.getMemberships();
    assertEquals(1, michaelsMemberships.size());

    users = getIdentityAPI().getAllUsersInRoleAndGroup(assistantRole.getUUID(), scrantonGroup.getUUID());
    assertEquals(1, users.size());
    assertEquals("dwight", users.get(0).getFirstName());

    users = getIdentityAPI().getAllUsersInRole(salesmanRole.getUUID());
    assertEquals(1, users.size());
    assertEquals("dwight", users.get(0).getFirstName());

    users = getIdentityAPI().getAllUsersInRole(managerRole.getUUID());
    assertEquals(1, users.size());
    assertEquals("michael", users.get(0).getFirstName());

    getIdentityAPI().removeMembershipFromUser(dwight.getUUID(), assistantMembership.getUUID());
    dwight = getIdentityAPI().getUserByUUID(dwight.getUUID());
    dwightMemberships = dwight.getMemberships();
    assertEquals(1, dwightMemberships.size());

    final Set<String> michaelsMembershipUUIDs = new HashSet<String>();
    michaelsMembershipUUIDs.add(salesmanMembership.getUUID());
    michaelsMembershipUUIDs.add(managerMembership.getUUID());
    getIdentityAPI().setUserMemberships(michael.getUUID(), michaelsMembershipUUIDs);
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    michaelsMemberships = michael.getMemberships();
    assertEquals(2, michaelsMemberships.size());

    getIdentityAPI().removeGroupByUUID(scrantonGroup.getUUID());
    dwight = getIdentityAPI().getUserByUUID(dwight.getUUID());
    michael = getIdentityAPI().getUserByUUID(michael.getUUID());
    dwightMemberships = dwight.getMemberships();
    assertEquals(0, dwightMemberships.size());
    michaelsMemberships = michael.getMemberships();
    assertEquals(0, michaelsMemberships.size());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
    
    getIdentityAPI().removeRoleByUUID(managerRole.getUUID());
    getIdentityAPI().removeRoleByUUID(assistantRole.getUUID());
    getIdentityAPI().removeRoleByUUID(salesmanRole.getUUID());
  }

  public void testGroupPaths() throws Exception {
    final Group firstLevel = getIdentityAPI().addGroup("first-level", "fl", "fd", null);
    final Group secondLevel = getIdentityAPI().addGroup("second-level", "sl", "sd", firstLevel.getUUID());
    final Group thirdLevel = getIdentityAPI().addGroup("third-level", "tl", "td", secondLevel.getUUID());
    final Group thirdfirstLevel = getIdentityAPI().addGroup("first-level", "ffl", "ffd", thirdLevel.getUUID());
    final Group thirdsecondLevel = getIdentityAPI().addGroup("second-level", "fsl", "fsd", thirdfirstLevel.getUUID());

    final List<String> hierarchy = new ArrayList<String>();
    hierarchy.add("first-level");

    Group group = getIdentityAPI().getGroupUsingPath(hierarchy);
    groupsEqual(firstLevel, group);

    hierarchy.add("second-level");
    group = getIdentityAPI().getGroupUsingPath(hierarchy);
    groupsEqual(secondLevel, group);

    hierarchy.add("third-level");
    hierarchy.add("first-level");
    hierarchy.add("second-level");
    group = getIdentityAPI().getGroupUsingPath(hierarchy);
    groupsEqual(thirdsecondLevel, group);

    hierarchy.add("third-level");
    group = getIdentityAPI().getGroupUsingPath(hierarchy);
    assertNull(group);

    getIdentityAPI().removeGroupByUUID(firstLevel.getUUID());
  }

  private void groupsEqual(final Group expected, final Group actual) {
    assertEquals(expected.getUUID(), actual.getUUID());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getLabel(), actual.getLabel());
    assertEquals(expected.getDescription(), actual.getDescription());
    final Group parentGroup = expected.getParentGroup();
    final Group actualParentGroup = actual.getParentGroup();
    if (parentGroup == null) {
      assertNull(actual.getParentGroup());
    } else if (actualParentGroup != null) {
      assertEquals(parentGroup.getUUID(), actualParentGroup.getUUID());
    } else {
      assertFalse(true);
    }
  }

  public void testGetUserInRoleOrderByFirstNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInRoleOrderByLastNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Schrute", users.get(0).getLastName());
    assertEquals("Scott", users.get(1).getLastName());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);

  }

  public void testGetUserInRoleOrderByUsernameAsc() throws Exception {

    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);

  }

  public void testGetUserInRoleOrderByFirstNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInRoleOrderByLastNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Scott", users.get(0).getLastName());
    assertEquals("Schrute", users.get(1).getLastName());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInRoleOrderByUsernameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInRole = getIdentityAPI().getAllUsersInRole(role.getUUID());
    assertEquals(4, allUsersInRole.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInRole(role.getUUID());
    assertEquals(allUsersInRole.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInRole(role.getUUID(), 0, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInRole(role.getUUID(), 2, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInGroupOrderByFirstNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.FIRST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInGroupOrderByLastNameAsc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.LAST_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("Schrute", users.get(0).getLastName());
    assertEquals("Scott", users.get(1).getLastName());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInGroupOrderByUsernameAsc() throws Exception {

    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("dwight.schrute", users.get(0).getUsername());
    assertEquals("jim.halpert", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.USER_NAME_ASC);
    assertEquals(2, users.size());
    assertEquals("michael.scott", users.get(0).getUsername());
    assertEquals("pam.halpert", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);

  }

  public void testGetUserInGroupOrderByFirstNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.FIRST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInGroupOrderByLastNameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Scott", users.get(0).getLastName());
    assertEquals("Schrute", users.get(1).getLastName());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.LAST_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("Halpert", users.get(0).getLastName());
    assertEquals("Halpert", users.get(1).getLastName());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testGetUserInGroupOrderByUsernameDesc() throws Exception {
    final User michael = getIdentityAPI().addUser("michael.scott", "bestboss", "Michael", "Scott", "Mr",
        "Regional Manager", null, null);
    final User dwight = getIdentityAPI().addUser("dwight.schrute", "battlestargalactica", "Dwight", "Schrute", "Mr",
        "Salesman - Assistant Regional Manager", michael.getUUID(), null);
    final User jim = getIdentityAPI().addUser("jim.halpert", "apassword", "Jim", "Halpert", "Mr", "Salesman",
        michael.getUUID(), null);
    final User pam = getIdentityAPI().addUser("pam.halpert", "anotherpassword", "Pam", "Halpert", "Mrs", "Salesman",
        michael.getUUID(), null);

    final Role role = getIdentityAPI().addRole("arole", "role", "A role");
    final Group group = getIdentityAPI().addGroup("agroup", "Group", "A Group", null);
    final Membership memberShip = getIdentityAPI().getMembershipForRoleAndGroup(role.getUUID(), group.getUUID());
    getIdentityAPI().addMembershipToUser(michael.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(dwight.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(jim.getUUID(), memberShip.getUUID());
    getIdentityAPI().addMembershipToUser(pam.getUUID(), memberShip.getUUID());

    final List<User> allUsersInGroup = getIdentityAPI().getAllUsersInGroup(group.getUUID());
    assertEquals(4, allUsersInGroup.size());

    final int numberOfMetadata = getIdentityAPI().getNumberOfUsersInGroup(group.getUUID());
    assertEquals(allUsersInGroup.size(), numberOfMetadata);

    List<User> users = getIdentityAPI().getUsersInGroup(group.getUUID(), 0, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("pam.halpert", users.get(0).getUsername());
    assertEquals("michael.scott", users.get(1).getUsername());

    users = getIdentityAPI().getUsersInGroup(group.getUUID(), 2, 2, UserCriterion.USER_NAME_DESC);
    assertEquals(2, users.size());
    assertEquals("jim.halpert", users.get(0).getUsername());
    assertEquals("dwight.schrute", users.get(1).getUsername());

    getIdentityAPI().removeGroupByUUID(group.getUUID());
    getIdentityAPI().removeRoleByUUID(role.getUUID());

    final Set<String> userUUIDs = new HashSet<String>();
    userUUIDs.add(michael.getUUID());
    userUUIDs.add(dwight.getUUID());
    userUUIDs.add(jim.getUUID());
    userUUIDs.add(pam.getUUID());
    getIdentityAPI().removeUsers(userUUIDs);
  }

  public void testDeleteAUserWithMetaData() throws Exception {
    Map<String, String> metadata = new HashMap<String, String>();
    metadata.put("meta", "data");
    final ProfileMetadata meta = getIdentityAPI().addProfileMetadata("meta");
    final User jane = getIdentityAPI().addUser("jane", "bpm", "Jane", "Doe", "", "", null, metadata);

    metadata = new HashMap<String, String>();
    metadata.put("meta", "meta");
    final User joe = getIdentityAPI().addUser("joe", "bpm", "Joe", "Doe", "", "", null, metadata);

    getIdentityAPI().removeUserByUUID(jane.getUUID());
    getIdentityAPI().removeUserByUUID(joe.getUUID());
    getIdentityAPI().removeProfileMetadataByUUID(meta.getUUID());
  }

  public void testDeleteGroups() throws BonitaException {
    final Group parentGroup = getIdentityAPI().addGroup("parentGroup", null);
    final Group childGroup = getIdentityAPI().addGroup("childGroup", parentGroup.getUUID());

    final List<String> groupUUIDs = new ArrayList<String>(2);
    groupUUIDs.add(parentGroup.getUUID());
    groupUUIDs.add(childGroup.getUUID());
    getIdentityAPI().removeGroups(groupUUIDs);
  }

  public void testRemoveGroupHierarchy() throws BonitaException {
    final Group parentGroup = getIdentityAPI().addGroup("parentGroup", null);
    final Group childGroup = getIdentityAPI().addGroup("childGroup", parentGroup.getUUID());
    final Group grandChildGroup = getIdentityAPI().addGroup("grandChildGroup", childGroup.getUUID());

    getIdentityAPI().removeGroupByUUID(parentGroup.getUUID());

    try {
      getIdentityAPI().getGroupByUUID(grandChildGroup.getUUID());
      fail("The grand parent group has been deleted so the grand child must be deleted as well");
    } catch (final GroupNotFoundException e) {
      // TODO: handle exception
    }
  }

}
