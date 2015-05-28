package fi.oulu.acp.communityreminder;

/**
 * Created by alex on 14.3.2015.
 */
public interface Config {
    // CONSTANTS
    static final String SERVER_URL =
            "http://pan0166.panoulu.net/community/backend/registerUser.php?key=fdsjfkiajl3ir3f";

    // Google project id
    static final String GOOGLE_SENDER_ID = "957322195637";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Android";

    static final String DISPLAY_MESSAGE_ACTION =
            "fi.oulu.acp.communityreminder.DISPLAY_MESSAGE";

    static final String EXTRA_MESSAGE = "message";
}
