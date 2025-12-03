package bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;

public class GetFoodURIGenerator extends URIGenerator {
    private static final String KEYWORD_SEPARATOR = "%20";
    private static final String FIRST_PART_OF_QUERY = "https://api.nal.usda.gov/fdc/v1/foods/search?query=";
    private static final String SECOND_PART_OF_QUERY = "&requireAllWords=true&api_key=" + API_KEY;

    @Override
    public String getURI(Command command) {
        String path = getPath(command.arguments());
        return FIRST_PART_OF_QUERY + path + SECOND_PART_OF_QUERY;
    }

    private String getPath(String[] keyWords) {
        StringBuilder res = new StringBuilder();
        res.append(keyWords[0]);
        for (int i = 1; i < keyWords.length; i++) {
            res.append(KEYWORD_SEPARATOR);
            res.append(keyWords[i]);
        }
        return res.toString();
    }

}
