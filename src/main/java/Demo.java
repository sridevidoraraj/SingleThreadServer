import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Demo {
    static ColorLogger colorLogger = new ColorLogger();
    static {
        InputStream stream = Demo.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main( String[] args ) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(8085)){
            while (true) {
                colorLogger.logInfo("Server Started ");
            try (Socket client = serverSocket.accept()) {
                Instant start = Instant.now();
                colorLogger.logInfo("Starting Time : " +start);
                handleClient(client);
                Instant end = Instant.now();
                colorLogger.logInfo("End Time : " +end);
                long elapsedTime = Duration.between(start,end).toMillis();
                colorLogger.logInfo("Duration of Request and Response : " +elapsedTime+ " milliseconds");
                }
            }
        }
    }

    private static void handleClient(Socket client) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        String request = requestBuilder.toString();
        String[] requestsLines = request.split("\r\n");
        String[] requestLine = requestsLines[0].split(" ");
        String method = requestLine[0];
        String path = requestLine[1];
        String version = requestLine[2];
        String host = requestsLines[1].split(" ")[1];

        List<String> headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            String header = requestsLines[h];
            headers.add(header);
        }

        String accessLog = String.format("Client %s, method %s, path %s, version %s, host %s, headers %s",
                client.toString(), method, path, version, host, headers.toString());
        System.out.println(accessLog);


        Path filePath = getFilePath(path);
        if (Files.exists(filePath)) {
            // file exist
            String contentType = guessContentType(filePath);
            sendResponse(client, "200 OK", contentType, Files.readAllBytes(filePath));
        } else {
            // 404
//            byte[] notFoundContent = "<h1>Not found :(</h1>".getBytes();
//            badResponse(client,"404 Not Found", "text/html",notFoundContent);
//            String contentType = guessContentType(filePath);
            badResponse(client, "404 Not Found");

        }

    }

    private static void sendResponse(Socket client, String status, String contentType, byte[] content) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status+"\r\n").getBytes());
        clientOutput.write(("ContentType: " + contentType + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientOutput.close();
        client.close();
    }

    private static void badResponse(Socket client, String status) throws IOException {
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status+"\r\n").getBytes());
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        clientOutput.close();
        client.close();
    }

//    private static void badResponse(Socket client) throws IOException {
//        OutputStream clientOutput = client.getOutputStream();
//        clientOutput.write("HTTP/1.1 404 Not Found\r\n".getBytes());
//        clientOutput.write("\r\n\r\n".getBytes());
//        clientOutput.flush();
//        client.close();
//    }
//        OutputStream clientOutput = client.getOutputStream();
//        clientOutput.write(status.getBytes());
//        clientOutput.write(contentType.getBytes());
//        clientOutput.write("\r\n".getBytes());
////        clientOutput.write(content);
//        clientOutput.flush();
//        clientOutput.close();
//        client.close();

//        clientOutput.write(("ContentType: text/html\r\n").getBytes());
//        clientOutput.write("\r\n".getBytes());
//        clientOutput.write("<b>It works!</b>".getBytes());


    private static Path getFilePath(String path) {
        if ("/".equals(path)) {
            path = "/index.html";
        }

        return Paths.get("D:/Projects/HTML/sample", path);
    }

    private static String guessContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }

}
