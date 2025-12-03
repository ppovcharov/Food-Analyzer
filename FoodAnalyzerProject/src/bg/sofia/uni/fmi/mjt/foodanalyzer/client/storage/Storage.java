package bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.Food;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodSearchResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodWithNutrition;

public interface Storage {
    void addFoodResponseByKeywords(String keywords, FoodSearchResponse response);

    void addFoodWithNutrition(FoodWithNutrition food);

    void addFoodWithBarcode(Food food);

    FoodSearchResponse getByKeywords(String keywords);

    FoodWithNutrition getFoodWithNutritionByID(int id);

    Food getByBarcode(String barcode);

}
