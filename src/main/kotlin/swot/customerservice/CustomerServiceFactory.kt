package swot.customerservice

/**
 * Factory for creating customer service instances with different configurations
 */
object CustomerServiceFactory {
    
    /**
     * Create a customer service manager with default configuration
     */
    fun createDefault(): CustomerServiceManager {
        val config = N8nConfig()
        val client = N8nClient(config)
        return CustomerServiceManager(client)
    }
    
    /**
     * Create a customer service manager with custom n8n configuration
     */
    fun create(config: N8nConfig): CustomerServiceManager {
        val client = N8nClient(config)
        return CustomerServiceManager(client)
    }
    
    /**
     * Create a customer service manager with custom n8n base URL
     */
    fun create(baseUrl: String, apiKey: String? = null): CustomerServiceManager {
        val config = N8nConfig(baseUrl = baseUrl, apiKey = apiKey)
        val client = N8nClient(config)
        return CustomerServiceManager(client)
    }
    
    /**
     * Create a customer service manager for testing with mock configuration
     */
    fun createForTesting(mockBaseUrl: String = "http://localhost:5678"): CustomerServiceManager {
        val config = N8nConfig(
            baseUrl = mockBaseUrl,
            timeout = 5000L // Shorter timeout for testing
        )
        val client = N8nClient(config)
        return CustomerServiceManager(client)
    }
}