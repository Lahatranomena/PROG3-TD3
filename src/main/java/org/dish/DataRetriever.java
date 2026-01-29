package org.dish;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class DataRetriever {
    DBConnection dbConnection = new DBConnection();

    Order findOrderByReference(String reference) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    select id, reference, creation_datetime, type, status from "order" where reference like ?""");
            preparedStatement.setString(1, reference);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Order order = new Order();
                Integer idOrder = resultSet.getInt("id");
                order.setId(idOrder);
                order.setReference(resultSet.getString("reference"));
                order.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());
                order.setOrderType(OrderType.valueOf(resultSet.getString("type")));
                order.setOrderStatut(OrderStatus.valueOf(resultSet.getString("status")));
                order.setDishOrderList(findDishOrderByIdOrder(idOrder));
                return order;
            }
            throw new RuntimeException("Order not found with reference " + reference);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishOrder> findDishOrderByIdOrder(Integer idOrder) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishOrder> dishOrders = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, id_dish, quantity from dish_order where dish_order.id_order = ?
                            """);
            preparedStatement.setInt(1, idOrder);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Dish dish = findDishById(resultSet.getInt("id_dish"));
                DishOrder dishOrder = new DishOrder();
                dishOrder.setId(resultSet.getInt("id"));
                dishOrder.setQuantity(resultSet.getInt("quantity"));
                dishOrder.setDish(dish);
                dishOrders.add(dishOrder);
            }
            dbConnection.closeConnection(connection);
            return dishOrders;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order updateOrder(Order orderToUpdate) {

        Order existingOrder = findOrderByReference(orderToUpdate.getReference());

        if (existingOrder.getOrderStatut() == OrderStatus.DELIVERED) {
            throw new RuntimeException(
                    "Une commande livrée ne peut pas être modifiée"
            );
        }

        return saveOrder(orderToUpdate);
    }

    public Order saveOrder(Order orderToSave) {
        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            if (orderToSave.getId() != null) {
                String checkStatusSql = "SELECT status FROM \"order\" WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkStatusSql)) {
                    ps.setInt(1, orderToSave.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String currentStatus = rs.getString("status");
                            if ("DELIVERED".equals(currentStatus)) {
                                throw new RuntimeException("Une commande livrée ne peut être modifiée");
                            }
                        }
                    }
                }
            }

            for (DishOrder dishOrder : orderToSave.getDishOrderList()) {
                Dish dish = dishOrder.getDish();
                int quantityOrdered = dishOrder.getQuantity();

                for (DishIngredient dishIngredient : dish.getDishIngredients()) {
                    Ingredient ingredient = dishIngredient.getIngredient();
                    double neededQuantity = dishIngredient.getQuantity() * quantityOrdered;

                    StockValue stockValue = ingredient.getStockValueAt(Instant.now());
                    double availableQuantity = (stockValue != null) ? stockValue.getQuantity() : 0;
                }
            }

            String insertOrderSql = """
            INSERT INTO "order" (id, reference, creation_datetime, type, status)
            VALUES (?, ?, ?, ?::order_type, ?::order_status)
            ON CONFLICT (id) DO UPDATE
            SET
                reference = EXCLUDED.reference,
                creation_datetime = EXCLUDED.creation_datetime,
                type = EXCLUDED.type,
                status = EXCLUDED.status
            RETURNING id
        """;
            int orderId;

            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql)) {
                if (orderToSave.getId() != null) {
                    ps.setInt(1, orderToSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "order", "id"));
                }

                ps.setString(2, orderToSave.getReference());
                ps.setTimestamp(3, Timestamp.from(Instant.now()));
                ps.setString(4, orderToSave.getOrderType().toString());
                ps.setString(5, orderToSave.getOrderStatut().toString());

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dish_order WHERE id_order = ?")) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            String insertDishOrderSql = """
        INSERT INTO dish_order (id_order, id_dish, quantity)
        VALUES (?, ?, ?)
        """;
            try (PreparedStatement ps = conn.prepareStatement(insertDishOrderSql)) {
                for (DishOrder dishOrder : orderToSave.getDishOrderList()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, dishOrder.getDish().getId());
                    ps.setInt(3, dishOrder.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();

            return findOrderByReference(orderToSave.getReference());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Order findOrderById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Order order = null;


        String orderQuery = "SELECT id, reference, creation_datetime, type, status FROM \"Order\" WHERE id = ?";


        String dishOrderQuery = """
        SELECT dorder.id as dish_order_id, dorder.id_dish, dorder.quantity,
               d.name as dish_name, d.dish_type, d.price
        FROM DishOrder dorder
        JOIN dish d ON dorder.id_dish = d.id
        WHERE dorder.id_order = ?
    """;

        try (Connection conn = dbConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(orderQuery)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        order = new Order();
                        order.setId(rs.getInt("id"));
                        order.setReference(rs.getString("reference"));
                        order.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
                        order.setOrderType(OrderType.valueOf(rs.getString("type")));
                        order.setOrderStatut(OrderStatus.valueOf(rs.getString("status")));
                        order.setDishOrderList(new ArrayList<>());
                    } else {
                        return null;
                    }
                }
            }


            try (PreparedStatement ps = conn.prepareStatement(dishOrderQuery)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DishOrder dishOrder = new DishOrder();
                        dishOrder.setId(rs.getInt("dish_order_id"));
                        dishOrder.setQuantity(rs.getInt("quantity"));

                        Dish dish = new Dish();
                        dish.setId(rs.getInt("id_dish"));
                        dish.setName(rs.getString("dish_name"));
                        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                        dish.setPrice(rs.getDouble("price"));

                        dishOrder.setDish(dish);
                        order.getDishOrderList().add(dishOrder);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la commande par ID", e);
        }

        return order;
    }

    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                         
                            select id_dish,dish.id, dish.name, dish.dish_type, dish.price 
                            from DishIngredient join dish on dish.id = id_dish join ingredient 
                            on ingredient.id= id_ingredient  where dish.id = ?;
                         
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                Dish dish = new Dish();
                dish.setId(resultSet.getInt("id_dish"));
                dish.setName(resultSet.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                dish.setPrice(resultSet.getDouble("price"));

                dish.setDishIngredients(findDishIngredientByDishId(id));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findDishIngredientByDishId(Integer idDish) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();

        List<DishIngredient> ingredients = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                     select id_ingredient, ingredient.id, ingredient.name, ingredient.price,
                     ingredient.category, dishingredient.id, dishingredient.quantity_required, dishingredient.unit
                     from dishIngredient join dish on dish.id = id_dish join 
                     ingredient on ingredient.id = id_ingredient 
                     where dish.id = ?
                       """);
            preparedStatement.setInt(1, idDish);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                DishIngredient dishingredient = new DishIngredient();
                Ingredient ingredient = new Ingredient();

                ingredient.setId(resultSet.getInt("id_ingredient"));
                ingredient.setName(resultSet.getString("name"));
                ingredient.setPrice(resultSet.getDouble("price"));

                String categoryStr = resultSet.getString("category");
                if (categoryStr != null) {
                    ingredient.setCategory(CategoryEnum.valueOf(categoryStr));
                }

                dishingredient.setId(resultSet.getInt("id"));
                dishingredient.setIngredient(ingredient);
                dishingredient.setQuantity(resultSet.getDouble("quantity_required"));

                String unitStr = resultSet.getString("unit");
                if (unitStr != null) {
                    dishingredient.setUnit(Unit.valueOf(unitStr));
                }

                ingredients.add(dishingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    Ingredient saveIngredient(Ingredient toSave) {
        String upsertIngredientSql = """
            INSERT INTO ingredient (id, name, price, category)
            VALUES (?, ?, ?, ?::ingredient_category)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                category = EXCLUDED.category,
                price = EXCLUDED.price
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer ingredientId;
            try (PreparedStatement ps = conn.prepareStatement(upsertIngredientSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                }
                ps.setString(2, toSave.getName());
                if (toSave.getPrice() != null) {
                    ps.setDouble(3, toSave.getPrice());
                } else {
                    ps.setNull(3, Types.DOUBLE);
                }
                if (toSave.getCategory() != null) {
                    ps.setString(4, toSave.getCategory().name());
                } else {
                    ps.setNull(4, Types.OTHER);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    ingredientId = rs.getInt(1);
                }
            }

            insertIngredientStockMovements(conn, ingredientId, toSave.getStockMovementList());

            conn.commit();
            return findIngredientById(ingredientId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void insertIngredientStockMovements(Connection conn, Integer ingredientId, List<StockMovement> stockMovementList) {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return;
        }
        String sql = """
        INSERT INTO stock_movement(id, id_ingredient, quantity, type, unit, creation_datetime)
        VALUES (?, ?, ?, ?::movement_type, ?::unit_type, ?)
        ON CONFLICT (id) DO NOTHING
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StockMovement stockMovement : stockMovementList) {
                if (stockMovement.getId() > 0) {
                    ps.setInt(1, stockMovement.getId());
                } else {
                    int newId = getNextSerialValue(conn, "stock_movement", "id");
                    ps.setInt(1, newId);
                    stockMovement.setId(newId);
                }

                ps.setInt(2, ingredientId);
                ps.setDouble(3, stockMovement.getValue().getQuantity());
                ps.setString(4, stockMovement.getType().name());
                ps.setString(5, stockMovement.getValue().getUnit().name());
                ps.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    Ingredient findIngredientById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        try (Connection connection = dbConnection.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select id, name, price, category from ingredient where id = ?;");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int idIngredient = resultSet.getInt("id");
                String name = resultSet.getString("name");

                String categoryStr = resultSet.getString("category");
                CategoryEnum category = (categoryStr != null) ? CategoryEnum.valueOf(categoryStr) : null;

                Double price = resultSet.getDouble("price");
                return new Ingredient(idIngredient, name, category, price, findStockMovementsByIngredientId(idIngredient));
            }
            throw new RuntimeException("Ingredient not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<StockMovement> findStockMovementsByIngredientId(Integer id) {

        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<StockMovement> stockMovementList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select id, quantity, unit, type, creation_datetime
                            from stock_movement
                            where stock_movement.id_ingredient = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                StockMovement stockMovement = new StockMovement();
                stockMovement.setId(resultSet.getInt("id"));
                stockMovement.setType(MovementTypeEnum.valueOf(resultSet.getString("type")));
                stockMovement.setCreationDatetime(resultSet.getTimestamp("creation_datetime").toInstant());

                StockValue stockValue = new StockValue();
                stockValue.setQuantity(resultSet.getDouble("quantity"));
                stockValue.setUnit(Unit.valueOf(resultSet.getString("unit")));
                stockMovement.setValue(stockValue);

                stockMovementList.add(stockMovement);
            }
            dbConnection.closeConnection(connection);
            return stockMovementList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    Dish saveDish(Dish toSave) {
        String upsertDishSql = """
            INSERT INTO dish (id, price, name, dish_type)
            VALUES (?, ?, ?, ?::dish_type)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                dish_type = EXCLUDED.dish_type,
                price = EXCLUDED.price
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                if (toSave.getPrice() != null) {
                    ps.setDouble(2, toSave.getPrice());
                } else {
                    ps.setNull(2, Types.DOUBLE);
                }
                ps.setString(3, toSave.getName());
                ps.setString(4, toSave.getDishType().name());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    dishId = rs.getInt(1);
                }
            }

            List<DishIngredient> newDishIngredients = toSave.getDishIngredients();
            if (newDishIngredients != null && !newDishIngredients.isEmpty()) {
                for (DishIngredient dishIngredient : newDishIngredients) {
                    if (dishIngredient.getIngredient() == null
                            || dishIngredient.getIngredient().getId() == null) {
                        throw new IllegalArgumentException(
                                "Tous les ingrédients doivent avoir un ID valide. " +
                                        "Les ingrédients doivent déjà exister dans la base de données.");
                    }

                    if (dishIngredient.getDish() == null) {
                        dishIngredient.setDish(new Dish());
                    }
                    dishIngredient.getDish().setId(dishId);
                }

                detachIngredients(conn, dishId);
                attachIngredients(conn, newDishIngredients);
            }

            conn.commit();
            return findDishById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void detachIngredients(Connection conn, Integer dishId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dishingredient WHERE id_dish = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void attachIngredients(Connection conn, List<DishIngredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        String attachSql = """
            insert into dishingredient (id, id_ingredient, id_dish, quantity_required, unit)
            values (?, ?, ?, ?, ?::unit_type)
        """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (DishIngredient dishIngredient : ingredients) {
                if (dishIngredient.getIngredient() == null
                        || dishIngredient.getIngredient().getId() == null) {
                    throw new IllegalStateException(
                            "L'ingrédient doit être sauvegardé avant d'être attaché à un plat");
                }

                int nextId = getNextSerialValue(conn, "dishingredient", "id");

                ps.setInt(1, nextId);
                ps.setInt(2, dishIngredient.getIngredient().getId());
                ps.setInt(3, dishIngredient.getDish().getId());
                ps.setDouble(4, dishIngredient.getQuantity());
                ps.setString(5, dishIngredient.getUnit().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return List.of();
        }
        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                        INSERT INTO ingredient (id, name, category, price)
                        VALUES (?, ?, ?::ingredient_category, ?)
                        RETURNING id
                    """;
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        int generatedId = rs.getInt(1);
                        ingredient.setId(generatedId);
                        savedIngredients.add(ingredient);
                    }
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }





    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        String syncSql = String.format(
                "SELECT setval('%s', GREATEST((SELECT COALESCE(MAX(%s), 0) + 1 FROM %s), nextval('%s'::regclass) - 1))",
                sequenceName, columnName, tableName, sequenceName
        );
        try (PreparedStatement ps = conn.prepareStatement(syncSql)) {
            ps.executeQuery();
        }
        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }


    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s), true)",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}

