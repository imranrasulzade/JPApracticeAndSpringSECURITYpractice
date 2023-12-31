package com.matrix.examplejpaapp.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrix.examplejpaapp.service.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> errorDetails = new HashMap<>();

        try {
            String accessToken = jwtUtil.resolveToken(request);
            if (accessToken == null ) {
                filterChain.doFilter(request, response);
                return;
            }
            log.info("token: {} ", accessToken);
            Claims claims = jwtUtil.resolveClaims(request);

            if(claims != null & jwtUtil.validateClaims(claims)){
                String user = claims.getSubject();
                Collection<GrantedAuthority> authorities = extractAuthorities(claims);
                log.info("user : {} ", user);
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user,"",authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("authentication : {} ", authentication);
            }

        }catch (Exception e){
            errorDetails.put("message", "Authentication Error");
            errorDetails.put("details",e.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            mapper.writeValue(response.getWriter(), errorDetails);

        }
        filterChain.doFilter(request, response);


    }

    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (claims.containsKey("authorities")) {
            List<String> roles = (List<String>) claims.get("authorities");
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return authorities;
    }
}
