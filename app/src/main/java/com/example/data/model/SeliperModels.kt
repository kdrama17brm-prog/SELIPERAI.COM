package com.example.data.model

import kotlinx.serialization.Serializable

/**
 * The Five Main SELIPER AI Modes
 */
@Serializable
enum class SeliperMode(
    val id: String,
    val title: String,
    val shortTitle: String,
    val subtitle: String,
    val description: String,
    val samplePrompts: List<String>
) {
    HOMEWORK_FRIEND(
        id = "homework_friend",
        title = "Homework Friend Mode",
        shortTitle = "Homework Friend",
        subtitle = "Step-by-step student learning & revision tutor",
        description = "Explains difficult subjects simply (Math, Science, English, History, Geography), provides step-by-step teaching without encouraging cheating, creates quizzes, study plans, and tests understanding.",
        samplePrompts = listOf(
            "Explain quadratic equations step-by-step for a beginner and quiz me",
            "Create a 5-day revision study plan for photosynthesis & cell biology",
            "Help me understand the causes of the French Revolution with simple examples",
            "Teach me how to analyze a poem's metaphor and rhythm without writing my essay"
        )
    ),
    HELPER(
        id = "helper",
        title = "Helper Mode",
        shortTitle = "Helper",
        subtitle = "Everyday productivity, writing, planning & checklists",
        description = "Friendly, clear, and practical assistance for everyday questions, writing messages/emails, making plans, creating checklists, translating text, summarizing, and organizing tasks.",
        samplePrompts = listOf(
            "Write a polite, professional email requesting a project deadline extension",
            "Create an organized packing and preparation checklist for a 4-day business trip",
            "Summarize key productivity habits into an actionable daily routine",
            "Translate 'Thank you for your collaboration, let us review the schedule tomorrow' into Spanish, French, and Hindi"
        )
    ),
    HUMANS(
        id = "humans",
        title = "Humans Mode",
        shortTitle = "Humans Mode",
        subtitle = "Natural, empathetic, context-aware conversation",
        description = "Warm, natural, and respectful human-friendly dialogue that adapts to your level, avoids jargon, remembers conversation context, and asks thoughtful clarifying questions without pretending to be literally human.",
        samplePrompts = listOf(
            "I'm feeling overwhelmed with learning too many new skills at once. Let's talk it through.",
            "Explain how cloud computing works in plain, everyday language",
            "Help me brainstorm a thoughtful gift idea for a friend who loves astronomy",
            "Let's have a friendly chat about balancing deep work and rest"
        )
    ),
    SUPER_AGENT(
        id = "super_agent",
        title = "Super AI Agent Mode",
        shortTitle = "Super Agent",
        subtitle = "Multi-step reasoning, connected tools & structured analysis",
        description = "Advanced SELIPER AI Agent that breaks complex tasks into clear steps, uses genuinely connected tools (Math Evaluator, Text/Document Analyzer, Study & Task Planner, Device Clock), and never fabricates tool execution.",
        samplePrompts = listOf(
            "Calculate the compound growth of $12,500 at 8.4% annual interest over 6 years and build a structured milestone table",
            "Break down a complete mobile app launch strategy into 4 phases with risk analysis",
            "Analyze the attached text/document and extract key metrics, action items, and a structured summary",
            "Run a step-by-step comparison of SQL vs NoSQL architectures for a real-time analytics platform"
        )
    ),
    FTA(
        id = "fta",
        title = "FTA Mode (Focus Topic Agent)",
        shortTitle = "FTA Mode",
        subtitle = "Dedicated domain-locked specialist for your chosen topic",
        description = "Focus Topic Agent (FTA) locks SELIPER AI into a specialized discipline (Coding, Mathematics, Science, Business, Education, Writing, Research, Technology, or Content Creation). Switch your active FTA topic anytime.",
        samplePrompts = listOf(
            "Write a clean, thread-safe Kotlin Coroutines repository with retry logic",
            "Derive the integration by parts formula and show two worked examples",
            "Design a lean B2B SaaS go-to-market unit economics model",
            "Outline a high-retention technical tutorial script with hook and key takeaways"
        )
    )
}

