package io.ballerina.generators.auth;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Maintain flags to identify authentication type used.
 */
public class AuthTypeMap {
    /**
     * Enum to keep type of authentication.
     */
    public enum Flags {
        BASIC, BEARER, CLIENT_CREDENTIAL, PASSWORD, API_KEY, HTTP_OR_OAUTH
    }

    private static final Map<Flags, Boolean> map = Collections.synchronizedMap(
            new EnumMap<>(Flags.class));

    /**
     * Set flag value of given auth type to true.
     * @param flag  Key of the flag
     * @param value Value of the flag
     */
    public static void setFlag(Flags flag, boolean value) {
        map.put(flag, value);
    }

    /**
     * Get value of a flag.
     * @param flag  Flag to get valuw
     * @return      Value of the flag
     */
    public static boolean getFlag(Flags flag) {
        if (map.containsKey(flag)) {
            return map.get(flag);
        }
        return false;
    }

    /**
     * clear the map of flags.
     */
    public static void resetFlags () {
        map.clear();
    }
}
