package net.fengberd.minecraftpe_server;

import java.io.*;
import java.nio.charset.Charset;

import android.content.Context;

public final class ServerUtils
{
	public static Context mContext;
	
	private static OutputStream stdin;
	private static InputStream stdout;

	final public static void setContext(Context mContext)
	{
		ServerUtils.mContext=mContext;
	}

	final public static String getAppDirectory()
	{
		return mContext.getApplicationInfo().dataDir;
	}

	final public static String getDataDirectory()
	{
		String dir=android.os.Environment.getExternalStorageDirectory().getPath() + (HomeActivity.nukkitMode?"/Nukkit":"/PocketMine");
		new File(dir).mkdirs();
		return dir;
	}

	final public static Boolean killProcessByName(String mProcessName)
	{
		return execCommand(getAppDirectory() + "/busybox killall -9 " + mProcessName);
	}

	final public static void stopServer()
	{
		killProcessByName(HomeActivity.nukkitMode?"java":"php");
	}

	static Process serverProc;

	public static Boolean isRunning()
	{
		try
		{
			serverProc.exitValue();
		}
		catch(Exception e)
		{
			return true;
		}
		return false;
	}

	final public static void runServer()
	{
		File f = new File(getDataDirectory(),"/tmp");
		if(!f.exists())
		{
			f.mkdir();
		}
		else if(!f.isDirectory())
		{
			f.delete();
			f.mkdir();
		}
		setPermission();
		String file = "/Nukkit.jar";
		if(!HomeActivity.nukkitMode)
		{
			if(new File(getDataDirectory() + "/PocketMine-MP.phar").exists())
			{
				file="/PocketMine-MP.phar";
			}
			else
			{
				file = "/src/pocketmine/PocketMine.php";
			}
		}
		File ini=new File(getDataDirectory() + "/php.ini");
		if(!HomeActivity.nukkitMode && !ini.exists())
		{
			try
			{
				ini.createNewFile();
				FileOutputStream os=new FileOutputStream(ini);
				os.write("phar.readonly=0\nphar.require_hash=1\ndate.timezone=Asia/Shanghai\nshort_open_tag=0\nasp_tags=0\nopcache.enable=1\nopcache.enable_cli=1\nopcache.save_comments=1\nopcache.fast_shutdown=0\nopcache.max_accelerated_files=4096\nopcache.interned_strings_buffer=8\nopcache.memory_consumption=128\nopcache.optimization_level=0xffffffff".getBytes("UTF8"));
				os.close();
			}
			catch(Exception e)
			{

			}
		}
		String[] args=null;
		if(HomeActivity.nukkitMode)
		{
			args=new String[]
			{
				getAppDirectory() + "/java/jre/bin/java",
				"-jar",
				getDataDirectory() + file,
				HomeActivity.ansiMode?"enable-ansi":"disable-ansi"
			};
		}
		else
		{
			args=new String[]
			{
				getAppDirectory() + "/php",
				"-c",
				getDataDirectory() + "/php.ini",
				getDataDirectory() + file,
				HomeActivity.ansiMode?"--enable-ansi":"--disable-ansi"
			};
		}
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(new File(getDataDirectory()));
		builder.environment().put("TMPDIR",getDataDirectory() + "/tmp");
		try
		{
			serverProc=builder.start();
			stdout=serverProc.getInputStream();
			stdin=serverProc.getOutputStream();
			Thread tMonitor = new Thread()
			{
				public void run()
				{
					InputStreamReader reader = new InputStreamReader(stdout,Charset.forName("UTF-8"));
					BufferedReader br = new BufferedReader(reader);
					while(isRunning())
					{
						try
						{
							char[] buffer = new char[8192];
							int size = 0;
							while((size = br.read(buffer,0,buffer.length)) != -1)
							{
								StringBuilder s = new StringBuilder();
								for(int i = 0; i < size; i++) 
								{
									char c = buffer[i];
									if(c == '\r')
									{
										continue;
									}
									if(c == '\n' || c == '\u0007')
									{
										String line = s.toString();
										if(c == '\u0007' && line.startsWith("\u001B]0;"))
										{
											//Do nothing.
										}
										else
										{
											LogActivity.log(line);
										}
										s=new StringBuilder();
									}
									else
									{
										s.append(buffer[i]);
									}
								}
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								br.close();
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					LogActivity.log("[PE Server] Server was stopped.");
					HomeActivity.stopNotifyService();
				}
			};
			tMonitor.start();
		}
		catch(Exception e)
		{
			LogActivity.log("[PE Server] Unable to start "+(HomeActivity.nukkitMode?"Java":"PHP")+".");
			LogActivity.log(e.toString());
			HomeActivity.stopNotifyService();
			killProcessByName(HomeActivity.nukkitMode?"java":"php");
		}
		return;
	}

	final public static boolean execCommand(String mCommand)
	{
		Runtime r = Runtime.getRuntime();
		try
		{
			r.exec(mCommand).waitFor();
		}
		catch(Exception e)
		{
			r=null;
			return false;
		}
		return true;
	}

	final static public void setPermission()
	{
		try
		{
			if(HomeActivity.nukkitMode)
			{
				Runtime.getRuntime().exec("./busybox chmod -R 755 java",new String[0],new File(getAppDirectory())).waitFor();
			}
			else
			{
				new File(getAppDirectory()+"/php").setExecutable(true,true);
			}
		}
		catch(Exception e)
		{

		}
	}

	public static void executeCMD(String Cmd)
	{
		try
		{
			stdin.write((Cmd + "\r\n").getBytes());
			stdin.flush();
		}
		catch(Exception e)
		{

		}
	}
}

