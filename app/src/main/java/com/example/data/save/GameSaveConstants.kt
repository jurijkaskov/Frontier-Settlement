package com.example.data.save

object GameSaveConstants {
    /**
     * Authoritative schema version for the game save format.
     * Increment when introducing breaking persistent schema changes.
     */
    const val CURRENT_SAVE_SCHEMA_VERSION = 1

    /**
     * Default fallback game application version string.
     */
    const val DEFAULT_GAME_VERSION = "1.0"
}
