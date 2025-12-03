package bg.sofia.uni.fmi.mjt.foodanalyzer.client.command;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.NotInStorageException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.Food;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodSearchResponse;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.food.FoodWithNutrition;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage.Storage;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri.GetFoodReportURIGenerator;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri.GetFoodURIGenerator;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri.URIFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {
    private Storage storage;

    public CommandExecutor(Storage storage) {
        this.storage = storage;
    }

    public String execute(Command command) throws URISyntaxException,
            NotInStorageException, ExecutionException, InterruptedException, InvalidCommandException {
        switch (command.command()) {
            case "get-food":
                if (checkKeywordsMap(command).isEmpty()) {
                    return executeGetFood(command);
                } else {
                    return checkKeywordsMap(command);
                }
            case "get-food-report":
                if (checkIdMap(command).isEmpty()) {
                    return executeGetReport(command);
                } else {
                    return checkIdMap(command);
                }
            case "get-food-by-barcode":
                if (getFoodByUpc(command).isEmpty()) {
                    throw new NotInStorageException("Element with barcode "
                            + command.arguments()[0] + " not found in storage.");
                } else {
                    return getFoodByUpc(command);
                }

        }
        return "";
    }

    private String checkKeywordsMap(Command command) {
        String keywords = getKeyWords(command.arguments());
        FoodSearchResponse response = storage.getByKeywords(keywords);
        if (response == null) {
            return "";
        }
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        return prettyGson.toJson(response.foods());
    }

    private String executeGetFood(Command command) throws URISyntaxException,
            ExecutionException, InterruptedException, NotInStorageException {
        URIFactory factory = new GetFoodURIGenerator();
        URI uri;
        try {
            uri = new URI(factory.getURI(command));
        } catch (URISyntaxException e) {
            throw new URISyntaxException("No special characters allowed in query.", "");
        }
        try (ExecutorService executor = Executors.newCachedThreadPool();
             HttpClient client = HttpClient.newBuilder().executor(executor).build();) {
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
            CompletableFuture<String> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);
            Gson gson = new Gson();
            FoodSearchResponse response = gson.fromJson(future.get(), FoodSearchResponse.class);
            if (response.foods().isEmpty()) {
                throw new NotInStorageException("There is no information about this product");
            }
            future.thenAcceptAsync(jsonResponse -> {
                addToKeywordsMap(getKeyWords(command.arguments()), jsonResponse);
            }, executor);

            String result = prettyPrintJsonResponseWithKeywords(future.get());
            future.join();
            return result;
        }
    }

    private void addToKeywordsMap(String keywords, String jsonResponse) {
        Gson gson = new Gson();
        FoodSearchResponse response = gson.fromJson(jsonResponse, FoodSearchResponse.class);
        storage.addFoodResponseByKeywords(keywords, response);
        for (Food f : response.foods()) {
            storage.addFoodWithBarcode(f);
        }
    }

    private String getKeyWords(String[] args) {
        StringBuilder res = new StringBuilder();
        res.append(args[0]);
        for (int i = 1; i < args.length; i++) {
            res.append(" ").append(args[i]);
        }
        return res.toString();
    }

    private String prettyPrintJsonResponseWithKeywords(String jsonResponse) {
        Gson gson = new Gson();
        FoodSearchResponse response = gson.fromJson(jsonResponse, FoodSearchResponse.class);
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonResponse = prettyGson.toJson(response.foods());
        return prettyJsonResponse;
    }

    private String checkIdMap(Command command) throws InvalidCommandException {
        int id;
        try {
            id = Integer.parseInt(command.arguments()[0]);
        } catch (NumberFormatException e) {
            throw new InvalidCommandException(command.arguments()[0] + " is not a valid code.");
        }
        FoodWithNutrition nutri = storage.getFoodWithNutritionByID(id);
        if (nutri == null) {
            return "";
        }
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonResponse = prettyGson.toJson(nutri);
        return prettyJsonResponse;
    }

    private String executeGetReport(Command command) throws URISyntaxException,
            ExecutionException, InterruptedException {
        URIFactory factory = new GetFoodReportURIGenerator();
        URI uri;
        try {
            uri = new URI(factory.getURI(command));
        } catch (URISyntaxException e) {
            throw new URISyntaxException("No special characters allowed in query.", "");
        }
        try (ExecutorService executor = Executors.newCachedThreadPool();
             HttpClient client = HttpClient.newBuilder().executor(executor).build();) {
            HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

            CompletableFuture<String> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body);

            future.thenAcceptAsync(jsonResponse -> {
                addToIDMap(jsonResponse);
            }, executor);

            String responseReport = prettyPrintJsonResponseReport(future.get());
            future.join();
            return responseReport;
        }
    }

    private String prettyPrintJsonResponseReport(String jsonResponse) {
        Gson gson = new Gson();
        FoodWithNutrition response = gson.fromJson(jsonResponse, FoodWithNutrition.class);
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        return prettyGson.toJson(response);
    }

    private void addToIDMap(String jsonResponse) {
        Gson gson = new Gson();
        FoodWithNutrition response = gson.fromJson(jsonResponse, FoodWithNutrition.class);
        storage.addFoodWithNutrition(response);
        addToBarcodeMap(response);
    }

    private void addToBarcodeMap(FoodWithNutrition food) {
        Gson gson = new Gson();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonResponse = prettyGson.toJson(food);
        Food f = gson.fromJson(prettyJsonResponse, Food.class);
        storage.addFoodWithBarcode(f);
    }

    private String getFoodByUpc(Command command) {
        String barcode = command.arguments()[0];
        Food food = storage.getByBarcode(barcode);
        if (food == null) {
            return "";
        }
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonResponse = prettyGson.toJson(food);
        return prettyJsonResponse;
    }

}
