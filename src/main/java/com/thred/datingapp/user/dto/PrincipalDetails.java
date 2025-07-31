package com.thred.datingapp.user.dto;

import com.thred.datingapp.common.entity.user.User;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@RequiredArgsConstructor
public class PrincipalDetails implements UserDetails {

    private final User user ;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add((GrantedAuthority) () -> user.getRole().getRole());
        return collection;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    public Long getUserId(){
        return user.getId();
    }

    public String getRole(){
        return user.getRole().getRole();
    }
}
