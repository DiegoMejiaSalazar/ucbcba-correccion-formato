package com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.others.dictionaries;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Diccionario {
    private Set<String> wordsSet;

    public Diccionario() throws IOException
    {
        Path path = Paths.get("palabras.txt");
        byte[] readBytes = Files.readAllBytes(path);
        String wordListContents = new String(readBytes, StandardCharsets.UTF_8);
        String[] words = wordListContents.split("\n");
        wordsSet = new HashSet<>();
        Collections.addAll(wordsSet, words);
    }


    public boolean contains(String word)
    {
        return wordsSet.contains(word);
    }
}

