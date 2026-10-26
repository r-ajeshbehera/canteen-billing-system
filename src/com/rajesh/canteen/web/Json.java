package com.rajesh.canteen.web;

import java.util.Locale;

// Small JSON helpers written by hand (no library needed).
public final class Json {

    private Json() {
    }

    // a JSON string with quotes and special characters escaped
    public static String str(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }

    // money with 2 decimals, always with a dot (not a comma) as separator
    public static String money(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
