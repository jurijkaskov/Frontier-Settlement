package com.example.data.save

/**
 * Standard identifier for game save slots.
 */
enum class SaveSlotId(
    val id: String,
    val displayName: String,
    val isManual: Boolean
) {
    AUTOSAVE(
        id = "autosave",
        displayName = "Автосохранение",
        isManual = false
    ),
    MANUAL_1(
        id = "manual_1",
        displayName = "Слот 1",
        isManual = true
    ),
    MANUAL_2(
        id = "manual_2",
        displayName = "Слот 2",
        isManual = true
    ),
    MANUAL_3(
        id = "manual_3",
        displayName = "Слот 3",
        isManual = true
    ),
    AUTOSAVE_BACKUP(
        id = "autosave_backup",
        displayName = "Резервная копия",
        isManual = false
    );

    companion object {
        fun fromId(id: String): SaveSlotId {
            return entries.find { it.id == id } ?: AUTOSAVE
        }

        fun allManualSlots(): List<SaveSlotId> {
            return entries.filter { it.isManual }
        }

        fun allPlayerVisibleSlots(): List<SaveSlotId> {
            return listOf(AUTOSAVE) + allManualSlots()
        }
    }
}
