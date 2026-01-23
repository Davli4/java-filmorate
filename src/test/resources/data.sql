INSERT INTO mpa_ratings (name, description)
SELECT 'G', 'Для всех возрастов'
    WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE name = 'G');

INSERT INTO mpa_ratings (name, description)
SELECT 'PG', 'Рекомендуется присутствие родителей'
    WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE name = 'PG');

INSERT INTO mpa_ratings (name, description)
SELECT 'PG-13', 'Детям до 13 лет просмотр не желателен'
    WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE name = 'PG-13');

INSERT INTO mpa_ratings (name, description)
SELECT 'R', 'Лицам до 17 лет обязательно присутствие взрослого'
    WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE name = 'R');

INSERT INTO mpa_ratings (name, description)
SELECT 'NC-17', 'Лицам до 18 лет просмотр запрещён'
    WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE name = 'NC-17');

INSERT INTO genres (name)
SELECT 'Комедия'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Комедия');

INSERT INTO genres (name)
SELECT 'Драма'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Драма');

INSERT INTO genres (name)
SELECT 'Мультфильм'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Мультфильм');

INSERT INTO genres (name)
SELECT 'Триллер'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Триллер');

INSERT INTO genres (name)
SELECT 'Документальный'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Боевик');

INSERT INTO genres (name)
SELECT 'Боевик'
    WHERE NOT EXISTS (SELECT 1 FROM genres WHERE name = 'Фантастика');
