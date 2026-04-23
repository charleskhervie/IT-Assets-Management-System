package ui.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJsonParser {
    private final String text;
    private int index;

    private SimpleJsonParser(String text) {
        this.text = text;
    }

    public static Object parse(String text) {
        SimpleJsonParser parser = new SimpleJsonParser(text);
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.isAtEnd()) {
            throw new IllegalArgumentException("Unexpected trailing content at position " + parser.index);
        }
        return value;
    }

    private Object parseValue() {
        skipWhitespace();
        if (isAtEnd()) {
            throw new IllegalArgumentException("Unexpected end of JSON input.");
        }

        char current = text.charAt(index);
        return switch (current) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> {
                if (current == '-' || Character.isDigit(current)) {
                    yield parseNumber();
                }
                throw new IllegalArgumentException("Unexpected character '" + current + "' at position " + index);
            }
        };
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> object = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            expect('}');
            return object;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object value = parseValue();
            object.put(key, value);
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                return object;
            }
            expect(',');
        }
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> array = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            expect(']');
            return array;
        }

        while (true) {
            array.add(parseValue());
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                return array;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd()) {
            char current = text.charAt(index++);
            if (current == '"') {
                return builder.toString();
            }
            if (current == '\\') {
                if (isAtEnd()) {
                    throw new IllegalArgumentException("Unterminated escape sequence.");
                }
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> builder.append(escaped);
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> builder.append(parseUnicodeEscape());
                    default -> throw new IllegalArgumentException("Invalid escape sequence \\" + escaped + " at position " + (index - 1));
                }
            } else {
                builder.append(current);
            }
        }
        throw new IllegalArgumentException("Unterminated string literal.");
    }

    private char parseUnicodeEscape() {
        if (index + 4 > text.length()) {
            throw new IllegalArgumentException("Incomplete unicode escape.");
        }
        String hex = text.substring(index, index + 4);
        index += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid unicode escape \\u" + hex, e);
        }
    }

    private Object parseNumber() {
        int start = index;
        if (peek('-')) {
            index++;
        }
        consumeDigits();
        boolean decimal = false;
        if (peek('.')) {
            decimal = true;
            index++;
            consumeDigits();
        }
        if (peek('e') || peek('E')) {
            decimal = true;
            index++;
            if (peek('+') || peek('-')) {
                index++;
            }
            consumeDigits();
        }
        String token = text.substring(start, index);
        try {
            if (decimal) {
                return Double.parseDouble(token);
            }
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number '" + token + "'", e);
        }
    }

    private void consumeDigits() {
        int start = index;
        while (!isAtEnd() && Character.isDigit(text.charAt(index))) {
            index++;
        }
        if (start == index) {
            throw new IllegalArgumentException("Expected digit at position " + index);
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (!text.startsWith(literal, index)) {
            throw new IllegalArgumentException("Expected '" + literal + "' at position " + index);
        }
        index += literal.length();
        return value;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (isAtEnd() || text.charAt(index) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + index);
        }
        index++;
    }

    private boolean peek(char expected) {
        return !isAtEnd() && text.charAt(index) == expected;
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }

    private boolean isAtEnd() {
        return index >= text.length();
    }
}
