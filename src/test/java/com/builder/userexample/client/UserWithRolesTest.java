package com.builder.userexample.client;

import com.builder.userexample.domain.StdUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class UserWithRolesTest {

    @Test
    public void populationTest() throws JsonProcessingException {
        final UserWithRoles user = UserWithRoles.create("test-user").withRoles("DEV");

        assertEquals("User id not set", "test-user", user.userId);
        assertEquals("role not set", "DEV", user.roles.stream().findFirst().orElse("NOT-FOUND"));
    }

    @Test
    public void unMarshallTest() throws JsonProcessingException {
        final UserWithRoles user = UserWithRoles.create("test-user").withRoles("DEV");

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(user);
        assertEquals("String representation not as expected", "{\"userWithRoles\":{\"user_id\":\"test-user\",\"attributes\":{\"ROLES\":\"[\\\"DEV\\\"]\"}}}", json);
    }

    @Test
    public void marshallTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final UserWithRoles user = mapper.readValue("{\"userWithRoles\":{\"user_id\":\"test-user\",\"attributes\":{\"ROLES\":\"[\\\"DEV\\\"]\"}}}", UserWithRoles.class);

        assertEquals("User id not populated", "test-user", user.userId);
        assertEquals("Role not populated", "DEV", user.roles.stream().findFirst().orElseGet(() -> "NOT-FOUND"));
    }

    @Test(expected = RuntimeException.class)
    public void invalidRoleTest() throws JsonProcessingException {
        final UserWithRoles user = UserWithRoles.create("invalid-user").withRoles("LOUNGE-LIZARD");
    }

    /**
     * Here I demonstrate how it can be used in a message based application where you create an instance of
     * {@link UserWithRoles} and then pass it over the wire as a JSON {@link StdUser} which contains the additional
     * metadata but does not expose it in the Java object instance. This allows easy forwarding, persistence and caching
     * with eventual conversion into the source type.
     */
    @Test
    public void multiStagedMarshallTest() throws JsonProcessingException {
        final UserWithRoles user = UserWithRoles.create("over-wire-user").withRoles("ADMIN");

        final ObjectMapper mapper = new ObjectMapper();
        final String userWithRoles = mapper.writeValueAsString(user.stdUser());

        final StdUser stdUserIntermediary = mapper.readValue(userWithRoles, StdUser.class);

        final UserWithRoles outputUser = UserWithRoles.create(stdUserIntermediary);

        assertEquals("Staged conversion failed", "ADMIN", outputUser.roles.stream().findFirst().orElseGet(() -> "NOT-FOUND"));
    }

}
