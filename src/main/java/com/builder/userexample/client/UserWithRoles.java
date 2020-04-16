package com.builder.userexample.client;

import com.builder.userexample.domain.StdUser;
import com.builder.userexample.domain.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Implements {@link User} but contains a private instance of {@link StdUser} which is the primary source of state,
 * the public fields give external access to that.
 */
public class UserWithRoles implements User {

    private static final List<String> allowed = Stream.of("USER", "DEV", "ADMIN").collect(toList());
    private static final String ROLE_KEY = "ROLES";
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("userWithRoles")
    private final StdUser userWithRoles;

    @JsonIgnore
    public final String userId;
    @JsonIgnore
    public final List<String> roles;

    /**
     * This {@link Proxy} Class grants local access to protected elements within the {@link StdUser} instance
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
    }

    @JsonCreator
    private UserWithRoles(@JsonProperty("userWithRoles") final StdUser userWithRoles) {
        this.userWithRoles = userWithRoles;
        this.userId = userWithRoles.userId;
        this.roles = Collections.unmodifiableList(roleGen(new Proxy(userWithRoles).getAttributes()));
    }

    private UserWithRoles(final String userId, final Map<String, String> attributes) {
        this.userWithRoles = new Proxy(userId, attributes);
        this.userId = userId;
        this.roles = Collections.unmodifiableList(roleGen(attributes));
    }

    /**
     * Grants access to the contained {@link StdUser} as a conversion utility
     */
    protected StdUser stdUser() {
        return this.userWithRoles;
    }

    /**
     * Utility function
     */
    private static List<String> roleGen(final Map<String, String> attributes) {
        return Optional.ofNullable(attributes.get(ROLE_KEY)).map(val -> {
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
    public static IRoles create(final String userId) {
        return roles -> new UserWithRoles(userId, Stream.of(new SimpleImmutableEntry<>(ROLE_KEY,
                mapper.writeValueAsString(Stream.of(roles)
                        .peek(role -> Optional.of(allowed.contains(role)).filter(val -> val).orElseThrow(() -> new RuntimeException("Invalid Role supplied")))
                        .collect(toList()))
                )).collect(toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue)));
    }

    /**
     * Conversion utility method
     */
    public static UserWithRoles create(final StdUser stdUser) {
        return new UserWithRoles(stdUser);
    }

    public interface IRoles {
        UserWithRoles withRoles(String... roles) throws JsonProcessingException;
    }

}
