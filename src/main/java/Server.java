import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final ExecutorService pool;
    private final ServerSocket connectionServer;

    public Server(ServerSocket connectionServer, ExecutorService service) {
        this.pool = service;
        this.connectionServer = connectionServer;
    }

    public static void main(String[] args ) throws Exception {
        int availableCores = Runtime.getRuntime().availableProcessors();
        new Server(new ServerSocket(80), Executors.newFixedThreadPool(availableCores)).start();
    }

    public void start()throws IOException{
        while (!connectionServer.isClosed()){
             Socket connection = connectionServer.accept();
             InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             pool.execute(new ClientHandler(connection,in,out));
        }
    }
}