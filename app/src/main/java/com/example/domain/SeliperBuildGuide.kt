package com.example.domain

/**
 * Beginner-Friendly 10-Step Architecture & Build Walkthrough (Section 19 & 20)
 * Explains every step of SELIPER AI clearly for a beginner:
 * - What to click
 * - What to create
 * - What code/configuration to add
 * - Where to put it
 * - How to test it
 * - What result you should see
 */
data class BuildGuideStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val overview: String,
    val whatToClick: String,
    val whatToCreate: String,
    val codeOrConfigToAdd: String,
    val whereToPutIt: String,
    val howToTestIt: String,
    val expectedResult: String
)

object SeliperBuildGuideRepository {

    val allSteps: List<BuildGuideStep> = listOf(
        BuildGuideStep(
            stepNumber = 1,
            title = "STEP 1 — Project Architecture",
            subtitle = "Clean MVVM + Multi-Model Gateway + Local Room DB",
            overview = "SELIPER AI is architected in 4 clean layers so you can swap AI models, add tools, or change themes without rebuilding the app.",
            whatToClick = "In the AI Studio left file explorer, expand `app/src/main/java/com/example/` to inspect the 4 folders: `data/model`, `data/local`, `data/remote`, `domain`, and `ui`.",
            whatToCreate = "4 core packages:\n1. `data/model/SeliperModels.kt` (5 Modes, 9 FTA Topics, Providers, 7 Themes)\n2. `data/local/SeliperDatabase.kt` (Room SQLite for chats & messages)\n3. `data/remote/MultiModelGateway.kt` (Provider -> Model -> SELIPER AI Backend)\n4. `domain/SeliperEngine.kt` (Safety filter, King of Best Thinking prompt architect, & Connected Tools).",
            codeOrConfigToAdd = """
// Architecture Flow:
// User Input -> SafetyPolicyEngine -> SeliperAgentTools (Connected Tools)
// -> SeliperPromptArchitect (Mode + FTA Topic) -> MultiModelGateway (Gemini / OpenAI / xAI)
// -> Streaming Response -> Room Database -> Jetpack Compose UI
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/data/` and `app/src/main/java/com/example/domain/`",
            howToTestIt = "Tap the 'Confirm & Next Step' button below or open the top navigation drawer to see the live architecture status badge.",
            expectedResult = "You see SELIPER AI's modular structure ready to connect your real API key, modes, and chat storage."
        ),
        BuildGuideStep(
            stepNumber = 2,
            title = "STEP 2 — UI Structure",
            subtitle = "Mobile-First Navigation Drawer, Mode Bar, & Chat Workspace",
            overview = "A responsive, mobile-first layout built with Jetpack Compose and Material 3 that adapts smoothly to phones, foldables, and tablets.",
            whatToClick = "Tap the top-left menu icon (☰) to open the SELIPER AI Sidebar Drawer, or tap any mode chip at the top of the Home/Chat screen.",
            whatToCreate = "Main navigation container (`SeliperAppScreen.kt`) with:\n• Top Bar showing active Mode, FTA Topic, and API connection badge\n• Slide-out Drawer with New Chat, Search Conversations, History list, 10-Step Guide, Settings, and About BRM\n• Bottom Composer with Image Picker, File Attachment, and Send/Stop button.",
            codeOrConfigToAdd = """
ModalNavigationDrawer(drawerState = drawerState, drawerContent = { SeliperSidebar(...) }) {
    Scaffold(topBar = { SeliperTopBar(...) }, bottomBar = { SeliperMessageComposer(...) }) {
        // Landing Hero Screen or Active Chat Stream
    }
}
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/ui/SeliperAppScreen.kt`",
            howToTestIt = "Open the left sidebar drawer, switch between Chat, Settings, About, and this 10-Step Guide, and test the back button.",
            expectedResult = "Smooth transitions, large touch targets (48dp+), and clear branding for SELIPER AI."
        ),
        BuildGuideStep(
            stepNumber = 3,
            title = "STEP 3 — Real AI API Connection",
            subtitle = "Zero Fake Responses — Secure BuildConfig & AI Studio Secrets",
            overview = "SELIPER AI never uses fake or hardcoded demo AI answers. It connects directly to real AI providers using environment secrets injected via `BuildConfig`.",
            whatToClick = "In Google AI Studio, open the **Secrets** panel (key icon in the sidebar) and add `GEMINI_API_KEY` with your real Google Gemini API key.",
            whatToCreate = "Uncomment `GEMINI_API_KEY=MY_GEMINI_API_KEY` in `.env.example` and verify `MultiModelGateway.isProviderConnected(provider)` before every request.",
            codeOrConfigToAdd = """
// In .env.example:
GEMINI_API_KEY=MY_GEMINI_API_KEY

// In MultiModelGateway.kt:
if (!isProviderConnected(provider)) {
    return AiServiceResult.NotConnected(
        message = "AI service is not connected. Please configure the API."
    )
}
            """.trimIndent(),
            whereToPutIt = "`.env.example` (root folder) and `app/src/main/java/com/example/data/remote/MultiModelGateway.kt`",
            howToTestIt = "Send a message without a key configured to verify the exact notice: \"AI service is not connected. Please configure the API.\" Then add your `GEMINI_API_KEY` in Secrets to receive real streaming AI answers.",
            expectedResult = "Honest connection status badge (Connected vs Not Connected) and fast real SSE streaming from Gemini 3.5 Flash or Gemini 3.1 Pro."
        ),
        BuildGuideStep(
            stepNumber = 4,
            title = "STEP 4 — Chat Functionality",
            subtitle = "Streaming, Markdown, Code Copy, Edit, Regenerate, Stop & Search",
            overview = "Full conversation management backed by Room SQLite database so your chats are saved automatically on your phone.",
            whatToClick = "Send a message in the Chat tab, then use the action icons underneath any AI response: **Copy**, **Regenerate**, or tap **Edit** on your own message.",
            whatToCreate = "Rich Markdown & Code Block renderer (`MarkdownMessageRenderer.kt`) with one-tap **Copy Code** button, table rendering, plus conversation rename/delete/search in `SeliperViewModel.kt`.",
            codeOrConfigToAdd = """
// Supported Chat Actions in SeliperViewModel:
fun sendMessage(prompt: String)
fun stopGeneration()
fun regenerateLastResponse()
fun editUserMessageAndResubmit(messageId: Long, newText: String)
fun renameConversation(id: Long, newTitle: String)
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/ui/SeliperViewModel.kt` & `ui/components/MarkdownMessageRenderer.kt`",
            howToTestIt = "Ask SELIPER AI to write a Python or Kotlin function. Tap 'Copy Code' on the code block, test 'Regenerate', or search your chats in the sidebar.",
            expectedResult = "Formatted headings, bullet lists, tables, syntax boxes with Copy buttons, and instant search across saved chats."
        ),
        BuildGuideStep(
            stepNumber = 5,
            title = "STEP 5 — Five Main SELIPER AI Modes",
            subtitle = "Homework Friend • Helper • Humans • Super AI Agent • FTA Mode",
            overview = "Each mode reconfigures SELIPER AI's system instructions, reasoning style, and starter prompts.",
            whatToClick = "Tap any of the 5 Mode cards on the Home screen or the horizontal Mode selector bar at the top of the chat.",
            whatToCreate = "`SeliperMode` enum in `SeliperModels.kt` and `SeliperPromptArchitect.buildSystemPrompt(...)` in `SeliperEngine.kt`.",
            codeOrConfigToAdd = """
enum class SeliperMode {
    HOMEWORK_FRIEND, // Teaches step-by-step, quizzes student, no cheating
    HELPER,          // Emails, checklists, plans, translations, productivity
    HUMANS,          // Warm, empathetic, jargon-free natural conversation
    SUPER_AGENT,     // Multi-step planning + genuinely connected tools
    FTA              // Focus Topic Agent locked to your chosen discipline
}
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/domain/SeliperEngine.kt`",
            howToTestIt = "Select **Homework Friend Mode** and tap a sample math/science prompt. Then switch to **Helper Mode** or **Humans Mode** and compare the response style.",
            expectedResult = "Homework Friend guides and quizzes you step-by-step; Helper builds practical checklists; Humans Mode chats naturally and warmly."
        ),
        BuildGuideStep(
            stepNumber = 6,
            title = "STEP 6 — Super AI Agent & Connected Tools",
            subtitle = "Honest Tool Execution — Math Engine, Document Inspector & Vision",
            overview = "SELIPER AI Agent uses genuinely connected tools and displays a verified 'Connected Tool Execution Log' badge on the message whenever a tool runs.",
            whatToClick = "Switch to **Super Agent** mode, or tap the **Attach File** (📄) / **Attach Image** (🖼️) buttons in the message bar.",
            whatToCreate = "`SeliperAgentTools` in `SeliperEngine.kt` providing:\n1. Recursive Math & Formula Evaluator\n2. Compound Growth Financial Calculator\n3. Attached Document/Text Inspector\n4. Device Clock & Schedule Context\n5. Multimodal Image Vision via Android Photo Picker.",
            codeOrConfigToAdd = """
// Reliability Guarantee:
// SELIPER AI NEVER claims "I searched the internet", "I opened your file",
// or "I executed the code" unless that exact connected tool ran in this turn.
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/domain/SeliperEngine.kt`",
            howToTestIt = "Ask: `Calculate 12500 * (1 + 0.084)^6 and create a milestone plan` or attach a `.txt`/`.md` document or image.",
            expectedResult = "You see a green 'Verified Connected Tool Used' card showing the exact tool input, output, and millisecond execution time."
        ),
        BuildGuideStep(
            stepNumber = 7,
            title = "STEP 7 — FTA Mode (Focus Topic Agent)",
            subtitle = "9 Specialized Topics You Can Switch Anytime",
            overview = "When you select **FTA Mode**, a secondary Topic Selector appears with 9 domain specialists: Coding, Mathematics, Science, Business, Education, Writing, Research, Technology, and Content Creation.",
            whatToClick = "Select **FTA Mode** in the top bar, then tap any topic chip (e.g., **Coding FTA** or **Mathematics FTA**).",
            whatToCreate = "`FtaTopic` enum with 9 topics, specialized directives, and topic-specific quick prompts.",
            codeOrConfigToAdd = """
// Active FTA Topic is injected into the system prompt and saved in DataStore:
ACTIVE MODE: MODE 5 — FTA MODE (FOCUS TOPIC AGENT)
Active Focus Topic: Coding FTA (CODE)
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/data/model/SeliperModels.kt`",
            howToTestIt = "Select **FTA Mode**, tap **Coding FTA**, and ask a programming question. Then tap **Mathematics FTA** or **Business FTA** to switch topics immediately.",
            expectedResult = "The UI header badge updates to your chosen FTA topic and SELIPER AI focuses its terminology and examples around that discipline."
        ),
        BuildGuideStep(
            stepNumber = 8,
            title = "STEP 8 — 7 Premium Themes & Branding",
            subtitle = "Dark Futuristic, Light Minimal, Deep Space, Glassmorphism, Aurora, Professional, AMOLED",
            overview = "Original visual identity for **SELIPER AI** with 7 custom background themes and high-contrast readability.",
            whatToClick = "Open **Settings** (gear icon in top bar or sidebar) and tap any of the 7 Theme cards under **Premium Background Themes**.",
            whatToCreate = "`SeliperThemePalette` in `ui/theme/Theme.kt` with custom gradient brushes, surface colors, and contrast-checked text tokens for all 7 themes.",
            codeOrConfigToAdd = """
enum class SeliperTheme {
    DARK_FUTURISTIC, LIGHT_MINIMAL, DEEP_SPACE,
    GLASSMORPHISM, AURORA, PROFESSIONAL, AMOLED_DARK
}
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/ui/theme/Theme.kt`",
            howToTestIt = "Tap through **Dark Futuristic**, **Aurora Borealis**, **AMOLED Pitch Black**, and **Light Minimal** in Settings.",
            expectedResult = "The entire app background, cards, chat bubbles, and accents transform immediately while keeping every word crisp and readable."
        ),
        BuildGuideStep(
            stepNumber = 9,
            title = "STEP 9 — Safety, Testing & Error Fixing",
            subtitle = "Safety Guardrails + Beginner-Friendly Error Messages + Retry",
            overview = "Tests safety refusals, empty API key states, network errors, and unit tests for math evaluation and prompt generation.",
            whatToClick = "In Settings, review **Safety Protections** and **API Connection Diagnostics**, or tap **Retry** on any failed network message.",
            whatToCreate = "`SafetyPolicyEngine` in `SeliperEngine.kt` and beginner-friendly HTTP/network error mapping in `MultiModelGateway.kt`.",
            codeOrConfigToAdd = """
// Beginner-friendly error example:
"Something went wrong while connecting to the AI service. Please check your API configuration and try again."
            """.trimIndent(),
            whereToPutIt = "`app/src/main/java/com/example/domain/SeliperEngine.kt` & `app/src/test/java/com/example/ExampleUnitTest.kt`",
            howToTestIt = "Try testing a blocked harmful keyword to verify the safe refusal message, or test offline mode to see the clear Retry button.",
            expectedResult = "Unsafe requests are refused clearly with safe educational alternatives; network or key errors show plain-English explanations."
        ),
        BuildGuideStep(
            stepNumber = 10,
            title = "STEP 10 — Deployment & Export",
            subtitle = "Run on Android Phone, Export APK/AAB, or Push to GitHub",
            overview = "Your SELIPER AI Android application is ready to run in the AI Studio Streaming Emulator or export as an installable Android APK/AAB.",
            whatToClick = "Use the **AI Studio Settings / Export menu** at the top right of the workspace to **Download APK**, **Export ZIP**, or **Push to GitHub**.",
            whatToCreate = "Production security checklist: before publishing publicly to the Google Play Store, move API keys from client `BuildConfig` to a server-side proxy or Firebase AI App Check so keys cannot be extracted from decompiled APKs.",
            codeOrConfigToAdd = """
// Application ID configured in app/build.gradle.kts:
applicationId = "com.aistudio.seliperai.brmxqz"
// App Name: SELIPER AI
            """.trimIndent(),
            whereToPutIt = "`app/build.gradle.kts` & `metadata.json`",
            howToTestIt = "Open the **About SELIPER AI** screen from the sidebar to verify branding and system readiness.",
            expectedResult = "A complete, fast, safe, multi-mode SELIPER AI Android app."
        )
    )
}
