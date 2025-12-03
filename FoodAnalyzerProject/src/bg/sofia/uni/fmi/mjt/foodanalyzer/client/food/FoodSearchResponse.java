package bg.sofia.uni.fmi.mjt.foodanalyzer.client.food;

import java.util.List;

public record FoodSearchResponse(FoodSearchCriteria foodSearchCriteria,
                                 double totalHits,
                                 double currentPage,
                                 double totalPages,
                                 List<Food> foods) {
}
