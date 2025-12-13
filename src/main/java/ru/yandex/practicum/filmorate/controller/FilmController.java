package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Get request, size: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Add request, film: {}", film);

        try{
            log.info("Starting to validation films");
            if(film.getName() == null || film.getName().isBlank()){
                String errorMessage = "Film name is mandatory";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if(film.getDescription().length() > 200){
                String errorMessage = "Film description is too long";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            LocalDate cinemaDate = LocalDate.of(1895,12,28);
            if(film.getReleaseDate().isBefore(cinemaDate)){
                String errorMessage = "Film release date is before cinema date";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            if(film.getDuration() == null || film.getDuration().toMinutes() <= 0){
                String errorMessage = "Film duration is negative";
                log.error("Validation is not passed {}", errorMessage);
                throw new ValidationException(errorMessage);
            }
            long newId = generateFilmId();
            film.setId(newId);
            films.put(film.getId(), film);

            return film;
        } catch(ValidationException e) {
            log.error("Error adding film: {}, case {}", film, e.getMessage());
            throw e;
        } catch(Exception e) {
            log.error("Unexpected error to adding film: {}", film, e);
            throw e;
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Update request, film: {}", film);

        try {
            log.info("Starting to validation films");
            if(film.getId() == null){
                String errorMessage = "Id should not be null";
                log.error("Validation is not passed {}", errorMessage);
                throw new ConditionsNotMetException(errorMessage);
            }

            if(films.containsKey(film.getId())){
                Film oldFilm = films.get(film.getId());
                if(film.getName() == null || film.getName().isBlank()){
                    String errorMessage = "Film name is mandatory";
                    log.error("Validation is not passed {}", errorMessage);
                    throw new ValidationException(errorMessage);
                }
                if(film.getDescription().length() > 200){
                    String errorMessage = "Film description is too long";
                    log.error("Validation is not passed {}", errorMessage);
                    throw new ValidationException(errorMessage);
                }
                LocalDate cinemaDate = LocalDate.of(1895,12,28);
                if(film.getReleaseDate().isBefore(cinemaDate)){
                    String errorMessage = "Film release date is before cinema date";
                    log.error("Validation is not passed {}", errorMessage);
                    throw new ValidationException(errorMessage);
                }
                if(film.getDuration() == null || film.getDuration().toMinutes() <= 0){
                    String errorMessage = "Film duration is negative";
                    log.error("Validation is not passed {}", errorMessage);
                    throw new ValidationException(errorMessage);
                }
                oldFilm.setName(film.getName());
                oldFilm.setDescription(film.getDescription());
                oldFilm.setReleaseDate(film.getReleaseDate());
                oldFilm.setDuration(film.getDuration());
                return oldFilm;
            }
            String errorMessage = "Film with id " + film.getId() + " not found";
            log.error("Validation is not passed {}", errorMessage);
            throw new NotFoundException(errorMessage);
        } catch(ValidationException | ConditionsNotMetException | NotFoundException e) {
            log.error("Error updating film: {}, case {}", film, e.getMessage());
            throw e;
        } catch(Exception e) {
            log.error("Unexpected error to updating film: {}", film, e);
            throw e;
        }
    }

    private long generateFilmId(){
        long currentId = films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++currentId;
    }
}