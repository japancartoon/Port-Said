package swot.customerservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client for integrating with n8n workflows for customer service automation
 */
class N8nClient(private val config: N8nConfig) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .build()
    
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    /**
     * Trigger domain verification workflow in n8n
     */
    fun triggerDomainVerification(request: DomainVerificationRequest): WorkflowResponse {
        return sendWebhookRequest(config.getDomainVerificationUrl(), request)
    }
    
    /**
     * Trigger support request workflow in n8n
     */
    fun triggerSupportRequest(request: SupportRequest): WorkflowResponse {
        return sendWebhookRequest(config.getSupportRequestUrl(), request)
    }
    
    /**
     * Send a generic webhook request to n8n
     */
    private fun sendWebhookRequest(url: String, payload: Any): WorkflowResponse {
        try {
            val json = objectMapper.writeValueAsString(payload)
            val requestBody = json.toRequestBody(jsonMediaType)
            
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
            
            // Add API key if configured
            config.apiKey?.let { apiKey ->
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            
            val httpRequest = requestBuilder.build()
            
            client.newCall(httpRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    val responseData = try {
                        objectMapper.readValue<Map<String, Any>>(responseBody)
                    } catch (e: Exception) {
                        mapOf("raw_response" to responseBody)
                    }
                    
                    return WorkflowResponse(
                        success = true,
                        message = "Workflow triggered successfully",
                        workflowId = responseData["workflowId"]?.toString(),
                        data = responseData
                    )
                } else {
                    return WorkflowResponse(
                        success = false,
                        message = "HTTP ${response.code}: ${response.message}",
                        workflowId = null
                    )
                }
            }
        } catch (e: IOException) {
            return WorkflowResponse(
                success = false,
                message = "Network error: ${e.message}",
                workflowId = null
            )
        } catch (e: Exception) {
            return WorkflowResponse(
                success = false,
                message = "Unexpected error: ${e.message}",
                workflowId = null
            )
        }
    }
    
    /**
     * Test connectivity to n8n instance
     */
    fun healthCheck(): Boolean {
        return try {
            val request = Request.Builder()
                .url("${config.baseUrl}/healthz")
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}