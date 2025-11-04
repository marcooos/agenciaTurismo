package com.dev.turismo.agencia.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    // guarda sessão e infos
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, SessionInfo> infos = new ConcurrentHashMap<>();

    public void onCreated(HttpSession s) {
        var info = new SessionInfo(
                s.getId(), null, null,
                Instant.ofEpochMilli(s.getCreationTime()),
                Instant.ofEpochMilli(s.getLastAccessedTime()),
                null, null, s.getMaxInactiveInterval());
        sessions.put(s.getId(), s);
        infos.put(s.getId(), info);
    }

    public void onDestroyed(String id) {
        sessions.remove(id);
        infos.remove(id);
    }

    /** Chame após autenticar no login */
    public void onLogin(HttpSession s, Long vendedorId, String nome) {
        var info = infos.getOrDefault(s.getId(), new SessionInfo());
        info.setId(s.getId());
        info.setVendedorId(vendedorId);
        info.setNome(nome);
        info.setCreatedAt(Instant.ofEpochMilli(s.getCreationTime()));
        info.setLastAccessAt(Instant.now());
        info.setMaxInactiveSeconds(s.getMaxInactiveInterval());
        infos.put(s.getId(), info);
        sessions.putIfAbsent(s.getId(), s);
    }

    /** Chame a cada request autenticada (atualiza último acesso, ip e UA) */
    public void onTouch(HttpSession s, HttpServletRequest req) {
        if (s == null)
            return; // <--- evita NPE

        var info = infos.get(s.getId());
        if (info == null) {
            // se não estava registrado (ex.: reinício quente), cria um básico
            onCreated(s);
            info = infos.get(s.getId());
        }
        info.setLastAccessAt(java.time.Instant.now());
        info.setMaxInactiveSeconds(s.getMaxInactiveInterval());
        // atualiza IP e UA a cada toque (ou só na primeira vez se preferir)
        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For")).orElse(req.getRemoteAddr());
        info.setIp(ip);
        info.setUserAgent(Optional.ofNullable(req.getHeader("User-Agent")).orElse("?"));
    }

    public List<SessionInfo> list() {
        var list = new ArrayList<>(infos.values());
        list.sort(Comparator.comparing(SessionInfo::getLastAccessAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());
        return list;
    }

    public boolean invalidate(String id) {
        HttpSession s = sessions.get(id);
        if (s != null) {
            s.invalidate();
            return true;
        }
        return false;
    }
}