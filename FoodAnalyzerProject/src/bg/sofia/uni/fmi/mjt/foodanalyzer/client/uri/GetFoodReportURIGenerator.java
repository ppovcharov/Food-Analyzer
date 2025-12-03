package bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;

public class GetFoodReportURIGenerator extends URIGenerator {
    private static final String FIRST_PART_OF_QUERY = "https://api.nal.usda.gov/fdc/v1/food/";
    private static final String SECOND_PART_OF_QUERY = "?api_key=" + API_KEY;

    @Override
    public String getURI(Command command) {
        return FIRST_PART_OF_QUERY + getFdcID(command) + SECOND_PART_OF_QUERY;
    }

    private String getFdcID(Command command) {
        return command.arguments()[0];
    }

}
