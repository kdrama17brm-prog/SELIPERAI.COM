package com.example.domain

import com.example.data.model.ConnectedToolExecution
import com.example.data.model.FtaTopic
import com.example.data.model.SeliperMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Safety & Policy Guardrail Engine (Section 12 & 13)
 * Rejects illegal activities, malware, credential theft, fraud, violence, or harmful wrongdoing
 * clearly and offers a safe educational alternative.
 */
object SafetyPolicyEngine {

    data class SafetyCheckResult(
        val isSafe: Boolean,
        val refusalMessage: String? = null
    )

    private val prohibitedPatterns = listOf(
        Regex("\\b(build|create|write|deploy)\\b.*\\b(malware|ransomware|keylogger|trojan|rootkit|spyware|exploit payload)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(steal|phish|harvest|crack|brute\\s*force)\\b.*\\b(passwords?|credentials?|credit\\s*cards?|bank\\s*accounts?|session\\s*tokens?)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(how\\s+to\\s+make|synthesize|manufacture)\\b.*\\b(bomb|explosive|chemical\\s+weapon|illegal\\s+drugs|fentanyl|methamphetamine)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(commit|execute)\\b.*\\b(identity\\s+theft|wire\\s+fraud|tax\\s+evasion|money\\s+laundering)\\b", RegexOption.IGNORE_CASE),
        Regex("\\b(self-harm|suicide\\s+instructions|how\\s+to\\s+kill)\\b", RegexOption.IGNORE_CASE)
    )

    fun evaluateUserPrompt(prompt: String): SafetyCheckResult {
        val trimmed = prompt.trim()
        for (pattern in prohibitedPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                return SafetyCheckResult(
                    isSafe = false,
                    refusalMessage = """
                        ### Safety Protection Notice — SELIPER AI
                        I cannot fulfill this request. **SELIPER AI** (owned by **BISWARUP MONDAL (BRM)**) strictly prohibits assistance with illegal activities, malware creation, credential theft, fraud, violence, or harmful wrongdoing.
                        
                        **Safe Alternative:**
                        If you are studying cybersecurity, law, or defensive system engineering, I can gladly explain **defensive security principles**, **encryption standards**, **input validation**, or **how organizations protect systems against threats** from an ethical and educational perspective.
                    """.trimIndent()
                )
            }
        }
        return SafetyCheckResult(isSafe = true)
    }
}

/**
 * Connected Local Agent Tools (Section 3 Mode 4 & Section 13 Reliability)
 * IMPORTANT: Only executes genuinely connected local tools and logs exact inputs/outputs.
 * Never claims web search or code execution when not performed.
 */
object SeliperAgentTools {

