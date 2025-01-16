package com.idp.launchdarkly;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.EvaluationReason;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(SpringExtension.class)
class LaunchDarklyServiceTest {
    private static final Logger log = Logger.getLogger(LaunchDarklyServiceTest.class.getName());

    @Mock
    private LDClient mockLdClient;

    private LaunchDarklyService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new LaunchDarklyService("sdk-123", "kill-switch");
        ReflectionTestUtils.setField(service, "ldClient", mockLdClient);
    }

    @Test
    void testEvaluateBooleanFlag() {
        // Arrange
        String flagKey = "my-flag";
        LDContext context = LDContext.create("test-user");
        EvaluationDetail<Boolean> detail = EvaluationDetail.fromValue(true, 1, EvaluationReason.fallthrough());
        
        given(mockLdClient.boolVariationDetail(eq(flagKey), eq(context), eq(false)))
            .willReturn(detail);

        // Act
        boolean result = service.evaluateBooleanFlag(flagKey, context);

        // Assert
        assertTrue(result);
        verify(mockLdClient).boolVariationDetail(eq(flagKey), eq(context), eq(false));
    }

    @Test
    void testEvaluateBooleanFlag_StringFallback() {
        // Arrange
        String flagKey = "string-flag";
        LDContext context = LDContext.create("test-user");
        
        // Mock boolean evaluation to throw exception (simulating wrong type)
        given(mockLdClient.boolVariationDetail(eq(flagKey), eq(context), eq(false)))
            .willThrow(new ClassCastException("Wrong type"));
        
        // Mock string evaluation to return "true"
        given(mockLdClient.stringVariation(eq(flagKey), eq(context), eq("false")))
            .willReturn("true");

        // Act
        boolean result = service.evaluateBooleanFlag(flagKey, context);

        // Assert
        assertTrue(result);
        verify(mockLdClient).boolVariationDetail(eq(flagKey), eq(context), eq(false));
        verify(mockLdClient).stringVariation(eq(flagKey), eq(context), eq("false"));
    }

    @Test
    void testEvaluateBooleanFlagWithDetail() {
        // Arrange
        String flagKey = "flag-with-detail";
        LDContext context = LDContext.create("test-user");
        EvaluationDetail<Boolean> detail =
            EvaluationDetail.fromValue(true, 1, EvaluationReason.ruleMatch(1, "test-rule"));
        
        given(mockLdClient.boolVariationDetail(eq(flagKey), eq(context), eq(false)))
            .willReturn(detail);

        // Act
        EvaluationDetail<Boolean> result = service.evaluateBooleanFlagWithDetail(flagKey, context);

        // Assert
        assertTrue(result.getValue());
        assertEquals(EvaluationReason.Kind.RULE_MATCH, result.getReason().getKind());
        verify(mockLdClient).boolVariationDetail(eq(flagKey), eq(context), eq(false));
    }

    @Test
    void testEvaluateStringFlag() {
        // Arrange
        String flagKey = "string-flag";
        String defaultVal = "defaultVal";
        String expectedResult = "server-returned-string";
        LDContext context = LDContext.create("test-user");
        
        given(mockLdClient.stringVariation(eq(flagKey), eq(context), eq(defaultVal)))
            .willReturn(expectedResult);

        // Act
        String actual = service.evaluateStringFlag(flagKey, context, defaultVal);

        // Assert
        assertEquals(expectedResult, actual);
        verify(mockLdClient).stringVariation(eq(flagKey), eq(context), eq(defaultVal));
    }

    @Test
    void testEvaluateJsonFlag() {
        // Arrange
        String flagKey = "json-flag";
        LDContext context = LDContext.create("test-user");
        ObjectNode fakeJson = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        fakeJson.put("entitlement", "READ");
        LDValue fakeValue = LDValue.parse(fakeJson.toString());
        
        given(mockLdClient.jsonValueVariation(eq(flagKey), eq(context), any()))
            .willReturn(fakeValue);

        // Act
        JsonNode result = service.evaluateJsonFlag(flagKey, context, null);

        // Assert
        assertEquals("READ", result.get("entitlement").asText());
        verify(mockLdClient).jsonValueVariation(eq(flagKey), eq(context), any());
    }

    @Test
    void testEvaluateFlag() {
        // Arrange
        String booleanFlagKey = "boolean-flag";
        String stringFlagKey = "string-flag";
        LDContext context = LDContext.builder("test-user").build();
        
        given(mockLdClient.boolVariation(eq(booleanFlagKey), eq(context), eq(false)))
            .willReturn(true);
        given(mockLdClient.stringVariation(eq(stringFlagKey), eq(context), eq("default")))
            .willReturn("value");

        // Act & Assert
        Object booleanResult = service.evaluateFlag(booleanFlagKey, context, false);
        Object stringResult = service.evaluateFlag(stringFlagKey, context, "default");

        assertTrue((Boolean) booleanResult);
        assertEquals("value", stringResult);

        verify(mockLdClient).boolVariation(eq(booleanFlagKey), eq(context), eq(false));
        verify(mockLdClient).stringVariation(eq(stringFlagKey), eq(context), eq("default"));
    }

// @Test
// void testBuildMultiContext() {
//     // Act
//     var multiCtx = service.buildMultiContext("alice", "admin", "acme", "gold");

//     // Debug logging
//     log.info("Multi-context created: " + multiCtx);

//     // Retrieve specific contexts by their kind
//     LDContext userContext = multiCtx.getContext("user");
//     LDContext orgContext = multiCtx.getContext("organization");

//     // Assert both contexts exist
//     assertNotNull(userContext, "User context should exist");
//     assertNotNull(orgContext, "Organization context should exist");

//     // Verify user context details
//     assertEquals("user", userContext.getKind(), "User context should have kind 'user'");
//     assertEquals("alice", userContext.getKey(), "User context should have key 'alice'");
//     assertEquals("admin", userContext.getValue("role"), "User context should have role 'admin'");

//     // Verify organization context details
//     assertEquals("organization", orgContext.getKind(), "Organization context should have kind 'organization'");
//     assertEquals("acme", orgContext.getKey(), "Organization context should have key 'acme'");
//     assertEquals("gold", orgContext.getValue("subscriptionLevel"), 
//                  "Organization context should have subscription level 'gold'");
// }

    @Test
    void testEmptyFlagKeyFallback() {
        // Arrange
        String defaultFlagKey = "kill-switch";
        LDContext context = LDContext.create("test-user");
        EvaluationDetail<Boolean> detail = EvaluationDetail.fromValue(false, 1, EvaluationReason.fallthrough());
        
        given(mockLdClient.boolVariationDetail(eq(defaultFlagKey), eq(context), eq(false)))
            .willReturn(detail);

        // Act
        boolean result = service.evaluateBooleanFlag(null, context);

        // Assert
        assertFalse(result);
        verify(mockLdClient).boolVariationDetail(eq(defaultFlagKey), eq(context), eq(false));
    }
}