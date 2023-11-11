package org.schedule.management.specification.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime>{
    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        String date = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
        return new JsonPrimitive(date);
    }

    @Override
    public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String date = jsonElement.getAsString();
        return LocalDateTime.parse(date,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
