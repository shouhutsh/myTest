package com.company;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        List<String> words = FontParser.parse(new File("/home/zl/code/git/myTest/FontParser/HZK16"), 16, 16);
        for(String line : words){
            System.out.println(line);
        }
    }
}
