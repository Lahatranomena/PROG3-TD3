package org.dish;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        DataRetriever retriever = new DataRetriever();

//        Dish dish = retriever.findDishById(1);
//        System.out.println(dish.getGrossMargin());


//        String referenceToSearch = "ORD00001"; //
//
//        try {
//            Order order = retriever.findOrderByReference(referenceToSearch);
//            System.out.println("Commande trouvée : ");
//            System.out.println("ID : " + order.getId());
//            System.out.println("Référence : " + order.getReference());
//            System.out.println("Date de création : " + order.getCreationDatetime());
//            System.out.println("Type : " + order.getOrderType());
//            System.out.println("Status : " + order.getOrderStatut());
//
//            System.out.println("Détails des plats commandés :");
//            for (DishOrder dishOrder : order.getDishOrderList()) {
//                System.out.println("  Plat : " + dishOrder.getDish().getName()
//                        + ", Quantité : " + dishOrder.getQuantity()
//                        + ", Prix unitaire : " + dishOrder.getDish().getPrice()
//                        + ", Coût total : " + dishOrder.getDish().getDishCost());
//            }
//
//        } catch (RuntimeException e) {
//            System.out.println("Erreur : " + e.getMessage());
//        }

        try {
            Dish dish = retriever.findDishById(3);

            DishOrder dishOrder = new DishOrder();
            dishOrder.setDish(dish);
            dishOrder.setQuantity(1);

            Order order = new Order();
            order.setId(2);
            order.setReference("ORD00002");
            order.setCreationDatetime(Instant.now());
            order.setOrderType(OrderType.TAKE_AWAY);
            order.setOrderStatut(OrderStatus.DELIVERED);
            order.setDishOrderList(List.of(dishOrder));

            Order savedOrder = retriever.saveOrder(order);

            System.out.println("Commande sauvegardée avec succès :");
            System.out.println(savedOrder);

        } catch (RuntimeException e) {
            System.err.println("Erreur lors de la création de la commande : " + e.getMessage());
        }

//        try {
//            Order order = retriever.findOrderByReference("ORD00001");
//            order.setOrderStatut(OrderStatus.DELIVERED);
//
//            retriever.updateOrder(order);
//
//        } catch (RuntimeException e) {
//            System.out.println(e.getMessage());
//        }

//        DBConnection dbConnection = new DBConnection();
//
//        try (Connection conn = dbConnection.getConnection()) {
//
//            PreparedStatement ps =
//                    conn.prepareStatement("SELECT id, name FROM ingredient");
//
//            ResultSet rs = ps.executeQuery();
//
//            System.out.println("=== STOCK ACTUEL DES INGREDIENTS ===");
//
//            while (rs.next()) {
//                int ingredientId = rs.getInt("id");
//                String name = rs.getString("name");
//
//                Ingredient ingredient =
//                        retriever.findIngredientById(ingredientId);
//
//                StockValue stock =
//                        ingredient.getStockValueAt(Instant.now());
//
//                if (stock == null) {
//                    System.out.println(name + " : aucun mouvement");
//                } else {
//                    System.out.println(
//                            name + " : " +
//                                    stock.getQuantity() + " " +
//                                    stock.getUnit()
//                    );
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
////
//
//        Ingredient laitue = retriever.findIngredientById(1);
//        Ingredient tomate = retriever.findIngredientById(2);
//        Ingredient poulet = retriever.findIngredientById(3);
//
//        Dish salade = new Dish();
//        salade.setName("Salade César");
//        salade.setDishType(DishTypeEnum.MAIN);
//        salade.setPrice(5000.0);
//
//        List<DishIngredient> ingredients = new ArrayList<>();
//
//        DishIngredient di1 = new DishIngredient();
//        di1.setIngredient(laitue);
//        di1.setQuantity(0.10);
//        di1.setUnit(Unit.KG);
//        ingredients.add(di1);
//
//        DishIngredient di2 = new DishIngredient();
//        di2.setIngredient(tomate);
//        di2.setQuantity(0.5);
//        di2.setUnit(Unit.KG);
//        ingredients.add(di2);
//
//        salade.setDishIngredients(ingredients);
//
//        Dish savedDish = retriever.saveDish(salade);
//
//        System.out.println("Plat sauvegardé avec succès !");
//        System.out.println("ID : " + savedDish.getId());
//        System.out.println("Nom : " + savedDish.getName());
//        System.out.println("Type : " + savedDish.getDishType());
//        System.out.println("Prix : " + savedDish.getPrice());
//        System.out.println("Ingrédients : ");
//        for (DishIngredient di : savedDish.getDishIngredients()) {
//            System.out.println("- " + di.getIngredient().getName() +
//                    ", Quantité : " + di.getQuantity() + " " + di.getUnit());
//        }
    }
}
