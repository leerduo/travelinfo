package com.ustc.dystu.dandelion.constant;

public interface Constants {
	
	public static final String APP_KEY = "1710941608"; 
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html"; 
	public static final String SCOPE = 
	"email,direct_messages_read,direct_messages_write,"
	+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
	+ "follow_app_official_microblog," + "invitation_write";
	
	
	 public static final String CLIENT_ID         = "client_id";
	    public static final String RESPONSE_TYPE     = "response_type";
	    public static final String USER_REDIRECT_URL = "redirect_uri";
	    public static final String DISPLAY           = "display";
	    public static final String USER_SCOPE        = "scope";
	    public static final String PACKAGE_NAME      = "packagename";
	    public static final String KEY_HASH          = "key_hash";
	    
	    public static final String THUMNAIL_CACHE_PATH = "dandelion_thumnail";
	    public static final String THUMNAIL_CACHE_PROFILE_PATH = "dandelion_thumnail/.profile";
	    public static final String THUMNAIL_CACHE_PROFILE_BIG_PATH = "dandelion_thumnail/.profile_big";
	    public static final String THUMNAIL_CACHE_SMALL_PATH = "dandelion_thumnail/.small_thumnail";
	    public static final String ACTION_CREATE_NOTE_SUCCESS = "com.ustc.dystu.dandelion.CREATE_NOTE_SUCCESS";
	    public static final String ACTION_EDIT_NOTE_SUCCESS = "com.ustc.dystu.dandelion.EDIT_NOTE_SUCCESS";
	    
	    public static final int BUFFER_SIZE = 128 * 1024;

}
