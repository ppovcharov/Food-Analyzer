package bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;

public interface URIFactory {
    String getURI(Command command);
}
