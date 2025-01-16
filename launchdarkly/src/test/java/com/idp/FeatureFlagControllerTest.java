package com.idp.launchdarkly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.EvaluationReason;
import com.launchdarkly.sdk.LDContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LaunchDarklyService ldService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetFlag_Boolean() throws Exception {
        // Given
        Boolean expectedValue = true;
        given(ldService.evaluateBooleanFlag(eq("my-flag"), any(LDContext.class)))
            .willReturn(expectedValue);

        // When & Then
        mockMvc.perform(get("/flags")
                .param("flagKey", "my-flag")
                .param("contextKey", "test-user")
                .param("type", "BOOLEAN"))
                .andDo(print()) // This will print the response for debugging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("my-flag"))
                .andExpect(jsonPath("$.value").value(expectedValue));

        // Verify
        verify(ldService).evaluateBooleanFlag(eq("my-flag"), any(LDContext.class));
    }

    @Test
    void testGetFlag_String() throws Exception {
        // Given
        String expectedValue = "test-value";
        given(ldService.evaluateStringFlag(eq("string-flag"), any(LDContext.class), any()))
            .willReturn(expectedValue);

        // When & Then
        mockMvc.perform(get("/flags")
                .param("flagKey", "string-flag")
                .param("contextKey", "test-user")
                .param("type", "STRING"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("string-flag"))
                .andExpect(jsonPath("$.value").value(expectedValue));

        // Verify
        verify(ldService).evaluateStringFlag(eq("string-flag"), any(LDContext.class), any());
    }

    @Test
    void testGetFlagDetail() throws Exception {
        // Given
        EvaluationDetail<Boolean> detail = 
            EvaluationDetail.fromValue(true, 1, EvaluationReason.ruleMatch(1, "test-rule"));
        given(ldService.evaluateBooleanFlagWithDetail(eq("my-flag"), any(LDContext.class)))
            .willReturn(detail);

        // When & Then
        mockMvc.perform(get("/flags/detail")
                .param("flagKey", "my-flag")
                .param("contextKey", "test-user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagKey").value("my-flag"))
                .andExpect(jsonPath("$.value").value(true))
                .andExpect(jsonPath("$.reason", startsWith("RULE_MATCH")));

        // Verify
        verify(ldService).evaluateBooleanFlagWithDetail(eq("my-flag"), any(LDContext.class));
    }

    @Test
    void testGetJsonFlag() throws Exception {
        // Given
        ObjectNode fakeJson = objectMapper.createObjectNode();
        fakeJson.put("entitlement", "WRITE");

        given(ldService.evaluateJsonFlag(eq("json-flag"), any(LDContext.class), isNull()))
            .willReturn(fakeJson);

        // When & Then
        mockMvc.perform(get("/flags/json")
                .param("flagKey", "json-flag")
                .param("contextKey", "test-user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entitlement").value("WRITE"));

        // Verify
        verify(ldService).evaluateJsonFlag(eq("json-flag"), any(LDContext.class), isNull());
    }

//   @Test
// void testGetFlagMulti() throws Exception {
//     // Ensure the mock returns "true"
//     given(ldService.evaluateStringFlag(eq("feature-1"), any(LDContext.class), eq("default-string")))
//         .willReturn("true");

//     // Perform the request
//     mockMvc.perform(get("/flags/multi")
//             .param("flagKey", "feature-1")
//             .param("userId", "bob")
//             .param("userRole", "guest")
//             .param("orgId", "org-default")
//             .param("subscription", "basic"))
//         .andDo(print()) // Print response for debugging
//         .andExpect(status().isOk())
//         .andExpect(jsonPath("$.flagKey").value("feature-1"))
//         .andExpect(jsonPath("$.value").value("true")); // Check for expected result

//     // Verify the mock was called with correct arguments
//     verify(ldService).evaluateStringFlag(eq("feature-1"), any(LDContext.class), eq("default-string"));
// }
}