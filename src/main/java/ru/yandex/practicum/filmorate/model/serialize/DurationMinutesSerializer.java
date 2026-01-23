package ru.yandex.practicum.filmorate.model.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

public class DurationMinutesSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(Duration duration, JsonGenerator gen,
                          SerializerProvider provider) throws IOException {
        if (duration != null) {
            gen.writeNumber(duration.toMinutes());
        } else {
            gen.writeNull();
        }
    }
}