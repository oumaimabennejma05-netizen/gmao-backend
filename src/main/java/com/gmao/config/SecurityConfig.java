package com.gmao.config;

import com.gmao.security.CustomUserDetailsService;
import com.gmao.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2FA endpoints — authenticated only (any role)
                        .requestMatchers("/api/auth/2fa/**").authenticated()

                        // Profile update — any authenticated user (security enforced in service)
                        .requestMatchers(HttpMethod.PUT, "/api/users/profile/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()

                        // Technicians list — ADMIN or RESPONSABLE (enforced via @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/users/technicians").hasAnyRole("ADMIN", "RESPONSABLE")

                        // Users CRUD — ADMIN only (additional @PreAuthorize on methods)
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Reports — ADMIN only
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")

                        // Maintenance alerts — all authenticated users
                        .requestMatchers("/api/machines/maintenance-alerts").authenticated()

                        // AI chat — ADMIN only
                        .requestMatchers("/api/ai/chat").hasRole("ADMIN")

                        // Machines — ADMIN only for write; anyone authenticated for read
                        .requestMatchers(HttpMethod.POST, "/api/machines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/machines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/machines/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/machines/**").authenticated()

                        // Tasks — all authenticated users (role checks via @PreAuthorize per method)
                        .requestMatchers("/api/tasks/**").authenticated()

                        // Dashboard + AI predictions — all authenticated
                        .requestMatchers("/api/ai/predictions").authenticated()
                        .requestMatchers("/api/dashboard/**").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}