package net.lzzy.networkdisk.constants;


import android.os.Environment;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.utils.AppUtils;

import java.util.regex.Pattern;

public class Constants {

    private Constants() {
    }

    /**
     * api
     */
     private static final String API_URL = "http://39.108.122.162:8080/NetworkDiskApi/api/";
    //private static final String API_URL = "http://192.168.1.118:8888/NetworkDiskApi/api/";
    //private static final String API_URL = "http://192.168.43.240:8888/NetworkDiskApi/api/";
    // private static final String API_URL = "http://169.254.0.62:8080/NetworkDiskApi/api/";
    //private static final String API_URL = "http://192.168.1.158:8080/NetworkDiskApi/api/";
    private static final String API_CHECK_UPDATE = "http://api.fir.im/apps/latest/5a982278ca87a84f853b4861?api_token=";
    private static final String API_CHECK_UPDATE_TOKEN = "4a2df521d9f3a8b44eec6ec6c93cc958";

    private static final String API_FIND_PASSWORD = "findPassword";
    private static final String API_CREATE = "create";
    private static final String API_UPLOAD = "upload";
    private static final String API_DELETE = "delete";
    private static final String API_RENAME = "rename";
    public static final String API_GET = "get";
    private static final String API_DOWNLOAD = "download";
    private static final String API_EXIT = "exit";
    private static final String API_SEND_EMAIl = "sendEmail";
    private static final String API_REGISTERED = "registered";
    private static final String API_LOGIN = "login";
    private static final String API_TOKEN = "loginByToken";
    private static final String API_GET_TYPE = "getType";
    private static final String API_SEARCH = "search";
    private static final String API_GET_USER_INFO = "getUserInfo";
    private static final String API_MODIFY_PASSWORD = "modifyPassword";
    private static final String API_EDIT_NAME = "editName";
    private static final String API_ADD_CAPACITY = "addCapacity";
    private static final String FILE_SAVE_PATH = "LanYunNetdisk";


    public static final String API_URL_FIND_PASSWORD = API_URL + API_FIND_PASSWORD;
    public static final String API_URL_ADD_CAPACITY = API_URL + API_ADD_CAPACITY;
    public static final String API_URL_EDIT_NAME = API_URL + API_EDIT_NAME;
    public static final String API_URL_EXIT = API_URL + API_EXIT;
    public static final String API_URL_CHECK_UPDATE = API_CHECK_UPDATE + API_CHECK_UPDATE_TOKEN;
    public static final String API_URL_GET_USER_INFO = API_URL + API_GET_USER_INFO;
    public static final String API_URL_SEARCH = API_URL + API_SEARCH;
    public static final String API_URL_MODIFY_PASSWORD = API_URL + API_MODIFY_PASSWORD;
    public static final String API_URL_DOWNLOAD = API_URL + API_DOWNLOAD;
    public static final String API_URL_GET_TYPE = API_URL + API_GET_TYPE;
    public static final String API_URL_GET = API_URL + API_GET;
    public static final String API_URL_CREATE = API_URL + API_CREATE;
    public static final String API_URL_DELETE = API_URL + API_DELETE;
    public static final String API_URL_RENAME = API_URL + API_RENAME;
    public static final String API_URL_UPLOAD = API_URL + API_UPLOAD;
    public static final String API_URL_REGISTERED = API_URL + API_REGISTERED;
    public static final String API_URL_SEND_EMAIl = API_URL + API_SEND_EMAIl;
    public static final String API_URL_LOGIN = API_URL + API_LOGIN;
    public static final String API_URL_LOGIN_TOKEN = API_URL + API_TOKEN;
    public static final String API_URL_GET_IMG = API_URL + Constants.API_DOWNLOAD + "?shortPath=img/";

    public static final String API_NEW_NAME = "newName";
    public static final String API_PATH = "path";
    public static final String API_NOWPATH = "nowPath";
    public static final String API_NEWPATH = "newPath";
    public static final String API_NAME = "name";
    public static final String API_PASSWORD = "password";
    public static final String API_EMAIL = "email";
    public static final String API_IS_MODIFY = "isModify";
    public static final String API_CODE = "code";
    public static final String API_SHORT_PATH = "shortPath";
    public static final String API_KEY = "key";
    public static final String API_OLD_PASSWORD = "oldPassword";
    public static final String API_NEW_PASSWORD = "newPassword";

    /**
     * 其它
     */
    // public static final String CON_FOLDER_NAME_PATTERN_MATCHES = "(?!((^(con)$)|^(con)/..*|(^(prn)$)|^(prn)/..*|(^(aux)$)|^(aux)/..*|(^(nul)$)|^(nul)/..*|(^(com)[1-9]$)|^(com)[1-9]/..*|(^(lpt)[1-9]$)|^(lpt)[1-9]/..*)|^/s+|.*/s$)(^[^/////:/*/?/\"/</>/|]{1,255}$)";
    public static final Pattern CON_FOLDER_NAME_PATTERN_MATCHES = Pattern.compile(AppUtils.getContext().getString(R.string.pattern_matches_folder_name));
    public static final String FILE_DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + Constants.FILE_SAVE_PATH;
    public static final String CON_DIRECTORY = "folder";
    public static final String ERROR_FAILED_TO_CONNECT_NO_RESPONSE = "failed to connect to /39.108.122.162 (port 8080) after 10000ms";
    public static final String ERROR_FAILED_TO_CONNECT = "Failed to connect to /39.108.122.162:8080";
    public static final String SP_ALL_SORT_STATE = "sp_all_sort_state";
    public static final String SP_PICTURE_SORT_STATE = "sp_picture_sort_state";
    public static final String SP_DOCUMENT_SORT_STATE = "sp_document_sort_state";
    public static final String SP_MUSIC_SORT_STATE = "sp_music_sort_state";
    public static final String SP_VIDEO_SORT_STATE = "sp_video_sort_state";
    public static final String SP_APP_STATE = "sp_app_state";
    public static final String SP_FILE_SAVE_PATH = "sp_file_save_path";
    public static final String SP_USER = "sp_user";
    public static final String SP_OK_GO_COOKIE = "okgo_cookie";
    public static final String SP_TOKEN = "sp_token";
    public static final String SP_EMAIL = "sp_email";
    public static final String SP_IMG_KEY = "sp_img_key";


    public static final String SP_CHECK_UPLOAD = "sp_check_upload";
    public static final String JSON_RESULTS_CODE = "resultCode";
    public static final String JSON_FILE_INFO = "fileInfo";
    public static final String JSON_STRING = "json_string";
    public static final String JSON_RESULTS_TOKEN = "token";
    public static final String JSON_RESULTS_DETAILS_USER = "detailsUser";
    public static final String JSON_VERSION = "version";
    public static final String JSON_VERSION_SHORT = "versionShort";
    public static final String JSON_CHANGELOG = "changelog";
    public static final String JSON_INSTALL_URL = "installUrl";
    public static final String JSON_SP_DETAILS = "sp_details";
    public static final String JSON_UPDATE_URL = "update_url";
    public static final String INTENT_PICTURE_POSITION = "intent_picture_position";
    public static final String INTENT_IS_PICTURE_TO = "intent_is_picture";
    public static final String INTENT_PICTURE_USERFILE_JSON = "intent_picture_userFile_json";
    public static final String INTENT_NAME="intent_name";
    public static final String INTENT_URI="intent_uri";
    public static final String INTENT_CAPACITY="intent_capacity";
}
