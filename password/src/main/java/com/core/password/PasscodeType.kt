package com.core.password

enum class PasscodeType(val value: String) {
    NONE("none"),
    PIN("pin"),
    PATTERN("pattern");

    companion object {
        fun fromValue(value: String): PasscodeType {
            return entries.firstOrNull { it.value == value } ?: NONE
        }
    }
}