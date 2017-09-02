package moe.berd.pocket_server.utils;

import android.content.*;

@SuppressWarnings("unused")
public class ConfigProvider
{
	private static SharedPreferences config;
	
	public static void init(SharedPreferences config)
	{
		ConfigProvider.config=config;
	}
	
	public static SharedPreferences getConfig()
	{
		return config;
	}
	
	public static void set(String key,Object value)
	{
		SharedPreferences.Editor editor=config.edit();
		if(value instanceof Boolean)
		{
			editor.putBoolean(key,(boolean)value);
		}
		else if(value instanceof Integer)
		{
			editor.putInt(key,(int)value);
		}
		else if(value instanceof String)
		{
			editor.putString(key,(String)value);
		}
		else
		{
			throw new RuntimeException("Unexpected value type.");
		}
		editor.apply();
	}
	
	public static int getInt(String key,int defaultValue)
	{
		return config.getInt(key,defaultValue);
	}
	
	public static String getString(String key,String defaultValue)
	{
		return config.getString(key,defaultValue);
	}
	
	public static boolean getBoolean(String key,boolean defaultValue)
	{
		return config.getBoolean(key,defaultValue);
	}
}
