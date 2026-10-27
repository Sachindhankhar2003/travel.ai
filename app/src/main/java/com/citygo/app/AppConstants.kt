package com.citygo.app

/**
 * Single source of truth for every external endpoint used by Travel.ai.
 * API keys are injected via BuildConfig (read from local.properties).
 */
object AppConstants {

    // ── Free APIs (no key required) ──────────────────────────────────────────
    /** Open-Meteo weather forecast API — free, no key */
    const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/"

    /** Photon geocoding (Komoot) — free, no key */
    const val PHOTON_BASE_URL = "https://photon.komoot.io/"

    /** OSRM public routing API — free, no key */
    const val OSRM_BASE_URL = "https://router.project-osrm.org/"

    /** OpenTripMap places API — key required: OPENTRIPMAP_API_KEY in local.properties
     *  Get free key at https://opentripmap.io/product */
    const val OPENTRIPMAP_BASE_URL = "https://api.opentripmap.com/0.1/"

    /** Wikipedia REST API — free, no key */
    const val WIKIPEDIA_BASE_URL = "https://en.wikipedia.org/api/rest_v1/"

    /** Overpass (OpenStreetMap data query) — free, no key */
    const val OVERPASS_API_URL = "https://overpass-api.de/api/interpreter"

    /** Delhi OTD GTFS static feed — free, no key
     *  Source: https://otd.delhi.gov.in/data/static/ */
    const val DELHI_GTFS_BASE_URL = "https://otd.delhi.gov.in/"

    // ── Self-hosted FastAPI backend ──────────────────────────────────────────
    /** Set BACKEND_BASE_URL in local.properties; defaults to emulator localhost */
    // Injected: BuildConfig.BACKEND_BASE_URL

    // ── Cache TTL constants ──────────────────────────────────────────────────
    const val WEATHER_CACHE_TTL_MS = 30 * 60 * 1000L        // 30 min
    const val BUS_CACHE_TTL_MS     = 30 * 60 * 1000L        // 30 min
    const val METRO_CACHE_TTL_MS   = 60 * 60 * 1000L        // 1 hour
    const val PRICING_CACHE_TTL_MS = 24 * 60 * 60 * 1000L   // 24 hours

    // ── Room pricing defaults (when backend is unreachable) ──────────────────
    val ROOM_PRICE_DEFAULTS = mapOf(
        "BUDGET"  to 800.0,
        "MID"     to 2000.0,
        "PREMIUM" to 5000.0
    )

    // ── Fuel cost defaults (INR per km) ──────────────────────────────────────
    const val DEFAULT_FUEL_COST_PER_KM = 7.0 // ~₹100/L, ~14 km/L avg

    // ── Retry policy ─────────────────────────────────────────────────────────
    const val RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 2000L

    // ── DataStore keys ───────────────────────────────────────────────────────
    const val PREF_ONBOARDING_DONE  = "onboarding_done"
    const val PREF_SELECTED_CITY    = "selected_city"
    const val PREF_TEMP_UNIT        = "temp_unit"       // "C" | "F"
    const val PREF_LANGUAGE         = "language"        // "en" | "hi"
}
