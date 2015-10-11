package com.company;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FontParser {

    private FontParser() {
    }

    public static List<String> parse(File file, int width, int length) throws IOException {
        FileInputStream in = new FileInputStream(file);
        int c, count = 0, size = length / 8;
        byte[] bytes = new byte[size];
        List<String> words = new ArrayList<>();
        while ((c = in.read()) != -1) {
            if (count == size) {
                words.add(toBinary(bytes, length));
                count = 0;
            }
            bytes[count] = (byte) c;
            ++count;
        }
        return words;
    }

    private static String toBinary(byte[] bytes, int length) {
        int size = (length / 8) - 1;
        StringBuffer sb = new StringBuffer(length);
        while (--length >= 0) {
            if ((bytes[size - (length / 8)] & (1 << length % 8)) > 0)
                sb.append("[]");
            else
                sb.append("  ");
        }
        return sb.toString();
    }
}