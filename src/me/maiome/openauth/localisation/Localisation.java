import java.io.*;
import java.util.*;

public abstract class Localisation implements ILocalisation {

    /**
     * This is all general information that needs to be known for use.
     *
     * This is VERY unfinished.
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
            throw new InvalidArgumentException("Provided path is a file, not a directory.");
        }

        this.localisationsDirectory = localisationsPath;
    }

    /**
     * Returns an ILocalisation instance.
     */
    public static ILocalisation getInstance() {
        return instance;
    }

    /**
     * Searches the localisations folder for translation files.
     *
     * Translation files are qualified as files that end with a .lang extension inside
     * the set localisations folder.
     */
    private List<File> findLocalisations();

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
     * @expectation: 2
     * translation.string.node => Translation string
     * welcome.message => Hi, %p, welcome to %w!
     *
     * ...etc, so on and so forth.
     */
    void processLocalisation();
    /**
     * Returns the translation string mapped to the given node.
     */
    String getLocalisedString(String nodeName);
    /**
     * Shorthand for getLocalisedString(String nodeName).
     */
    String get(String nodeName);
}
