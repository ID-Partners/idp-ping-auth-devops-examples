package com.idp.launchdarkly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.sdk.server.Components;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.FlagValueChangeEvent;
import com.launchdarkly.sdk.server.interfaces.FlagValueChangeListener;
import com.launchdarkly.logging.*;
import com.launchdarkly.sdk.server.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * LaunchDarklyService - advanced usage examples for PDP or other microservices.
 *
 * Key docs:
 * - Server-Side Java SDK:
 * https://docs.launchdarkly.com/sdk/server-side/java
 * - Boolean variations:
 * https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#boolean-variations
 * - Evaluation reasons:
 * https://docs.launchdarkly.com/sdk/features/evaluation-reasons?platform=java
 * - JSON variations:
 * https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#json-variations
 * - Multi-context:
 * https://docs.launchdarkly.com/sdk/features/multi-context?platform=java
 * - Flag updates/listeners:
 * https://docs.launchdarkly.com/sdk/features/flag-updates?platform=java
 */
@Service
public class LaunchDarklyService {

    private static final Logger log = LoggerFactory.getLogger(LaunchDarklyService.class);

    @Value("${launchdarkly.logLevel:DEBUG}")
    private String ldLogLevel;

    private final String sdkKey;
    private final String defaultFlagKey;
    @Value("${launchdarkly.appId:example-app}")
    private  String appId;
    @Value("${launchdarkly.appVersion:1.0.0}")
    private  String appVersion;

    private LDClient ldClient;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor-based injection for the SDK key and default flag key.
     * 
     * Spring will inject these values based on your application.properties or
     * environment:
     * - 'launchdarkly.sdkKey' (fallback: empty)
     * - 'launchdarkly.flagKey' (fallback: "kill-switch")
     */
    public LaunchDarklyService(
            @Value("${launchdarkly.sdkKey:}") String sdkKey,
            @Value("${launchdarkly.flagKey:kill-switch}") String defaultFlagKey) {
        this.sdkKey = sdkKey;
        this.defaultFlagKey = defaultFlagKey;
    }

