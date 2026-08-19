package com.mapmory.shared.data.local.photo

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 갤러리 사진을 빠르게 찾기 위한 로컬 인덱스다.
 * 사진 원본은 저장하지 않고 MediaStore에서 다시 읽을 수 있는 정보만 보관한다.
 */
@Entity(tableName = "photo_metadata")
data class PhotoMetadataEntity(
    @PrimaryKey val mediaId: Long,
    val contentUri: String,
    val displayName: String,
    val capturedAtMillis: Long?,
    val modifiedAtSeconds: Long,
    val latitude: Double?,
    val longitude: Double?,
    val mimeType: String?,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val scanId: Long,
)
