package swot.customerservice

/**
 * Configuration for n8n webhook endpoints and customer service settings
 */
data class N8nConfig(
    val baseUrl: String = "https://your-n8n-instance.com",
    val webhookPath: String = "/webhook",
    val domainVerificationWorkflow: String = "domain-verification",
    val supportRequestWorkflow: String = "support-request",
    val apiKey: String? = null,
    val timeout: Long = 30000L // 30 seconds
) {
    fun getDomainVerificationUrl(): String = "$baseUrl$webhookPath/$domainVerificationWorkflow"
    fun getSupportRequestUrl(): String = "$baseUrl$webhookPath/$supportRequestWorkflow"
}