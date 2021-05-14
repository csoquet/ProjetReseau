import java.io.*;
import java.net.Socket;

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
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void invoke() throws IOException{
        Response response = new Response(inputStream,socket,outputStream);
        response.log();
        response.prepareResponseFile();
    }

}
