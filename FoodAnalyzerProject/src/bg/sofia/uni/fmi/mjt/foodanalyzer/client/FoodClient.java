package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FoodClient {
    private static final int BUFFER_SIZE = 20 * 1024;
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private String serverHost;
    private int serverPort;

    public FoodClient(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    private void run() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
            while (true) {
                System.out.println("Enter message: ");
                String command = scanner.nextLine();
                if (command.isBlank()) {
                    System.out.println("Command cannot be empty");
                    continue;
                }
                if ("Done".equalsIgnoreCase(command.split(" ")[0])) {
                    System.out.println("Bye!");
                    break;
                }
                buffer.clear();
                buffer.put(command.getBytes());
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                System.out.println(new String(byteArray));
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to the server. Please try again later.");
        }
    }

    public static void main(String[] args) {
        final String host = "localhost";
        final int port = 1111;
        FoodClient foodClient = new FoodClient(host, port);
        foodClient.run();
    }
}