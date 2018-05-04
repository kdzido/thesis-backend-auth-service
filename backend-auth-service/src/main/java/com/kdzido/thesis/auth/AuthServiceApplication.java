package com.kdzido.thesis.auth;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author krzysztof.dzido@gmail.com
 */
@SpringBootApplication
@EnableEurekaClient
@RestController
@EnableResourceServer
@EnableAuthorizationServer
public class AuthServiceApplication {

    static UUID instanceUUID = UUID.randomUUID();

    @RequestMapping(value="/v1/uuid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> getInstanceUUID() {
        return ImmutableMap.of("instanceUUID" , String.valueOf(instanceUUID.toString()));
    }

    @RequestMapping(value="/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> user(final OAuth2Authentication user) {
        final Map<String, Object> details = new HashMap<>();

        details.put("user", user.getUserAuthentication().getPrincipal());
        details.put("authorities", AuthorityUtils.authorityListToSet(user.getUserAuthentication().getAuthorities()));
        return details;
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
