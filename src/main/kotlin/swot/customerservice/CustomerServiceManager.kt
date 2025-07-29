package swot.customerservice

import swot.findSchoolNames
import swot.isAcademic

/**
 * Main customer service class that integrates swot domain verification with n8n workflows
 */
class CustomerServiceManager(private val n8nClient: N8nClient) {
    
    /**
     * Process a domain verification request with automated customer service
     */
    fun processDomainVerification(
        domain: String,
        institutionName: String,
        requesterEmail: String,
        additionalInfo: Map<String, String> = emptyMap()
    ): CustomerServiceResult {
        
        // First, validate the domain using existing swot logic
        val isAcademicDomain = isAcademic(domain)
        val schoolNames = findSchoolNames(domain)
        
        val verificationRequest = DomainVerificationRequest(
            domain = domain,
            institutionName = institutionName,
            requesterEmail = requesterEmail,
            additionalInfo = additionalInfo + mapOf(
                "pre_verified" to isAcademicDomain.toString(),
                "existing_school_names" to schoolNames.joinToString(", "),
                "verification_method" to "swot_library"
            )
        )
        
        // Trigger n8n workflow for domain verification
        val workflowResponse = n8nClient.triggerDomainVerification(verificationRequest)
        
        return CustomerServiceResult(
            success = workflowResponse.success,
            message = if (isAcademicDomain) {
                "Domain is already verified as academic. Workflow triggered for processing."
            } else {
                "Domain verification workflow initiated for manual review."
            },
            isPreVerified = isAcademicDomain,
            existingSchoolNames = schoolNames,
            workflowId = workflowResponse.workflowId,
            additionalData = workflowResponse.data
        )
    }
    
    /**
     * Process a general support request
     */
    fun processSupportRequest(
        email: String,
        subject: String,
        message: String,
        priority: Priority = Priority.NORMAL,
        domain: String? = null
    ): CustomerServiceResult {
        
        // Extract domain from email if not provided
        val actualDomain = domain ?: email.substringAfter('@')
        val isAcademicDomain = isAcademic(actualDomain)
        
        val supportRequest = SupportRequest(
            email = email,
            domain = actualDomain,
            subject = subject,
            message = message,
            priority = priority
        )
        
        // Trigger n8n workflow for support request
        val workflowResponse = n8nClient.triggerSupportRequest(supportRequest)
        
        return CustomerServiceResult(
            success = workflowResponse.success,
            message = "Support request submitted successfully",
            isPreVerified = isAcademicDomain,
            existingSchoolNames = if (isAcademicDomain) findSchoolNames(actualDomain) else emptyList(),
            workflowId = workflowResponse.workflowId,
            additionalData = workflowResponse.data
        )
    }
    
    /**
     * Batch process multiple domain verification requests
     */
    fun batchProcessDomainVerifications(requests: List<DomainVerificationRequest>): List<CustomerServiceResult> {
        return requests.map { request ->
            processDomainVerification(
                domain = request.domain,
                institutionName = request.institutionName,
                requesterEmail = request.requesterEmail,
                additionalInfo = request.additionalInfo
            )
        }
    }
    
    /**
     * Check the health of the customer service system
     */
    fun healthCheck(): HealthCheckResult {
        val n8nHealthy = n8nClient.healthCheck()
        
        return HealthCheckResult(
            n8nConnectivity = n8nHealthy,
            swotLibrary = true, // Always true if we reach this point
            overallHealthy = n8nHealthy
        )
    }
}

/**
 * Result of a customer service operation
 */
data class CustomerServiceResult(
    val success: Boolean,
    val message: String,
    val isPreVerified: Boolean,
    val existingSchoolNames: List<String>,
    val workflowId: String?,
    val additionalData: Map<String, Any> = emptyMap()
)

/**
 * Health check result for the customer service system
 */
data class HealthCheckResult(
    val n8nConnectivity: Boolean,
    val swotLibrary: Boolean,
    val overallHealthy: Boolean
)