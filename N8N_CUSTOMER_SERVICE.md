# n8n Customer Service Integration

This document describes the n8n customer service integration added to the swot library.

## Overview

The swot library now includes customer service automation capabilities using n8n workflows. This integration allows for automated processing of domain verification requests and support tickets while leveraging the existing swot domain verification logic.

## Features

- **Domain Verification Automation**: Automatically trigger n8n workflows for domain verification requests
- **Support Request Processing**: Route support requests through n8n workflows with context about domain verification status
- **Batch Processing**: Handle multiple domain verification requests in batches
- **Health Monitoring**: Check the health of both the swot library and n8n connectivity
- **Pre-verification**: Leverage existing swot logic to pre-verify academic domains before triggering workflows

## Components

### N8nConfig
Configuration class for n8n webhook endpoints and settings.

```kotlin
val config = N8nConfig(
    baseUrl = "https://your-n8n-instance.com",
    apiKey = "your-api-key", // optional
    timeout = 30000L
)
```

### N8nClient
HTTP client for communicating with n8n webhooks.

```kotlin
val client = N8nClient(config)
val response = client.triggerDomainVerification(request)
```

### CustomerServiceManager
Main service class that integrates swot logic with n8n workflows.

```kotlin
val customerService = CustomerServiceManager(client)
val result = customerService.processDomainVerification(
    domain = "stanford.edu",
    institutionName = "Stanford University", 
    requesterEmail = "admin@stanford.edu"
)
```

### CustomerServiceFactory
Factory for easy creation of customer service instances.

```kotlin
// Default configuration
val customerService = CustomerServiceFactory.createDefault()

// Custom configuration
val customerService = CustomerServiceFactory.create("https://my-n8n.com", "api-key")

// Testing configuration
val customerService = CustomerServiceFactory.createForTesting()
```

## Usage Examples

### Basic Domain Verification
```kotlin
val customerService = CustomerServiceFactory.create("https://your-n8n.com")

val result = customerService.processDomainVerification(
    domain = "university.edu",
    institutionName = "University Name",
    requesterEmail = "admin@university.edu"
)

if (result.success) {
    println("Workflow triggered: ${result.workflowId}")
    if (result.isPreVerified) {
        println("Domain is already verified as academic")
    }
}
```

### Support Request Processing
```kotlin
val result = customerService.processSupportRequest(
    email = "student@university.edu",
    subject = "License Help",
    message = "I need help with my student license",
    priority = Priority.HIGH
)
```

### Batch Processing
```kotlin
val requests = listOf(
    DomainVerificationRequest("school1.edu", "School 1", "admin1@school1.edu"),
    DomainVerificationRequest("school2.edu", "School 2", "admin2@school2.edu")
)

val results = customerService.batchProcessDomainVerifications(requests)
```

### Health Monitoring
```kotlin
val health = customerService.healthCheck()
if (health.overallHealthy) {
    println("System is healthy")
} else {
    println("n8n connectivity: ${health.n8nConnectivity}")
    println("swot library: ${health.swotLibrary}")
}
```

## n8n Workflow Configuration

Your n8n instance should have the following webhook endpoints configured:

1. **Domain Verification Workflow**: `/webhook/domain-verification`
   - Receives: `DomainVerificationRequest` JSON
   - Should handle both pre-verified and unverified domains
   
2. **Support Request Workflow**: `/webhook/support-request`
   - Receives: `SupportRequest` JSON
   - Should route based on priority and domain verification status

## Data Models

### DomainVerificationRequest
```kotlin
data class DomainVerificationRequest(
    val domain: String,
    val institutionName: String,
    val requesterEmail: String,
    val requestType: String = "domain_verification",
    val timestamp: Long = System.currentTimeMillis(),
    val additionalInfo: Map<String, String> = emptyMap()
)
```

### SupportRequest
```kotlin
data class SupportRequest(
    val email: String,
    val domain: String?,
    val subject: String,
    val message: String,
    val priority: Priority = Priority.NORMAL,
    val requestType: String = "support",
    val timestamp: Long = System.currentTimeMillis()
)
```

### WorkflowResponse
```kotlin
data class WorkflowResponse(
    val success: Boolean,
    val message: String,
    val workflowId: String?,
    val data: Map<String, Any> = emptyMap()
)
```

## Error Handling

The integration includes comprehensive error handling:

- Network errors are caught and returned as failed responses
- HTTP errors include status codes in the response
- JSON parsing errors are handled gracefully
- Timeouts are configurable

## Testing

The integration includes comprehensive test coverage:

- Unit tests for all components
- Mock server tests for HTTP interactions  
- Integration tests for the complete workflow
- Factory tests for different configurations

Run tests with:
```bash
./gradlew test
```

Run the demo with:
```bash
./gradlew runDemo
```

## Dependencies

The customer service integration adds the following dependencies:

- OkHttp 4.12.0 for HTTP client
- Jackson 2.16.1 for JSON processing  
- MockWebServer 4.12.0 for testing

These are added automatically when you include the customer service package.