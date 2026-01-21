CREATE TYPE movement_type as enum ('IN', 'OUT');


CREATE TABLE stock_movement(
    id SERIAL primary key,
    id_ingredient int,
    quantity numeric,
    type movement_type,
    unit unit_type,
    creation_datetime timestamp
);