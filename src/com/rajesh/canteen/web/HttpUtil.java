package com.rajesh.canteen.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

// Small helpers for reading requests and writing responses.
// Uses only the JDK's built-in HTTP server, no framework.
public final class HttpUtil {

    private HttpUtil() {
    }

    // reads "a=1&b=two" from the query string or a form body into a map
    public static Map<String, String> parseForm(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null || text.isEmpty()) {
            return map;
        }
        for (String pair : text.split("&")) {
            int eq = pair.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            map.put(key, value.trim());
        }
        return map;
    }

    public static Map<String, String> query(HttpExchange ex) {
        return parseForm(ex.getRequestURI().getRawQuery());
    }

    public static Map<String, String> body(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return parseForm(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    public static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        send(ex, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    public static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, "{\"error\":" + Json.str(message) + "}");
    }

    private static void send(HttpExchange ex, int status, String type, byte[] data) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(data);
        }
    }

    // serves index.html, style.css and app.js from the web folder
    public static void sendStaticFile(HttpExchange ex, Path webRoot) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }
        Path file = webRoot.resolve(path.substring(1)).normalize();
        // block paths like /../../secret.txt
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            sendError(ex, 404, "Page not found");
            return;
        }
        send(ex, 200, contentType(file.toString()), Files.readAllBytes(file));
    }

    private static String contentType(String name) {
        if (name.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (name.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (name.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        return "application/octet-stream";
    }

    public static Path webRoot() {
        return Paths.get("web").toAbsolutePath().normalize();
    }
}
