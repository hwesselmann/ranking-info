package de.hdawg.rankinginfo.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.Closeable
import java.sql.Connection
import javax.sql.DataSource

/**
 * Replaces the auto-configured DataSource for the dev profile with a wrapper that silently ignores
 * Connection#setReadOnly calls. SQLite's JDBC driver throws if setReadOnly is called on an already-
 * established connection, which Exposed's SpringTransactionManager does unconditionally.
 */
@Configuration
@Profile("dev")
class SqliteDataSourceConfig {
    @Bean
    fun dataSource(properties: DataSourceProperties): DataSource {
        val hikariDs =
            properties
                .initializeDataSourceBuilder()
                .type(HikariDataSource::class.java)
                .build()
        return SqliteCompatibleDataSource(hikariDs)
    }
}

class SqliteCompatibleDataSource(
    private val delegate: DataSource,
) : DataSource by delegate,
    Closeable {
    override fun getConnection(): Connection = SqliteCompatibleConnection(delegate.connection)

    override fun getConnection(
        username: String,
        password: String,
    ): Connection =
        SqliteCompatibleConnection(
            delegate.getConnection(username, password),
        )

    override fun close() {
        if (delegate is Closeable) delegate.close()
    }
}

class SqliteCompatibleConnection(
    private val delegate: Connection,
) : Connection by delegate {
    @Suppress("EmptyFunctionBlock")
    override fun setReadOnly(readOnly: Boolean) {}
}
