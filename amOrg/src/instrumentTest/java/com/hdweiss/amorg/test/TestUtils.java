package com.hdweiss.amorg.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TestUtils {

    public static void writeStringAsFile(final String fileContents, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(fileContents);
            writer.close();
        } catch (IOException e) {
        }
    }

    public static String readFileAsString(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line).append("\n");

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }
}
