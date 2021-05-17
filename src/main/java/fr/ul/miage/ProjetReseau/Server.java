package fr.ul.miage.ProjetReseau;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
        Scanner sc = new Scanner(new File("properties.txt"));
        List<String> tab = new ArrayList<String>();
        while(sc.hasNextLine()){
            tab.add(sc.nextLine());
        }
        int port = Integer.parseInt(tab.get(1).substring(tab.get(1).indexOf(":")+1)); // Permet de récuperer le numéro de port dans la deuxième ligne du fichier
        new Server(new ServerSocket(port), Executors.newFixedThreadPool(availableCores)).start();
    }

    public void start()throws IOException{
        while (!connectionServer.isClosed()){
             Socket connection = connectionServer.accept();
             InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream();
             pool.execute(new ClientHandler(connection,in,out));
        }
    }

    private static String removeColon(String str) {
        str = str.substring(str.indexOf(":")+1);
        return str.trim();
    }
}