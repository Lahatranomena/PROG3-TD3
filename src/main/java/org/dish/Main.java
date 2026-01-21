package org.dish;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        // Log before changes
        DataRetriever dataRetriever = new DataRetriever();
        Dish dish = dataRetriever.findDishById(4

        );
        System.out.println(dish);

        //Log after Object changes;
        dish.setIngredients(List.of(new Ingredient(1), new Ingredient(2)));
        Dish newDish = dataRetriever.saveDish(dish);
        System.out.println(newDish);

        // Ingredient creations
        //List<Ingredient> createdIngredients = dataRetriever.createIngredients(List.of(new Ingredient(null, "Fromage", CategoryEnum.DAIRY, 1200.0)));
        //System.out.println(createdIngredients);
    }
}
