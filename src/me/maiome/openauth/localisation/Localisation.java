import java.io.*;
import java.util.*;
import java.util.regex.*;

import me.maiome.openauth.localisation.TranslationFilenameFilter;

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
    private Map<String, String> translationMap;
    /**
     * State of translation thingy.
     */
    private boolean translationLoaded;
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
    public static ILocalisation instance;

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
     * Number of expected translation strings. Not required, but recommended.
     */
    private int expectedTranslationCount = 0;
    /**
     * Number of found translation strings.
     */
    private int foundTranslationCount = 0;

    /**
     * Localisation handling methods.
     */

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
     * Returns an ILocalisation instance.
     */
    public static ILocalisation getInstance() {
        return instance;
    }

    /**
     * Shorthand for getLocalisedString(String nodeName).
     */
    public static String get(String nodeName) {
        return Localisation.getInstance().getLocalisedString(nodeName);
    }

    /**
     * Shorthand for getLocalisedString(String nodeName, Map<String, String> replacements).
     */
    public static String get(String nodeName, Map<String, String> replacements) {
        return Localisation.getInstance().getLocalisedString(nodeName, replacements);
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
            if (translation.getName().equals(name)) {
                try {
                    this.processLocalisation(translation);
                    return true;
                } catch (java.lang.Exception e) {
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
     * @expectation: 2
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
            if (line.charAt(0) == '@') { // check for data points.
                if (line.substring(0, line.indexOf(' ')).equals("@localisation:")) { // localisation name
                    String givenTransName = line.substring(line.indexOf(' ')).trim();
                    if (!(givenTransName.equals(translationFilename))) {
                        throw new Exception(String.format("Translation filename and data point name do not match: [%s <=> %s]", translationFilename, givenTransName));
                    }
                    this.translationName = givenTransName;
                } else if (line.substring(0, line.indexOf(' ')).equals("@expectation:")) { // translation expectation
                    String expectedCount = line.substring(line.indexOf(' ')).trim();
                    int expCount = Integer.valueOf(expectedCount);
                    this.expectedTranslationCount = expCount;
                } else { // invalid data point
                    continue;
                }
            } else if (Pattern.matches("(.+) \\=\\> (.+)", line)) { // this should be a node element
                String[] lineAr = line.split("\\=\\>");
                lineAr[0] = lineAr[0].trim();
                if (lineAr[1].charAt(0) == ' ') lineAr[1] = lineAr[1].substring(1);
                this.translationMap.put(lineAr[0], lineAr[1]);
                this.foundTranslationCount++;
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