    fun runConnectedToolsIfNeeded(
        prompt: String,
        attachedFileName: String?,
        attachedFileContent: String?,
        hasAttachedImage: Boolean,
        agentToolsEnabled: Boolean
    ): List<ConnectedToolExecution> {
        if (!agentToolsEnabled) return emptyList()
        val executions = mutableListOf<ConnectedToolExecution>()

        // 1. Connected Tool: Math & Expression Calculator
        val mathCandidate = extractMathExpression(prompt)
        if (mathCandidate != null) {
            val start = System.currentTimeMillis()
            val evalResult = evaluateMathExpressionSafely(mathCandidate)
            if (evalResult != null) {
                executions.add(
                    ConnectedToolExecution(
                        toolName = "SELIPER Math & Formula Evaluator (Local Connected Tool)",
                        toolInput = mathCandidate,
                        toolOutput = evalResult,
                        executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                    )
                )
            }
        }

        // 2. Connected Tool: Compound Interest / Financial Growth Calculator
        val compoundResult = tryEvaluateCompoundInterest(prompt)
        if (compoundResult != null) {
            executions.add(compoundResult)
        }

        // 3. Connected Tool: Document / Text Statistics & Readability Inspector
        if (!attachedFileContent.isNullOrBlank()) {
            val start = System.currentTimeMillis()
            val words = attachedFileContent.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            val lines = attachedFileContent.lines()
            val chars = attachedFileContent.length
            val readingMinutes = (words.size / 200.0).let { String.format(Locale.US, "%.1f min", it) }
            executions.add(
                ConnectedToolExecution(
                    toolName = "SELIPER Document Inspector (Connected File Tool)",
                    toolInput = "File: ${attachedFileName ?: "Attached Text"}",
                    toolOutput = "Characters: $chars | Words: ${words.size} | Lines: ${lines.size} | Est. Reading Time: $readingMinutes",
                    executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                )
            )
        } else if (prompt.length > 240 && (prompt.contains("analyze", ignoreCase = true) || prompt.contains("summarize", ignoreCase = true) || prompt.contains("word count", ignoreCase = true))) {
            val start = System.currentTimeMillis()
            val words = prompt.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            val sentences = prompt.split(Regex("[.!?]+")).filter { it.isNotBlank() }
            executions.add(
                ConnectedToolExecution(
                    toolName = "SELIPER Text Metrics Analyzer (Local Connected Tool)",
                    toolInput = "User input text (${prompt.length} chars)",
                    toolOutput = "Word count: ${words.size} | Sentence count: ${sentences.size} | Avg words/sentence: ${if (sentences.isNotEmpty()) words.size / sentences.size else words.size}",
                    executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                )
            )
        }

        // 4. Connected Tool: Device System Clock & Timezone Inspector (when time/date/schedule is asked)
        if (Regex("\\b(time|date|today|schedule|plan|day|calendar|deadline)\\b", RegexOption.IGNORE_CASE).containsMatchIn(prompt)) {
            val start = System.currentTimeMillis()
            val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy — HH:mm:ss z", Locale.US)
            val nowFormatted = formatter.format(Date())
            executions.add(
                ConnectedToolExecution(
                    toolName = "SELIPER Device Time & Schedule Context (Local Connected Tool)",
                    toolInput = "System.currentTimeMillis()",
                    toolOutput = "Current Local Device Time: $nowFormatted",
                    executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                )
            )
        }

        return executions
    }

    private fun extractMathExpression(prompt: String): String? {
        val explicitRegex = Regex("([0-9]+(?:\\.[0-9]+)?\\s*[+\\-*/^]\\s*[0-9()+\\-*/.^\\s]+)")
        val match = explicitRegex.find(prompt) ?: return null
        val candidate = match.value.trim()
        return if (candidate.length >= 3 && candidate.any { it in "+-*/^" }) candidate else null
    }

    private fun tryEvaluateCompoundInterest(prompt: String): ConnectedToolExecution? {
        if (!prompt.contains("compound", ignoreCase = true) && !prompt.contains("interest", ignoreCase = true)) {
            return null
        }
        val start = System.currentTimeMillis()
        val amountMatch = Regex("\\$([0-9,]+(?:\\.[0-9]+)?)").find(prompt)
        val rateMatch = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*%").find(prompt)
        val yearsMatch = Regex("([0-9]+)\\s*years?", RegexOption.IGNORE_CASE).find(prompt)

        if (amountMatch != null && rateMatch != null && yearsMatch != null) {
            val principal = amountMatch.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
            val ratePct = rateMatch.groupValues[1].toDoubleOrNull() ?: return null
            val years = yearsMatch.groupValues[1].toIntOrNull() ?: return null
            if (years in 1..100) {
                val finalAmount = principal * (1.0 + ratePct / 100.0).pow(years.toDouble())
                val profit = finalAmount - principal
                return ConnectedToolExecution(
                    toolName = "SELIPER Financial Compounding Engine (Local Connected Tool)",
                    toolInput = "Principal=$principal, Rate=$ratePct%, Years=$years (Annual Compounding)",
                    toolOutput = String.format(
                        Locale.US,
                        "Final Balance: $%,.2f | Total Interest Earned: $%,.2f",
                        finalAmount,
                        profit
                    ),
                    executionTimeMs = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                )
            }
        }
        return null
    }

