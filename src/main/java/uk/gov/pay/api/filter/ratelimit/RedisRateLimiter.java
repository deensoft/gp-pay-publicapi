package uk.gov.pay.api.filter.ratelimit;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.RateLimiterConfig;
import uk.gov.pay.api.filter.RateLimiterKey;
import uk.gov.pay.api.managed.RedisClientManager;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

@Singleton
public class RedisRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);

    private RateLimitManager rateLimitManager;
    private RedisClientManager redisClientManager;

    @Inject
    public RedisRateLimiter(RateLimiterConfig rateLimiterConfig, RedisClientManager redisClientManager) {
        this.rateLimitManager = new RateLimitManager(rateLimiterConfig);
        this.redisClientManager = redisClientManager;
    }

    /**
     * @throws RateLimitException
     */
    synchronized void checkRateOf(String accountId, RateLimiterKey key)
            throws RedisException, RateLimitException {

        Long count;

        try {
            int rateLimitInterval = rateLimitManager.getRateLimitInterval(accountId);
            count = updateAllowance(key.getKey(), rateLimitInterval);
        } catch (Exception e) {
            LOGGER.info("Failed to update allowance. Cause of error: " + e.getMessage());
            // Exception possible if redis is unavailable or perMillis is too high        
            throw new RedisException();
        }

        if (count != null) {
            int allowedNumberOfRequests = rateLimitManager.getAllowedNumberOfRequests(key, accountId);
            if (count > allowedNumberOfRequests) {
                LOGGER.info(String.format("RedisRateLimiter - Rate limit exceeded for account [%s] and method [%s] - count: %d, rate allowed: %d",
                        accountId, key.getKeyType(), count, allowedNumberOfRequests));
                throw new RateLimitException();
            }
        }
    }

    synchronized private Long updateAllowance(String key, int rateLimitInterval) {
        String derivedKey = getKeyForWindow(key, rateLimitInterval);
        Long count = redisClientManager.getRedisConnection().sync().incr(derivedKey);

        if (count == 1) {
            redisClientManager.getRedisConnection().sync().expire(derivedKey, rateLimitInterval / 1000);
        }

        return count;
    }

    /**
     * Derives Key (Service Key + Window) to use in Redis for noOfReq limiting.
     * Recommended to use perMillis to lowest granularity. i.e, to seconds. 1000, 2000
     * <p>
     * Depends on perMillis
     * <p>
     * - perMillis < 1000 : Window considered for milliseconds
     * - perMillis >=1000 && <60000 : Window considered for seconds(s)
     * - perMillis >=60000 && <3600000 : Window considered for minute(s)
     *
     * @return new key based on perMillis (works for second/minute/hour windows only)
     */
    private String getKeyForWindow(String key, int rateLimitInterval) throws OutOfScopeException {

        LocalDateTime now = LocalDateTime.now();

        int window;

        if (rateLimitInterval >= 1 && rateLimitInterval < 1000) {
            window = (now.get(ChronoField.MILLI_OF_DAY) / rateLimitInterval) + 1;
        } else if (rateLimitInterval >= 1000 && rateLimitInterval < 60000) {
            window = now.get(ChronoField.SECOND_OF_MINUTE) / (rateLimitInterval / 1000);
        } else if (rateLimitInterval >= 60000 && rateLimitInterval < 3600000) {
            window = now.get(ChronoField.MINUTE_OF_HOUR) / (rateLimitInterval / 1000);
        } else {
            throw new OutOfScopeException("Rate limit interval specified is not currently supported");
        }

        return key + window;
    }
}
