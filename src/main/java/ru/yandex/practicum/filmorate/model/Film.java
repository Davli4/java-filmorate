package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = {"releaseDate", "duration"})
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @JsonDeserialize(using = DurationDeserializer.class)
    Duration duration;
}
