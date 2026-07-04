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
 * Design notes (these match the AAR currently shipped inside DinoSpace -
 * keep them if you rebuild, or the app will regress):
 *
 *  - A FRESH Conversation is created for every question and closed right
 *    after. Reusing one Conversation makes the context fill up with every
 *    past prompt, which slows answers down and eventually breaks them.
 *    The DinoSpace app injects any needed chat history into the prompt
 *    itself, so the engine must stay stateless.
 *
 *  - NO system prompt or prompt rewriting happens here. The C# side builds
 *    the complete prompt (rules, retrieved facts, history) and it must
 *    reach the model untouched.
 */
class Gemma4Engine(private val context: Context) {

    /** Token-by-token streaming callbacks (mirrors StreamCallback). */
    interface StreamListener {
        fun onToken(token: String)
        fun onDone()
        fun onError(error: String)
    }

    private var engine: Engine? = null

    private fun getModelPath(): String {
        // DinoSpace places the assembled model at this exact path.
        return File(context.filesDir, "NovaSaur.litertlm").absolutePath
    }

    fun initialize() {
        if (engine != null) return

        val config = EngineConfig(
            modelPath = getModelPath(),
            backend = Backend.CPU(),
            cacheDir = context.cacheDir.absolutePath,
            // Prompt + answer budget. Kept moderate: small enough to hold down
            // the per-conversation KV-cache memory (the 2048 default was
            // exhausting memory and stalling the engine after a few questions),
            // but large enough that the model still initialises and answers
            // reliably (768 was too tight and broke the first inference).
            maxNumTokens = 1536
        )

        val newEngine = Engine(config)
        newEngine.initialize()
        engine = newEngine
    }

    // Lower temperature = the model sticks closer to the facts the DinoSpace
    // app injects in its prompt, which is exactly what a kids' encyclopedia
    // answer needs. 0.5 keeps answers factual and steady without sounding
    // robotic. topK/topP trim off the unlikely, off-topic tokens.
    // If your LiteRT-LM version names these differently, you can delete this
    // config and call createConversation() with no arguments.
    private fun conversationConfig() = ConversationConfig(
        samplerConfig = SamplerConfig(topK = 40, topP = 0.9, temperature = 0.5)
    )

    /** Blocking: returns the full answer for one prompt. */
    fun reply(prompt: String): String {
        val e = engine ?: error("Engine not initialized")
        val conversation = e.createConversation(conversationConfig())
        try {
            return conversation.sendMessage(prompt).toString()
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

    fun close() {
        engine?.close()
        engine = null
    }
}
