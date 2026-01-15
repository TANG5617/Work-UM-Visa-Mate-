package com.um.visamate.data.models

// Attachment metadata

data class Attachment(
    val id: String,
    val name: String,
    val uri: String,
    val checksum: String? = null
)

