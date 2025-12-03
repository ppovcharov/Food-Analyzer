package bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.Food;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodSearchResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodWithNutrition;

import java.util.HashMap;
import java.util.Map;

public class FoodStorage implements Storage {
    private Map<String, FoodSearchResponse> keywordsMap;
    private Map<Integer, FoodWithNutrition> fdcIdMap;
    private Map<String, Food> gtinUpcMap;

    public FoodStorage() {
        keywordsMap = new HashMap<>();
        fdcIdMap = new HashMap<>();
        gtinUpcMap = new HashMap<>();
    }

    @Override
    public void addFoodResponseByKeywords(String keywords, FoodSearchResponse response) {
        keywordsMap.putIfAbsent(keywords, response);
    }

    @Override
    public void addFoodWithNutrition(FoodWithNutrition food) {
        fdcIdMap.putIfAbsent(food.getFdcId(), food);
    }

    @Override
    public FoodSearchResponse getByKeywords(String keywords) {
        return keywordsMap.get(keywords);
    }

    @Override
    public FoodWithNutrition getFoodWithNutritionByID(int id) {
        return fdcIdMap.get(id);
    }

    @Override
    public Food getByBarcode(String barcode) {
        return gtinUpcMap.get(barcode);
    }

    @Override
    public void addFoodWithBarcode(Food food) {
        String barcode = food.getUpc();
        gtinUpcMap.putIfAbsent(barcode, food);
    }
}
