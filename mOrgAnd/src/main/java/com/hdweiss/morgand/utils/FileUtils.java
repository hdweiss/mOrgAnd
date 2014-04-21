package com.hdweiss.morgand.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }


    public static ArrayList<String> fileToArrayList(String fileName) throws IOException {
        String line;
        BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
        ArrayList<String> fileContent = new ArrayList<String>();
        while ((line = in.readLine()) != null)
            fileContent.add(line);

        return fileContent;
    }
}
