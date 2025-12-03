package bg.sofia.uni.fmi.mjt.foodanalyzer.client.uri;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.Command;

public abstract class URIGenerator implements URIFactory {
    protected static final String API_KEY = "RQfONStJkXa2ZMnnr0wa36wuAqXWVwfknO6dPMdH";

    public abstract String getURI(Command command);

}