    /**
     * PostConstruct ensures the SDK is initialized after the constructor sets the
     * fields.
     */
    @PostConstruct
    public void init() {
        if (sdkKey == null || sdkKey.isEmpty()) {
            log.error("No LaunchDarkly SDK key found. " +
                    "Please set 'launchdarkly.sdkKey' or LAUNCHDARKLY_SDK_KEY env var.");
            throw new RuntimeException("No LaunchDarkly SDK key found.");
        }

        log.info("Initializing LaunchDarkly SDK with key: {}", sdkKey);
        log.info("Setting LaunchDarkly log level to: {}", ldLogLevel);

        // 1) Convert String -> LDLogLevel (with a fallback)
        LDLogLevel levelEnum;
        try {
            levelEnum = LDLogLevel.valueOf(ldLogLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If it doesn't match a valid LDLogLevel ("DEBUG", "INFO", "WARN", "ERROR",
            // etc.)
            log.warn("Invalid LDLogLevel '{}', defaulting to INFO", ldLogLevel);
            levelEnum = LDLogLevel.INFO;
        }
        // Build config. See docs for advanced config (streaming, etc.).
        LDConfig config = new LDConfig.Builder()
                .applicationInfo(
                        Components.applicationInfo()
                                .applicationId(appId)
                                .applicationVersion(appVersion))
                .logging(
                        Components.logging(Logs.toStream(System.out)).level(levelEnum) // Optional: set log level
                )
                .build();

        ldClient = new LDClient(sdkKey, config);
        if (ldClient.isInitialized()) {
            log.info("LaunchDarkly SDK successfully initialized!");
        } else {
            log.error("LaunchDarkly SDK failed to initialize. Check network connection or SDK key.");
            throw new RuntimeException("LaunchDarkly SDK failed to initialize.");
        }

    }

    public Object evaluateFlag(String flagKey, LDContext context, Object defaultValue) {
        try {
            Object result;
            if (defaultValue instanceof Boolean) {
                result = ldClient.boolVariation(flagKey, context, (Boolean) defaultValue);
            } else if (defaultValue instanceof String) {
                result = ldClient.stringVariation(flagKey, context, (String) defaultValue);
            } else {
                throw new IllegalArgumentException("Unsupported type");
            }
            log.info("Generic flag evaluation - Key: '{}', Context: {}, Result: {}, Type: {}",
                    flagKey, context, result, result.getClass().getSimpleName());
            return result;
        } catch (Exception e) {
            log.error("Error evaluating flag '{}' - Error: {}", flagKey, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Evaluate a boolean flag (fallback to false).
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#boolean-variations
     */
    public boolean evaluateBooleanFlag(String flagKey, LDContext context) {
        if (flagKey == null || flagKey.isEmpty()) {
            log.warn("Flag key is empty; using default '{}'", defaultFlagKey);
            flagKey = defaultFlagKey;
        }

        try {
            EvaluationDetail<Boolean> detail = ldClient.boolVariationDetail(flagKey, context, false);
            log.info("Boolean flag evaluation - Key: '{}', Context: {}, Result: {}, Reason: {}",
                    flagKey, context, detail.getValue(), detail.getReason());
            return detail.getValue();
        } catch (Exception e) {
            log.warn("Failed to evaluate as boolean, trying string conversion for flag: {}", flagKey);
            try {
                String stringResult = ldClient.stringVariation(flagKey, context, "false");
                log.info("Successfully evaluated flag '{}' as string: '{}'", flagKey, stringResult);
                boolean result = Boolean.parseBoolean(stringResult);
                log.info("Converted string value to boolean: {}", result);
                return result;
            } catch (Exception ex) {
                log.error("Failed to evaluate flag '{}' as either boolean or string. Error: {}",
                        flagKey, e.getMessage());
                return false;
            }
        }
    }

    /**
     * Evaluate a boolean flag and return the detailed evaluation (value + reason).
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/evaluation-reasons?platform=java
     */
    public EvaluationDetail<Boolean> evaluateBooleanFlagWithDetail(String flagKey, LDContext context) {
        if (flagKey == null || flagKey.isEmpty()) {
            flagKey = defaultFlagKey;
        }
        EvaluationDetail<Boolean> detail = ldClient.boolVariationDetail(flagKey, context, false);
        log.debug("Boolean flag '{}' -> value={}, reason={}",
                flagKey, detail.getValue(), detail.getReason());
        return detail;
    }

    /**
     * Evaluate a string flag.
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#string-variations
     */
    public String evaluateStringFlag(String flagKey, LDContext context, String defaultValue) {
        if (flagKey == null || flagKey.isEmpty()) {
            flagKey = defaultFlagKey;
        }
        try {
            String result = ldClient.stringVariation(flagKey, context, defaultValue);
            log.info("String flag evaluation - Key: '{}', Context: {}, Result: '{}', Default: '{}'",
                    flagKey, context, result, defaultValue);
            return result;
        } catch (Exception e) {
            log.error("Error evaluating string flag '{}' - Error: {}", flagKey, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Evaluate a JSON variation, returning a Jackson JsonNode.
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#json-variations
     */
    public JsonNode evaluateJsonFlag(String flagKey, LDContext context, JsonNode defaultValue) {
        if (flagKey == null || flagKey.isEmpty()) {
            flagKey = defaultFlagKey;
        }
        LDValue ldValue = ldClient.jsonValueVariation(flagKey, context, LDValue.ofNull());
        JsonNode node = ldValueToJson(ldValue);

        if (node == null && defaultValue != null) {
            node = defaultValue;
        }
        log.debug("JSON flag '{}' -> {}", flagKey, node);
        return node;
    }

    /**
     * Helper: Convert LDValue to Jackson JsonNode.
     */
    private JsonNode ldValueToJson(LDValue ldValue) {
        try {
            return mapper.readTree(ldValue.toJsonString());
        } catch (IOException e) {
            log.error("Failed to convert LDValue to JsonNode: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Build a multi-context with user + organization, etc.
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/multi-context?platform=java
     */
    public LDContext buildMultiContext(String userId, String userRole, String orgId, String subscription) {
        var userCtx = LDContext.builder(userId)
                .kind("user")
                .set("role", userRole)
                .build();

        var orgCtx = LDContext.builder(orgId)
                .kind("organization")
                .set("subscriptionLevel", subscription)
                .build();

        var multi = LDContext.multiBuilder()
                .add(userCtx)
                .add(orgCtx)
                .build();

        log.info("Built multi-context - User: {} (role: {}), Org: {} (subscription: {})",
                userId, userRole, orgId, subscription);
        log.debug("Full context object: {}", multi);
        return multi;
    }

    /**
     * Optional: subscribe to a specific flag's changes for real-time updates.
     * 
     * Docs:
     * https://docs.launchdarkly.com/sdk/features/flag-updates?platform=java
     */
    public void subscribeToFlagChanges(String... flagKeys) {
        for (String flagKey : flagKeys) {
            ldClient.getFlagTracker().addFlagValueChangeListener(flagKey, LDContext.create("example-listener"),
                    new FlagValueChangeListener() {
                        @Override
                        public void onFlagValueChange(FlagValueChangeEvent event) {
                            log.info("Flag '{}' changed from {} to {}",
                                    event.getKey(), event.getOldValue(), event.getNewValue());
                        }
                    });
            log.info("Subscribed to changes for '{}'", flagKey);
        }
    }

    /**
     * Graceful shutdown of the LDClient on application exit.
     */
    @PreDestroy
    public void shutdown() {
        if (ldClient != null) {
            try {
                ldClient.close();
                log.info("LaunchDarkly SDK closed.");
            } catch (IOException e) {
                log.error("Error closing LaunchDarkly SDK: {}", e.getMessage(), e);
            }
        }
    }
}