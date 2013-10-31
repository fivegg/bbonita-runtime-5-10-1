package org.ow2.bonita.search;

import java.util.List;

import org.ow2.bonita.APITestCase;
import org.ow2.bonita.facade.identity.User;
import org.ow2.bonita.search.index.ContactInfoIndex;
import org.ow2.bonita.search.index.UserIndex;

public class SearchUserTest extends APITestCase {

  private boolean isAdmin(User user) {
    return "admin".equals(user.getUsername()) && user.getFirstName() == null
    && user.getLastName() == null
    && user.getProfessionalContactInfo() == null
    && user.getPersonalContactInfo() == null;
  }

  private boolean isJack(User user) {
    return "jack".equals(user.getUsername()) && "Jack".equals(user.getFirstName())
    && "Doe".equals(user.getLastName())
    && user.getProfessionalContactInfo() == null
    && user.getPersonalContactInfo() == null;
  }

  private boolean isJames(User user) {
    return "james".equals(user.getUsername()) && "James".equals(user.getFirstName())
    && "Doe".equals(user.getLastName())
    && user.getProfessionalContactInfo() == null
    && user.getPersonalContactInfo() == null;
  }

  private boolean isJohn(User user) {
    return "john".equals(user.getUsername()) && "John".equals(user.getFirstName())
    && "Doe".equals(user.getLastName())
    && user.getProfessionalContactInfo() == null
    && user.getPersonalContactInfo() == null;
  }

  public void testSearchUserUsingFirstName() throws Exception {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.FIRST_NAME).equalsTo("John");

    List<User> users = getQueryRuntimeAPI().search(query,0, 10);
    assertEquals(1, users.size());
    User john = users.get(0);
    assertTrue(isJohn(john));
  }

  public void testSearchUserUsingAFullUserName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.NAME).equalsTo("james");

    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, users.size());
    User user = users.get(0);
    assertTrue("User is James", isJames(user));
  }
  
  public void testSearchUsersUsingUserNamesStartWith() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.NAME).startsWith("ja");

    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(2, users.size());
    User first = users.get(0);
    User second = users.get(1);
    if (isJames(first)) {
      assertTrue(isJack(second));
    } else {
      assertTrue(isJack(first));
      assertTrue(isJames(second));
    }
  }  

  public void testSearchUsersUsingSingleWildcardUserName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.NAME).equalsTo("jam?s");
    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals("Cannot use single wildcard in a field value in this way", 0, users.size());
  }

  public void testSearchUsersUsingFuzzySearchUserName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.NAME).equalsTo("jame~");
    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals("Cannot use the fuzzy search character in a field value in this way", 0, users.size());
  }

  public void testSearchUsersUsingRangeSearchUserName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.NAME).ranges("admin", "jack", false);
    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);

    assertEquals(2, users.size());
    User first = users.get(0);
    User second = users.get(1);
    assertTrue("First User is admin or Jack", isAdmin(first) || isJack(first));
    assertTrue("Second user is admin or Jack", isAdmin(second) || isJack(second));
  }

  public void testSearchUsersUsingLastName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe");
    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(3, users.size());
    User first = users.get(0);
    User second = users.get(1);
    User third = users.get(2);
    assertTrue("First User is Jack, James or Joe", isJack(first) || isJames(first) || isJohn(first));
    assertTrue("Second User is Jack, James or Joe", isJack(second) || isJames(second) || isJohn(second));
    assertTrue("Third User is Jack, James or Joe", isJack(third) || isJames(third) || isJohn(third));
  }

  public void testSearchUserUsingEmail() throws Exception {
    String email = "jane.doe@bonita.org";
    User jane = getIdentityAPI().addUser("jane", "bpm", "Jane", "Doe", "", "", null, null);
    getIdentityAPI().updateUserProfessionalContactInfo(jane.getUUID(), email, "", "", "", "", "", "", "", "", "", "", "");

    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.PROFESSIONAL_INFO + ContactInfoIndex.EMAIL).equalsTo(email);
    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);

    assertEquals(1, users.size());
    User user = users.get(0);
    assertEquals("jane", user.getUsername());
    assertEquals("Jane", user.getFirstName());
    assertEquals("Doe", user.getLastName());
    assertEquals(email, user.getProfessionalContactInfo().getEmail());
    getIdentityAPI().removeUserByUUID(jane.getUUID());
  }

  public void testSearcUsersUsingFirstNameAndLastName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe")
    .and().criterion(UserIndex.NAME).equalsTo("Jack");

    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(1, users.size());
    User first = users.get(0);
    assertTrue("First User is Jack", isJack(first));
  }

  public void testSearchUsersUsingUserNameOrLastName() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe")
    .or().criterion(UserIndex.NAME).equalsTo("admin");

    List<User> users = getQueryRuntimeAPI().search(query, 0, 10);
    assertEquals(4, users.size());
    User first = users.get(0);
    User second = users.get(1);
    User third = users.get(2);
    User fourth = users.get(3);
    assertTrue("First User is admin, Jack, James or Joe", isAdmin(first) || isJack(first) || isJames(first) || isJohn(first));
    assertTrue("Second User is admin, Jack, James or Joe", isAdmin(second) || isJack(second) || isJames(second) || isJohn(second));
    assertTrue("Third User is admin, Jack, James or Joe", isAdmin(third) || isJack(third) || isJames(third) || isJohn(third));
    assertTrue("Fourth User is admin, Jack, James or Joe", isAdmin(fourth) || isJack(fourth) || isJames(fourth) || isJohn(fourth));
  }

  public void testCount() throws Exception {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe")
    .or().criterion(UserIndex.NAME).equalsTo("admin");
    
    int count = getQueryRuntimeAPI().search(query);
    assertEquals(4, count);
  }

  public void testSearchTwoUsersAmongFour() {
    SearchQueryBuilder query = new SearchQueryBuilder(new UserIndex());
    query.criterion(UserIndex.LAST_NAME).equalsTo("Doe")
    .or().criterion(UserIndex.NAME).equalsTo("admin");

    List<User> users = getQueryRuntimeAPI().search(query, 0, 2);
    assertEquals(2, users.size());
    User first = users.get(0);
    User second = users.get(1);
    assertTrue("First User is admin, Jack, James or Joe", isAdmin(first) || isJack(first) || isJames(first) || isJohn(first));
    assertTrue("Second User is admin, Jack, James or Joe", isAdmin(second) || isJack(second) || isJames(second) || isJohn(second));
  }

}
