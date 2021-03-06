package me.maiome.openauth.localisation;

import java.io.*;
import java.util.*;

public interface ILocalisation {

    /**
     * This is all general information that needs to be known for use.
     */

    /**
     * This is the File instance for the file containing the translation strings we're going to use.
     */
    File localisationFile = null;
    /**
     * This Map will hold all of the final strings, etc.
     */
    Map<String, String> translationMap = null;
    /**
     * State of translation thingy.
     */
    boolean translationLoaded = false;
    /**
     * Source directory for localisations.
     */
    String localisationsDirectory = "";
    /**
     * List of found translation files.
     */
    List<File> translationList = null;
    /**
     * Holds the instance of the localiser.
     */
    ILocalisation instance = null;

    /**
     * The following is all information about the loaded translation.
     */

    /**
     * This is the name of the loaded translation.
     *
     * Should look like the following:
     *   de
     *   en_US
     *   en_UK
     *   es
     * Where the first two/three characters is the language, and the last characters after
     * the underscore denotes the locale (differences in the languages between two different countries/nations.
     *
     * Translation names should also correlate to file names, if you have a translation with the name
     * as en_US, then the filename should be en_US.lang.
     */
    String translationName = "";
    /**
     * Number of found translation strings.
     */
    int foundTranslationCount = 0;

    /**
     * Localisation handling methods.
     */

    /**
     * Sets the localisations folder to search for translation files.
     */
    void setLocalisationsFolder(String localisationsPath);
    /**
     * Returns the localiser instance.
     */
    /* static ILocalisation getInstance(); */
    /**
     * Searches the localisations folder for translation files.
     *
     * Translation files are qualified as files that end with a .lang extension inside
     * the set localisations folder.
     */
    List<File> findLocalisations() throws Exception;
    /**
     * Searches the translation list to find a translation that matches the requested translation.
     * After it successfully matches a translation, it sets the matched translation file in the
     * localisationFile variable.
     */
    boolean requestTranslation(String name);
    /**
     * Reads the data from the localisation file found by requestTranslation(String name). Localisation files should look like this:
     *
     * @localisation: en_US
     * translation.string.node => Translation string
     * welcome.message => Hi, %p, welcome to %w!
     *
     * ...etc, so on and so forth.
     */
    void processLocalisation(File localisationFile) throws Exception;
    /**
     * Returns the translation string mapped to the given node.
     */
    String getLocalisedString(String nodeName);
    /**
     * Returns the translation string mapped to the given node with the given replacements processed.
     */
    String getLocalisedString(String nodeName, Map<String, String> replacements);
    /**
     * Shorthand for getLocalisedString(String nodeName).
     */
    /* static String get(String nodeName); */
}
