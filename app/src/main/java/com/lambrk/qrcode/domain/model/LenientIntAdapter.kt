package com.lambrk.qrcode.domain.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Reads an Int from a JSON number (int/long/double, e.g. 2, 2.0) or a numeric string ("2", " 2.0 ").
 * Anything else (null, blank, "abc", 2.5, out of Int range) becomes null, which the validator
 * reports as an invalid digit range instead of crashing the whole master list.
 */
class LenientIntAdapter : TypeAdapter<Int?>() {

    override fun read(reader: JsonReader): Int? {
        val number: Double? = when (reader.peek()) {
            JsonToken.NUMBER -> reader.nextString().toDoubleOrNull()
            JsonToken.STRING -> reader.nextString().trim().toDoubleOrNull()
            JsonToken.NULL -> { reader.nextNull(); null }
            else -> { reader.skipValue(); null }
        }
        if (number == null || number != Math.floor(number)) return null
        if (number < Int.MIN_VALUE || number > Int.MAX_VALUE) return null
        return number.toInt()
    }

    override fun write(writer: JsonWriter, value: Int?) {
        if (value == null) writer.nullValue() else writer.value(value)
    }
}
