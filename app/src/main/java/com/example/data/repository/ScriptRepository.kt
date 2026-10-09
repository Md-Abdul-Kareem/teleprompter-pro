package com.example.data.repository

import com.example.data.local.ScriptDao
import com.example.data.local.ScriptEntity
import kotlinx.coroutines.flow.Flow

class ScriptRepository(private val scriptDao: ScriptDao) {
    val allScripts: Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    fun searchScripts(query: String): Flow<List<ScriptEntity>> = scriptDao.searchScripts(query)

    suspend fun getScriptById(id: Long): ScriptEntity? = scriptDao.getScriptById(id)

    fun getScriptFlowById(id: Long): Flow<ScriptEntity?> = scriptDao.getScriptFlowById(id)

    suspend fun insertScript(script: ScriptEntity): Long = scriptDao.insertScript(script)

    suspend fun updateScript(script: ScriptEntity) = scriptDao.updateScript(script)

    suspend fun deleteScript(script: ScriptEntity) = scriptDao.deleteScript(script)

    suspend fun deleteScriptById(id: Long) = scriptDao.deleteScriptById(id)

    suspend fun duplicateScript(script: ScriptEntity): Long {
        val duplicated = script.copy(
            id = 0L,
            title = "${script.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return scriptDao.insertScript(duplicated)
    }
}
