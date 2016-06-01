package com.sonsofhesslow.games.risk.graphics;

import com.sonsofhesslow.games.risk.graphics.geometry.Bezier;
import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;
import com.sonsofhesslow.games.risk.graphics.geometry.BezierPathBuilder;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

class SvgReader {
    private final PushbackReader pushbackReader;
    private Vector2 pos = Vector2.zero();
    public SvgReader(InputStream inputStream) {
        // relying on default encoding here is find. The input file is treated as raw
        // and supported by the developers.
        pushbackReader = new PushbackReader(new BufferedReader(new InputStreamReader(inputStream)),100);
    }

    private float readFloat() throws IOException {
        //fast minimal float stream parsing.
        float ret = 0;
        final float[] table = {0.1f, 0.01f, 0.001f, 0.0001f, 0.00001f};
        int decimal = -1;
        boolean negative = false;
        for (int i = 0; i < 20; ++i) {
            int c = pushbackReader.read();
            if (c == -1) break;
            if (c == '.') {
                decimal = 0;
                continue;
            }
            if (c == '-') {
                negative = true;
                continue;
            }
            int num = c - 48;
            if (num >= 0 && num < 10) { //is digit
                if (decimal >= 0) {
                    ret += num * table[decimal];
                    ++decimal;
                    if (decimal >= table.length) break;
                } else {
                    ret = ret * 10 + num;
                }
            } else {
                pushbackReader.unread(c);
                break;
            }
        }
        skipDigit();
        return negative ? -ret : ret;
    }


    private void skipWhite() throws IOException {
        for (; ; ) {
            int c = pushbackReader.read();
            if (!Character.isWhitespace(c) || c == -1) {
                pushbackReader.unread(c);
                break;
            }
        }
    }


    private void skipDigit() throws IOException {
        for (; ; ) {
            int c = pushbackReader.read();
            if (!Character.isDigit(c) || c == -1) {
                pushbackReader.unread(c);
                break;
            }
        }
    }

    private Vector2 readVector2(boolean relative) throws IOException {
        skipWhite();
        float x = readFloat();
        skipWhite();
        int comma = pushbackReader.read();
        if (comma != ',') throw new FileFormatException("file format mismatch");
        skipWhite();
        float y = readFloat();
        if (relative)
            return Vector2.add(new Vector2(x, y), pos);
        else return new Vector2(x, y);
    }

    private Bezier readBeiz(char mode) throws IOException {
        switch (mode) {
            case 'm':
                if (pos.x != 0 || pos.y != 0) {
                    return readBeiz('l');
                }
                pos = readVector2(true);
                return null;
            case 'M':
                if (pos.x != 0 || pos.y != 0) {
                    return readBeiz('L');
                }
                pos = readVector2(false);
                return null;
            case 'C': {
                Vector2 start = pos;
                Vector2 c1 = readVector2(false);
                Vector2 c2 = readVector2(false);
                pos = readVector2(false);
                return new Bezier(start, c1, c2, pos);
            }
            case 'c': {
                Vector2 start = pos;
                Vector2 c1 = readVector2(true);
                Vector2 c2 = readVector2(true);
                pos = readVector2(true);
                return new Bezier(start, c1, c2, pos);
            }
            case 'L': {
                Vector2 start = pos;
                pos = readVector2(false);
                return new Bezier(start, start, pos, pos);
            }
            case 'l': {
                Vector2 start = pos;
                pos = readVector2(true);
                return new Bezier(start, start, pos, pos);
            }
            default:
                throw new RuntimeException("unknown/unimplemented mode:\'" + mode + "\'");
        }
    }

    public SVGPath readPath() throws IOException {
        // parsing a path,
        // [starts here] ...noise... <path  ...data... /> [ends here]
        if (!advancePast("<path")) return null;
        boolean isDashed = false;
        boolean isCont = false;
        boolean isReg = false;

        BezierPath ret = null;
        for (; ; ) {
            skipWhite();
            String s = readWord();
            if (s.length() == 0) return null;
            advancePast('=');
            advancePast('"');
            switch (s) {
                case "d":
                    pos = Vector2.zero();
                    BezierPathBuilder b = new BezierPathBuilder();

                    for (; ; ) {
                        skipWhite();
                        int c = pushbackReader.read();
                        if (c == -1) break;
                        if (c == '\"') {
                            ret = b.get(false);
                            pushbackReader.unread('\"'); //we'll advance past later on. need to keep our return state consitant.
                            break;
                        }
                        if (c == 'z' || c == 'Z') {
                            ret = b.get(true);
                            break;
                        }
                        while (isNextFloat()) {
                            Bezier beiz = readBeiz((char) c);
                            if (beiz != null)
                                b.addBeiz(beiz);
                        }
                    }
                    if (ret == null) {
                        throw new FileFormatException("no data in the path->d tag");
                    }
                    break;
                case "id":
                    String id = readWord();
                    if (id.equals("cont")) isCont = true;
                    if (id.equals("reg")) isReg = true;
                    break;
                case "style":
                    for (; ; ) {
                        skipWhite();
                        String attr = readWord(":\"");
                        if (attr.equals("stroke-dasharray")) {
                            pushbackReader.read();//reads the :
                            skipWhite();
                            if (isNextFloat()) {
                                isDashed = true;
                            }
                            break;
                        }
                        skipWhite();
                        readWord(";\"");
                        int p = peek();
                        if (p == -1 || p == '\"') break;
                        pushbackReader.read(); // reads the ;
                    }
                    break;
            }
            advancePast('"');
            skipWhite();
            if (peek(2).equals("/>")) break;
            if (peek() == -1) break;
        }
        advancePast("/>");
        return new SVGPath(ret, isDashed, isReg, isCont);
    }

    private int peek() throws IOException {
        int c = pushbackReader.read();
        if (c != -1)
            pushbackReader.unread((char) c);
        return c;
    }

    private String peek(int length) throws IOException {
        char buffer[] = new char[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = (char) pushbackReader.read();
        }
        pushbackReader.unread(buffer);
        return new String(buffer, 0, length);
    }

    private boolean isNextFloat() throws IOException {
        skipWhite();
        char c = (char) peek();
        return Character.isDigit(c) || c == '-' || c == '.';
    }

    private boolean advancePast(String string) throws IOException {
        int currentChar = 0;
        char[] s = string.toCharArray();
        for (; ; ) {
            int c = pushbackReader.read();
            if (c == s[currentChar]) {
                if (++currentChar == s.length) return true;
            } else {
                currentChar = 0;
            }
            if (c == -1) return false;
        }
    }

    private boolean advancePast(char s) throws IOException {
        for (; ; ) {
            int c = pushbackReader.read();
            if (c == -1) return false;
            if ((char) c == s) return true;
        }
    }

    private String readWord() throws IOException {
        char[] s = new char[20]; // we don't need any longer words than that.
        int i = 0;
        for (; i < 20; ++i) {
            int c = pushbackReader.read();
            if (c == -1) break;
            if (Character.isLetter(c)) {
                s[i] = (char) c;
            } else {
                pushbackReader.unread(c);
                break;
            }
        }
        return new String(s, 0, i);
    }

    private String readWord(String delimiter) throws IOException {
        char[] s = new char[20];
        int i = 0;
        for (; i < 20; ++i) {
            int c = pushbackReader.read();
            if (c == -1) break;
            if (delimiter.indexOf(c) == -1) {
                s[i] = (char) c;
            } else {
                pushbackReader.unread(c);
                break;
            }
        }
        return new String(s, 0, i);
    }

    static class FileFormatException extends RuntimeException {
        FileFormatException(String message) {
            super(message);
        }
    }
}
