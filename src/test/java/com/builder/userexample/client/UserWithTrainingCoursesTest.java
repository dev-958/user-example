package com.builder.userexample.client;

import com.builder.userexample.domain.StdUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UserWithTrainingCoursesTest {

    @Test
    public void populationTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");

        assertEquals("User id not set", "test-user", user.userId);
        assertEquals("training course not set", "COURSE1", user.getTrainingCompleted().stream().findFirst().orElse("NOT-FOUND"));
    }

    @Test
    public void unMarshallTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");

        final ObjectMapper mapper = new ObjectMapper();
        final String json = mapper.writeValueAsString(user);
        assertEquals("String representation not as expected", "{\"userWithTrainingCompleted\":{\"user_id\":\"test-user\",\"attributes\":{\"TRAINING_COURSES\":\"[\\\"COURSE1\\\"]\"}}}", json);
    }

    @Test
    public void marshallTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final UserWithTrainingCompleted user = mapper.readValue("{\"userWithTrainingCompleted\":{\"user_id\":\"test-user\",\"attributes\":{\"TRAINING_COURSES\":\"[\\\"COURSE1\\\"]\"}}}", UserWithTrainingCompleted.class);

        assertEquals("User id not populated", "test-user", user.userId);
        assertEquals("Role not populated", "COURSE1", user.getTrainingCompleted().stream().findFirst().orElseGet(() -> "NOT-FOUND"));
    }

    @Test(expected = RuntimeException.class)
    public void invalidTrainingCourseTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("invalid-user").withTrainingCourses("LOUNGE-LIZARD");
    }

    /**
     * Here I demonstrate how it can be used in a message based application where you create an instance of
     * {@link UserWithRoles} and then pass it over the wire as a JSON {@link StdUser} which contains the additional
     * metadata but does not expose it in the Java object instance. This allows easy forwarding, persistence and caching
     * with eventual conversion into the source type.
     */
    @Test
    public void multiStagedMarshallTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("over-wire-user").withTrainingCourses("COURSE1");

        final ObjectMapper mapper = new ObjectMapper();
        final String userJson = mapper.writeValueAsString(user.stdUser());

        final StdUser stdUserIntermediary = mapper.readValue(userJson, StdUser.class);

        final UserWithTrainingCompleted outputUser = UserWithTrainingCompleted.create(stdUserIntermediary);

        assertEquals("Staged conversion failed", "COURSE1", outputUser.getTrainingCompleted().stream().findFirst().orElseGet(() -> "NOT-FOUND"));
    }

    /**
     * Test that the lazy marshalling is actually happening.
     */
    @Test
    public void lazyGetterTest() throws JsonProcessingException, NoSuchFieldException, IllegalAccessException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");
        final List<String> expectedTrainingCompleted = Collections.singletonList("COURSE1");

        // validate that the trainingCompleted field has not been set for the user
        final Field field = UserWithTrainingCompleted.class.getDeclaredField("trainingCompleted");
        field.setAccessible(true);
        assertNull("The trainingCompleted field has been populated, but it should have been null", field.get(user));

        // call the user's getter method which should then populate the trainingCompleted field after pulling the data out of the attribute map
        assertEquals("Failed to get the training completed field", expectedTrainingCompleted, user.getTrainingCompleted());

        // check that the user now has the trainingCompleted field populated
        assertEquals("Failed to get the training completed field", expectedTrainingCompleted, field.get(user));
    }

    @Test
    public void toStringTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");
        assertEquals("ToString behaviour has been changed", "UserWithTrainingCompleted[userId='test-user', trainingCompleted=[COURSE1]]", user.toString());
    }

    @Test
    public void equalsTestWithDifferentUserClasses() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");
        final StdUser stdUser = user.stdUser();
        assertTrue("Equals no longer works for comparing UserWithTrainingCompleted class with a StdUser class", user.equals(stdUser));
    }

    @Test
    public void identicalObjectEqualsTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");
        assertTrue("Equals behaviour no longer works when tested against the same object", user.equals(user));
    }

    @Test
    public void sameClassAndContentsEqualsTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user1 = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");
        final UserWithTrainingCompleted user2 = UserWithTrainingCompleted.create("test-user").withTrainingCourses("COURSE1");

        assertTrue("Equals behaviour no longer works when tested against different objects but the same contents", user1.equals(user2));
    }

    @Test
    public void sameClassDifferentContentsEqualsTest() throws JsonProcessingException {
        final UserWithTrainingCompleted user1 = UserWithTrainingCompleted.create("a-user").withTrainingCourses("COURSE1");
        final UserWithTrainingCompleted user2 = UserWithTrainingCompleted.create("different-user").withTrainingCourses("COURSE1");

        assertFalse("Equals behaviour no longer works when tested against different objects with different contents", user1.equals(user2));
    }
}
