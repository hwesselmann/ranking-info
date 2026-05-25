package de.hdawg.rankinginfo.api.v1

import java.time.LocalDate

/** Generic error response returned for 4xx responses. */
data class ErrorResponse(
    val error: String,
)

// Players search

/** Parameters echoed back in a player search response. */
data class PlayerSearchRequest(
    val lastname: String?,
    val yob: String?,
    val total_count: Int,
)

/** Self-link for a player resource. */
data class PlayerLink(
    val self: String,
)

/** A single item in the player search result list. */
data class PlayerSearchItem(
    val dtb_id: Int,
    val lastname: String,
    val firstname: String,
    val club: String,
    val links: PlayerLink,
)

/** Response envelope for the player search endpoint. */
data class PlayerSearchResponse(
    val request: PlayerSearchRequest,
    val data: List<PlayerSearchItem>,
)

// Player detail

/** A single ranking entry within a player's history. */
data class RankingEntry(
    val quarter: LocalDate,
    val age_group: String,
    val ranking_position: Int,
    val score: String,
)

/** Full player profile including all ranking history entries. */
data class PlayerDetailData(
    val dtb_id: Int,
    val lastname: String,
    val firstname: String,
    val nationality: String,
    val club: String,
    val federation: String,
    val rankings: List<RankingEntry>,
)

/** Response envelope for the player detail endpoint. */
data class PlayerDetailResponse(
    val data: PlayerDetailData,
)

// Listings

/** Parameters echoed back in a listing response. */
data class ListingRequest(
    val quarter: String,
    val age_group: String,
    val age_group_options: String?,
    val federation: String?,
    val club: String?,
    val year_end: Boolean,
    val page: Int,
    val per_page: Int,
    val total_count: Long,
)

/** A single ranked player entry in a listing response. */
data class ListingItem(
    val dtb_id: Int,
    val ranking_position: Int,
    val lastname: String,
    val firstname: String,
    val nationality: String,
    val club: String,
    val federation: String,
    val score: String,
    /** Position change relative to the previous quarter; null if no prior data is available. */
    val position_change: Int?,
    val links: PlayerLink,
)

/** Response envelope for the listing endpoint. */
data class ListingResponse(
    val request: ListingRequest,
    val data: List<ListingItem>,
)
