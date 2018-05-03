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
//    final static AUTHSERVICE_URI = "http://localhost:7999"

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
        auth.basic("newsapp", "newsappsecret")
        it
    }

    // TODO should reject token generation on invalid client credentials
    // TODO should reject token generation on invalid user password

    def "should issue valid access token for client application credentials grant"() {
        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {
                def resp = authServiceClient.post(
                        path: "/oauth/token",
                        body: clientCredentialsGrant,
                        requestContentType : URLENC)    // TODO multipart/form-data
                resp.status == 200 &&
                        resp.headers.'Content-Type'.contains(MediaType.APPLICATION_JSON_VALUE) &&
                        resp.data.'access_token'.isEmpty() == false &&
                        resp.data.'token_type' == "bearer" &&
//                        resp.data.'refresh_token'.isEmpty() == false && // TODO multipart/form-data otherwise refresh_token is absent
                        resp.data.'expires_in' ==~ /\d+/ &&
                        resp.data.'scope' == "webclient"
            } catch (e) {
                return false
            }
        })
    }

    def "should issue valid access token for user password grant"() {
        expect:
        await().atMost(3, TimeUnit.MINUTES).until({
            try {
                def resp = authServiceClient.post(
                        path: "/oauth/token",
                        body: passwordGrant,
                        requestContentType : URLENC)    // TODO multipart/form-data
                resp.status == 200 &&
                        resp.headers.'Content-Type'.contains(MediaType.APPLICATION_JSON_VALUE) &&
                        resp.data.'access_token'.isEmpty() == false &&
                        resp.data.'token_type' == "bearer" &&
//                        resp.data.'refresh_token'.isEmpty() == false && // TODO multipart/form-data otherwise refresh_token is absent
                        resp.data.'expires_in' ==~ /\d+/ &&
                        resp.data.'scope' == "mobileclient"
            } catch (e) {
                return false
            }
        })
    }

}