/**
 * Topics for Mode 5 — Focus Topic Agent (FTA)
 */
@Serializable
enum class FtaTopic(
    val id: String,
    val displayName: String,
    val badge: String,
    val focusDirective: String,
    val samplePrompts: List<String>
) {
    CODING(
        id = "coding",
        displayName = "Coding FTA",
        badge = "CODE",
        focusDirective = "Act as a specialized Senior Software Architect and Coding FTA. Focus strictly on clean code, algorithms, debugging, system design, modern frameworks, security best practices, and well-commented code blocks.",
        samplePrompts = listOf(
            "Implement a debounced search flow in Kotlin StateFlow with unit test examples",
            "Explain time and space complexity of Dijkstra's algorithm with clean implementation"
        )
    ),
    MATHEMATICS(
        id = "math",
        displayName = "Mathematics FTA",
        badge = "MATH",
        focusDirective = "Act as a specialized Mathematics FTA. Focus on rigorous step-by-step proofs, algebra, calculus, linear algebra, probability, statistics, and clear mathematical notation and verification.",
        samplePrompts = listOf(
            "Solve the system of linear equations using Gaussian elimination step-by-step",
            "Explain Bayes' Theorem with a clear numerical medical-testing example"
        )
    ),
    SCIENCE(
        id = "science",
        displayName = "Science FTA",
        badge = "SCI",
        focusDirective = "Act as a specialized Science FTA covering Physics, Chemistry, Biology, and Earth Science. Focus on empirical principles, mechanisms, real-world experiments, and accurate scientific explanations.",
        samplePrompts = listOf(
            "Explain how CRISPR-Cas9 gene editing works at the molecular level",
            "Compare classical thermodynamics with statistical mechanics clearly"
        )
    ),
    BUSINESS(
        id = "business",
        displayName = "Business FTA",
        badge = "BIZ",
        focusDirective = "Act as a specialized Business & Strategy FTA. Focus on market analysis, unit economics, operations, product management, financial modeling, leadership, and actionable execution frameworks.",
        samplePrompts = listOf(
            "Build a unit economics breakdown comparing CAC, LTV, and payback period",
            "Create a 90-day operational roadmap for launching a new digital service"
        )
    ),
    EDUCATION(
        id = "education",
        displayName = "Education FTA",
        badge = "EDU",
        focusDirective = "Act as a specialized Education & Pedagogy FTA. Focus on curriculum design, active recall, Bloom's taxonomy, lesson planning, rubric creation, and adaptive student learning.",
        samplePrompts = listOf(
            "Design a 45-minute active-learning lesson plan on renewable energy",
            "Create a 10-question diagnostic quiz with answer explanations on fractions"
        )
    ),
    WRITING(
        id = "writing",
        displayName = "Writing FTA",
        badge = "WRITE",
        focusDirective = "Act as a specialized Writing & Editorial FTA. Focus on clarity, tone, structure, rhetoric, storytelling, copywriting, grammar refinement, and persuasive prose.",
        samplePrompts = listOf(
            "Rewrite this rough paragraph into crisp, executive-ready prose",
            "Create a compelling narrative arc for a short sci-fi story about deep space"
        )
    ),
    RESEARCH(
        id = "research",
        displayName = "Research FTA",
        badge = "R&D",
        focusDirective = "Act as a specialized Research Methodology & Synthesis FTA. Focus on structured literature synthesis, hypothesis formulation, critical evaluation of evidence, methodology design, and objective analysis.",
        samplePrompts = listOf(
            "Design a rigorous experimental methodology to test UI readability across themes",
            "Structure a systematic review outline on battery energy density trends"
        )
    ),
    TECHNOLOGY(
        id = "technology",
        displayName = "Technology FTA",
        badge = "TECH",
        focusDirective = "Act as a specialized Technology & Systems FTA. Focus on cloud infrastructure, AI systems, hardware architecture, networking, cybersecurity, and emerging tech trade-offs.",
        samplePrompts = listOf(
            "Compare edge computing vs cloud inference for mobile AI applications",
            "Explain zero-trust network architecture and how to implement it practically"
        )
    ),
    CONTENT_CREATION(
        id = "content",
        displayName = "Content Creation FTA",
        badge = "CREATE",
        focusDirective = "Act as a specialized Content Creation & Media Strategy FTA. Focus on audience retention hooks, scriptwriting, content calendars, storytelling structure, and multi-platform repurposing.",
        samplePrompts = listOf(
            "Create a 30-day educational tech content calendar with hooks and formats",
            "Write a high-retention 3-minute video script explaining how neural networks learn"
        )
    )
}

