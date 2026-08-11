package com.mapmory.shared.data.remote

import com.mapmory.shared.data.remote.model.ApiResponseDto
import com.mapmory.shared.data.remote.model.CountryDto
import com.mapmory.shared.data.remote.model.LocationDto
import com.mapmory.shared.data.remote.model.toDomain
import com.mapmory.shared.domain.model.Country
import com.mapmory.shared.domain.model.Location
import com.mapmory.shared.domain.repository.LocationRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class LocationRemoteRepository(
    private val client: HttpClient,
    baseUrl: String,
) : LocationRepository {
    private val apiBaseUrl = baseUrl.trimEnd('/')

    override suspend fun getCountries(): Result<List<Country>> = runCatching {
        client.get("$apiBaseUrl/countries")
            .requireSuccess()
            .body<ApiResponseDto<List<CountryDto>>>()
            .data
            .map(CountryDto::toDomain)
    }

    override suspend fun getLocations(
        countryId: Long?,
        parentId: Long?,
        keyword: String?,
    ): Result<List<Location>> = runCatching {
        client.get("$apiBaseUrl/locations") {
            countryId?.let { parameter("countryId", it) }
            parentId?.let { parameter("parentId", it) }
            keyword?.takeIf(String::isNotBlank)?.let { parameter("keyword", it) }
        }
            .requireSuccess()
            .body<ApiResponseDto<List<LocationDto>>>()
            .data
            .map(LocationDto::toDomain)
    }

    override suspend fun getLocation(id: Long): Result<Location> = runCatching {
        client.get("$apiBaseUrl/locations/$id")
            .requireSuccess()
            .body<ApiResponseDto<LocationDto>>()
            .data
            .toDomain()
    }
}
