CREATE TABLE dishIngredient(
                               id SERIAL PRIMARY KEY,
                               id_dish INT NOT NULL REFERENCES dish(id),
                               id_ingredient INT NOT NULL REFERENCES ingredient(id),
                               quantity_required numeric(10,2),
                               unit unit_type
);

create type unit_type as enum ('PCS', 'KG', 'L');

INSERT INTO dishIngredient(id, id_dish, id_ingredient, quantity_required, unit)
VALUES (1, 1, 1, 0.20, 'KG'),
       (2, 1, 2, 0.15, 'KG'),
       (3, 2, 3, 1.00, 'KG'),
       (4, 4, 4, 0.30, 'KG'),
       (5, 4, 5, 0.20, 'KG');

INSERT INTO dish(id, name, dish_type, price)
VALUES (1, 'Salade fraîche', 'STARTER', 3500.00),
       (2, 'Poulet grillé', 'MAIN', 12000.00),
       (3, 'Riz au légumes', 'MAIN', null),
       (4, 'Gateau au chocolat', 'DESSERT', 8000.00),
       (5, 'Salade de fruits', 'DESSERT', null);