package com.mapmory.shared.data.repository

import com.mapmory.shared.domain.model.MapRegionSummary
import com.mapmory.shared.domain.repository.MapSummaryRepository
import kotlinx.serialization.Serializable

@Serializable
data class MapSummarySnapshot(
    val roots: List<MapRegionSummary> = emptyList(),
    val childrenByRegionId: Map<Long, List<MapRegionSummary>> = emptyMap(),
)

interface MapSummaryCache {
    fun read(): MapSummarySnapshot?

    fun write(snapshot: MapSummarySnapshot)

    fun clear()
}

class MemoryMapSummaryCache : MapSummaryCache {
    private var snapshot: MapSummarySnapshot? = null

    override fun read(): MapSummarySnapshot? = snapshot

    override fun write(snapshot: MapSummarySnapshot) {
        this.snapshot = snapshot
    }

    override fun clear() {
        snapshot = null
    }
}

internal class CachedMapSummaryRepository(
    private val delegate: MapSummaryRepository,
    private val cache: MapSummaryCache,
) : MapSummaryRepository {
    private var snapshot = cache.read() ?: MapSummarySnapshot()

    override fun getCachedRootRegions(): List<MapRegionSummary>? =
        snapshot.roots.takeIf { roots -> roots.isNotEmpty() }

    override fun getCachedChildRegions(regionId: Long): List<MapRegionSummary>? =
        snapshot.childrenByRegionId[regionId]

    override suspend fun getRootRegions(): Result<List<MapRegionSummary>> =
        delegate.getRootRegions().onSuccess { roots ->
            snapshot = snapshot.copy(roots = roots)
            runCatching { cache.write(snapshot) }
        }

    override suspend fun getChildRegions(regionId: Long): Result<List<MapRegionSummary>> =
        delegate.getChildRegions(regionId).onSuccess { children ->
            snapshot = snapshot.copy(
                childrenByRegionId = snapshot.childrenByRegionId + (regionId to children),
            )
            runCatching { cache.write(snapshot) }
        }

    fun invalidate() {
        snapshot = MapSummarySnapshot()
        runCatching(cache::clear)
    }
}
