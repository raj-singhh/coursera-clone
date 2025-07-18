// src/main/java/com/example/coursera_clone/security/jwt/AuthTokenFilter.java
package com.example.coursera_clone.security.jwt;

import com.example.coursera_clone.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            // DEBUG: Log the incoming request URI and Authorization header
            logger.info("DEBUG (AuthTokenFilter): Processing request for URI: {}", request.getRequestURI());
            String headerAuth = request.getHeader("Authorization");
            logger.info("DEBUG (AuthTokenFilter): Received Authorization header: {}", 
                headerAuth != null ? (headerAuth.length() > 50 ? headerAuth.substring(0, 50) + "..." : headerAuth) : "null");


            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("DEBUG (AuthTokenFilter): Successfully authenticated user: {}", username);
            } else {
                logger.warn("DEBUG (AuthTokenFilter): No valid JWT found or JWT validation failed for URI: {}. JWT: {}", 
                    request.getRequestURI(), jwt != null ? (jwt.length() > 50 ? jwt.substring(0, 50) + "..." : jwt) : "null");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for URI {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response); // Ensure filterChain.doFilter is called to continue the filter chain
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
