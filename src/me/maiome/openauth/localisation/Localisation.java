package me.maiome.openauth.localisation;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import me.maiome.openauth.util.Reloadable;

public class Localisation implements ILocalisation {

    /**
     * This is all general information that needs to be known for use.
     */

    /**
     * This is the File instance for the file containing the translation strings we're going to use.
     */
    private File localisationFile;
    /**
     * This Map will hold all of the final strings, etc.
     */
    private Map<String, String> translationMap = new HashMap<String, String>();
    /**
     * State of translation thingy.
     */
    private boolean translationLoaded = false;
    /**
     * Source directory for localisations.
     */
    private String localisationsDirectory = "";
    /**
     * List of found translation files.
     */
    private List<File> translationList;
    /**
     * Holds the instance of the localiser.
     */
    public static ILocalisation instance = null;

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
    private String translationName = "";
    /**
     * Number of found translation strings.
     */
    private int foundTranslationCount = 0;

    /**
     * Localisation handling methods.
     */

    /**
     * Returns an ILocalisation instance.
     */
    public static ILocalisation getInstance() {
        return ((instance != null) ? instance : new Localisation());
    }

    /**
     * Shorthand for getLocalisedString(String nodeName).
     */
    public static String getString(String nodeName) {
        return Localisation.getInstance().getLocalisedString(nodeName);
    }

    /**
     * Shorthand for getLocalisedString(String nodeName, Map<String, String> replacements).
     */
    public static String getString(String nodeName, Map<String, String> replacements) {
        return Localisation.getInstance().getLocalisedString(nodeName, replacements);
    }

    /**
     * Simple constructor.
     */
    private Localisation() {
        instance = this;
    }

    /**
     * Sets the localisations folder to search for translation files.
     */
    public void setLocalisationsFolder(String localisationsPath) {
        if (this.localisationsDirectory != "") {
            throw new UnsupportedOperationException("Cannot reset localisations directory.");
        }
        if (!(new File(localisationsPath).isDirectory())) {
            throw new IllegalArgumentException("Provided path is a file, not a directory.");
        }

        this.localisationsDirectory = localisationsPath;

        List<File> localisations;

        try {
            localisations = this.findLocalisations();
        } catch (java.lang.IllegalArgumentException e) {
            this.localisationsDirectory = "";
            throw e;
        } catch (java.lang.Exception e) {
            this.localisationsDirectory = "";
            return;
        }

        this.translationList = localisations;
    }

    /**
     * Searches the localisations folder for translation files.
     *
     * Translation files are qualified as files that end with a .lang extension inside
     * the set localisations folder.
     */
    public List<File> findLocalisations() throws Exception {
        if (this.localisationsDirectory == "") {
            throw new Exception("Localisation directory is not set.");
        }

        File locDirectory = new File(this.localisationsDirectory);

        if (!(locDirectory.isDirectory())) {
            throw new IllegalArgumentException("Cannot use a file for the translation DIRECTORY.");
        }

        List<File> translations = Arrays.asList(locDirectory.listFiles(new TranslationFilenameFilter()));
        return translations;
    }

    /**
     * Searches the translation list to find a translation that matches the requested translation.
     * After it successfully matches a translation, it sets the matched translation file in the
     * localisationFile variable.
     */
    public boolean requestTranslation(String name) {
        for (File translation : this.translationList) {
            if (translation.getName().split("\\.")[0].equals(name)) {
                try {
                    this.processLocalisation(translation);
                    return true;
                } catch (java.lang.Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Reads the data from the localisation file found by requestTranslation(String name). Localisation files should look like this:
     *
     * @localisation: en_US
     * translation.string.node => Translation string
     * welcome.message => Hi, %p, welcome to %w!
     *
     * ...etc, so on and so forth.
     */
    public void processLocalisation(File translationFile) throws Exception {
        Scanner translation;
        try {
            translation = new Scanner(translationFile, "UTF-8");
        } catch (java.lang.Exception e) {
            throw e;
        }
        String translationFilename = translationFile.getName().split("\\.")[0];
        while (translation.hasNextLine()) {
            String line = translation.nextLine();
            if (line.trim().equals("")) {
                continue;
            }
            if (line.charAt(0) == '#') { // check for comment lines.
                continue;
            }
            if (line.charAt(0) == '@') { // check for data markers
                if (line.substring(0, line.indexOf(' ')).equals("@localisation:")) { // localisation name
                    String givenTransName = line.substring(line.indexOf(' ')).trim();
                    if (!(givenTransName.equals(translationFilename))) {
                        throw new Exception(String.format("Translation filename and data point name do not match: [%s <=> %s]", translationFilename, givenTransName));
                    }
                    this.translationName = givenTransName;
                    continue;
                } else { // invalid data point
                    continue;
                }
            } else if (Pattern.matches("(.+)(?:\\s+\\=\\>\\s+)(.+)", line)) { // this should be a node element
                Pattern p = Pattern.compile("(.+)(?:\\s+\\=\\>\\s+)(.+)");
                Matcher m = p.matcher(line);
                if (m.find() && m.groupCount() != 2) continue;
                try {
                    String node = m.group(1).trim();
                    String transl = m.group(2).trim();
                    this.translationMap.put(node, transl);
                    this.foundTranslationCount++;
                } catch (java.lang.Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * Returns the translation string mapped to the given node.
     */
    public String getLocalisedString(String nodeName) {
        return this.translationMap.get(nodeName);
    }

    /**
     * Returns the translation string mapped to the given node and all given replacements performed.
     */
    public String getLocalisedString(String nodeName, Map<String, String> replacements) {
        String trans = this.translationMap.get(nodeName);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            trans = trans.replace(entry.getKey(), entry.getValue());
        }
        return trans;
    }
}
