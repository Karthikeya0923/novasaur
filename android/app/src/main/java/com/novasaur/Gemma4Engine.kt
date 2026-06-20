package com.novasaur

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import java.io.File

class Gemma4Engine(private val context: Context) {

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    private fun getModelPath(): String {
        return File(context.filesDir, "NovaSaur.litertlm").absolutePath
    }


    fun initialize() {

        if (conversation != null) {
            return
        }


        if (engine == null) {

            val config = EngineConfig(

                modelPath = getModelPath(),

                backend = Backend.CPU(),

                cacheDir = context.cacheDir.absolutePath
            )


            val newEngine = Engine(config)

            newEngine.initialize()

            engine = newEngine
        }


        if (conversation == null) {

            conversation = engine!!.createConversation()

        }
    }


    fun reply(prompt: String): String {

        val c = conversation
            ?: error("Engine not initialized")

        return c.sendMessage(prompt).toString()

    }


    fun close() {

        conversation?.close()

        conversation = null


        engine?.close()

        engine = null

    }
}