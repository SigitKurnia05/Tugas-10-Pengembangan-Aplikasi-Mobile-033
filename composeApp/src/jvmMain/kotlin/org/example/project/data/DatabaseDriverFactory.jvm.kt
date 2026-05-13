package org.example.project.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.example.project.db.NotesDatabase

// Actual untuk Desktop (JVM)
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:notes.db")
        NotesDatabase.Schema.create(driver)
        return driver
    }
}