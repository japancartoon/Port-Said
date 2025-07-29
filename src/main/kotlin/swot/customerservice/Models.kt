package swot.customerservice

/**
 * Data models for customer service requests and responses
 */

data class DomainVerificationRequest(
    val domain: String,
    val institutionName: String,
    val requesterEmail: String,
    val requestType: String = "domain_verification",
    val timestamp: Long = System.currentTimeMillis(),
    val additionalInfo: Map<String, String> = emptyMap()
)

data class SupportRequest(
    val email: String,
    val domain: String?,
    val subject: String,
    val message: String,
    val priority: Priority = Priority.NORMAL,
    val requestType: String = "support",
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkflowResponse(
    val success: Boolean,
    val message: String,
    val workflowId: String?,
    val data: Map<String, Any> = emptyMap()
)

enum class Priority {
    LOW, NORMAL, HIGH, URGENT
}