    /**
     * Recursive descent parser for mathematical expressions (+, -, *, /, ^, parentheses, sqrt, sin, cos, tan, ln)
     */
    fun evaluateMathExpressionSafely(expr: String): String? {
        return try {
            val clean = expr.replace(",", "").trim()
            val result = object {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < clean.length) clean[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < clean.length) throw IllegalArgumentException("Unexpected char")
                    return x
                }

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        when {
                            eat('+'.code) -> x += parseTerm()
                            eat('-'.code) -> x -= parseTerm()
                            else -> return x
                        }
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        when {
                            eat('*'.code) -> x *= parseFactor()
                            eat('/'.code) -> {
                                val denom = parseFactor()
                                if (denom == 0.0) throw ArithmeticException("Division by zero")
                                x /= denom
                            }
                            else -> return x
                        }
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = clean.substring(startPos, pos).toDouble()
                    } else if (ch in 'a'.code..'z'.code) {
                        while (ch in 'a'.code..'z'.code) nextChar()
                        val func = clean.substring(startPos, pos)
                        x = parseFactor()
                        x = when (func) {
                            "sqrt" -> sqrt(x)
                            "sin" -> sin(Math.toRadians(x))
                            "cos" -> cos(Math.toRadians(x))
                            "tan" -> tan(Math.toRadians(x))
                            "ln" -> ln(x)
                            else -> throw IllegalArgumentException("Unknown function: $func")
                        }
                    } else {
                        throw IllegalArgumentException("Unexpected character")
                    }

                    if (eat('^'.code)) x = x.pow(parseFactor())
                    return x
                }
            }.parse()

            if (result.isNaN() || result.isInfinite()) null
            else if (result == result.toLong().toDouble()) result.toLong().toString()
            else String.format(Locale.US, "%.6f", result).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * System Instruction Builder for SELIPER AI (Sections 1, 3, 4, 9, 12, 13)
 */
object SeliperPromptArchitect {

