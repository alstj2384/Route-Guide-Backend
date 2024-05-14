package csu.RouteGuideBackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions((frameOption) -> frameOption.disable()))
                .httpBasic(Customizer.withDefaults())
                // url 권한 설정
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/member/relationship/**", "/test/**", "/path-find/**").hasRole("USER")
                        .requestMatchers("/", "/login","/loginForm","/join", "/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                // 로그인 페이지 설정
                .formLogin(login -> login
                        .loginPage("/loginForm").permitAll()
                        .loginProcessingUrl("/login").permitAll()
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/"))
//                .oauth2Login(login -> login
//                        .loginPage("/login-form")
//                        .userInfoEndpoint()
//                        .userService(principalOauth2UserService))
                        ;
        return http.build();
    }
}
