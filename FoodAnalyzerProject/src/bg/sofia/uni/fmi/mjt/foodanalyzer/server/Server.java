package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.InvalidCommandException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception.NotInStorageException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage.FoodStorage;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.storage.Storage;
import com.google.zxing.NotFoundException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Server {
    Writer exceptionsFile;
    private Map<SocketChannel, String> clientInfoMap = new HashMap<>();
    private static final int BUFFER_SIZE = 20 * 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;

    private final int port;
    private boolean isServerWorking = true;

    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    private Selector selector;

    public Server(int port, CommandExecutor commandExecutor, Writer exceptionsFile) {
        this.port = port;
        this.commandExecutor = commandExecutor;
        this.exceptionsFile = exceptionsFile;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            while (isServerWorking) {
                try {
                    if (selector.select() == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            handleReadableKey(clientChannel, key, clientInfoMap.get(clientChannel));
                        } else if (key.isAcceptable()) {
                            accept(key);
                        }
                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    if (e.getMessage() != null) {
                        System.out.println("Error occurred while processing client request: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    private void handleReadableKey(SocketChannel clientChannel, SelectionKey key, String clientInfo) {
        try {
            writeClientOutput(clientChannel, read(key, clientInfo));
        } catch (IOException e) {
            key.cancel();
            try {
                clientChannel.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = channel.accept();
            InetSocketAddress clientAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            String clientInfo = clientAddress.getAddress().getHostAddress() + ":" + clientAddress.getPort();
            clientInfoMap.put(socketChannel, clientInfo);
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String read(SelectionKey key, String clientInfo) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            String message = getClientInput(channel);
            String reply = commandExecutor.execute(CommandCreator.newCommand(message));
            buffer.clear();
            return reply + System.lineSeparator();
        } catch (URISyntaxException | IOException | InvalidCommandException | NotInStorageException |
                 NotFoundException e) {
            return writeToFile(clientInfo + ": " + e.getMessage(), getStackTraceAsString(e));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public void stop() throws IOException {
        this.isServerWorking = false;
        exceptionsFile.close();
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private String writeToFile(String message, String stackTrace) {
        try {
            exceptionsFile.write(message + System.lineSeparator());
            exceptionsFile.write("Stack trace: " + stackTrace + System.lineSeparator());
            exceptionsFile.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] split = message.split(" ");
        StringBuilder res = new StringBuilder();
        res.append(split[1]);
        for (int i = 2; i < split.length; i++) {
            res.append(" ").append(split[i]);
        }
        return res.toString();
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return "";
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();
        clientChannel.write(buffer);
    }

    public static void main(String... args) throws IOException {
        final int port = 1111;
        Storage storage = new FoodStorage();
        CommandExecutor executor = new CommandExecutor(storage);
        Writer bf = new BufferedWriter(new FileWriter("C:\\Users\\Plamen Ovcharov\\Desktop\\exc.txt"));
        Server server = new Server(port, executor, bf);
        server.start();
    }
}

