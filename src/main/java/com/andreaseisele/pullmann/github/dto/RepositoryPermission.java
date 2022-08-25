package com.andreaseisele.pullmann.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryPermission(
    Permission permission,
    @JsonProperty("role_name") String roleName
) {

    public static RepositoryPermission none() {
        return new RepositoryPermission(Permission.NONE, "none");
    }

    public enum Permission {
        ADMIN,
        WRITE,
        READ,
        NONE
    }

}
