package dev.kourosh.metamorphosis_sample

import kotlinx.serialization.Serializable

@Serializable
data class Data(
    val latestVersion: String,
    val latestVersionCode: Int,
    val url: String,
    val required: Boolean,
    val releaseNotes: List<String>
)