package com.mapmory.shared.domain.repository

import com.mapmory.shared.domain.model.Country
import com.mapmory.shared.domain.model.Location

interface LocationRepository {
    suspend fun getCountries(): Result<List<Country>>

    suspend fun getLocations(
        countryId: Long? = null,
        parentId: Long? = null,
        keyword: String? = null,
    ): Result<List<Location>>

    suspend fun getLocation(id: Long): Result<Location>
}
