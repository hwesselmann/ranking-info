package de.hdawg.rankinginfo.config

import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.ExperimentalKeywordApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Overrides the Exposed default DatabaseConfig to disable keyword casing preservation.
 *
 * By default Exposed quotes SQL keywords (like `date`) in their original lowercase form, producing
 * `"date"` in SQL statements. H2 (used in tests) stores unquoted identifiers as uppercase, so
 * `"date"` would fail to match the column `DATE`. With preserveKeywordCasing=false, Exposed uses
 * the database's native casing — uppercase `"DATE"` for H2, lowercase `"date"` for PostgreSQL —
 * ensuring keyword-named columns are found correctly in all environments.
 */
@Configuration
class ExposedConfig {
    @OptIn(ExperimentalKeywordApi::class)
    @Bean
    fun databaseConfig(): DatabaseConfig =
        DatabaseConfig {
            preserveKeywordCasing = false
        }
}
