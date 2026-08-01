package io.github.opensabre.security.token;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.security.config.InternalTokenProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * HS256 implementation of the OpenSabre internal token protocol.
 */
public class DefaultInternalTokenService implements InternalTokenService {

    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final InternalTokenProperties properties;
    private final Clock clock;

    public DefaultInternalTokenService(ObjectMapper objectMapper, InternalTokenProperties properties) {
        this(objectMapper, properties, Clock.systemUTC());
    }

    DefaultInternalTokenService(ObjectMapper objectMapper, InternalTokenProperties properties, Clock clock) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public String issue(InternalTokenRequest request) {
        validateRequest(request);
        HmacKeyRing keyRing = new HmacKeyRing(properties);
        long now = clock.instant().getEpochSecond();
        long ttl = properties.getTtl().toSeconds();
        long maxTtl = properties.getMaxTtl().toSeconds();
        if (ttl <= 0 || maxTtl <= 0 || ttl > maxTtl || maxTtl > 120) {
            throw new InternalTokenException(
                    InternalTokenError.INVALID_CONFIGURATION,
                    "ttl must be positive, no greater than max-ttl, and max-ttl must not exceed 120 seconds");
        }
        validateExtensions(request.extensions());

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", InternalTokenConstants.ALGORITHM);
        header.put("typ", InternalTokenConstants.TYPE);
        header.put("kid", keyRing.activeKeyId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("iss", request.issuer());
        payload.put("sub", request.subject());
        payload.put("username", request.username());
        payload.put("aud", request.audience());
        payload.put("jti", UUID.randomUUID().toString());
        payload.put("iat", now);
        payload.put("nbf", now);
        payload.put("exp", now + ttl);
        payload.put("src", request.issuer());
        payload.put("dst", request.audience());
        payload.put("scope", request.scopes());
        payload.put("roles", request.roles());
        payload.put("authorities", request.authorities());
        payload.put("hop", request.hop());
        payload.put("parent_jti", request.parentTokenId());
        payload.put("trace_id", request.traceId());
        payload.put("key_config_version", properties.getKeyConfigVersion());
        payload.put("ext", request.extensions());

        try {
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = BASE64_URL_ENCODER.encodeToString(sign(signingInput, keyRing.activeKey()));
            String token = signingInput + "." + signature;
            if (token.getBytes(StandardCharsets.US_ASCII).length > properties.getMaxTokenBytes()) {
                throw new InternalTokenException(InternalTokenError.INVALID_EXTENSIONS, "internal token exceeds size limit");
            }
            return token;
        } catch (InternalTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, "cannot issue internal token", exception);
        }
    }

