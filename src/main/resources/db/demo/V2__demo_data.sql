-- Demo data: a fictional concert band and big band, so Swagger UI has something to show from the first run.
-- Dates are relative to the day the database is created, so the reports always have interesting results:
-- the Autumn Concert is two weeks away, two of its pieces are under-rehearsed and one musician rarely attends.
-- Loaded in the default and postgres profiles only; tests start from an empty schema.

INSERT INTO ensemble (id, name, type, city) VALUES
    (1, 'Riverside Concert Band', 'BAND', 'Valencia'),
    (2, 'Night Owls Big Band', 'BIG_BAND', 'Valencia');

INSERT INTO musician (id, name, email, main_instrument) VALUES
    (1, 'Lucia Moreno', 'lucia.moreno@example.com', 'Flute'),
    (2, 'Pablo Herrera', 'pablo.herrera@example.com', 'Clarinet'),
    (3, 'Marta Gil', 'marta.gil@example.com', 'Alto saxophone'),
    (4, 'Javier Soler', 'javier.soler@example.com', 'Tenor saxophone'),
    (5, 'Elena Navarro', 'elena.navarro@example.com', 'Trumpet'),
    (6, 'Diego Ramos', 'diego.ramos@example.com', 'Trombone'),
    (7, 'Sara Vidal', 'sara.vidal@example.com', 'Tuba'),
    (8, 'Hugo Castro', 'hugo.castro@example.com', 'Percussion');

INSERT INTO membership (ensemble_id, musician_id, instrument, joined_on) VALUES
    (1, 1, 'Flute', DATE '2018-09-15'),
    (1, 2, 'First clarinet', DATE '2016-10-01'),
    (1, 3, 'First alto saxophone', DATE '2013-09-20'),
    (1, 4, 'Tenor saxophone', DATE '2019-01-12'),
    (1, 5, 'First trumpet', DATE '2015-09-05'),
    (1, 6, 'Trombone', DATE '2020-02-01'),
    (1, 7, 'Tuba', DATE '2017-11-18'),
    (1, 8, 'Percussion', DATE '2022-09-10'),
    (2, 3, 'Lead alto saxophone', DATE '2021-03-01'),
    (2, 5, 'Lead trumpet', DATE '2021-03-01');

INSERT INTO piece (id, ensemble_id, title, composer, arranger, duration_seconds, difficulty, genre) VALUES
    (1, 1, 'El Camino Real', 'Alfred Reed', NULL, 660, 4, 'Concert band'),
    (2, 1, 'Arsenal', 'Jan Van der Roost', NULL, 270, 3, 'March'),
    (3, 1, 'Amparito Roca', 'Jaime Texidor', NULL, 180, 2, 'Pasodoble'),
    (4, 1, 'Festive Overture', 'Dmitri Shostakovich', 'Donald Hunsberger', 360, 5, 'Overture'),
    (5, 1, 'First Suite in E-flat', 'Gustav Holst', NULL, 660, 4, 'Suite'),
    (6, 1, 'Second Suite in F', 'Gustav Holst', NULL, 690, 4, 'Suite'),
    (7, 1, 'Lincolnshire Posy', 'Percy Grainger', NULL, 960, 5, 'Suite'),
    (8, 1, 'The Stars and Stripes Forever', 'John Philip Sousa', NULL, 210, 3, 'March'),
    (9, 1, 'Armenian Dances (Part I)', 'Alfred Reed', NULL, 660, 5, 'Concert band'),
    (10, 1, 'October', 'Eric Whitacre', NULL, 390, 3, 'Concert band'),
    (11, 1, 'Sleep', 'Eric Whitacre', NULL, 330, 3, 'Concert band'),
    (12, 1, 'Danzon No. 2', 'Arturo Marquez', 'Oliver Nickel', 600, 5, 'Latin'),
    (13, 1, 'Espana Cani', 'Pascual Marquina', NULL, 180, 2, 'Pasodoble'),
    (14, 1, 'Florentiner March', 'Julius Fucik', NULL, 330, 3, 'March'),
    (15, 1, 'Valdres', 'Johannes Hanssen', NULL, 270, 3, 'March'),
    (16, 2, 'In the Mood', 'Joe Garland', NULL, 210, 2, 'Swing'),
    (17, 2, 'Take the A Train', 'Billy Strayhorn', NULL, 180, 2, 'Swing'),
    (18, 2, 'Sing, Sing, Sing', 'Louis Prima', NULL, 480, 4, 'Swing'),
    (19, 2, 'Moanin''', 'Bobby Timmons', NULL, 300, 3, 'Hard bop');

-- Weekly rehearsals at 19:30: five in the past and one still to come.
INSERT INTO rehearsal (id, ensemble_id, starts_at, location, notes) VALUES
    (1, 1, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '40' DAY + INTERVAL '1170' MINUTE, 'Music school', 'First rehearsal after the summer'),
    (2, 1, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '30' DAY + INTERVAL '1170' MINUTE, 'Music school', NULL),
    (3, 1, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '20' DAY + INTERVAL '1170' MINUTE, 'Music school', NULL),
    (4, 1, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '12' DAY + INTERVAL '1170' MINUTE, 'Town hall', 'Sectionals'),
    (5, 1, CAST(CURRENT_DATE AS TIMESTAMP) - INTERVAL '5' DAY + INTERVAL '1170' MINUTE, 'Music school', NULL),
    (6, 1, CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '3' DAY + INTERVAL '1170' MINUTE, 'Town hall', 'Dress rehearsal');

INSERT INTO rehearsal_piece (rehearsal_id, piece_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 7),
    (2, 1), (2, 4), (2, 5), (2, 9),
    (3, 2), (3, 4), (3, 8), (3, 10),
    (4, 1), (4, 3), (4, 5), (4, 8),
    (5, 1), (5, 2),
    (6, 4), (6, 8);

-- Everyone came to the past rehearsals except Javier (sick once) and Hugo, who only made it once.
INSERT INTO attendance (rehearsal_id, musician_id, attended, reason)
SELECT r.id,
       m.musician_id,
       CASE
           WHEN m.musician_id = 8 AND r.id <> 2 THEN FALSE
           WHEN m.musician_id = 4 AND r.id = 3 THEN FALSE
           ELSE TRUE
       END,
       CASE
           WHEN m.musician_id = 8 AND r.id <> 2 THEN 'Work shift'
           WHEN m.musician_id = 4 AND r.id = 3 THEN 'Sick'
       END
FROM rehearsal r
JOIN membership m ON m.ensemble_id = r.ensemble_id
WHERE r.ensemble_id = 1 AND r.id <= 5;

INSERT INTO concert (id, ensemble_id, title, concert_date, venue) VALUES
    (1, 1, 'Autumn Concert', CAST(CURRENT_DATE + INTERVAL '14' DAY AS DATE), 'Palau de la Musica');

INSERT INTO program_item (concert_id, piece_id, sort_order) VALUES
    (1, 1, 1),
    (1, 2, 2),
    (1, 3, 3),
    (1, 4, 4),
    (1, 5, 5),
    (1, 8, 6);

-- The rows above use explicit ids; move every identity past them so new rows never collide.
ALTER TABLE ensemble ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE musician ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE membership ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE piece ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE rehearsal ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE attendance ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE concert ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE program_item ALTER COLUMN id RESTART WITH 1000;
