/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.sample.app.config;

import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.validator.WebAuthnAuthenticationContextValidator;
import net.sharplab.springframework.security.webauthn.WebAuthnRegistrationRequestValidator;
import net.sharplab.springframework.security.webauthn.authenticator.WebAuthnAuthenticatorService;
import net.sharplab.springframework.security.webauthn.config.configurers.WebAuthnAuthenticationProviderConfigurer;
import net.sharplab.springframework.security.webauthn.sample.app.security.SampleUsernameNotFoundHandler;
import net.sharplab.springframework.security.webauthn.sample.domain.component.UserManager;
import net.sharplab.springframework.security.webauthn.userdetails.WebAuthnUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.mfa.MultiFactorAuthenticationProviderConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.Collections;

import static net.sharplab.springframework.security.fido.server.config.configurer.FidoServerConfigurer.fidoServer;
import static net.sharplab.springframework.security.webauthn.config.configurers.WebAuthnConfigurer.webAuthn;


/**
 * Security Configuration
 */
@Configuration
@Import(value = WebSecurityBeanConfig.class)
@EnableWebSecurity
public class
WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ADMIN_ROLE = "ADMIN";

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;

    @Autowired
    private WebAuthnUserDetailsService userDetailsService;

    @Autowired
    private WebAuthnAuthenticatorService authenticatorService;

    @Autowired
    private WebAuthnAuthenticationContextValidator webAuthnAuthenticationContextValidator;

    @Autowired
    private WebAuthnRegistrationRequestValidator webAuthnRegistrationRequestValidator;

    @Autowired
    private UserManager userManager;

    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.apply(new WebAuthnAuthenticationProviderConfigurer<>(userDetailsService, authenticatorService, webAuthnAuthenticationContextValidator))
            .expectedAuthenticationExtensionIds(Collections.singletonList("example.extension"));
        builder.apply(new MultiFactorAuthenticationProviderConfigurer<>(daoAuthenticationProvider));
    }

    @Override
    public void configure(WebSecurity web) {
        // ignore static resources
        web.ignoring().antMatchers(
                "/favicon.ico",
                "/static/**",
                "/webjars/**",
                "/angular",
                "/angular/**");
    }

    /**
     * Configure SecurityFilterChain
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // WebAuthn Config
        http.apply(webAuthn())
                .rpName("Spring Security WebAuthn Sample")
                .publicKeyCredParams()
                .addPublicKeyCredParams(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.RS256)  // Windows Hello
                .addPublicKeyCredParams(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256); // FIDO U2F Key, etc

        // FIDO Server Endpoints
        http.apply(fidoServer())
                .fidoServerAttestationOptionsEndpoint()
                .and()
                .fidoServerAttestationResultEndpointConfig()
                .expectedRegistrationExtensionIds(Collections.singletonList("example.extension"))
                .webAuthnUserDetailsService(userDetailsService)
                .webAuthnRegistrationRequestValidator(webAuthnRegistrationRequestValidator)
                .usernameNotFoundHandler(new SampleUsernameNotFoundHandler(userManager))
                .and()
                .fidoServerAssertionOptionsEndpointConfig()
                .and()
                .fidoServerAssertionResultEndpoint();

        // Authorization
        http.authorizeRequests()
                .mvcMatchers("/").permitAll()
                .mvcMatchers("/api/auth/status").permitAll()
                .mvcMatchers(HttpMethod.GET, "/login").permitAll()
                .mvcMatchers(HttpMethod.POST, "/api/profile").permitAll()
                .mvcMatchers("/health/**").permitAll()
                .mvcMatchers("/info/**").permitAll()
                .mvcMatchers("/h2-console/**").denyAll()
                .mvcMatchers("/api/admin/**").hasRole(ADMIN_ROLE)
                .anyRequest().fullyAuthenticated();

        http.sessionManagement()
                .sessionAuthenticationFailureHandler(authenticationFailureHandler);

        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        //TODO:
        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

        http.csrf().ignoringAntMatchers("/webauthn/**");


    }

}
