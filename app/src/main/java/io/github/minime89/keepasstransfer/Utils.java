package io.github.minime89.keepasstransfer;

/**
 * Some utility functions.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Format of hex string output when using
     *
     * @see Utils#byteToHex
     * @see Utils#bytesToHex
     */
    public enum HexFormat {
        /**
         * Example output: 0011223344
         */
        CONDENSED,
        /**
         * Example output: 00 11 22 33 44
         */
        SPACING,
        /**
         * Example output: \x00\x11\x22\x33\x44
         */
        UNIX
    }

    /**
     * Convert byte to hex string.
     *
     * @param b      The byte which will be converted into a hex string
     * @param format
     * @return
     */
    public static String byteToHex(byte b, HexFormat format) {
        return bytesToHex(new byte[]{b}, format).trim();
    }

    /**
     * Convert byte array to hex string.
     * Source: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
     *
     * @param bytes  The array of bytes which will be converted into a hex string
     * @param format The format of the hex output. See {@link HexFormat} for possible formats.
     * @return
     */
    public static String bytesToHex(byte[] bytes, HexFormat format) {
        char[] hexChars;
        if (format == HexFormat.CONDENSED) {
            hexChars = new char[bytes.length * 2];
        } else if (format == HexFormat.SPACING) {
            hexChars = new char[bytes.length * 3];
        } else {
            hexChars = new char[bytes.length * 4];
        }

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;

            if (format == HexFormat.CONDENSED) {
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            } else if (format == HexFormat.SPACING) {
                hexChars[j * 3] = hexArray[v >>> 4];
                hexChars[j * 3 + 1] = hexArray[v & 0x0F];
                hexChars[j * 3 + 2] = ' ';
            } else if (format == HexFormat.UNIX) {
                hexChars[j * 4] = '\\';
                hexChars[j * 4 + 1] = 'x';
                hexChars[j * 4 + 2] = hexArray[v >>> 4];
                hexChars[j * 4 + 3] = hexArray[v & 0x0F];
            }
        }

        return new String(hexChars);
    }
}
