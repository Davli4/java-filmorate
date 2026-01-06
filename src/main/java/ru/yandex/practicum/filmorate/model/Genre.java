package ru.yandex.practicum.filmorate.model;

import lombok.Getter;

public enum Genre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    CARTOON("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");

    @Getter
    private final String name;

    Genre(String name) {
        this.name = name;
    }
}