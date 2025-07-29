package swot.customerservice

import junit.framework.TestCase

class CustomerServiceFactoryTest : TestCase() {
    
    fun testCreateDefault() {
        val customerService = CustomerServiceFactory.createDefault()
        
        assertNotNull(customerService)
        
        // Test health check to ensure it's properly configured
        val healthResult = customerService.healthCheck()
        assertNotNull(healthResult)
        assertTrue(healthResult.swotLibrary) // Swot library should always be healthy
    }
    
    fun testCreateWithConfig() {
        val config = N8nConfig(
            baseUrl = "https://custom-n8n.example.com",
            apiKey = "test-api-key",
            timeout = 10000L
        )
        
        val customerService = CustomerServiceFactory.create(config)
        
        assertNotNull(customerService)
        
        // Verify the configuration is used (health check will fail for non-existent URL, which is expected)
        val healthResult = customerService.healthCheck()
        assertNotNull(healthResult)
        assertFalse(healthResult.n8nConnectivity) // Expected to fail for non-existent URL
        assertTrue(healthResult.swotLibrary)
    }
    
    fun testCreateWithBaseUrl() {
        val customerService = CustomerServiceFactory.create(
            baseUrl = "https://my-n8n.example.com",
            apiKey = "my-api-key"
        )
        
        assertNotNull(customerService)
        
        // Test basic functionality
        val healthResult = customerService.healthCheck()
        assertNotNull(healthResult)
        assertTrue(healthResult.swotLibrary)
    }
    
    fun testCreateForTesting() {
        val customerService = CustomerServiceFactory.createForTesting()
        
        assertNotNull(customerService)
        
        // Testing configuration should have shorter timeouts
        val healthResult = customerService.healthCheck()
        assertNotNull(healthResult)
        assertTrue(healthResult.swotLibrary)
    }
    
    fun testCreateForTestingWithCustomUrl() {
        val customerService = CustomerServiceFactory.createForTesting("http://localhost:9999")
        
        assertNotNull(customerService)
        
        val healthResult = customerService.healthCheck()
        assertNotNull(healthResult)
        assertTrue(healthResult.swotLibrary)
        assertFalse(healthResult.n8nConnectivity) // Expected to fail for non-existent URL
    }
}