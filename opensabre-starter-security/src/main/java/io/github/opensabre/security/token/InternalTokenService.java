package io.github.opensabre.security.token;

/**
 * Issues and verifies short-lived service-to-service tokens.
 */
public interface InternalTokenService {

    /**
     * Issues a new token signed with the current active shared key.
     *
     * @param request trusted user and next-hop data
     * @return compact JWS token
     */
    String issue(InternalTokenRequest request);

    /**
     * Verifies a token for the current target service.
     *
     * @param token compact JWS token
     * @param expectedAudience current service name
     * @return trusted immutable claims
     */
    InternalTokenClaims verify(String token, String expectedAudience);
}
