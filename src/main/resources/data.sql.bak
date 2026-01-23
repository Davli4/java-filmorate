DELETE FROM likes;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;
DELETE FROM friendships;


ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN id RESTART WITH 1;
ALTER TABLE mpa_ratings ALTER COLUMN id RESTART WITH 1;



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


INSERT INTO users (email, login, name, birthday)
SELECT 'user1@example.com', 'user1', 'Иван Иванов', '1990-01-15'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user1@example.com');

INSERT INTO users (email, login, name, birthday)
SELECT 'user2@example.com', 'user2', 'Мария Петрова', '1995-05-20'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user2@example.com');

INSERT INTO users (email, login, name, birthday)
SELECT 'user3@example.com', 'user3', 'Алексей Сидоров', '1985-11-30'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user3@example.com');

INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
SELECT 'Фильм 1', 'Описание первого фильма', '2020-01-01', 120,
       (SELECT id FROM mpa_ratings WHERE name = 'PG-13')
WHERE NOT EXISTS (SELECT 1 FROM films WHERE name = 'Фильм 1' AND release_date = '2020-01-01');

INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
SELECT 'Фильм 2', 'Описание второго фильма', '2021-05-15', 95,
       (SELECT id FROM mpa_ratings WHERE name = 'R')
WHERE NOT EXISTS (SELECT 1 FROM films WHERE name = 'Фильм 2' AND release_date = '2021-05-15');

INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
SELECT 'Фильм 3', 'Описание третьего фильма', '2022-10-20', 150,
       (SELECT id FROM mpa_ratings WHERE name = 'PG')
WHERE NOT EXISTS (SELECT 1 FROM films WHERE name = 'Фильм 3' AND release_date = '2022-10-20');

INSERT INTO film_genres (film_id, genre_id)
SELECT
    (SELECT id FROM films WHERE name = 'Фильм 1' AND release_date = '2020-01-01'),
    (SELECT id FROM genres WHERE name = 'Комедия')
WHERE NOT EXISTS (
    SELECT 1 FROM film_genres fg
                      JOIN films f ON fg.film_id = f.id
                      JOIN genres g ON fg.genre_id = g.id
    WHERE f.name = 'Фильм 1' AND g.name = 'Комедия'
);

INSERT INTO film_genres (film_id, genre_id)
SELECT
    (SELECT id FROM films WHERE name = 'Фильм 1' AND release_date = '2020-01-01'),
    (SELECT id FROM genres WHERE name = 'Драма')
WHERE NOT EXISTS (
    SELECT 1 FROM film_genres fg
                      JOIN films f ON fg.film_id = f.id
                      JOIN genres g ON fg.genre_id = g.id
    WHERE f.name = 'Фильм 1' AND g.name = 'Драма'
);

INSERT INTO film_genres (film_id, genre_id)
SELECT
    (SELECT id FROM films WHERE name = 'Фильм 2' AND release_date = '2021-05-15'),
    (SELECT id FROM genres WHERE name = 'Боевик')
WHERE NOT EXISTS (
    SELECT 1 FROM film_genres fg
                      JOIN films f ON fg.film_id = f.id
                      JOIN genres g ON fg.genre_id = g.id
    WHERE f.name = 'Фильм 2' AND g.name = 'Боевик'
);

INSERT INTO likes (film_id, user_id)
SELECT
    (SELECT id FROM films WHERE name = 'Фильм 1' AND release_date = '2020-01-01'),
    (SELECT id FROM users WHERE email = 'user1@example.com')
WHERE NOT EXISTS (
    SELECT 1 FROM likes l
                      JOIN films f ON l.film_id = f.id
                      JOIN users u ON l.user_id = u.id
    WHERE f.name = 'Фильм 1' AND u.email = 'user1@example.com'
);

INSERT INTO likes (film_id, user_id)
SELECT
    (SELECT id FROM films WHERE name = 'Фильм 1' AND release_date = '2020-01-01'),
    (SELECT id FROM users WHERE email = 'user2@example.com')
WHERE NOT EXISTS (
    SELECT 1 FROM likes l
                      JOIN films f ON l.film_id = f.id
                      JOIN users u ON l.user_id = u.id
    WHERE f.name = 'Фильм 1' AND u.email = 'user2@example.com'
);

INSERT INTO friendships (user_id, friend_id, status)
SELECT
    (SELECT id FROM users WHERE email = 'user1@example.com'),
    (SELECT id FROM users WHERE email = 'user2@example.com'),
    'CONFIRMED'
WHERE NOT EXISTS (
    SELECT 1 FROM friendships f
                      JOIN users u1 ON f.user_id = u1.id
                      JOIN users u2 ON f.friend_id = u2.id
    WHERE u1.email = 'user1@example.com' AND u2.email = 'user2@example.com'
);