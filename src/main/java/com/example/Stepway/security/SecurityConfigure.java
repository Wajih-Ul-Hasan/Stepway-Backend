package com.example.Stepway.security;

import com.example.Stepway.Service.impl.MyUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class SecurityConfigure extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyUserDetailService myUserDetailsService;
    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Override   ///   ====>>>      authentication
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService);
    }

@Override         ///   ====>>>      authorization
protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint((request, response, exception) -> response.sendError(401))
            .and().authorizeRequests()
            .antMatchers("/health").permitAll()
            // Permit access to login endpoint
            .antMatchers(HttpMethod.POST, "/api/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/user").permitAll()
            .antMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/resend-verification").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
            // Permit access to public API documentation and Swagger UI
            .antMatchers(
                    "/v3/api-docs",
                    "/v2/api-docs",
                    "/swagger-resources/**",
                    "/swagger-ui/**",
                    "/webjars/**"
            ).permitAll()
            // Role-based access control for specific endpoints
            .antMatchers("/api/student/**").hasRole("STUDENT")
            .antMatchers("/api/teacher/**").hasRole("TEACHER")
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            // Personal goals are scoped to the authenticated owner inside the service.
            .antMatchers("/api/me/goals", "/api/me/goals/**").authenticated()
            // Match the application's actual write routes.
            .antMatchers(HttpMethod.POST, "/api/available-enrollment").hasAnyRole("STUDENT", "ADMIN")
            .antMatchers(HttpMethod.POST, "/api/assessment", "/api/content").hasAnyRole("TEACHER", "ADMIN")
            .antMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
            .antMatchers("/api/role/**", "/api/allRoles", "/api/permission/**", "/api/allPermission",
                    "/api/allUsers", "/api/user/**", "/api/students", "/api/teachers",
                    "/api/allPayments", "/api/payment/**", "/api/allProfiles", "/api/profile/**",
                    "/api/allResumes", "/api/resume/**", "/api/allEnrollments", "/api/enrollment/*")
                    .hasRole("ADMIN")
            // Other read requests require authentication.
            .anyRequest().authenticated()
            .and()
            // Configure session management
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    // Add JWT filter
    http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
}
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
