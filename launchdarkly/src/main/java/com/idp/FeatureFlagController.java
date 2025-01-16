package com.idp.launchdarkly;

import com.fasterxml.jackson.databind.JsonNode;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.LDContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // <-- For logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * FeatureFlagController with constructor-based injection, logging, and optional advanced endpoints.
 *
 * References:
 * - LaunchDarkly Java SDK: https://docs.launchdarkly.com/sdk/server-side/java
 * - Boolean flags: https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#boolean-variations
 * - Evaluation reasons: https://docs.launchdarkly.com/sdk/features/evaluation-reasons?platform=java
 * - JSON variations: https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#json-variations
 * - Multi-context: https://docs.launchdarkly.com/sdk/features/multi-context?platform=java
 */
@RestController
@RequestMapping("/flags")
public class FeatureFlagController {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagController.class);

    private final LaunchDarklyService ldService;

    @Autowired
    public FeatureFlagController(LaunchDarklyService ldService) {
        this.ldService = ldService;
    }

    /**
     * GET /flags?flagKey={flagKey}&contextKey={contextKey}
     *
     * Evaluates a boolean flag with a basic user context.
     * Original usage from your snippet.
     */
    @GetMapping
    public ResponseEntity<Object> getFlag(
            @RequestParam(name = "flagKey", required = false) String flagKey,
            @RequestParam(name = "contextKey", required = false, defaultValue = "example-user") String contextKey,
            @RequestParam(name = "type", defaultValue = "STRING") String type
    ) {
        log.info("GET /flags [dynamic], flagKey={}, contextKey={}, type={}", flagKey, contextKey, type);

        var context = LDContext.builder(contextKey).build();
        Object result;

        switch (type.toUpperCase()) {
            case "STRING":
                result = ldService.evaluateStringFlag(flagKey, context, "default-string");
                break;
            case "BOOLEAN":
            default:
                result = ldService.evaluateBooleanFlag(flagKey, context);
                break;
        }

        log.debug("Flag '{}' -> {}", flagKey, result);
        return ResponseEntity.ok(new FlagResponse(flagKey, result));
    }

    /**
     * GET /flags/detail?flagKey={flagKey}&contextKey={contextKey}
     *
     * Example endpoint returning an EvaluationDetail (flag value + reason).
     * Docs: https://docs.launchdarkly.com/sdk/features/evaluation-reasons?platform=java
     */
    @GetMapping("/detail")
    public ResponseEntity<DetailResponse<Boolean>> getFlagDetail(
            @RequestParam(name = "flagKey", required = false) String flagKey,
            @RequestParam(name = "contextKey", required = false, defaultValue = "example-user") String contextKey
    ) {
        log.info("GET /flags/detail [boolean+reason], flagKey={}, contextKey={}", flagKey, contextKey);

        LDContext ctx = LDContext.builder(contextKey).build();
        EvaluationDetail<Boolean> detail = ldService.evaluateBooleanFlagWithDetail(flagKey, ctx);

        log.debug("Flag '{}' -> value={}, reason={}", flagKey, detail.getValue(), detail.getReason());
        return ResponseEntity.ok(
                new DetailResponse<>(flagKey, detail.getValue(), detail.getReason().toString())
        );
    }

    /**
     * GET /flags/json?flagKey={flagKey}&contextKey={contextKey}
     *
     * Demonstrates returning a JSON variation.
     * Docs: https://docs.launchdarkly.com/sdk/features/data-model-server-side?platform=java#json-variations
     */
    @GetMapping("/json")
    public ResponseEntity<JsonNode> getJsonFlag(
            @RequestParam(name = "flagKey", required = false) String flagKey,
            @RequestParam(name = "contextKey", required = false, defaultValue = "example-user") String contextKey
    ) {
        log.info("GET /flags/json [JSON variation], flagKey={}, contextKey={}", flagKey, contextKey);

        LDContext ctx = LDContext.builder(contextKey).build();
        JsonNode node = ldService.evaluateJsonFlag(flagKey, ctx, null);

        log.debug("Flag '{}' -> JSON: {}", flagKey, node);
        return ResponseEntity.ok(node);
    }

    /**
     * GET /flags/multi?flagKey={flagKey}&userId={...}&orgId={...}
     *
     * Example endpoint for multi-context usage (user + org, etc.).
     * Docs: https://docs.launchdarkly.com/sdk/features/multi-context?platform=java
     */
@GetMapping("/multi")
public ResponseEntity<FlagResponse> getFlagMultiContext(
        @RequestParam(name = "flagKey", required = false) String flagKey,
        @RequestParam(name = "userId", defaultValue = "anon-user") String userId,
        @RequestParam(name = "userRole", defaultValue = "guest") String userRole,
        @RequestParam(name = "orgId", defaultValue = "org-default") String orgId,
        @RequestParam(name = "subscription", defaultValue = "basic") String subscription
) {
    log.info("GET /flags/multi [multi-context], flagKey={}, userId={}, userRole={}, orgId={}, subscription={}",
             flagKey, userId, userRole, orgId, subscription);

    // Build the multi-context
    var multiCtx = ldService.buildMultiContext(userId, userRole, orgId, subscription);
    log.debug("Built multi-context: {}", multiCtx);

    Object result;
    try {
        // Always evaluate as a string
        result = ldService.evaluateStringFlag(flagKey, multiCtx, "default-string");
        log.info("String flag evaluation - Key: '{}', Result: '{}'", flagKey, result);
    } catch (Exception e) {
        log.error("Error evaluating flag: {}", e.getMessage());
        result = "default-string"; // Default fallback for errors
        log.info("Flag evaluation failed, returning default: {}", result);
    }

    log.debug("Flag '{}' with multi-context -> {}", flagKey, result);
    return ResponseEntity.ok(new FlagResponse(flagKey, result));
}



    // Simple response model
    static class FlagResponse {
        public String flagKey;
        public Object value;

        public FlagResponse(String flagKey, Object value) {
            this.flagKey = flagKey;
            this.value = value;
        }
    }

    static class DetailResponse<T> {
        public String flagKey;
        public T value;
        public String reason;

        public DetailResponse(String flagKey, T value, String reason) {
            this.flagKey = flagKey;
            this.value = value;
            this.reason = reason;
        }
    }
}