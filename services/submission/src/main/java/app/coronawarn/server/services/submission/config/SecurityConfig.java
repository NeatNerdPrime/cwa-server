/*-
 * ---license-start
 * Corona-Warn-App
 * ---
 * Copyright (C) 2020 SAP SE and all other contributors
 * All modifications are copyright (c) 2020 Devside SRL.
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.server.services.submission.config;

import static app.coronawarn.server.services.submission.controller.AuthorizationCodeController.AC_PROCESS_PATH;

import app.coronawarn.server.services.submission.controller.SubmissionController;
import java.util.Arrays;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String ACTUATOR_ROUTE = "/actuator/";
  private static final String HEALTH_ROUTE = ACTUATOR_ROUTE + "health";
  private static final String PROMETHEUS_ROUTE = ACTUATOR_ROUTE + "prometheus";
  private static final String READINESS_ROUTE = ACTUATOR_ROUTE + "readiness";
  private static final String LIVENESS_ROUTE = ACTUATOR_ROUTE + "liveness";
  private static final String SUBMISSION_ROUTE =  "/version/v1" + SubmissionController.SUBMISSION_ROUTE;
  private static final String AC_ROUTE = "/version/v1" + AC_PROCESS_PATH;

  @Bean
  protected HttpFirewall strictFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(Arrays.asList(
        HttpMethod.GET.name(),
        HttpMethod.POST.name()));
    return firewall;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers(HttpMethod.GET, HEALTH_ROUTE, PROMETHEUS_ROUTE, READINESS_ROUTE, LIVENESS_ROUTE).permitAll()
        .mvcMatchers(HttpMethod.POST, SUBMISSION_ROUTE,AC_ROUTE).permitAll()
        .anyRequest().denyAll()
        .and().csrf().disable();
    http.headers().contentSecurityPolicy("default-src 'self'");
  }

  /**
   * Validation factory bean is configured here because its message interpolation mechanism
   * is considered a potential threat if enabled.
   */
  @Bean
  public static LocalValidatorFactoryBean defaultValidator() {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    factoryBean.setMessageInterpolator(new ParameterMessageInterpolator());
    return factoryBean;
  }

}
