package com.builder.userexample.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the holder for all data but only {@code userId} is public along with the static {@code create()} method.
 */
public class StdUser implements User {

    @JsonProperty("user_id")
    public final String userId;
    @JsonProperty("attributes")
    protected final Map<String, String> attributes; // must be protected

    private StdUser(final String userId) {
        this.userId = userId;
        this.attributes = new HashMap<>();
    }

    protected StdUser(final StdUser stdUser) { // must be protected
        this.userId = stdUser.userId;
        this.attributes = stdUser.attributes;
    }

    @JsonCreator
    protected StdUser( @JsonProperty("user_id") final String userId,  @JsonProperty("attributes") final Map<String, String> entries) {
        this.userId = userId;
        this.attributes = Collections.unmodifiableMap(entries);
    }

    public static StdUser create(final String userId) {
        return new StdUser(userId);
    }

}
