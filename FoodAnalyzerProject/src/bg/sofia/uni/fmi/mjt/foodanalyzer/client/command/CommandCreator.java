package bg.sofia.uni.fmi.mjt.foodanalyzer.client.command;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.barcode.BarcodeResolver;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.InvalidCommandException;
import com.google.zxing.NotFoundException;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandCreator {
    private static final int MAX_BARCODE_COMMAND_SIZE = 3;
    private static final String CODE_START = "--code=";
    private static final String IMG_START = "--img=";

    public static Command newCommand(String clientInput) throws NotFoundException, IOException,
            InvalidCommandException {
        if (clientInput == null || clientInput.isBlank()) {
            throw new InvalidCommandException("Command cannot be empty.");
        }
        if (clientInput.contains("\"")) {
            throw new InvalidCommandException("Command cannot contain special characters like \".");
        }
        return getCommand(clientInput.trim());
    }

    private static Command getCommand(String input) throws NotFoundException, IOException, InvalidCommandException {
        String[] split = input.split(" ");
        switch (split[0]) {
            case "get-food":
                return getFoodCommand(split);
            case "get-food-report":
                return getFoodReportCommand(split);
            case "get-food-by-barcode":
                return getFoodByBarcodeCommand(input);

        }
        throw new InvalidCommandException("Invalid command");
    }

    private static Command getFoodCommand(String[] split) throws InvalidCommandException {
        if (split == null || split.length <= 1) {
            throw new InvalidCommandException("get-food command is invalid");
        }
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        return new Command(split[0], args);
    }

    private static Command getFoodReportCommand(String[] split) throws InvalidCommandException {
        if (split == null || split.length != 2) {
            throw new InvalidCommandException("get-food-report command is invalid");
        }
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        return new Command(split[0], args);
    }

    private static Command getFoodByBarcodeCommand(String input) throws NotFoundException,
            IOException, InvalidCommandException {
        if (countOccurrences(input, "--code=") > 1) {
            throw new InvalidCommandException("get-food-by-barcode should provide only one code.");
        }
        if (countOccurrences(input, "--img=") > 1) {
            throw new InvalidCommandException("get-food-by-barcode should provide only one image path.");
        }
        String[] split = input.split("\\s+(?=(--code|--img))");
        if (split == null || split.length > MAX_BARCODE_COMMAND_SIZE || split.length <= 1) {
            throw new InvalidCommandException("get-food-by-barcode command is invalid");
        }
        if (split.length == 2) {
            if (split[1].startsWith(CODE_START)) {
                String code = split[1].substring(CODE_START.length());
                return new Command(split[0], new String[]{code});
            }
            if (split[1].startsWith(IMG_START)) {
                String code = BarcodeResolver.resolveImage(split[1].substring(IMG_START.length()));
                return new Command(split[0], new String[]{code});
            }
        }
        if (split[1].startsWith(CODE_START)) {
            String code = split[1].substring(CODE_START.length());
            return new Command(split[0], new String[]{code});
        }
        String code = split[2].substring(CODE_START.length());
        return new Command(split[0], new String[]{code});
    }

    private static int countOccurrences(String text, String substr) {
        Pattern p = Pattern.compile(substr);
        Matcher m = p.matcher(text);
        int count = 0;
        while (m.find()) {
            count += 1;
        }
        return count;
    }

}
