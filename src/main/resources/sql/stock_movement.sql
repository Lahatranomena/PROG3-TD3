CREATE TYPE movement_type as enum ('IN', 'OUT');


CREATE TABLE stock_movement(
    id SERIAL primary key,
    id_ingredient int,
    quantity numeric,
    type movement_type,
    unit unit_type,
    creation_datetime timestamp

--foreign key id_ingredient
);

INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
VALUES (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00'),
       (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00'),
       (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00'),
       (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00'),
       (5, 3, 10.0, 'IN', 'KG', '2024-01-04 09:00'),
       (6, 3, 1.0, 'OUT', 'KG', '2024-01-16 13:00'),
       (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00'),
       (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 14:00'),
       (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00'),
       (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 14:00');


ALTER TABLE stock_movement
    ADD CONSTRAINT fk_ingredient
        FOREIGN KEY (id_ingredient) REFERENCES ingredient(id);

alter table ingredient
    add column if not exists initial_stock numeric(10, 2);

create table if not exists "order"
(
    id                serial primary key,
    reference         varchar(255),
    creation_datetime timestamp without time zone
    );

create table if not exists dish_order
(
    id       serial primary key,
    id_order int references "order" (id),
    id_dish  int references dish (id),
    quantity int
    );

UPDATE ingredient SET initial_stock = 5.0 WHERE id = 1;
UPDATE ingredient SET initial_stock = 4.0 WHERE id = 2;
UPDATE ingredient SET initial_stock = 10.0 WHERE id = 3;
UPDATE ingredient SET initial_stock = 3.0 WHERE id = 4;
UPDATE ingredient SET initial_stock = 2.5 WHERE id = 5;