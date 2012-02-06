package miRNAFolding;

/**
 * @author Eirik Krogstad
 * Adapted from http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
 */
public class OsDetector {
    private static String os;

    public static String getOSName() {
        if (os == null)
            os = System.getProperty("os.name");
        return os;
    }

    public static boolean isWindows() {
		return (getOSName().toLowerCase().indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (getOSName().toLowerCase().indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (getOSName().toLowerCase().indexOf("nix") >= 0 || getOSName().toLowerCase().indexOf("nux") >= 0);
	}

	public static boolean isSolaris() {
		return (getOSName().toLowerCase().indexOf("sun") >= 0);
	}

    public static String getOSExtension() {
        String osExtension = "";
        if (OsDetector.isWindows())
            osExtension = ".exe";
        return osExtension;
    }
}
