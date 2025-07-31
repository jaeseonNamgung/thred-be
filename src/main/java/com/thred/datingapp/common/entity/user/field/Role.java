package com.thred.datingapp.common.entity.user.field;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private static final Map<String, Role> descriptions =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(Role::getRole, Function.identity())));
    private String role;
    Role(String role) {
        this.role = role;
    }

    public static Role findRole(String role) {
        return descriptions.get(role);
    }
}
