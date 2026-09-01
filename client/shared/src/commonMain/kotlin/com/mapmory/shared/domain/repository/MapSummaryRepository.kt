package com.mapmory.shared.domain.repository

import com.mapmory.shared.domain.model.MapRegionSummary

interface MapSummaryRepository {
    fun getCachedRootRegions(): List<MapRegionSummary>? = null

    fun getCachedChildRegions(regionId: Long): List<MapRegionSummary>? = null

    suspend fun getRootRegions(): Result<List<MapRegionSummary>>

    suspend fun getChildRegions(regionId: Long): Result<List<MapRegionSummary>>
}
