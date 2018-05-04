package com.kdzido.thesis.auth

import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.URLENC
import org.springframework.http.MediaType
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.concurrent.TimeUnit

import static org.awaitility.Awaitility.await

/**
 * @author krzysztof.dzido@gmail.com
 */
@Stepwise
class AuthServiceIntegSpec extends Specification {

    final static AUTHSERVICE_URI = System.getenv("AUTHSERVICE_URI")

    final clientCredentialsGrant = [
            grant_type: "client_credentials",
            scope: "webclient"]

    final passwordGrant = [
            grant_type: "password",
            scope: "mobileclient",
            username: "reader",
            password: "readerpassword"]

    def authServiceClient = new RESTClient("$AUTHSERVICE_URI").with {
        setHeaders(Accept: MediaType.APPLICATION_JSON_VALUE)
        it
    }

    def "should acquire access token for client credentials grant"() {
        setup:
        authServiceClient.auth.basic("newsapp", "newsappsecret")

        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {
                def authServerResp = authServiceClient.post(
                        path: "/auth/oauth/token",
                        body: clientCredentialsGrant,
                        requestContentType : URLENC)    // TODO multipart/form-data
                authServerResp.status == 200 &&
                        authServerResp.headers.'Content-Type'.contains(MediaType.APPLICATION_JSON_VALUE) &&
                        authServerResp.data.'access_token'.isEmpty() == false &&
                        authServerResp.data.'token_type' == "bearer" &&
//                        resp.data.'refresh_token'.isEmpty() == false && // TODO multipart/form-data otherwise refresh_token is absent
                        authServerResp.data.'expires_in' ==~ /\d+/ &&
                        authServerResp.data.'scope' == "webclient"
            } catch (e) {
                return false
            }
        })
    }

    def "should acquire and validate access token for password grant"() {
        setup:
        authServiceClient.auth.basic("newsapp", "newsappsecret")
        String accessToken

        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {

                def authServerResp = authServiceClient.post(
                        path: "/auth/oauth/token",
                        body: passwordGrant,
                        requestContentType : URLENC)    // TODO multipart/form-data

                // token extracted
                accessToken = authServerResp.data.'access_token'

                def userInfoClient = new RESTClient("$AUTHSERVICE_URI").with {
                    setHeaders(
                            Accept: MediaType.APPLICATION_JSON_VALUE,
                            Authorization: "Bearer $accessToken"
                    )
                    it
                }
                def userInfoResp = userInfoClient.get(path: "/auth/user")
                return userInfoResp.status == 200 &&
                        userInfoResp.headers.'Content-Type'.contains(MediaType.APPLICATION_JSON_VALUE) &&
                        userInfoResp.data.user.username == "reader" &&
                        userInfoResp.data.user.password == null &&
                        userInfoResp.data.user.authorities.authority == ["ROLE_READER"] &&
                        userInfoResp.data.user.accountNonExpired == true &&
                        userInfoResp.data.user.accountNonLocked == true &&
                        userInfoResp.data.user.credentialsNonExpired == true &&
                        userInfoResp.data.user.enabled == true &&
                        userInfoResp.data.authorities == ["ROLE_READER"]
            } catch (e) {
                return false
            }
        })
    }

    def "should reject token generation on invalid user password"() {
        setup:
        authServiceClient.auth.basic("newsapp", "WRONG_SECRET")

        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {
                authServiceClient.post(
                        path: "/auth/oauth/token",
                        body: passwordGrant,
                        requestContentType : URLENC)
                return false
            } catch (e) {
                assert e.response.status == 401
                return true
            }
        })
    }

    def "should reject invalid access token"() {
        setup:
        def userInfoClient = new RESTClient("$AUTHSERVICE_URI").with {
            setHeaders(
                    Accept: MediaType.APPLICATION_JSON_VALUE,
                    Authorization: "Bearer INVALID_TOKEN")
            it
        }

        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {
                userInfoClient.get(path: "/auth/user")
                return false
            } catch (e) {
                assert e.response.status == 401
                return true
            }
        })
    }

    // TODO should reject token generation on invalid client credentials

}
