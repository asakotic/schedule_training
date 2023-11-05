package org.schedule.management.specification.adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    @Override
    public JsonElement serialize(LocalDate localDate, Type type, JsonSerializationContext jsonSerializationContext) {
        String date = DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
        return new JsonPrimitive(date);
    }

    @Override
    public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String date = jsonElement.getAsString();
        return LocalDate.parse(date,DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