/**
 * Multi-Model Provider Architecture
 */
@Serializable
enum class AiProvider(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val secretKeyName: String,
    val defaultModels: List<AiModelOption>
) {
    GOOGLE_GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        baseUrl = "https://generativelanguage.googleapis.com/",
        secretKeyName = "GEMINI_API_KEY",
        defaultModels = listOf(
            AiModelOption("gemini-3.5-flash", "Gemini 3.5 Flash", "Fastest everyday reasoning, multimodal & tools"),
            AiModelOption("gemini-3.1-pro-preview", "Gemini 3.1 Pro Preview", "Deepest reasoning, complex STEM, coding & agent workflows"),
            AiModelOption("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite", "Ultra-low latency quick responses")
        )
    ),
    OPENAI_COMPATIBLE(
        id = "openai",
        displayName = "OpenAI",
        baseUrl = "https://api.openai.com/",
        secretKeyName = "OPENAI_API_KEY",
        defaultModels = listOf(
            AiModelOption("gpt-4o-mini", "GPT-4o Mini", "Fast, lightweight multi-purpose model"),
            AiModelOption("gpt-4o", "GPT-4o", "High-intelligence reasoning and coding model")
        )
    ),
    XAI_GROK(
        id = "xai",
        displayName = "xAI Grok",
        baseUrl = "https://api.x.ai/",
        secretKeyName = "XAI_API_KEY",
        defaultModels = listOf(
            AiModelOption("grok-2-latest", "Grok 2 Latest", "Advanced reasoning and analysis via xAI REST API")
        )
    )
}

@Serializable
data class AiModelOption(
    val modelId: String,
    val displayName: String,
    val badgeDescription: String
)

/**
 * 7 Premium Background Themes
 */
@Serializable
enum class SeliperTheme(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val isDark: Boolean
) {
    DARK_FUTURISTIC("dark_futuristic", "Violet-Pink-Crimson Glow (Default)", "Vibrant mixture of deep violet, magenta-pink, and crimson-red gradient with deep bold white answers", true),
    LIGHT_MINIMAL("light_minimal", "Light Minimal", "Crisp alabaster daylight surface with deep bold white AI answer cards", false),
    DEEP_SPACE("deep_space", "Deep Space Violet", "Cosmic nebula violet & magenta glow with pure white typography", true),
    GLASSMORPHISM("glassmorphism", "Rose-Violet Glassmorphism", "Translucent frosted crystal layers over violet, pink, and red sunset gradients", true),
    AURORA("aurora", "Aurora Sunset", "Vibrant magenta, coral-red, and violet atmospheric glow", true),
    PROFESSIONAL("professional", "Executive Dark Slate", "High-contrast obsidian & ruby-violet accents with deep bold white text", true),
    AMOLED_DARK("amoled_dark", "AMOLED Pitch Black", "True #000000 OLED black with neon pink-violet-red accents and pure white text", true)
}

/**
 * Connected Local & Agent Tools metadata
 */
@Serializable
data class ConnectedToolExecution(
    val toolName: String,
    val toolInput: String,
    val toolOutput: String,
    val executionTimeMs: Long
)
