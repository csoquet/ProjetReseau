import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

public class Response {

    //Données pour construire la réponse.
    private final String method;
    private final String path;
    private final String version;
    private final String host;

    //Données pour envoyer la réponse au client.
    private final List<String> headers;
    private final Socket socket;
    private final OutputStream outputStream;

    public Response(InputStream inputStream,Socket socket, OutputStream outputStream) throws IOException {
        this.socket = socket;
        this.outputStream = outputStream;

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }
        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        method = requestLine[0];
        path = requestLine[1];
        version = requestLine[2];
        host = requestsLines[1].split(" ")[1];

        headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }
    }

    private void sendResponse(String status, String contentType, byte[] content) throws IOException {
        outputStream.write(("HTTP/1.1 \r\n" + status).getBytes());
        outputStream.write(("ContentType: " + contentType + "\r\n").getBytes());
        outputStream.write(("Content-Encoding:" + "gzip" + "\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(content);
        outputStream.write("\r\n\r\n".getBytes());
        outputStream.flush();
        socket.close();
    }

    public void prepareResponseFile() throws IOException {
        Path filePath = getFilePath(path, host);
        //Si le fichier existe...
        if (Files.exists(filePath)) {
            String contentType = Files.probeContentType(filePath);
            byte[] data =  Files.readAllBytes(filePath); //Données à transmettre au client.
            sendResponse("200 OK", contentType,compressRessource(data));
        } else { //Si le fichier n'existe pas... Erreur 404
            byte[] notFoundContent = "<h1>Not found</h1>".getBytes();
            sendResponse("404 Not Found", "text/html", notFoundContent);
        }
    }

    private byte[] compressRessource(byte[] data) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        data = bos.toByteArray();
        bos.close();
        return data;
    }

    private Path getFilePath(String path, String host) throws IOException {
        Scanner sc = new Scanner(new File("properties.txt"));
        List<String> tab = new ArrayList<String>();
        while(sc.hasNextLine()){
            tab.add(sc.nextLine());
        }
        String chemin = tab.get(0).substring(tab.get(0).indexOf(":")+1); //Permet de récuperer le repertoire racine dans le fichier properties
        if ("/".equals(path)) {
            path = "/index.html";
        }
        return Paths.get( chemin + host, path);
    }

    public void log(){
        String time = java.time.LocalTime.now().toString();
        String accessLog = String.format(" [%s] | Client : %s, method : %s, path : %s, version : %s, host : %s, headers : %s",
                time,this.socket.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);
    }



}
