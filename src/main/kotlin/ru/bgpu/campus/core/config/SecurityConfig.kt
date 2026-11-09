package ru.bgpu.campus

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.client.RestTemplate
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import ru.bgpu.campus.core.security.JwtAuthFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(

    private val jwtAuthFilter: JwtAuthFilter
) {


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    @Bean
    fun disableAutoRegistration(filter: JwtAuthFilter): org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthFilter> {
        val registration = org.springframework.boot.web.servlet.FilterRegistrationBean(filter)
        registration.isEnabled = false
        return registration
    }
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {

            allowedOriginPatterns = listOf("*")

            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")

            allowedHeaders = listOf("*")

            allowCredentials = true

            exposedHeaders = listOf("Authorization")
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }



    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                auth.requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"


                ).permitAll()

                auth.requestMatchers("/admin/**").hasRole("ADMIN")



                auth.anyRequest().authenticated()
            }
            // 4. Подключаем ваш JwtAuthFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        return InMemoryUserDetailsManager()
    }
}
