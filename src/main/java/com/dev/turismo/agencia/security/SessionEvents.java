package com.dev.turismo.agencia.security;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;

@Component
public class SessionEvents implements HttpSessionListener {

    private final SessionRegistry registry;

    public SessionEvents(SessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        registry.onCreated(se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        registry.onDestroyed(se.getSession().getId());
    }
}