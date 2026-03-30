package com.sadop.energymanagement.config;

import com.sadop.energymanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Allow preflight OPTIONS requests for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Allow root and static assets
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/vite.svg", "/static/**").permitAll()

                        // Auth endpoints (login/register)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Worker-only endpoints
                        .requestMatchers("/worker/**").hasAuthority("ROLE_ATTENDANT")

                        // CEO-only endpoints
                        .requestMatchers("/admin/**").hasAuthority("ROLE_CEO")

                        // Manager, Assistant Manager, and CEO
                        .requestMatchers("/manager/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO")
                        .requestMatchers("/api/fuel-tanks/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO")
                        .requestMatchers("/api/sales-entries/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO")
                        .requestMatchers("/api/debts/**", "/api/shorts/**", "/api/initial-pump/**").hasAnyAuthority("ROLE_MANAGER", "ROLE_ASSISTANT_MANAGER", "ROLE_CEO")
                        .requestMatchers("/api/users/register")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_CEO")

                        // Everything else must be authenticated
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Allow frontend from local and Tailscale IPs
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://100.66.203.45:5173", // Desktop
                "http://100.82.243.64:5173"  // Laptop
        ));

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Branch-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
