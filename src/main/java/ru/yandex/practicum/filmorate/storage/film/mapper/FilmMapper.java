package ru.yandex.practicum.filmorate.storage.film.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        int seconds = rs.getInt("duration");
        film.setDuration(Duration.ofMinutes(seconds / 60));

        if (rs.getInt("mpa_rating_id") > 0) {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getInt("mpa_rating_id"));
            mpaRating.setName(rs.getString("mpa_name"));
            mpaRating.setDescription(rs.getString("mpa_description"));
            film.setMpa(mpaRating);
        }
        if (rs.getTimestamp("created_at") != null) {
            film.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime().toLocalDate());
        } else {
            film.setCreatedAt(null);
        }
        return film;
    }
}