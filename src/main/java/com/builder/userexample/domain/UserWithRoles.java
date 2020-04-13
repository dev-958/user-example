package com.builder.userexample.domain;

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

    @JsonCreator
    private UserWithRoles(@JsonProperty("userWithRoles") final StdUser userWithRoles) {
        this.userWithRoles = userWithRoles;
        this.userId = userWithRoles.userId;
        this.roles = Collections.unmodifiableList(roleGen(userWithRoles.attributes));
    }

    private UserWithRoles(final String userId, final Map<String, String> attributes) {
        this.userWithRoles = new StdUser(userId, attributes);
        this.userId = userId;
        this.roles = Collections.unmodifiableList(roleGen(attributes));
    }

    private static List<String> roleGen(final Map<String, String> attributes) {
        return Optional.ofNullable(attributes.get(ROLE_KEY)).map(val -> {
            try {
                return mapper.readValue(val, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (JsonProcessingException e) {
                return new ArrayList<String>();
            }
        }).orElseGet(ArrayList::new);
    }

    public static IRoles create(final String userId) {
        return roles -> new UserWithRoles(userId, Stream.of(new SimpleImmutableEntry<>(ROLE_KEY,
                mapper.writeValueAsString(Stream.of(roles)
                        .peek(role -> Optional.of(allowed.contains(role)).filter(val -> val).orElseThrow(() -> new RuntimeException("Invalid Role supplied")))
                        .collect(toList()))
                )).collect(toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue)));
    }

    public interface IRoles {
        UserWithRoles withRoles(String... roles) throws JsonProcessingException;
    }

}
