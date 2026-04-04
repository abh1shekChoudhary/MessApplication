package org.messplacement.messsecond.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Public ────────────────────────────────────────────────
                .requestMatchers("/auth/**").permitAll()

                // ── Prices & Menu — readable by all authenticated roles ───
                .requestMatchers(HttpMethod.GET, "/prices", "/menu", "/menu/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/prices/**", "/menu/**").hasRole("ADMIN")

                // ── ADMIN write operations ────────────────────────────────
                .requestMatchers(HttpMethod.POST,   "/students").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/students").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/students/**").hasRole("ADMIN")

                // ── ADMIN + GUEST read (guest sees live data, frontend masks reg) ─
                .requestMatchers(HttpMethod.GET, "/getStudents").hasAnyRole("ADMIN", "GUEST")
                .requestMatchers(HttpMethod.GET, "/students/dues").hasAnyRole("ADMIN", "GUEST")

                // ── ADMIN + STUDENT read (personal data) ─────────────────
                .requestMatchers(HttpMethod.GET, "/students/**").hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers(HttpMethod.GET, "/studentTotal/**").hasAnyRole("ADMIN", "STUDENT")

                // ── Any authenticated role ────────────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "http://localhost",
                "https://mess-application-front-end-angular.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
