package com.dev.turismo.agencia.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dev.turismo.agencia.repository.VendedorRepository;

@Service
public class VendedorUserDetailsService implements UserDetailsService {
    private final VendedorRepository repo;

    public VendedorUserDetailsService(VendedorRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var v = repo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("email não encontrado"));
        var auth = new SimpleGrantedAuthority("ROLE_" + v.getRole().name());
        return new User(v.getEmail(), v.getSenha(), List.of(auth));
    }
}
