package zeroone.developers.clinicapp.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
    private val jwtTokenFilter: JwtTokenFilter,
    private val exceptionEntryPoint: ExceptionEntryPoint
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { request ->
                request.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                request.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                request.requestMatchers("/api/v1/hook/**").permitAll()
                request.anyRequest().authenticated()
            }
            .addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter::class.java)
            .exceptionHandling { handling ->
                handling
                    .authenticationEntryPoint(exceptionEntryPoint)
            }
        return http.build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}

