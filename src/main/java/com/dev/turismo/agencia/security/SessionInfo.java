package com.dev.turismo.agencia.security;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String id;
    private Long vendedorId;
    private String nome;
    private Instant createdAt;
    private Instant lastAccessAt;
    private String ip;
    private String userAgent;
    private int maxInactiveSeconds;
}