package com.example.library.web;

import com.example.library.dao.BookDao;
import com.example.library.dao.MemberDao;
import com.example.library.dao.LoanDao;
import com.example.library.model.Book;
import com.example.library.model.Member;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonObject;
import spark.Filter;
import spark.Spark;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class WebServer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> src == null ? null : new com.google.gson.JsonPrimitive(src.toString()))
            .create();

    public static void start(int port) {
        Spark.port(port);
        // Static files must be configured before any routes/filters in Spark
        Spark.staticFiles.location("/public"); // src/main/resources/public
        enableCors();

        // Redirect root to index.html so hitting / opens the site
        Spark.get("/", (req, res) -> { res.redirect("/index.html"); return ""; });

        // Fallback serving if staticFiles doesn't find resources in some environments
        Spark.get("/index.html", (req, res) -> {
            res.type("text/html;charset=utf-8");
            return readResource("/public/index.html");
        });
        Spark.get("/styles.css", (req, res) -> {
            res.type("text/css;charset=utf-8");
            return readResource("/public/styles.css");
        });
        Spark.get("/script.js", (req, res) -> {
            res.type("application/javascript;charset=utf-8");
            return readResource("/public/script.js");
        });

        BookDao bookDao = new BookDao();
        MemberDao memberDao = new MemberDao();
        LoanDao loanDao = new LoanDao();

        Spark.get("/api/books", (req, res) -> {
            res.type("application/json");
            try {
                List<Book> books = bookDao.listAll();
                return GSON.toJson(books);
            } catch (SQLException e) {
                res.status(500);
                return error(e.getMessage());
            }
        });

        // Create a new book
        Spark.post("/api/books", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject obj = GSON.fromJson(req.body(), JsonObject.class);
                Book b = new Book();
                b.setIsbn(obj.get("isbn").getAsString());
                b.setTitle(obj.get("title").getAsString());
                b.setAuthor(obj.get("author").getAsString());
                int total = obj.get("copiesTotal").getAsInt();
                b.setCopiesTotal(total);
                b.setCopiesAvailable(total);
                bookDao.create(b);
                res.status(201);
                return GSON.toJson(b);
            } catch (Exception e) {
                res.status(400);
                return error(e.getMessage());
            }
        });

        Spark.get("/api/members", (req, res) -> {
            res.type("application/json");
            try {
                List<Member> members = memberDao.listAll();
                return GSON.toJson(members);
            } catch (SQLException e) {
                res.status(500);
                return error(e.getMessage());
            }
        });

        // Create a new member
        Spark.post("/api/members", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject obj = GSON.fromJson(req.body(), JsonObject.class);
                Member m = new Member();
                m.setName(obj.get("name").getAsString());
                m.setEmail(obj.get("email").getAsString());
                m.setPhone(obj.has("phone") && !obj.get("phone").isJsonNull() ? obj.get("phone").getAsString() : "");
                memberDao.create(m);
                res.status(201);
                return GSON.toJson(m);
            } catch (Exception e) {
                res.status(400);
                return error(e.getMessage());
            }
        });

        // List active loans
        Spark.get("/api/loans", (req, res) -> {
            res.type("application/json");
            try {
                return GSON.toJson(loanDao.listActive());
            } catch (SQLException e) {
                res.status(500);
                return error(e.getMessage());
            }
        });

        // List returned loans
        Spark.get("/api/loans/returned", (req, res) -> {
            res.type("application/json");
            try {
                return GSON.toJson(loanDao.listReturned());
            } catch (SQLException e) {
                res.status(500);
                return error(e.getMessage());
            }
        });

        // Issue a loan
        Spark.post("/api/loans", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject obj = GSON.fromJson(req.body(), JsonObject.class);
                long bookId = obj.get("bookId").getAsLong();
                long memberId = obj.get("memberId").getAsLong();
                int days = obj.get("days").getAsInt();
                java.time.LocalDate due = java.time.LocalDate.now().plusDays(days);
                loanDao.createLoan(bookId, memberId, due);
                res.status(201);
                JsonObject ok = new JsonObject();
                ok.addProperty("status", "issued");
                ok.addProperty("dueDate", due.toString());
                return GSON.toJson(ok);
            } catch (Exception e) {
                res.status(400);
                return error(e.getMessage());
            }
        });

        // Return a loan
        Spark.post("/api/loans/return", (req, res) -> {
            res.type("application/json");
            try {
                JsonObject obj = GSON.fromJson(req.body(), JsonObject.class);
                long loanId = obj.get("loanId").getAsLong();
                loanDao.returnLoan(loanId);
                JsonObject ok = new JsonObject();
                ok.addProperty("status", "returned");
                return GSON.toJson(ok);
            } catch (Exception e) {
                res.status(400);
                return error(e.getMessage());
            }
        });

        Spark.get("/health", (req, res) -> "OK");
    }

    private static void enableCors() {
        Filter corsHeaders = (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        };
        Spark.afterAfter(corsHeaders);
        Spark.options("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            return "OK";
        });
    }

    private static String error(String msg) {
        return GSON.toJson(new ErrorMessage(msg));
    }

    private static final class ErrorMessage {
        final String error;
        ErrorMessage(String e) { this.error = e; }
    }

    private static String readResource(String path) {
        try (InputStream in = WebServer.class.getResourceAsStream(path)) {
            if (in == null) return "";
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}


