package org.dish;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        DataRetriever dataRetriever = new DataRetriever();

//        System.out.println(dataRetriever.findDishById(5));

        System.out.println(dataRetriever.findIngredientByDishId(2));

//        Ingredient ingredient = new Ingredient(5, "Fromage fond", CategoryEnum.MARINE, 3000.0);
//        Ingredient ingredient1 = new Ingredient(6, "Pain", CategoryEnum.MARINE, 2500.0);
//
//        dataRetriever.createIngredients(List.of(ingredient, ingredient1));
//        Log after Object changes;
//        dish.setIngredients(List.of(new DishIngredient(), new DishIngredient()));
//        Dish newDish = dataRetriever.saveDish(dish);
//        System.out.println(newDish);

//        DishIngredient dishing = new DishIngredient(24, new Ingredient(3, "Chocolat", CategoryEnum.OTHER, 3000.00), 3.0, Unit.PCS);
//        DishIngredient dishing1 = new DishIngredient(25, new Ingredient(5, "Beurre", CategoryEnum.OTHER, 2500.00), 0.5, Unit.KG);
//        DishIngredient dishing2 = new DishIngredient(30, new Ingredient(1, "Laitue", CategoryEnum.OTHER, 3500.00), 3.0, Unit.KG);
//
//        Dish dish = new Dish(23, "Pain choco",DishTypeEnum.MAIN, 1000.00, List.of(dishing, dishing1, dishing2));
//
//        System.out.println(dataRetriever.saveDish(dish));
//        Ingredient tomato = new Ingredient();
//        tomato.setId(1);
//        tomato.setName("Tomato");
//        tomato.setCategory(CategoryEnum.VEGETABLE);
//        tomato.setPrice(0.50);
//
//        Ingredient cheese = new Ingredient();
//        cheese.setId(2);
//        cheese.setName("Cheese");
//        cheese.setCategory(CategoryEnum.DAIRY);
//        cheese.setPrice(2.00);
//
//        Dish pizza = new Dish();
//        pizza.setId(3);
//        pizza.setName("Pizza");
//        pizza.setDishType(DishTypeEnum.MAIN);
//
//        List<DishIngredient> ingredients = new ArrayList<>();
//
//        DishIngredient di1 = new DishIngredient();
//        di1.setIngredient(tomato);
//        di1.setQuantity(200.0);
//        di1.setUnit(Unit.KG);
//
//        DishIngredient di2 = new DishIngredient();
//        di2.setIngredient(cheese);
//        di2.setQuantity(150.0);
//        di2.setUnit(Unit.KG);
//
//        ingredients.add(di1);
//        ingredients.add(di2);
//
//        pizza.setIngredients(ingredients);
//
//        DataRetriever dishDAO = new DataRetriever();
//        try {
//            Dish savedPizza = dishDAO.saveDish(pizza);
//            System.out.println("Plat sauvegardé avec succès !");
//            System.out.println("ID : " + savedPizza.getId());
//            System.out.println("Nom : " + savedPizza.getName());
//            System.out.println("Type : " + savedPizza.getDishType());
//            System.out.println("Ingrédients : ");
//            for (DishIngredient di : savedPizza.getIngredients()) {
//                System.out.println("- " + di.getIngredient().getName() +
//                        ", Quantité : " + di.getQuantity() + " " + di.getUnit());
//            }
//        } catch (RuntimeException e) {
//            System.err.println("Erreur lors de la sauvegarde du plat : " + e.getMessage());
//            e.printStackTrace();
//        }
            // Ingredient creations
        //List<Ingredient> createdIngredients = dataRetriever.createIngredients(List.of(new Ingredient(null, "Fromage", CategoryEnum.DAIRY, 1200.0)));
        //System.out.println(createdIngredients);



    }
}
