# Launch Darkly

Launch Darkly has been added as a sidecar container to the pdp to control the feature flags for the application. You will need to create a new project in Launch Darkly get the SDK Key and update the docker-compose file with the SDK key.

The container holds a java api service that is invoked by the pdp as an http service. The API supports a number of methods to get the feature flags and the flag details.

User ids may also be passed as a context value to allow feature flags to be set for user segments. 

The http service will need to be setup with the following attributes:

url: http://launchdarkly:8080/flags/detail?featureKey=featureKey


# LaunchDarkly FeatureFlagController

This controller exposes several HTTP endpoints for evaluating LaunchDarkly feature flags. It demonstrates:
	•	Basic boolean and string flag evaluations,
	•	Returning evaluation details (including the “reason”),
	•	Working with JSON variations,
	•	And using multi-context for more complex use cases (e.g., user + organization contexts).


@Service
public class LaunchDarklyService {
    private final LDClient ldClient;

    public LaunchDarklyService() {
        // The SDK key can be configured via application.properties or environment variables
        this.ldClient = new LDClient("YOUR_SDK_KEY");
    }

    // Methods: evaluateBooleanFlag, evaluateStringFlag, buildMultiContext, etc.
    // ...
}


# Endpoints Overview

All endpoints are served under the base path:

/flags

Each endpoint uses query parameters to customize the LaunchDarkly feature flag evaluation.

## 1. GET /flags

Description
Evaluates a feature flag and returns a string or boolean result (based on the type parameter).

Query Parameters
	•	flagKey (optional): The key of the LaunchDarkly flag you want to evaluate.
	•	contextKey (optional, default: "example-user"): Identifies the user or context.
	•	type (optional, default: "STRING"): Determines if the flag is evaluated as a boolean or string.
	•	STRING → evaluateStringFlag(flagKey, context, "default-string")
	•	BOOLEAN → evaluateBooleanFlag(flagKey, context)

Examples
	•	String flag evaluation:

GET http://localhost:8080/flags?flagKey=my-string-flag&contextKey=john_doe&type=STRING

Response might be:

{
  "flagKey": "my-string-flag",
  "value": "someStringVariation"
}


	•	Boolean flag evaluation:

GET http://localhost:8080/flags?flagKey=my-boolean-flag&contextKey=john_doe&type=boolean

Response might be:

{
  "flagKey": "my-boolean-flag",
  "value": true
}



If flagKey is not provided, LaunchDarkly may return the default value depending on your implementation in LaunchDarklyService.

## 2. GET /flags/detail

Description
Evaluates a boolean flag but returns additional information (the “reason” the flag had that particular value), using LaunchDarkly’s EvaluationDetail.

Query Parameters
	•	flagKey (optional)
	•	contextKey (optional, default: "example-user")

Example

GET http://localhost:8080/flags/detail?flagKey=my-boolean-flag&contextKey=john_doe

Sample Response

{
  "flagKey": "my-boolean-flag",
  "value": true,
  "reason": "RULE_MATCH (some description of the matching rule)"
}

Where value is the boolean evaluation, and reason can show if it was the default, a specific targeting rule, etc.

## 3. GET /flags/json

Description
Evaluates a JSON variation from LaunchDarkly. For example, if you have a feature flag that returns a JSON object or array, you can fetch it here.

Query Parameters
	•	flagKey (optional)
	•	contextKey (optional, default: "example-user")

Example

GET http://localhost:8080/flags/json?flagKey=my-json-flag&contextKey=jane_doe

Sample Response

{
  "someKey": "someValue",
  "anotherField": 123
}

## 4. GET /flags/multi

Description
Demonstrates a multi-context evaluation, combining multiple contexts (e.g., user, organization, subscription). By default, the controller calls evaluateStringFlag but you could adjust it as needed.

Query Parameters
	•	flagKey (optional)
	•	userId (default: "anon-user")
	•	userRole (default: "guest")
	•	orgId (default: "org-default")
	•	subscription (default: "basic")

These are all combined into a multi-context within LaunchDarklyService.buildMultiContext.

### Example

GET http://localhost:8080/flags/multi?flagKey=my-string-flag&userId=alice123&userRole=admin&orgId=someOrg&subscription=premium

Sample Response

{
  "flagKey": "my-string-flag",
  "value": "aStringVariationOrDefault"
}

If the flag evaluation fails for any reason, the code defaults to "default-string".

Return Data Models

FlagResponse

{
  "flagKey": "someFlag",
  "value": "the result type could be string or boolean"
}

DetailResponse (for /flags/detail)

{
  "flagKey": "someFlag",
  "value": true,
  "reason": "RULE_MATCH"
}

# Logs

The controller logs requests at INFO level and logs the final evaluated flag value at DEBUG level. To see detailed logs, ensure your logging configuration allows DEBUG messages for com.idp.launchdarkly.FeatureFlagController.

# Frequently Asked Questions
1.	What happens if flagKey is missing?
	•	The ldService methods may return the default variation defined in your LaunchDarkly environment or the fallback value specified in code (e.g., "default-string").
2.	Can I evaluate custom data types?
	•	Yes, you can modify the controller or the LaunchDarklyService to handle numeric variations or more complex logic as needed.
3.	How do I set up a multi-context in LaunchDarkly?
	•	Typically, you define contexts in the LaunchDarkly dashboard to target rules by user ID, organization ID, etc. Then pass those context fields to LDContext.multi(...) or a custom builder pattern (as shown in your code) to combine them.

Troubleshooting
•	No SDK key or invalid credentials: Ensure your LaunchDarklyService is using a valid LaunchDarkly SDK key. You’ll get errors in logs if the connection fails.
	•	Flag not found: If you provide a flagKey that doesn’t exist, LaunchDarkly returns the default variation or an error in logs.
	•	Network issues: Make sure the application can reach the LaunchDarkly service. Firewalls or missing proxy configs can block requests.

Additional Resources
	•	LaunchDarkly Java SDK Docs
	•	Evaluation reasons & detail objects
	•	JSON variations
	•	Multi-context usage
