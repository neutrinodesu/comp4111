import org.apache.http.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    public static void main(String[] args) throws Exception {
        int listenPort = 8080;
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(15000)
                .setTcpNoDelay(true)
                .build();
        final org.apache.http.impl.bootstrap.HttpServer httpServer = ServerBootstrap.bootstrap()
                .setListenerPort(listenPort)
                .setServerInfo("Test/1.1")
                .setSocketConfig(socketConfig)
                .setSslContext(null)
                .setExceptionLogger(new StdErrorExceptionLogger())
                .registerHandler("*", new HttpTestHandler())
                .create();
        httpServer.start();
        httpServer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> httpServer.shutdown(5, TimeUnit.SECONDS)));
    }

    static class StdErrorExceptionLogger implements ExceptionLogger {
        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println("Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }
    }

    static class HttpTestHandler implements HttpRequestHandler {
        public HttpTestHandler() {
            super();
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws IOException {

            //Read the request body
            String requestBody = "";
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
                InputStream stream = httpEntity.getContent();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                String input;
                while ((input = bufferedReader.readLine()) != null)
                    builder.append(input);
                requestBody = builder.toString();
                bufferedReader.close();
            }

            //Read the request line
            String requestLine = request.getRequestLine().getUri();
            requestLine = URLDecoder.decode(requestLine, "UTF-8");

            //Read the request method
            String requestMethod = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);

            //Store the parameters in statement string[]
            String[] parameter = new String[4];

            //User Log In
            if (requestLine.equals("/BookManagementService/login")) {
                parameter[0] = "LOGIN";
                parameter[1] = requestBody;
            }

            //User Log Out
            if (requestLine.contains("logout?token=")) {
                String token = requestLine.substring(requestLine.indexOf('=') + 1);
                parameter[0] = "LOGOUT";
                parameter[1] = token;
            }

            //Operations on Books
            if (requestLine.contains("books")) {
                switch (requestMethod) {
                    //Adding books
                    case "POST": {
                        String token = requestLine.substring(requestLine.indexOf('=') + 1);
                        parameter[0] = "ADD";
                        parameter[1] = requestBody;
                        parameter[2] = token;
                        break;
                    }
                    //Book lookup
                    case "GET": {
                        String token = requestLine.substring(requestLine.indexOf("token=") + 6);
                        //Transfer to parameters
                        String condition = requestLine.replaceAll("/BookManagementService/books[?]", "{\"");
                        condition = condition.replaceAll("&token=".concat(token), "");
                        condition = condition.replaceAll("token=".concat(token).concat("&"), "");
                        //If no conditions
                        condition = condition.replaceAll("token=".concat(token), "");
                        condition = condition.replaceAll("=", "\":\"");
                        condition = condition.replaceAll("&", "\",\"");
                        condition = condition.concat("\"}");

                        parameter[0] = "LOOKUP";
                        parameter[1] = condition;
                        parameter[2] = token;
                        break;
                    }
                    //Book loaning
                    case "PUT": {
                        String token = requestLine.substring(requestLine.indexOf('=') + 1);
                        String query = requestLine.replaceAll("/BookManagementService/books/", "");
                        String bookID = query.substring(0, query.indexOf('?'));
                        requestBody = requestBody.toLowerCase();

                        parameter[0] = "LOANRETURN";
                        parameter[1] = bookID;
                        parameter[2] = token;

                        if (requestBody.contains("false")) {
                            parameter[3] = "0";//Loan
                        } else {
                            parameter[3] = "1";//Return
                        }
                        break;
                    }
                    //Book deletion
                    case "DELETE": {
                        String token = requestLine.substring(requestLine.indexOf('=') + 1);
                        String query = requestLine.replaceAll("/BookManagementService/books/", "");
                        if (query.contains("?")) {
                            String bookID = query.substring(0, query.indexOf('?'));
                            parameter[0] = "DELETE";
                            parameter[1] = bookID;
                            parameter[2] = token;
                        } else {
                            parameter[0] = "DELETE";
                            parameter[1] = "";
                            parameter[2] = "";
                        }
                        break;
                    }
                }
            }

            //Transaction behaviors
            if (requestLine.contains("transaction")) {
                String token = requestLine.substring(requestLine.indexOf('=') + 1);
                //
                if (requestMethod.equals("POST")) {
                    //Step 1
                    if (requestBody.equals("")) {
                        parameter[0] = "T1";
                        parameter[1] = token;
                    }

                    // Step 3
                    else {
                        parameter[0] = "T3";
                        parameter[1] = requestBody;
                        parameter[2] = token;
                    }
                }

                // PUT means Step 2
                else {
                    parameter[0] = "T2";
                    parameter[1] = requestBody;
                    parameter[2] = token;
                }
            }

            // Pass parameters and Http response to DBTrial class to process the database
            Database.main(parameter, response);
        }
    }
}