package bg.sofia.uni.fmi.mjt.foodanalyzer.command;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.NotInStorageException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage.FoodStorage;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage.Storage;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri.URIFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class CommandExecutorTest {
    private Storage storage;
    private CommandExecutor executor;

    private String getFoodTwoResultResponse = "[\n" +
            "  {\n" +
            "    \"fdcId\": 2212521,\n" +
            "    \"description\": \"VERY YOUNG SMALL SWEET PEAS WITH MUSHROOMS \\u0026 PEARL ONIONS\",\n" +
            "    \"gtinUpc\": \"020000103617\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"fdcId\": 2614555,\n" +
            "    \"description\": \"VERY YOUNG SMALL WITH MUSHROOMS \\u0026 PEARL ONIONS SWEET PEAS\",\n" +
            "    \"gtinUpc\": \"0020000103617\"\n" +
            "  }\n" +
            "]";

    private String getFoodByBarcodeResponse = "{\n" +
            "  \"fdcId\": 2041155,\n" +
            "  \"description\": \"RAFFAELLO, ALMOND COCONUT TREAT\",\n" +
            "  \"gtinUpc\": \"009800146130\"\n" +
            "}";

    private String getFoodReport415269 = "{\n" +
            "  \"description\": \"RAFFAELLO, ALMOND COCONUT TREAT\",\n" +
            "  \"ingredients\": \"VEGETABLE OILS (PALM AND SHEANUT). DRY COCONUT, SUGAR, ALMONDS, SKIM MILK POWDER, WHEY POWDER (MILK), WHEAT FLOUR, NATURAL AND ARTIFICIAL FLAVORS, LECITHIN AS EMULSIFIER (SOY), SALT, SODIUM BICARBONATE AS LEAVENING AGENT.\",\n" +
            "  \"fdcId\": 415269,\n" +
            "  \"gtinUpc\": \"009800146130\",\n" +
            "  \"labelNutrients\": {\n" +
            "    \"fat\": {\n" +
            "      \"value\": 15.0\n" +
            "    },\n" +
            "    \"carbohydrates\": {\n" +
            "      \"value\": 12.0\n" +
            "    },\n" +
            "    \"fiber\": {\n" +
            "      \"value\": 0.99\n" +
            "    },\n" +
            "    \"protein\": {\n" +
            "      \"value\": 2.0\n" +
            "    },\n" +
            "    \"calories\": {\n" +
            "      \"value\": 190.0\n" +
            "    }\n" +
            "  }\n" +
            "}";
    private String getFoodRaffaelloTreatResponse = "[\n" +
            "  {\n" +
            "    \"fdcId\": 2041155,\n" +
            "    \"description\": \"RAFFAELLO, ALMOND COCONUT TREAT\",\n" +
            "    \"gtinUpc\": \"009800146130\"\n" +
            "  }\n" +
            "]";

    @BeforeEach
    void setUp() {
        storage = new FoodStorage();
        executor = new CommandExecutor(storage);
    }

    @Test
    void testGetFood() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food", new String[]{"raffaello", "treat"});
        assertEquals(executor.execute(command), getFoodRaffaelloTreatResponse, "Incorrect information retrieved.");
    }

    @Test
    void testGetFromStorage() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food", new String[]{"raffaello", "treat"});
        long startTime = System.nanoTime();
        String result = executor.execute(command);
        long elapsedTime = System.nanoTime() - startTime;
        long result2StartTime = System.nanoTime();
        String result2 = executor.execute(command);
        long result2ElapsedTime = result2StartTime - System.nanoTime();
        assertTrue(result2ElapsedTime < 20 * elapsedTime, "Completing the same query for a second time took too long.");

    }

    @Test
    void testGetFoodReport() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food-report", new String[]{"415269"});
        assertEquals(executor.execute(command), getFoodReport415269, "Invalid information retrieved.");
    }

    @Test
    void testGetFoodReportFromStorage() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food-report", new String[]{"415269"});
        long startTime = System.nanoTime();
        String result = executor.execute(command);
        assertEquals(result, getFoodReport415269, "Invalid information retrieved.");
        long elapsedTime = System.nanoTime() - startTime;
        long result2StartTime = System.nanoTime();
        String result2 = executor.execute(command);
        assertEquals(result2, getFoodReport415269, "Invalid information retrieved.");
        long result2ElapsedTime = result2StartTime - System.nanoTime();
        assertTrue(result2ElapsedTime < 20 * elapsedTime, "Completing the same query for a second time took too long.");
    }

    @Test
    void testGetProductInfoByBarcode() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food", new String[]{"raffaello", "treat"});
        String res = executor.execute(command);
        Command barcodeCommand = new Command("get-food-by-barcode", new String[]{"009800146130"});
        assertEquals(getFoodByBarcodeResponse, executor.execute(barcodeCommand), "Wrong output for get-food-by-barcode query");
    }

    @Test
    void testGetFoodInfoByBarcodeFail() {
        Command barcodeCommand = new Command("get-food-by-barcode", new String[]{"009800146130"});
        assertThrows(NotInStorageException.class, () -> executor.execute(barcodeCommand));
    }

    @Test
    void testGetFoodReportFalseID() {
        Command command = new Command("get-food-report", new String[]{"123asd123"});
        assertThrows(InvalidCommandException.class, () -> executor.execute(command), "get-food-report should be invalid if it does not provide a valid id.");
    }

    @Test
    void testGetFoodNoInformation() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food", new String[]{"ьььь"});
        assertThrows(NotInStorageException.class, () -> executor.execute(command));
    }

    @Test
    void testGetFoodMultipleResults() throws InvalidCommandException, NotInStorageException, URISyntaxException, ExecutionException, InterruptedException {
        Command command = new Command("get-food", new String[]{"very", "onions"});
        assertEquals(getFoodTwoResultResponse, executor.execute(command));
    }
}
