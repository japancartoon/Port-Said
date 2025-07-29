package swot.customerservice.example

import swot.customerservice.*

/**
 * Example usage of the n8n customer service integration
 */
fun main() {
    println("=== Swot n8n Customer Service Demo ===\n")
    
    // Create customer service manager
    val customerService = CustomerServiceFactory.createForTesting("http://localhost:5678")
    
    // Example 1: Domain verification for academic domain
    println("1. Processing domain verification for academic domain...")
    val academicResult = customerService.processDomainVerification(
        domain = "stanford.edu",
        institutionName = "Stanford University",
        requesterEmail = "admin@stanford.edu",
        additionalInfo = mapOf(
            "department" to "IT Department",
            "contact_phone" to "+1-650-555-0123"
        )
    )
    
    println("   Result: ${academicResult.message}")
    println("   Pre-verified: ${academicResult.isPreVerified}")
    println("   Existing schools: ${academicResult.existingSchoolNames}")
    println("   Workflow ID: ${academicResult.workflowId ?: "N/A"}")
    println()
    
    // Example 2: Domain verification for non-academic domain
    println("2. Processing domain verification for non-academic domain...")
    val nonAcademicResult = customerService.processDomainVerification(
        domain = "newschool.org",
        institutionName = "New Educational Institution",
        requesterEmail = "admin@newschool.org"
    )
    
    println("   Result: ${nonAcademicResult.message}")
    println("   Pre-verified: ${nonAcademicResult.isPreVerified}")
    println("   Workflow ID: ${nonAcademicResult.workflowId ?: "N/A"}")
    println()
    
    // Example 3: Support request from student
    println("3. Processing support request from student...")
    val supportResult = customerService.processSupportRequest(
        email = "student@mit.edu",
        subject = "License Activation Issue",
        message = "I'm having trouble activating my JetBrains license with my student email.",
        priority = Priority.HIGH
    )
    
    println("   Result: ${supportResult.message}")
    println("   Academic email: ${supportResult.isPreVerified}")
    println("   Workflow ID: ${supportResult.workflowId ?: "N/A"}")
    println()
    
    // Example 4: Batch processing
    println("4. Batch processing multiple domain verifications...")
    val batchRequests = listOf(
        DomainVerificationRequest("harvard.edu", "Harvard University", "admin@harvard.edu"),
        DomainVerificationRequest("berkeley.edu", "UC Berkeley", "admin@berkeley.edu"),
        DomainVerificationRequest("example.edu", "Example College", "admin@example.edu")
    )
    
    val batchResults = customerService.batchProcessDomainVerifications(batchRequests)
    batchResults.forEachIndexed { index, result ->
        println("   Request ${index + 1}: ${if (result.success) "Success" else "Failed"} - Pre-verified: ${result.isPreVerified}")
    }
    println()
    
    // Example 5: Health check
    println("5. Checking system health...")
    val health = customerService.healthCheck()
    println("   n8n Connectivity: ${health.n8nConnectivity}")
    println("   Swot Library: ${health.swotLibrary}")
    println("   Overall Health: ${health.overallHealthy}")
    println()
    
    println("=== Demo Complete ===")
}