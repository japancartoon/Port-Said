package swot.customerservice

import junit.framework.TestCase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

class CustomerServiceManagerTest : TestCase() {
    
    private lateinit var mockServer: MockWebServer
    private lateinit var customerService: CustomerServiceManager
    
    override fun setUp() {
        super.setUp()
        mockServer = MockWebServer()
        mockServer.start()
        
        val config = N8nConfig(
            baseUrl = mockServer.url("/").toString().removeSuffix("/"),
            timeout = 5000L
        )
        val client = N8nClient(config)
        customerService = CustomerServiceManager(client)
    }
    
    override fun tearDown() {
        super.tearDown()
        try {
            mockServer.shutdown()
        } catch (e: IOException) {
            // Ignore shutdown errors in tests
        }
    }
    
    fun testProcessDomainVerificationWithAcademicDomain() {
        // Mock successful n8n response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "workflowId": "wf-academic-123"}""")
            .addHeader("Content-Type", "application/json"))
        
        val result = customerService.processDomainVerification(
            domain = "stanford.edu",
            institutionName = "Stanford University",
            requesterEmail = "admin@stanford.edu"
        )
        
        assertTrue(result.success)
        assertTrue(result.isPreVerified) // stanford.edu should be pre-verified
        assertTrue(result.message.contains("already verified as academic"))
        assertEquals("wf-academic-123", result.workflowId)
        assertFalse(result.existingSchoolNames.isEmpty()) // Should find Stanford
    }
    
    fun testProcessDomainVerificationWithNonAcademicDomain() {
        // Mock successful n8n response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "workflowId": "wf-manual-456"}""")
            .addHeader("Content-Type", "application/json"))
        
        val result = customerService.processDomainVerification(
            domain = "example.com",
            institutionName = "Example School",
            requesterEmail = "admin@example.com"
        )
        
        assertTrue(result.success)
        assertFalse(result.isPreVerified) // example.com should not be pre-verified
        assertTrue(result.message.contains("manual review"))
        assertEquals("wf-manual-456", result.workflowId)
        assertTrue(result.existingSchoolNames.isEmpty()) // Should not find school names
    }
    
    fun testProcessSupportRequestWithAcademicEmail() {
        // Mock successful n8n response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "ticketId": "support-789"}""")
            .addHeader("Content-Type", "application/json"))
        
        val result = customerService.processSupportRequest(
            email = "student@stanford.edu",
            subject = "License Help",
            message = "I need assistance with my student license",
            priority = Priority.HIGH
        )
        
        assertTrue(result.success)
        assertTrue(result.isPreVerified) // stanford.edu is academic
        assertEquals("Support request submitted successfully", result.message)
        assertFalse(result.existingSchoolNames.isEmpty()) // Should find Stanford
    }
    
    fun testProcessSupportRequestWithNonAcademicEmail() {
        // Mock successful n8n response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "ticketId": "support-101"}""")
            .addHeader("Content-Type", "application/json"))
        
        val result = customerService.processSupportRequest(
            email = "user@gmail.com",
            subject = "General Inquiry",
            message = "I have a question about educational licenses"
        )
        
        assertTrue(result.success)
        assertFalse(result.isPreVerified) // gmail.com is not academic
        assertEquals("Support request submitted successfully", result.message)
        assertTrue(result.existingSchoolNames.isEmpty()) // Should not find school names
    }
    
    fun testBatchProcessDomainVerifications() {
        // Mock multiple successful responses
        repeat(3) {
            mockServer.enqueue(MockResponse()
                .setResponseCode(200)
                .setBody("""{"success": true, "workflowId": "wf-batch-$it"}""")
                .addHeader("Content-Type", "application/json"))
        }
        
        val requests = listOf(
            DomainVerificationRequest("stanford.edu", "Stanford University", "admin1@stanford.edu"),
            DomainVerificationRequest("mit.edu", "MIT", "admin2@mit.edu"),
            DomainVerificationRequest("example.com", "Example School", "admin3@example.com")
        )
        
        val results = customerService.batchProcessDomainVerifications(requests)
        
        assertEquals(3, results.size)
        assertTrue(results[0].success && results[0].isPreVerified) // Stanford
        assertTrue(results[1].success && results[1].isPreVerified) // MIT  
        assertTrue(results[2].success && !results[2].isPreVerified) // Example.com
    }
    
    fun testHealthCheck() {
        // Mock health check response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("OK"))
        
        val healthResult = customerService.healthCheck()
        
        assertTrue(healthResult.n8nConnectivity)
        assertTrue(healthResult.swotLibrary)
        assertTrue(healthResult.overallHealthy)
    }
    
    fun testHealthCheckWithN8nDown() {
        // Mock health check failure
        mockServer.enqueue(MockResponse()
            .setResponseCode(503)
            .setBody("Service Unavailable"))
        
        val healthResult = customerService.healthCheck()
        
        assertFalse(healthResult.n8nConnectivity)
        assertTrue(healthResult.swotLibrary) // Swot library itself is fine
        assertFalse(healthResult.overallHealthy) // Overall system unhealthy due to n8n
    }
}