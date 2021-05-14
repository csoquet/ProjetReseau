import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Response {

    private final String method;
    private final String path;
    private final String version;
    private final String host;
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
        outputStream.write("\r\n".getBytes());
        outputStream.write(content);
        outputStream.write("\r\n\r\n".getBytes());
        outputStream.flush();
        socket.close();
    }

    public void prepareResponseFile() throws IOException {
        Path filePath = getFilePath(path, host);
        if (Files.exists(filePath)) {
            // file exist
            String contentType = guessContentType(filePath);
            sendResponse("200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            // 404
            byte[] notFoundContent = "<h1>Not found</h1>".getBytes();
            sendResponse("404 Not Found", "text/html", notFoundContent);
        }
    }

    private Path getFilePath(String path, String host) {
        if ("/".equals(path)) {
            path = "/index.html";
        }
        return Paths.get("tmp/www/" + host, path);
    }

    private String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

    public void log(){
        String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                this.socket.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);
    }

}
