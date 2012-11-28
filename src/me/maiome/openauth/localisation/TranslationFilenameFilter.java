package me.maiome.openauth.localisation;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TranslationFilenameFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        return Pattern.matches("(\\w+_\\w+|\\w{2})\\.lang", name);
    };

};
