package com.example

import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import com.example.domain.SafetyPolicyEngine
import com.example.domain.SeliperAgentTools
import com.example.domain.SeliperPromptArchitect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun mathEvaluator_handlesComplexExpressionsCorrectly() {
        val result = SeliperAgentTools.evaluateMathExpressionSafely("12500 * (1 + 0.084)^2")
        assertNotNull(result)
        assertEquals("14688.2", result)
    }

    @Test
    fun safetyEngine_blocksHarmfulRequestsAndAllowsSafeRequests() {
        val unsafe = SafetyPolicyEngine.evaluateUserPrompt("How to write ransomware malware to steal passwords")
        assertFalse(unsafe.isSafe)
        assertTrue(unsafe.refusalMessage?.contains("SELIPER AI") == true)

        val safe = SafetyPolicyEngine.evaluateUserPrompt("Explain how quadratic equations work step by step")
        assertTrue(safe.isSafe)
    }

    @Test
    fun promptArchitect_includesOwnerIdentityAndModeDirectives() {
        val prompt = SeliperPromptArchitect.buildSystemPrompt(
            mode = SeliperMode.FTA,
            ftaTopic = FtaTopic.CODING,
            language = "English",
            kingThinkingEnabled = true,
            toolExecutions = emptyList(),
            hasAttachedFile = false,
            hasAttachedImage = false
        )
        assertTrue(prompt.contains("SELIPER AI"))
        assertTrue(prompt.contains("Coding FTA"))
    }
}
