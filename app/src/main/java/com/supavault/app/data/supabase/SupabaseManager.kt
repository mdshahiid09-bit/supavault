package com.supavault.app.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.supavault.app.BuildConfig
import com.supavault.app.data.model.VaultItem
import com.supavault.app.data.model.VaultItemInsert
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

object SupabaseManager {
    private const val PREFS_NAME = "supavault_prefs"
    private const val KEY_URL = "supabase_url"
    private const val KEY_KEY = "supabase_key"

    private lateinit var prefs: SharedPreferences
    private var _client: SupabaseClient? = null
    val client: SupabaseClient
        get() = _client ?: throw IllegalStateException("SupabaseManager is not initialized yet")

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        buildClient(url, key)
    }

    fun getSupabaseUrl(): String {
        return prefs.getString(KEY_URL, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_SUPABASE_URL
    }

    fun getSupabaseKey(): String {
        return prefs.getString(KEY_KEY, null)?.takeIf { it.isNotBlank() }
            ?: BuildConfig.DEFAULT_SUPABASE_ANON_KEY
    }

    fun isConfigured(): Boolean {
        val url = getSupabaseUrl()
        val key = getSupabaseKey()
        return url.startsWith("http") && !url.contains("your-project") && key.isNotBlank() && key != "your-anon-key"
    }

    fun updateConfig(url: String, key: String) {
        prefs.edit()
            .putString(KEY_URL, url.trim())
            .putString(KEY_KEY, key.trim())
            .apply()
        buildClient(url.trim(), key.trim())
    }

    private fun buildClient(url: String, key: String) {
        try {
            _client = createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
            }
            _isConnected.value = true
        } catch (e: Exception) {
            _isConnected.value = false
        }
    }

    // --- Authentication ---
    suspend fun signUp(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = client.auth.currentUserOrNull()?.id ?: "registered"
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            val userId = client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("Login succeeded but user is null")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
        } catch (_: Exception) {}
    }

    fun getCurrentUser() = client.auth.currentUserOrNull()

    // --- Database (PostgREST) ---
    suspend fun fetchUserItems(userId: String): Result<List<VaultItem>> = withContext(Dispatchers.IO) {
        try {
            val items = client.from("vault_items")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<VaultItem>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllItems(): Result<List<VaultItem>> = withContext(Dispatchers.IO) {
        try {
            val items = client.from("vault_items")
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<VaultItem>()
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertItem(
        title: String,
        description: String,
        imageBytes: ByteArray?,
        fileExtension: String = "jpg"
    ): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val user = getCurrentUser() ?: throw IllegalStateException("Not authenticated")
            var imageUrl: String? = null

            // Upload image to Storage if provided
            if (imageBytes != null && imageBytes.isNotEmpty()) {
                val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$fileExtension"
                val bucket = client.storage.from("vault_media")
                bucket.upload(fileName, imageBytes, upsert = true)
                imageUrl = bucket.publicUrl(fileName)
            }

            val newItem = VaultItemInsert(
                user_id = user.id,
                user_email = user.email ?: "anonymous@supavault.app",
                title = title,
                description = description,
                image_url = imageUrl
            )

            client.from("vault_items").insert(newItem)

            // Return inserted representation
            Result.success(
                VaultItem(
                    id = UUID.randomUUID().toString(),
                    user_id = newItem.user_id,
                    user_email = newItem.user_email,
                    title = newItem.title,
                    description = newItem.description,
                    image_url = newItem.image_url,
                    created_at = "Just now"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            client.from("vault_items").delete {
                filter {
                    eq("id", itemId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Storage Diagnostics ---
    suspend fun testStorageUpload(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val testBytes = "Hello Supabase Storage from SupaVault Diagnostic Test!\nTimestamp: ${System.currentTimeMillis()}".toByteArray()
            val fileName = "diagnostic_test_${System.currentTimeMillis()}.txt"
            val bucket = client.storage.from("vault_media")
            bucket.upload(fileName, testBytes, upsert = true)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
