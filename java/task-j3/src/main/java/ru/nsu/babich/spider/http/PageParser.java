package ru.nsu.babich.spider.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.nsu.babich.spider.crawl.FetchException;
import ru.nsu.babich.spider.crawl.Page;

final class PageParser {
    private final String s;
    private int i;

    private PageParser(String s) {
        this.s = s;
    }

    public static Page parse(String json) throws FetchException {
        try {
            Object root = new PageParser(json).value();
            if (!(root instanceof Map<?, ?> map)) {
                throw new FetchException("JSON root is not an object", false);
            }
            if (!(map.get("message") instanceof String message)) {
                throw new FetchException("Missing string field 'message'", false);
            }
            List<String> successors = new ArrayList<>();
            if (map.get("successors") instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof String next) {
                        successors.add(next);
                    }
                }
            }
            return new Page(message, successors);
        } catch (RuntimeException e) {
            throw new FetchException("Malformed JSON", e, false);
        }
    }

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    private Object value() {
        ws();
        return switch (s.charAt(i)) {
            case '{' -> object();
            case '[' -> array();
            case '"' -> string();
            default -> literal();
        };
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;
        while (true) {
            ws();
            char c = s.charAt(i);
            if (c == ',') {
                i++;
                continue;
            }
            if (c == '}') {
                i++;
                return m;
            }
            String key = string();
            ws();
            if (s.charAt(i++) != ':') {
                throw new IllegalStateException("':' expected");
            }
            m.put(key, value());
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++;
        while (true) {
            ws();
            char c = s.charAt(i);
            if (c == ',') {
                i++;
                continue;
            }
            if (c == ']') {
                i++;
                return l;
            }
            l.add(value());
        }
    }

    private String string() {
        if (s.charAt(i) != '"') {
            throw new IllegalStateException("string expected");
        }
        StringBuilder sb = new StringBuilder();
        i++;
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = s.charAt(i++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                }
                default -> sb.append(e);
            }
        }
    }

    private Object literal() {
        int start = i;
        while (i < s.length() && ",}] \t\r\n".indexOf(s.charAt(i)) < 0) {
            i++;
        }
        if (start == i) {
            throw new IllegalStateException("unexpected character");
        }
        String t = s.substring(start, i);
        return switch (t) {
            case "null" -> null;
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> t;
        };
    }
}