    fun buildSystemPrompt(
        mode: SeliperMode,
        ftaTopic: FtaTopic,
        language: String,
        kingThinkingEnabled: Boolean,
        toolExecutions: List<ConnectedToolExecution>,
        hasAttachedFile: Boolean,
        hasAttachedImage: Boolean
    ): String {
        val modeSpecificDirective = when (mode) {
            SeliperMode.HOMEWORK_FRIEND -> """
                ACTIVE MODE: MODE 1 — HOMEWORK FRIEND MODE (Student Learning & Revision Tutor)
                - Your goal is to help students genuinely learn and understand.
                - Explain difficult subjects simply across Mathematics, Science, English, History, Geography, and Computer Science.
                - Provide clear step-by-step solutions and concrete examples.
                - Do NOT simply encourage cheating or hand over blind answers without teaching. Always explain the underlying concept and how to solve the problem step-by-step.
                - Proactively offer a short practice question, mini-quiz, or revision tip at the end to test the student's understanding.
            """.trimIndent()

            SeliperMode.HELPER -> """
                ACTIVE MODE: MODE 2 — HELPER MODE (Everyday Personal & Productivity Assistant)
                - Be friendly, clear, practical, and action-oriented.
                - Excel at answering everyday questions, drafting polished messages/emails, making structured plans, creating actionable checklists, translating text accurately, summarizing complex information, and organizing tasks.
                - Use clean formatting (bullet points, numbered checklists, bold highlights, and markdown tables when comparing options).
            """.trimIndent()

            SeliperMode.HUMANS -> """
                ACTIVE MODE: MODE 3 — HUMANS MODE (Natural, Empathetic, Human-Friendly Conversation)
                - Speak in a warm, natural, conversational, and empathetic tone.
                - Remember conversation context, adapt explanations to the user's comfort level, and avoid unnecessary jargon or overly robotic phrasing.
                - Ask a helpful clarifying question when the user's goal is open-ended.
                - IMPORTANT: Do NOT pretend to literally be a biological human; you are SELIPER AI speaking in a natural, human-friendly way.
            """.trimIndent()

            SeliperMode.SUPER_AGENT -> """
                ACTIVE MODE: MODE 4 — SUPER AI AGENT / SELIPER AI AGENT
                - You are the advanced multi-step reasoning and tool-assisted agent of SELIPER AI.
                - Break complex requests into clear, structured phases (1. Objective & Scope, 2. Step-by-Step Execution Plan, 3. Detailed Output / Analysis, 4. Verification & Next Actions).
                - Use results from genuinely connected tools provided in the prompt context and explicitly explain what tools were used.
                - CRITICAL RELIABILITY RULE: Only reference tools that were actually executed in the Connected Tool Log below. NEVER invent search results, external web fetches, files, or code executions that did not happen.
            """.trimIndent()

            SeliperMode.FTA -> """
                ACTIVE MODE: MODE 5 — FTA MODE (FOCUS TOPIC AGENT)
                - Active Focus Topic: **${ftaTopic.displayName}** (${ftaTopic.badge})
                - Directive: ${ftaTopic.focusDirective}
                - Keep all explanations, examples, frameworks, and terminology anchored around **${ftaTopic.displayName}**. If the user asks a general question, answer it thoroughly while highlighting perspectives relevant to ${ftaTopic.displayName}.
            """.trimIndent()
        }

        val thinkingDirective = if (kingThinkingEnabled) {
            """
                "KING OF BEST THINKING" ARCHITECTURE:
                1. Understand the user's core intent and constraints.
                2. Identify the exact task and required output format.
                3. Break complicated tasks into logical internal steps and verify calculations/facts for obvious errors before responding.
                4. Do NOT expose messy raw internal chain-of-thought; instead, begin complex answers with a concise `> **Reasoning Summary:**` blockquote (1-2 sentences outlining your approach) followed by the clearest, best-structured final answer.
            """.trimIndent()
        } else {
            "Provide clear, accurate, well-structured responses."
        }

        val toolContextBlock = if (toolExecutions.isNotEmpty()) {
            buildString {
                appendLine("\nGENUINELY EXECUTED LOCAL AGENT TOOLS FOR THIS TURN:")
                toolExecutions.forEachIndexed { idx, tool ->
                    appendLine("${idx + 1}. Tool: ${tool.toolName}")
                    appendLine("   Input: ${tool.toolInput}")
                    appendLine("   Verified Output: ${tool.toolOutput} (${tool.executionTimeMs}ms)")
                }
                appendLine("Incorporate these verified tool outputs into your answer and briefly mention the tool used.")
            }
        } else {
            "\nTOOL STATUS FOR THIS TURN: No external web search or external code execution tool was invoked for this turn. Do NOT claim 'I searched the internet' or 'I executed the code'."
        }

        val attachmentNotice = buildString {
            if (hasAttachedFile) appendLine("- The user attached a document/text file in this message; you HAVE actually read its contents provided in the prompt.")
            else appendLine("- No document file was attached in this turn; do NOT claim 'I opened your file' unless referencing a file explicitly included in the conversation.")
            if (hasAttachedImage) appendLine("- The user attached an image in this turn; analyze the visual content directly.")
        }

        return """
            You are **SELIPER AI**, a fast, safe, modern, multi-mode AI assistant and agent.
            - **AI Name:** SELIPER AI
            - **Owner & Creator:** BISWARUP MONDAL (BRM)
            - **Identity Rule:** You are an original AI product called SELIPER AI owned by BISWARUP MONDAL (BRM). Do NOT claim to be ChatGPT, Gemini, or Grok. If asked who created or owns you, state that you are **SELIPER AI**, owned by **BISWARUP MONDAL (BRM)**.
            - **Preferred Response Language:** $language (unless the user explicitly asks to translate into another language).
            
            $modeSpecificDirective
            
            $thinkingDirective
            
            RELIABILITY & HONESTY RULES:
            - Never invent facts, citations, API results, or files.
            - When uncertain about a real-time or unverifiable fact, state your uncertainty clearly and ask for clarification if helpful.
            - Never claim "I searched the internet", "I opened your file", or "I executed the code" unless that specific tool was genuinely used in this turn.
            $attachmentNotice
            $toolContextBlock
            
            SAFETY RULES:
            - Never assist with illegal activities, malware, credential theft, fraud, violence, or harmful wrongdoing. Refuse unsafe requests clearly and offer safe educational alternatives.
        """.trimIndent()
    }
}
