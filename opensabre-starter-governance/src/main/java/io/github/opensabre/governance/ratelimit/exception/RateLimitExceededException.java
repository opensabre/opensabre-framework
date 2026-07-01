package io.github.opensabre.governance.ratelimit.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final int remaining;

    private final long resetTime;

    public RateLimitExceededException(String message, int remaining, long resetTime) {
        super(message);
        this.remaining = remaining;
        this.resetTime = resetTime;
    }
}
