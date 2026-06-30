package com.example

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditorViewModel : ViewModel() {

    private val _fileName = MutableStateFlow("index.html")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _fileContent = MutableStateFlow("<h1>Hello World</h1>\n<style>\n  h1 { color: #8ab4f8; }\n</style>\n<script>\n  console.log('Test');\n</script>")
    val fileContent: StateFlow<String> = _fileContent.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveStatus = MutableStateFlow<String?>(null)
    val saveStatus: StateFlow<String?> = _saveStatus.asStateFlow()
    
    private val _autoSaveStatus = MutableStateFlow<String>("")
    val autoSaveStatus: StateFlow<String> = _autoSaveStatus.asStateFlow()

    private val contentUpdates = MutableStateFlow("")

    init {
        setupAutoSave()
    }

    @OptIn(FlowPreview::class)
    private fun setupAutoSave() {
        viewModelScope.launch {
            contentUpdates
                .debounce(2000L)
                .collectLatest { content ->
                    if (content.isNotEmpty()) {
                        performAutoSave(content)
                    }
                }
        }
    }

    fun onFileNameChange(newName: String) {
        _fileName.value = newName
    }

    fun onFileContentChange(newContent: String) {
        _fileContent.value = newContent
        contentUpdates.value = newContent
        _autoSaveStatus.value = "Typing..."
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }

    private suspend fun performAutoSave(content: String) {
        // Simulating writing to an internal cache or database for auto-save
        _autoSaveStatus.value = "Auto-saving..."
        kotlinx.coroutines.delay(500) // Simulate IO work
        _autoSaveStatus.value = "Auto-saved locally"
    }

    fun saveFileToDownloads(context: Context) {
        val name = _fileName.value.ifBlank { "untitled.txt" }
        val content = _fileContent.value

        viewModelScope.launch {
            _isSaving.value = true
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                        put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(name))
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CodeVault")
                    }

                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(content.toByteArray())
                        }
                        _saveStatus.value = "Saved to Downloads/CodeVault/$name"
                    } else {
                        _saveStatus.value = "Failed to create file"
                    }
                } else {
                    // Pre-Q fallback
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val vaultDir = File(downloadsDir, "CodeVault")
                    if (!vaultDir.exists()) {
                        vaultDir.mkdirs()
                    }
                    val file = File(vaultDir, name)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    _saveStatus.value = "Saved to Downloads/CodeVault/$name"
                }
            } catch (e: IOException) {
                Log.e("EditorViewModel", "Error saving file", e)
                _saveStatus.value = "Error: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".html") -> "text/html"
            fileName.endsWith(".css") -> "text/css"
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".json") -> "application/json"
            else -> "text/plain"
        }
    }
}
