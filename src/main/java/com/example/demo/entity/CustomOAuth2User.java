package com.example.demo.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements org.springframework.security.oauth2.core.user.OAuth2User {
    private final User user;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(User user, Map<String,Object>attributes, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getSocialId();
    }

    public User getUser() {
        return user;
    }
}
