package com.rh4;

import com.rh4.entities.SuperAdmin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.rh4.entities.MyUser;
import com.rh4.repositories.SuperAdminRepo;
import com.rh4.repositories.UserRepo;
import org.springframework.security.core.userdetails.User;

import static org.springframework.security.config.Customizer.*;

import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    public SuperAdminRepo srepo;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return new CustomAuthenticationSuccessHandler();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepo repo) {
        return (String username) -> {
            MyUser dbUser = repo.findByUsername(username);
            if (dbUser == null) {
                throw new UsernameNotFoundException("Username: " + username + " not found");
            }
            return User.builder()
                    .username(dbUser.getUsername())
                    .password(dbUser.getPassword())
                    .roles(dbUser.getRole())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/assets/**", "/message", "/app/message/**", "/bisag_internship/**").permitAll()
                .requestMatchers("/bisag/admin/**").hasRole("ADMIN")
                .requestMatchers("/bisag/guide/**").hasRole("GUIDE")
                .requestMatchers("/bisag/intern/**").hasRole("INTERN")
                .requestMatchers("/bisag/super_admin/**").hasRole("SUPERADMIN")
                .requestMatchers("/bisag/hr/**").hasRole("HR")
                .requestMatchers("/under_process_intern/**").hasRole("UNDERPROCESSINTERN")
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .successHandler(customAuthenticationSuccessHandler)
                .permitAll()
        );

        http.headers(headers -> headers.frameOptions(frameOptions ->
                frameOptions.sameOrigin()
                        .contentSecurityPolicy(cps -> cps
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://stackpath.bootstrapcdn.com https://maxcdn.bootstrapcdn.com; " +
                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://ajax.googleapis.com https://code.jquery.com https://stackpath.bootstrapcdn.com https://maxcdn.bootstrapcdn.com; " +
                                                "font-src 'self' https://cdnjs.cloudflare.com https://stackpath.bootstrapcdn.com;"
                                )
                        )
        ));

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
//	@Bean
//    CommandLineRunner loadInitialUsersInDB(UserRepo repo)
//	{
//        return args -> {
//
//        	SuperAdmin s1 = new SuperAdmin();
//        	s1.setName("superadmin");
//        	s1.setEmailId("projectsmsfc@gmail.com");
//        	s1.setContactNo((long)23123466);
//        	s1.setLocation("bharuch");
//        	s1.setPassword(passwordEncoder().encode("123"));
//        	s1.setCreatedAt(null);
//        	srepo.save(s1);
//            MyUser user3 = new MyUser();
//            user3.setUsername("projectsmsfc@gmail.com");
//            user3.setPassword(passwordEncoder().encode("123"));
//            user3.setRole("SUPERADMIN");
//            user3.setUserId(Long.toString(s1.getSuperAdminId()));
//            user3.setEnabled(true);
//            repo.save(user3);
//
//            System.out.println("users save in DB");
//        };
//	}
}
