package bg.sofia.uni.fmi.mjt.foodanalyzer.client.food;

public record FoodSearchCriteria(String query,
                                 String generalSearchInput,
                                 double pageNumber,
                                 boolean requireAllWords) {
}
