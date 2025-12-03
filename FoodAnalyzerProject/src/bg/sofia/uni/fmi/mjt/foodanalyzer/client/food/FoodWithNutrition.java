package bg.sofia.uni.fmi.mjt.foodanalyzer.client.food;

public class FoodWithNutrition {
    private String description;
    private String ingredients;
    private int fdcId;
    private String gtinUpc;
    private NutrientsHolder labelNutrients;

    public int getFdcId() {
        return fdcId;
    }
}
