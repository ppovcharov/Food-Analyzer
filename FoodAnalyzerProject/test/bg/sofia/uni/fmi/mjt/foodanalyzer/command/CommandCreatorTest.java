package bg.sofia.uni.fmi.mjt.foodanalyzer.command;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.InvalidCommandException;
import com.google.zxing.NotFoundException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CommandCreatorTest {

    @Test
    void testEmptyCommandCreation() {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand(""), "Empty command should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("   "), "Blank command should throw InvalidCommandException");
    }

    @Test
    void testGetReportInvalidCommand() {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-report"), "Command with no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("     get-food-report    "), "Non-trimmed command with no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-report    "), "Command with multiple spacings and no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-report \"abcd 1234\""), "Command with special characters should throw InvalidCommandException");
    }

    @Test
    void testGetFoodInvalidCommand() {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-fod"), "Command with typo should be invalid.");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food"), "Command with no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("     get-food    "), "Non-trimmed command with no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food    "), "Command with multiple spacings and no arguments should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand(null), "Null command should throw InvalidCommandException");
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food \"abcd 1234\""), "Command with special characters should throw InvalidCommandException");
    }

    @Test
    void testGetFoodValidCommandsWithMultipleArguments() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food", new String[]{"raffaello", "treat"});
        Command test = CommandCreator.newCommand("get-food raffaello treat");
        assertEquals(command.command(), test.command(), "Wrong command creation");
        assertTrue(command.arguments().length == test.arguments().length, "Wrong amount of arguments in command");
        assertTrue(test.arguments().length == 2, "For command get-food raffaello treat, command should have 2 arguments");
        assertEquals(command.arguments()[0], test.arguments()[0], "Wrong first argument");
        assertEquals(command.arguments()[1], test.arguments()[1], "Wrong second argument");
    }

    @Test
    void testGetFoodValidCommandsWithOneArgument() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food", new String[]{"raffaello"});
        Command test = CommandCreator.newCommand("get-food raffaello");
        assertEquals(command.command(), test.command(), "Wrong command creation");
        assertTrue(command.arguments().length == test.arguments().length, "Wrong amount of arguments in command");
        assertTrue(test.arguments().length == 1, "For command get-food raffaello treat, command should have 2 arguments");
        assertEquals(command.arguments()[0], test.arguments()[0], "Wrong argument");
    }

    @Test
    void testGetReportCommandCorrect() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-report", new String[]{"415269"});
        Command test = CommandCreator.newCommand("get-food-report 415269");
        assertEquals(command.command(), test.command(), "Wrong command creation");
        assertTrue(command.arguments().length == test.arguments().length, "Wrong amount of arguments in command");
        assertTrue(test.arguments().length == 1, "For command get-food raffaello treat, command should have 2 arguments");
        assertEquals(command.arguments()[0], test.arguments()[0], "Wrong argument");
    }

    @Test
    void testGetReportCommandMultipleCodes() throws InvalidCommandException, NotFoundException, IOException {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-report 415269 415269"), "Command with multiple codes should be invalid.");
    }

    @Test
    void testGetBarcodeCommandOnlyImage() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-by-barcode", new String[]{"009800146130"});
        Command test = CommandCreator.newCommand("get-food-by-barcode --img=barcode.gif");
        assertTrue(test.arguments()[0].equals("009800146130"), "Invalid code retrieved from barcode picture.");
        assertTrue(test.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test.arguments().length == 1, "There must be only one command argument.");
    }

    @Test
    void testGetBarcodeOnlyImageWrongPath() throws InvalidCommandException, NotFoundException, IOException {
        Command test = CommandCreator.newCommand("get-food-by-barcode --img=barcode.gif");
        assertThrows(IOException.class, () -> CommandCreator.newCommand("get-food-by-barcode --img=codebar.gif"));
    }

    @Test
    void testGetBarcodeCommandOnlyCode() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-by-barcode", new String[]{"009800146130"});
        Command test = CommandCreator.newCommand("get-food-by-barcode --code=009800146130");
        assertTrue(test.arguments()[0].equals("009800146130"), "Wrong code retrieved");
        assertTrue(test.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test.arguments().length == 1, "There must be only one command argument.");
    }

    @Test
    void testGetBarcode() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-by-barcode", new String[]{"009800146130"});
        Command test = CommandCreator.newCommand("get-food-by-barcode --code=009800146130 --img=barcode.gif");
        assertTrue(test.arguments()[0].equals("009800146130"), "Wrong code retrieved");
        assertTrue(test.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test.arguments().length == 1, "There must be only one command argument.");
    }

    @Test
    void testGetBarcodeIgnoresPicture() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-by-barcode", new String[]{"009800146130"});
        Command test = CommandCreator.newCommand("get-food-by-barcode --code=009800146130 --img=codebar.gif");
        assertTrue(test.arguments()[0].equals("009800146130"), "Wrong code retrieved");
        assertTrue(test.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test.arguments().length == 1, "There must be only one command argument.");
    }

    @Test
    void testGetBarcodeOrderDoesNotMatter() throws InvalidCommandException, NotFoundException, IOException {
        Command command = new Command("get-food-by-barcode", new String[]{"009800146130"});
        Command test = CommandCreator.newCommand("get-food-by-barcode --code=009800146130 --img=codebar.gif");
        Command test2 = CommandCreator.newCommand("get-food-by-barcode --img=codebar.gif --code=009800146130");
        assertTrue(test.arguments()[0].equals("009800146130"), "Wrong code retrieved");
        assertTrue(test.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test.arguments().length == 1, "There must be only one command argument.");
        assertTrue(test2.arguments()[0].equals("009800146130"), "Wrong code retrieved");
        assertTrue(test2.command().equals("get-food-by-barcode"), "Invalid first command.");
        assertTrue(test2.arguments().length == 1, "There must be only one command argument.");
    }

    @Test
    void testGetBarcodeMultipleCodes() {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-by-barcode --code=009800146130 --code=009800146130"), "get-food-by-barcode should be invalid if it has multiple codes provided.");
    }

    @Test
    void testGetBarcodeMultipleImages() {
        assertThrows(InvalidCommandException.class, () -> CommandCreator.newCommand("get-food-by-barcode --img=somepath --img=somepath"), "get-food-by-barcode should be invalid if it has multiple img paths provided.");
    }
}
