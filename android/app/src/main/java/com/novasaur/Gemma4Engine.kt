package com.novasaur

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import java.io.File

/**
 * Thin wrapper around LiteRT-LM for the NovaSaur model.
 *
 * Design notes (these match the AAR shipped inside DinoSpace - keep them
 * if you rebuild, or the app will regress):
 *
 *  - Every question is INDEPENDENT. A fresh Conversation is created per
 *    question, and after the answer is delivered the whole engine is torn
 *    down and reloaded (see [reset]). LiteRT-LM draws every conversation
 *    from one shared token budget (maxNumTokens), so without the reload
 *    the budget runs dry after roughly three answers and the engine
 *    simply stops responding. A reload starts the budget from zero every
 *    time: question 50 behaves exactly like question 1.
 *
 *  - NO system prompt or prompt rewriting happens here. The C# side builds
 *    the complete prompt and it must reach the model untouched.
 *
 *  - One inference at a time. The caller (DinoSpace serialises calls with
 *    a semaphore) must never overlap two inferences or an inference with
 *    a reset; [reset] and [initialize] are synchronized here as a second
 *    line of defence.
 */
class Gemma4Engine(private val context: Context) {

    /** Token-by-token streaming callbacks (mirrors StreamCallback). */
    interface StreamListener {
        fun onToken(token: String)
        fun onDone()
        fun onError(error: String)
    }

    @Volatile
    private var engine: Engine? = null

    /** How many answers the current engine instance has produced. */
    @Volatile
    var answersSinceLoad: Int = 0
        private set

    private fun getModelPath(): String {
        // DinoSpace places the assembled model at this exact path.
        return File(context.filesDir, "NovaSaur.litertlm").absolutePath
    }

    @Synchronized
    fun initialize() {
        if (engine != null) return

        val config = EngineConfig(
            modelPath = getModelPath(),
            backend = Backend.CPU(),
            cacheDir = context.cacheDir.absolutePath,
            // Prompt + answer budget for ONE question. The engine reloads
            // after every answer, so this only has to fit a single prompt
            // and reply - big enough for a long creative answer, small
            // enough to keep the KV cache light on low-RAM phones.
            maxNumTokens = 1536
        )

        val newEngine = Engine(config)
        newEngine.initialize()
        engine = newEngine
        answersSinceLoad = 0
    }

    /**
     * Full teardown + reload. Called after each answer so the next question
     * starts against a clean engine with an untouched token budget. Blocking
     * (the model reload takes a while) - run it off the UI thread, and never
     * concurrently with an inference.
     */
    @Synchronized
    fun reset() {
        try { engine?.close() } catch (_: Exception) {}
        engine = null
        initialize()
    }

    fun isLoaded(): Boolean = engine != null

    // Lower temperature = steadier, more factual answers, which is what a
    // kids' encyclopedia needs. topK/topP trim off the unlikely, off-topic
    // tokens. If your LiteRT-LM version names these differently, delete this
    // config and call createConversation() with no arguments.
    private fun conversationConfig() = ConversationConfig(
        samplerConfig = SamplerConfig(topK = 40, topP = 0.9, temperature = 0.5)
    )

    /** Blocking: returns the full answer for one prompt. */
    fun reply(prompt: String): String {
        val e = engine ?: error("Engine not initialized")
        val conversation = e.createConversation(conversationConfig())
        try {
            val answer = conversation.sendMessage(prompt).toString()
            answersSinceLoad++
            return answer
        } finally {
            try { conversation.close() } catch (_: Exception) {}
        }
    }

    /** Streaming: delivers the answer token by token. */
    fun replyStream(prompt: String, listener: StreamListener) {
        val e = engine ?: error("Engine not initialized")
        val conversation = e.createConversation(conversationConfig())

        val callback = object : MessageCallback {
            override fun onMessage(message: Message) {
                listener.onToken(message.toString())
            }

            override fun onDone() {
                answersSinceLoad++
                try { conversation.close() } catch (_: Exception) {}
                listener.onDone()
            }

            override fun onError(error: Throwable) {
                try { conversation.close() } catch (_: Exception) {}
                listener.onError(error.message ?: "Unknown error")
            }
        }

        conversation.sendMessageAsync(prompt, callback)
    }

    @Synchronized
    fun close() {
        try { engine?.close() } catch (_: Exception) {}
        engine = null
    }
}