    @Override
    public InternalTokenClaims verify(String token, String expectedAudience) {
        if (token == null || token.isBlank()) {
            throw new InternalTokenException(InternalTokenError.MISSING_TOKEN, "internal token is missing");
        }
        if (token.getBytes(StandardCharsets.US_ASCII).length > properties.getMaxTokenBytes()) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, "internal token exceeds size limit");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, "internal token must contain three parts");
        }

        try {
            Map<String, Object> header = decodeMap(parts[0]);
            verifyHeader(header);
            String keyId = text(header, "kid");
            HmacKeyRing keyRing = new HmacKeyRing(properties);
            byte[] key = keyRing.verificationKey(keyId);
            if (key == null) {
                throw new InternalTokenException(InternalTokenError.UNKNOWN_KEY, "unknown internal token key id");
            }
            byte[] expectedSignature = sign(parts[0] + "." + parts[1], key);
            
            byte[] actualSignature;
            try {
                actualSignature = BASE64_URL_DECODER.decode(parts[2]);
            } catch (IllegalArgumentException exception) {
                throw new InternalTokenException(InternalTokenError.INVALID_SIGNATURE, "internal token signature encoding is invalid", exception);
            }
            
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw new InternalTokenException(InternalTokenError.INVALID_SIGNATURE, "internal token signature is invalid");
            }
            
            InternalTokenClaims claims = toClaims(decodeMap(parts[1]));
            validateClaims(claims, expectedAudience);
            validateExtensions(claims.extensions());
            return claims;
        } catch (InternalTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, "cannot parse internal token", exception);
        }
    }

    private void validateRequest(InternalTokenRequest request) {
        if (request == null
                || !hasText(request.issuer())
                || !hasText(request.subject())
                || !hasText(request.audience())) {
            throw new InternalTokenException(
                    InternalTokenError.MALFORMED_TOKEN, "issuer, subject and audience are required");
        }
        if (request.hop() < 0 || request.hop() > properties.getMaxHop()) {
            throw new InternalTokenException(InternalTokenError.MAX_HOP_EXCEEDED, "internal token hop is invalid");
        }
        Set<String> allowedIssuers = properties.getAllowedIssuers();
        if (!allowedIssuers.isEmpty() && !allowedIssuers.contains(request.issuer())) {
            throw new InternalTokenException(InternalTokenError.INVALID_ISSUER, "issuer is not allowed");
        }
    }

    private void verifyHeader(Map<String, Object> header) {
        if (!InternalTokenConstants.ALGORITHM.equals(text(header, "alg"))
                || !InternalTokenConstants.TYPE.equals(text(header, "typ"))) {
            throw new InternalTokenException(
                    InternalTokenError.UNSUPPORTED_ALGORITHM, "only HS256 OS-INTERNAL tokens are accepted");
        }
    }

    private void validateClaims(InternalTokenClaims claims, String expectedAudience) {
        if (!hasText(expectedAudience)
                || !expectedAudience.equals(claims.audience())
                || !expectedAudience.equals(claims.destination())) {
            throw new InternalTokenException(InternalTokenError.INVALID_AUDIENCE, "internal token target does not match");
        }
        if (!claims.issuer().equals(claims.source())) {
            throw new InternalTokenException(InternalTokenError.INVALID_ISSUER, "issuer and source must match");
        }
        Set<String> allowedIssuers = properties.getAllowedIssuers();
        if (!allowedIssuers.isEmpty() && !allowedIssuers.contains(claims.issuer())) {
            throw new InternalTokenException(InternalTokenError.INVALID_ISSUER, "issuer is not allowed");
        }
        long now = clock.instant().getEpochSecond();
        long skew = properties.getClockSkew().toSeconds();
        if (claims.issuedAt() > now + skew || claims.notBefore() > now + skew || claims.expiresAt() <= claims.issuedAt()) {
            throw new InternalTokenException(InternalTokenError.INVALID_TIME, "internal token time claims are invalid");
        }
        if (claims.expiresAt() < now - skew) {
            throw new InternalTokenException(InternalTokenError.TOKEN_EXPIRED, "internal token has expired");
        }
        if (claims.expiresAt() - claims.issuedAt() > properties.getMaxTtl().toSeconds()
                || claims.expiresAt() - claims.issuedAt() > 120) {
            throw new InternalTokenException(InternalTokenError.TOKEN_TOO_LONG_LIVED, "internal token lifetime is too long");
        }
        if (claims.hop() < 0 || claims.hop() > properties.getMaxHop()) {
            throw new InternalTokenException(InternalTokenError.MAX_HOP_EXCEEDED, "internal token maximum hop exceeded");
        }
        if (!hasText(claims.subject()) || !hasText(claims.tokenId())) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, "subject and jti are required");
        }
    }

    private void validateExtensions(Map<String, Object> extensions) {
        Set<String> allowedKeys = properties.getAllowedExtensionKeys();
        if (!extensions.isEmpty() && (allowedKeys.isEmpty() || !allowedKeys.containsAll(extensions.keySet()))) {
            throw new InternalTokenException(InternalTokenError.INVALID_EXTENSIONS, "extension key is not allowed");
        }
        try {
            if (objectMapper.writeValueAsBytes(extensions).length > properties.getMaxExtensionBytes()) {
                throw new InternalTokenException(InternalTokenError.INVALID_EXTENSIONS, "extensions exceed size limit");
            }
        } catch (InternalTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InternalTokenException(
                    InternalTokenError.INVALID_EXTENSIONS, "extensions cannot be serialized", exception);
        }
    }

    private InternalTokenClaims toClaims(Map<String, Object> claims) {
        return new InternalTokenClaims(
                text(claims, "iss"),
                text(claims, "sub"),
                nullableText(claims, "username"),
                text(claims, "aud"),
                text(claims, "jti"),
                number(claims, "iat").longValue(),
                number(claims, "nbf").longValue(),
                number(claims, "exp").longValue(),
                text(claims, "src"),
                text(claims, "dst"),
                strings(claims, "scope"),
                strings(claims, "roles"),
                strings(claims, "authorities"),
                number(claims, "hop").intValue(),
                nullableText(claims, "parent_jti"),
                nullableText(claims, "trace_id"),
                number(claims, "key_config_version").longValue(),
                map(claims, "ext"));
    }

    private byte[] sign(String signingInput, byte[] key) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(key, HMAC_SHA_256));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private Map<String, Object> decodeMap(String encoded) throws Exception {
        byte[] json = BASE64_URL_DECODER.decode(encoded);
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    private static String text(Map<String, Object> values, String key) {
        String value = nullableText(values, key);
        if (!hasText(value)) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " is required");
        }
        return value;
    }

    private static String nullableText(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Number number(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (!(value instanceof Number number)) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " must be numeric");
        }
        return number;
    }

    private static List<String> strings(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " must be an array");
        }
        List<String> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (!(item instanceof String string)) {
                throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " must contain strings");
            }
            result.add(string);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Map<String, Object> values, String key) {
        Object value = values.get(key);
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " must be an object");
        }
        for (Object item : map.keySet()) {
            if (!(item instanceof String)) {
                throw new InternalTokenException(InternalTokenError.MALFORMED_TOKEN, key + " keys must be strings");
            }
        }
        return (Map<String, Object>) map;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
