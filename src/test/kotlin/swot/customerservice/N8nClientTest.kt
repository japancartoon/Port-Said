package swot.customerservice

import junit.framework.TestCase
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.IOException

class N8nClientTest : TestCase() {
    
    private lateinit var mockServer: MockWebServer
    private lateinit var n8nClient: N8nClient
    
    override fun setUp() {
        super.setUp()
        mockServer = MockWebServer()
        mockServer.start()
        
        val config = N8nConfig(
            baseUrl = mockServer.url("/").toString().removeSuffix("/"),
            timeout = 5000L
        )
        n8nClient = N8nClient(config)
    }
    
    override fun tearDown() {
        super.tearDown()
        try {
            mockServer.shutdown()
        } catch (e: IOException) {
            // Ignore shutdown errors in tests
        }
    }
    
    fun testTriggerDomainVerificationSuccess() {
        // Mock successful response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "workflowId": "wf-123"}""")
            .addHeader("Content-Type", "application/json"))
        
        val request = DomainVerificationRequest(
            domain = "test.edu",
            institutionName = "Test University",
            requesterEmail = "admin@test.edu"
        )
        
        val response = n8nClient.triggerDomainVerification(request)
        
        assertTrue(response.success)
        assertEquals("Workflow triggered successfully", response.message)
        assertEquals("wf-123", response.workflowId)
    }
    
    fun testTriggerDomainVerificationFailure() {
        // Mock error response
        mockServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"))
        
        val request = DomainVerificationRequest(
            domain = "test.edu",
            institutionName = "Test University", 
            requesterEmail = "admin@test.edu"
        )
        
        val response = n8nClient.triggerDomainVerification(request)
        
        assertFalse(response.success)
        assertTrue(response.message.contains("HTTP 500"))
        assertNull(response.workflowId)
    }
    
    fun testTriggerSupportRequestSuccess() {
        // Mock successful response
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("""{"success": true, "ticketId": "ticket-456"}""")
            .addHeader("Content-Type", "application/json"))
        
        val request = SupportRequest(
            email = "student@test.edu",
            domain = "test.edu",
            subject = "License Request",
            message = "I need help with my license",
            priority = Priority.NORMAL
        )
        
        val response = n8nClient.triggerSupportRequest(request)
        
        assertTrue(response.success)
        assertEquals("Workflow triggered successfully", response.message)
        assertEquals("ticket-456", response.data["ticketId"])
    }
    
    fun testHealthCheckSuccess() {
        // Mock health check endpoint
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody("OK"))
        
        val isHealthy = n8nClient.healthCheck()
        
        assertTrue(isHealthy)
    }
    
    fun testHealthCheckFailure() {
        // Mock health check failure
        mockServer.enqueue(MockResponse()
            .setResponseCode(503)
            .setBody("Service Unavailable"))
        
        val isHealthy = n8nClient.healthCheck()
        
        assertFalse(isHealthy)
    }
}