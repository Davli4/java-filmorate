package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.serialize.DurationMinutesSerializer;
import ru.yandex.practicum.filmorate.model.serialize.MinutesDurationDeserializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    @JsonSerialize(using = DurationMinutesSerializer.class)
    @JsonDeserialize(using = MinutesDurationDeserializer.class)
    private Duration duration;
    private Set<Long> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private MpaRating mpa;
    private LocalDate createdAt;
}