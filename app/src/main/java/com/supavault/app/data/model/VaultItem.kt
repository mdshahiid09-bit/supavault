package com.supavault.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VaultItem(
    val id: String? = null,
    val user_id: String,
    val user_email: String,
    val title: String,
    val description: String,
    val image_url: String? = null,
    val created_at: String? = null
)

@Serializable
data class VaultItemInsert(
    val user_id: String,
    val user_email: String,
    val title: String,
    val description: String,
    val image_url: String? = null
)
