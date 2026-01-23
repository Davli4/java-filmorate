MERGE INTO mpa_ratings (id, name, description) KEY(id)
    VALUES
        (1, 'G', 'General Audiences. All ages admitted.'),
        (2, 'PG', 'Parental Guidance Suggested. Some material may not be suitable for children.'),
        (3, 'PG-13', 'Parents Strongly Cautioned. Some material may be inappropriate for children under 13.'),
        (4, 'R', 'Restricted. Children under 17 require accompanying parent or adult guardian.'),
        (5, 'NC-17', 'Adults Only. No one 17 and under admitted.');

MERGE INTO genres (id, name) KEY(id)
    VALUES
        (1, 'Комедия'),
        (2, 'Драма'),
        (3, 'Мультфильм'),
        (4, 'Триллер'),
        (5, 'Документальный'),
        (6, 'Боевик');