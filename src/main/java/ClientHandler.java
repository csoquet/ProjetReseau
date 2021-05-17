import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;


    public ClientHandler(Socket client, InputStream input, OutputStream output) throws IOException{
        socket = client;
        inputStream = input;
        outputStream = output;
    }

    @Override
    public void run(){
        try{
            invoke();
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    void invoke() throws IOException, NoSuchAlgorithmException {
        Response response = new Response(inputStream,socket,outputStream);
        response.log();
        response.prepareResponseFile();
    }

}
