package com.base.project.global.config.securityConfig;


import com.base.project.global.config.securityConfig.jwt.JwtAuthenticationCheckFilter;
import com.base.project.global.config.securityConfig.jwt.JwtAuthenticationEntryPoint;
import com.base.project.global.config.securityConfig.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.authentication.AuthenticationManagerFactoryBean;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMITTED_URLS = {
            "/api/member/signup", "/api/member/login", "/h2-console/**"
    };

    @Bean
    public static BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) throws Exception {
        http
                .cors()//CORS ?????? ??????(Front , Back ????????? ???????????? ???????????? ??????)
                .and()
                .csrf().disable() // //????????? ??????????????? ???????????? ?????? ????????? ?????? ???????????? csrf ???????????? ????????? ????????? ??????.
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //???????????????????????? ????????????????????? ???????????? ??????????????? ?????? ->JWT ????????????????????? ?????? ???????????? ??????
                .and()
                .formLogin().disable()  // ????????????  View??? ???????????? ???????????? disable
                .httpBasic().disable() // JWT ?????? ????????? ??????????????? httpBasic??? ????????? ???????????? ??????X

                .addFilterAfter(jwtAuthenticationCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(config -> config
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeRequests(ant -> ant
                        .antMatchers(PERMITTED_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                .headers()
                .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN));
        return http.build();
    }

    // CORS ?????? ??????
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(ApplicationContext context) throws Exception {
        AuthenticationManagerFactoryBean authenticationManagerFactoryBean = new AuthenticationManagerFactoryBean();
        authenticationManagerFactoryBean.setBeanFactory(context);
        return authenticationManagerFactoryBean.getObject();
    }

    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public JwtAuthenticationCheckFilter jwtAuthenticationCheckFilter(JwtTokenProvider jwtTokenProvider) {
        return new JwtAuthenticationCheckFilter(jwtTokenProvider);
    }

}
