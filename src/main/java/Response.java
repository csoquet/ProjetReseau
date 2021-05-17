import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;


public class Response {

    //Données pour construire la réponse.
    private final String method;
    private String path;
    private final String version;
    private final String host;

    //Données pour envoyer la réponse au client.
    private final List<String> headers;
    private final Socket socket;
    private final OutputStream outputStream;
    private String cookies = "";

    public Response(InputStream inputStream,Socket socket, OutputStream outputStream) throws IOException {
        this.socket = socket;
        this.outputStream = outputStream;

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        //On commence le parsing de la requete...
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
            if(header.contains("Cookie")){ //On  extrait les cookies pour gérer l'authentification.
                this.cookies = header.substring(header.indexOf(":")+1);
            }
            headers.add(header);
        }
    }

    //Construction d'une réponse pour le client.
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

    //Surcharge de la méthode réponse, celle-ci est capable d'envoyer un cookie.
    private void sendResponse(String status, String contentType, byte[] content,String cookie,String cookieValue) throws IOException {
        outputStream.write(("HTTP/1.1 \r\n" + status).getBytes());
        outputStream.write(("ContentType: " + contentType + "\r\n").getBytes());
        outputStream.write(("Set-Cookie:" + cookie + "=" + cookieValue + "\r\n").getBytes());
        outputStream.write(("Content-Encoding:" + "gzip" + "\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(content);
        outputStream.write("\r\n\r\n".getBytes());
        outputStream.flush();
        socket.close();
    }

    public void prepareResponseFile() throws IOException, NoSuchAlgorithmException {
        Path filePath = getFilePath(path, host, false);
        boolean connected = false;
        if(path.contains("?")){ //Il y a des paramètres dans la requête.
            //Récupération de l'username et du password da la requête GET.
            String username = path.substring(path.indexOf('=')+1,path.indexOf('&'));
            String password = path.substring(path.lastIndexOf('=')+1,path.length());

            //Récupération de l'username et du password du fichier htpasswd.
            File file = new File("tmp/www/"+host+"/.htpasswd");;
            Scanner sc = new Scanner(file);
            sc.useDelimiter("\\R");
            String actualUsername = sc.next();
            String actualPassword = sc.next();

            if(getMD5(password).equals(actualPassword) && username.equals(actualUsername)){
                path = "/";
                filePath = getFilePath(path,host,true);
                connected = true;
            }
        }
        //Si le fichier existe...
        if (Files.exists(filePath)) {
            System.out.println(filePath);
            String contentType = Files.probeContentType(filePath);
            byte[] data =  Files.readAllBytes(filePath); //Données à transmettre au client.
            if(connected){ // Si l'authentification est valide on set un cookie qui correspond a l'host courant...
                sendResponse("200 OK", contentType,compressRessource(data),host,"allowed");
            }else {
                sendResponse("200 OK", contentType,compressRessource(data));
            }
        } else { //Si le fichier n'existe pas... Erreur 404
            byte[] notFoundContent = "<h1>Not found</h1>".getBytes();
            sendResponse("404 Not Found", "text/html", notFoundContent);
        }
    }
    //Permet de récupérer un String qui représente le password passé en parametre.
    private String getMD5(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(password.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }

    //Permet de compresser un fichier au format GZIP.
    private byte[] compressRessource(byte[] data) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        data = bos.toByteArray();
        bos.close();
        return data;
    }

    private Path getFilePath(String path, String host,boolean allowed) throws IOException {
        //Lecture du fichier properties.
        Scanner sc = new Scanner(new File("properties.txt"));
        List<String> tab = new ArrayList<String>();
        while(sc.hasNextLine()){
            tab.add(sc.nextLine());
        }
        String chemin = tab.get(0).substring(tab.get(0).indexOf(":")+1); //Permet de récuperer le repertoire racine dans le fichier properties
        if ("/".equals(path)) {
            path = "/index.html";
        }
        //SI le dossier courant contient un fichier htpasswd alors il est protégé, il faut vérifier si l'utilisateur à le droit d'y accèder.
        File currentFolder = new File(chemin+host+"/.htpasswd");
        boolean folderIsProtected = currentFolder.exists();
        if(folderIsProtected && !allowed && !this.cookies.contains(host)){ //Un cookie contenant le host existe alors l'utilisateur à le droit d'accèder à la ressource.
            return  Paths.get(chemin+"loginPage/index.html");
        }else{
            return Paths.get(chemin + host, path);
        }
    }

    public void log(){
        String time = java.time.LocalTime.now().toString();
        String accessLog = String.format(" [%s] | Client : %s, method : %s, path : %s, version : %s, host : %s, headers : %s",
                time,this.socket.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);
    }



}
