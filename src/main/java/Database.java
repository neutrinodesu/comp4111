import bean.book;
import bean.transaction;
import bean.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.Random;

public class Database {
    public static void main(String[] args, HttpResponse response) {
        //Initialization
        Connection connection = null;
        Statement statement = null;
        try {
            //Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost/COMP4111?" + "user=root&password=19971217ller&useSSL=false");
            statement = connection.createStatement();

            if (args[0].equals("LOGIN")) {
                login(statement, null, args[1], response);
            }

            if (args[0].equals("LOGOUT")) {
                logout(statement, null, args[1], response);
            }

            if (args[0].equals("ADD")) {
                addBook(statement, null, args[1], args[2], response);
            }

            if (args[0].equals("DELETE")) {
                deleteBook(statement, null, args[1], args[2], response);
            }

            if (args[0].equals("LOANRETURN")) {
                loanReturn(statement, null, args[1], args[2], args[3], response);
            }

            if (args[0].equals("LOOKUP")) {
                lookUp(statement, null, args[1], args[2], response);
            }

            //Transaction Step 1
            if (args[0].equals("T1")) {
                t1(statement, null, args[1], response);
            }

            //Transaction Step 2
            if (args[0].equals("T2")) {
                t2(statement, null, args[1], args[2], response);
            }

            //Transaction Step 3
            if (args[0].equals("T3")) {
                t3(statement, null, args[1], args[2], response);
            }
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            //Release recourse
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
    }

    //Transfer to sql statements
    public static String sqlHelper(String base) {
        return "'".concat(base).concat("'");
    }

    public static void login(Statement statement, ResultSet rs, String body, HttpResponse response) {
        ObjectMapper mapper = new ObjectMapper();//transfer java to json
        try {
            user userTemp1 = mapper.readValue(body, user.class);

            //If password or username is null or ""
            if (userTemp1.getPassword().length() == 0 || userTemp1.getUsername() == null
                || userTemp1.getPassword() == null || userTemp1. getUsername().length() == 0) {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400
                return;
            }

            String userTemp1Name = sqlHelper(userTemp1.getUsername());
            String rightPassword = "";
            String token = "";
            rs = statement.executeQuery("select * from userdb where username = ".concat(userTemp1Name));//compare to database 1 by 1
            while (rs.next()) {
                rightPassword = rs.getString(3);
                token = rs.getString(4);
            }

            //Incorrect password
            if (!userTemp1.getPassword().equals(rightPassword)) {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }

            //Non-null token
            else if (token != null) {
                response.setStatusCode(HttpStatus.SC_CONFLICT);//409
            } else {
                //Guarantee the token is unique
                String tokenHelper;
                do {
                    token = tokenGenerator();
                    tokenHelper = sqlHelper(token);
                    rs = statement.executeQuery("select id from userdb where token =".concat(tokenHelper));
                }
                while (rs.next());

                //Update token into database and response it in json format
                statement.executeUpdate("update userdb set token = ".concat(tokenHelper).concat("where username =").concat(userTemp1Name));
                String responseToken = "{ \"Token\" : \"".concat(token).concat("\"}");
                StringEntity se = new StringEntity(responseToken);//transfer to json content
                response.setEntity(se);
                response.setStatusCode(HttpStatus.SC_OK);//200
            }
        } catch (Exception e) {
            e.printStackTrace();//when try catches an error, e will deal with it
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
    }

    //TokenGenerator: xxxyyyy (x: a-z, y: 0-9)
    public static String tokenGenerator() {
        char[] token = new char[7];
        Random r = new Random();
        for (int i = 0; i < 3; i++) {
            int number = r.nextInt(26);
            token[i] = (char) ('a' + number);
        }
        for (int i = 3; i < 7; i++) {
            int number = r.nextInt(10);
            token[i] = (char) (number + '0');
        }
        return new String(token);
    }

    //Check if token is correct
    public static boolean checkToken(Statement statement, ResultSet rs, String token) {
        if (token != null) {
            String tokenHelper = sqlHelper(token);
            try {
                rs = statement.executeQuery("select id from userdb where token =".concat(tokenHelper));
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public static void logout(Statement statement, ResultSet rs, String token, HttpResponse response) {
        String tokenHelper = sqlHelper(token);
        try {
            rs = statement.executeQuery("select id from userdb where token =".concat(tokenHelper));
            if (rs.next()) {
                String id = rs.getString(1);

                //If token is correct, set to null and logout
                statement.executeUpdate("update userdb set token = null ".concat("where id = ").concat(id));
                response.setStatusCode(HttpStatus.SC_OK);
            } else {
                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
    }

    public static void addBook(Statement statement, ResultSet rs, String body, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                book book1 = mapper.readValue(body, book.class);
                String titleHelper = sqlHelper(book1.getTitle());
                rs = statement.executeQuery("select id from bookdb where title = ".concat(titleHelper));

                //If same title exists, return 409 Conflict
                if (rs.next()) {
                    response.setHeader("Duplicate record", "/books/".concat(rs.getString(1)));
                    response.setStatusCode(HttpStatus.SC_CONFLICT);//409
                } else {
                    String authorHelper = sqlHelper(book1.getAuthor());
                    String publisherHelper = sqlHelper(book1.getPublisher());
                    String year = Integer.toString(book1.getYear());
                    statement.executeUpdate("insert into bookdb set title = ".concat(titleHelper).concat(", author = ").
                            concat(authorHelper).concat(", publisher = ").concat(publisherHelper).
                            concat(", year =").concat(year));

                    //Get book id after adding it
                    rs = statement.executeQuery("select id from bookdb where title = ".concat(titleHelper));
                    String id = "";
                    if (rs.next()) {
                        id = rs.getString(1);
                    }

                    //Return id in response header
                    response.setHeader("Location", "/books/".concat(id));
                    response.setStatusCode(HttpStatus.SC_CREATED);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400
        }
    }

    public static void deleteBook(Statement statement, ResultSet rs, String id, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                if (isInteger(id)) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400
                } else {
                    rs = statement.executeQuery("select id from bookdb where id = ".concat(id));
                    if (rs.next()) {
                        statement.executeUpdate("delete from bookdb where id =".concat(id));
                        response.setStatusCode(HttpStatus.SC_OK);//400
                    } else {

                        // if the book needs deleting doesn't exist
                        response.setStatusCode(HttpStatus.SC_NOT_FOUND);//404
                        response.setReasonPhrase("No book record");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //Book Loaning and Book Returning
    public static void loanReturn(Statement statement, ResultSet rs, String id, String token, String status, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                if (isInteger(id)) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                } else {
                    rs = statement.executeQuery("select available from bookdb where id = ".concat(id));
                    if (rs.next()) {
                        int loanOrReturn = Integer.parseInt(status);//string to integer
                        int available = rs.getInt(1);

                        //Check if available satisfies loan or return
                        if (available != loanOrReturn) {
                            statement.executeUpdate("update bookdb set available = ".concat(status).concat(" where id = ").concat(id));
                            response.setStatusCode(HttpStatus.SC_OK);//200
                        } else {
                            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400
                        }
                    } else {
                        response.setStatusCode(HttpStatus.SC_NOT_FOUND);//404
                        response.setReasonPhrase("No book record");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400: token wrong
        }
    }

    //deal with like in sql
    public static String sqlLikeHelper(String condition) {
        return "'%".concat(condition).concat("%'");
    }

    //lookup sql for lookup book
    public static String lookUpSQL(String query) {
        //No constraints
        if (query.equals("{\"\"}")) {
            return "select * from bookdb";
        }

        // generate sql
        ObjectMapper mapper = new ObjectMapper();//java to json

        String sql = "select * from bookdb where ";

        try {
            book constraint = mapper.readValue(query, book.class);

            if (constraint.getId() > 0) {
                sql = sql.concat("id = ").concat(Integer.toString(constraint.getId())).concat(" and ");
            }
            if (constraint.getYear() > 0) {
                sql = sql.concat("year = ").concat(Integer.toString(constraint.getYear())).concat(" and ");
            }

            //Look up could be implemented even if not equal, so use like
            if (constraint.getAuthor() != null) {
                sql = sql.concat("author like ").concat(sqlLikeHelper(constraint.getAuthor())).concat(" and ");
            }
            if (constraint.getTitle() != null) {
                sql = sql.concat("title like ").concat(sqlLikeHelper(constraint.getTitle())).concat(" and ");
            }
            if (constraint.getPublisher() != null) {
                sql = sql.concat("publisher like ").concat(sqlLikeHelper(constraint.getPublisher())).concat(" and ");
            }
            if (query.contains("available")) {
                sql = sql.concat("available = ").concat(String.valueOf(constraint.isAvailable())).concat(" and ");
            }

            //If there is at least one condition, remove the last like
            if (sql.contains("=") || sql.contains("like")) {
                sql = sql.substring(0, sql.length() - 4);
            }
            //If there is not, remove where
            else {
                sql = sql.replace("where", "");
            }
            if (constraint.getSortby() != null) {
                sql = sql.concat("order by ").concat(constraint.getSortby()).concat(" ");
            }
            if (constraint.getOrder() != null) {
                sql = sql.concat(constraint.getOrder()).concat(" ");
            }
            if (constraint.getLimit() > 0) {
                sql = sql.concat("limit ").concat(Integer.toString(constraint.getLimit()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return sql;
    }

    //Generate lookup response json
    public static String lookUpResponse(ResultSet rs) {
        String response = "{\"FoundBooks\": ";
        try {
            if (rs.last()) {
                int rowNum = rs.getRow();
                response = response.concat(String.valueOf(rowNum)).concat(", \"Results\": [");
                rs.beforeFirst();
            }
            while (rs.next()) {
                response = response.concat("{\"Title\": \"").concat(rs.getString(2)).concat("\",");
                response = response.concat("\"Author\": \"").concat(rs.getString(3)).concat("\",");
                response = response.concat("\"Publisher\": \"").concat(rs.getString(4)).concat("\",");
                response = response.concat("\"Year\": ").concat(rs.getString(5)).concat("}, ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
        //Remove the last comma
        response = response.substring(0, response.length() - 2);
        response = response.concat("]}");
        return response;
    }

    public static int lookUpID(String query) {
        ObjectMapper mapper = new ObjectMapper();
        int bookID = 0;
        try{
            book constraint = mapper.readValue(query, book.class);
            bookID = constraint.getId();
        }  catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return bookID;
    }

    public static int lookUpLimit(String query) {
        ObjectMapper mapper = new ObjectMapper();
        int limit = 0;
        try{
            book constraint = mapper.readValue(query, book.class);
            limit = constraint.getLimit();
        }  catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return limit;
    }

    public static void lookUp(Statement statement, ResultSet rs, String query, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                if (query.contains("id")) {
                    int id = lookUpID(query);
                    if (id <= 0) {
                        response.setStatusCode(HttpStatus.SC_NO_CONTENT);
                    }
                } else if (query.contains("limit")) {
                    int limit = lookUpLimit(query);
                    if (limit <= 0) {
                        response.setStatusCode(HttpStatus.SC_NO_CONTENT);
                    }
                 } else {
                    String sql = lookUpSQL(query);
                    rs = statement.executeQuery(sql);

                    //If there is record found
                    if (rs.next()) {
                        String responseMessage = lookUpResponse(rs);
                        //Return in json format
                        StringEntity responseEntity = new StringEntity(responseMessage);
                        response.setEntity(responseEntity);
                        response.setStatusCode(HttpStatus.SC_OK);
                    } else {
                        response.setStatusCode(HttpStatus.SC_NO_CONTENT);//204
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //Transaction Step 1: generate a transaction id
    public static void t1(Statement statement, ResultSet rs, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                rs = statement.executeQuery("select  transaction_id from transactiondb ");
                if (rs.next()) {
                    String transactionId = rs.getString(1);

                    //Set available = 0 after getting transaction id
                    statement.executeUpdate("update transactiondb set available = 0 where transaction_id = ".concat(transactionId));

                    //Return transaction id in json format
                    String responseMessage = "{\"Transaction\":".concat(transactionId).concat("}");
                    StringEntity responseEntity = new StringEntity(responseMessage);
                    response.setEntity(responseEntity);
                    response.setStatusCode(HttpStatus.SC_OK);
                } else {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //Transaction Step 2: preparation check
    public static boolean t2check(String prev, String cur, Statement statement, ResultSet rs, String bookId, String operation) {
        String[] base = prev.split("_");//string to string array
        String[] add = cur.split("_");

        //If transaction body contains the same bookid, the last action should be opposite
        for (int i = base.length - 2; i >= 0; i -= 2) {
            if (base[i].equals(add[0])) {
                return !base[i + 1].equals(add[1]);
            }
        }
        int status;

        //If not, check with the bookdb
        try {
            rs = statement.executeQuery("select available from bookdb where id =".concat(bookId));
            if (rs.next()) {
                status = Integer.parseInt(rs.getString(1));
                if ((operation.equals("loan") && status == 1) || (operation.equals("return") && status == 0)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        }
        return false;
    }

    // Transaction Step 2: Operation
    public static void t2(Statement statement, ResultSet rs, String body, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                transaction t = mapper.readValue(body, transaction.class);
                String action = t.getAction();

                //If the action is not return or loan return, bad request
                if (!(action.equals("return") || action.equals("loan"))) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);//400
                    return;
                }

                String bookID = t.getId();
                String transactionId = t.getTransaction();
                String prev;
                rs = statement.executeQuery("select  body from transactiondb where transaction_id= ".concat(transactionId));

                //Check if already exists
                if (rs.next()) {
                    prev = rs.getString(1);
                    String cur;

                    //Format bookid_0_ or bookid_1_
                    if (action.equals("loan")) {
                        cur = bookID.concat("_0_");
                    } else {
                        cur = bookID.concat("_1_");
                    }

                    //Check whether is allowed to add this new action
                    if (t2check(prev, cur, statement, rs, bookID, action)) {
                        prev = prev.concat(cur);
                        String prevHelper = sqlHelper(prev);

                        //If fulfills requirements, add it to transaction body
                        statement.executeUpdate("update transactiondb set body = ".concat(prevHelper).concat(" where transaction_id = ").concat(transactionId));
                        response.setStatusCode(HttpStatus.SC_OK);
                    } else {
                        response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    }
                } else {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    //Transaction Step 3: Commit or cancel
    public static void t3(Statement statement, ResultSet rs, String body, String token, HttpResponse response) {
        if (checkToken(statement, rs, token)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                transaction t = mapper.readValue(body, transaction.class);

                //If not commit or cancel return bad request
                String operation = t.getOperation();
                if (!(operation.equals("commit") || operation.equals("cancel"))) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    return;
                }

                String transactionId = t.getTransaction();
                rs = statement.executeQuery("select  body from transactiondb where transaction_id= ".concat(transactionId));

                //If id already exist
                if (rs.next()) {
                    if (operation.equals("commit")) {
                        String toTO = rs.getString(1);
                        String[] base = toTO.split("_");

                        //Commit!
                        for (int i = 0; i < base.length; i += 2) {
                            statement.executeUpdate("update bookdb set available = ".concat(base[i + 1]).concat(" where id = ").concat(base[i]));
                        }
                    }

                    //Set body = "" and available = 1 after operation
                    statement.executeUpdate("update transactiondb set body = '', available = 1 where transaction_id = ".concat(transactionId));
                    response.setStatusCode(HttpStatus.SC_OK);
                } else {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                        sqlEx.printStackTrace();
                    }
                }
            }
        } else {
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return true;
        }
        return false;
    }
}
