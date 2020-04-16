package com.builder.userexample.client;

import com.builder.userexample.domain.StdUser;
import com.builder.userexample.domain.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Implements {@link User} but contains a private instance of {@link StdUser} which is the primary source of state,
 * the public fields give external access to that.
 */
public class UserWithTrainingCompleted implements User {

    private static final List<String> allowed = Stream.of("COURSE1", "COURSE2", "COURSE3").collect(toList());
    private static final String TRAINING_COURSE_KEY = "TRAINING_COURSES";
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("userWithTrainingCompleted")
    private final StdUser userWithTrainingCompleted;

    @JsonIgnore
    public String userId;
    @JsonIgnore
    private List<String> trainingCompleted = null;

    /**
     * This {@link com.builder.userexample.client.UserWithTrainingCompleted.Proxy} Class grants local access to protected elements within the {@link StdUser} instance
     */
    private static class Proxy extends StdUser {
        public Proxy(final String userId, final Map<String, String> attributes) {
            super(userId, attributes);
        }
        public Proxy(final StdUser stdUser) {
            super(stdUser);
        }
        public Map<String, String> getAttributes() {
            return super.attributes;
        }

        public String getUserId() {
            return super.userId;
        }
    }

    @JsonCreator
    private UserWithTrainingCompleted(@JsonProperty("userWithTrainingCompleted") final StdUser UserWithTrainingCompleted) {
        this.userWithTrainingCompleted = UserWithTrainingCompleted;
        this.userId = UserWithTrainingCompleted.userId;
    }

    private UserWithTrainingCompleted(final String userId, final Map<String, String> attributes) {
        this.userWithTrainingCompleted = new Proxy(userId, attributes);
        this.userId = userId;
    }

    @JsonIgnore
    public List<String> getTrainingCompleted() {
        if (this.trainingCompleted == null) {
            this.trainingCompleted = trainingCourseGen(new Proxy(userWithTrainingCompleted).getAttributes());
        }
        return this.trainingCompleted;
    }

    /**
     * Grants access to the contained {@link StdUser} as a conversion utility
     */
    protected StdUser stdUser() {
        return this.userWithTrainingCompleted;
    }

    /**
     * Utility function
     */
    private static List<String> trainingCourseGen(final Map<String, String> attributes) {
        return Optional.ofNullable(attributes.get(TRAINING_COURSE_KEY)).map(val -> {
            try {
                return mapper.readValue(val, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                return new ArrayList<String>();
            }
        }).orElseGet(ArrayList::new);
    }

    /**
     * Static factory method
     */
    public static ITrainingCourses create(final String userId) {
        return trainingCourses -> new UserWithTrainingCompleted(userId, Stream.of(new SimpleImmutableEntry<>(TRAINING_COURSE_KEY,
                mapper.writeValueAsString(Stream.of(trainingCourses)
                        .peek(course -> Optional.of(allowed.contains(course)).filter(val -> val).orElseThrow(() -> new RuntimeException("Invalid training course supplied")))
                        .collect(toList()))
        )).collect(toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue)));
    }

    /**
     * Conversion utility method
     */
    public static com.builder.userexample.client.UserWithTrainingCompleted create(final StdUser stdUser) {
        return new com.builder.userexample.client.UserWithTrainingCompleted(stdUser);
    }

    public interface ITrainingCourses {
        UserWithTrainingCompleted withTrainingCourses(String... trainingCourses) throws JsonProcessingException;
    }

    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object.getClass() != UserWithTrainingCompleted.class) {
            if (object instanceof StdUser) {
                return equals(UserWithTrainingCompleted.create((StdUser) object));
            } else {
                return false;
            }
        }

        final UserWithTrainingCompleted that = (UserWithTrainingCompleted) object;
        return userId.equals(that.userId) &&
                java.util.Objects.equals(getTrainingCompleted(), that.getTrainingCompleted()); //needs to use getter method because of the lazy instanciation of this field
    }

    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, getTrainingCompleted()); //needs to use getter method for the training completed because of the lazy instanciation of this field
    }

    public java.lang.String toString() {
        return new java.util.StringJoiner(", ", UserWithTrainingCompleted.class.getSimpleName() + "[", "]")
                .add("userId='" + userId + "'")
                .add("trainingCompleted=" + getTrainingCompleted()) //needs to use getter method because of the lazy instanciation of this field
                .toString();
    }
}
