package net.fengberd.minecraftpe_server;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;

import android.content.Context;

public final class ServerUtils
{
	public static Context mContext;

	private static Process serverProcess;
	private static OutputStream stdin;
	private static InputStream stdout;

	public static void setContext(Context mContext)
	{
		ServerUtils.mContext=mContext;
	}

	public static String getAppDirectory()
	{
		return mContext.getApplicationInfo().dataDir;
	}

	public static String getDataDirectory()
	{
		String dir=android.os.Environment.getExternalStorageDirectory().getPath() + (MainActivity.nukkitMode?"/Nukkit":"/PocketMine");
		new File(dir).mkdirs();
		return dir;
	}

	public static void killServer()
	{
		try
		{
			Runtime.getRuntime().exec(getAppDirectory() + "/busybox killall -9 " +(MainActivity.nukkitMode?"java":"php")).waitFor();
		}
		catch(Exception e)
		{
			
		}
	}

	public static Boolean isRunning()
	{
		try
		{
			serverProcess.exitValue();
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
		if(!MainActivity.nukkitMode)
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
		if(!MainActivity.nukkitMode && !ini.exists())
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
		if(MainActivity.nukkitMode)
		{
			args=new String[]
			{
				getAppDirectory() + "/java/jre/bin/java",
				"-jar",
				getDataDirectory() + file,
				MainActivity.ansiMode?"enable-ansi":"disable-ansi",
				"disable-jline"
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
				MainActivity.ansiMode?"--enable-ansi":"--disable-ansi"
			};
		}
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(new File(getDataDirectory()));
		Map<String,String> env=builder.environment();
		env.put("TMPDIR",getDataDirectory() + "/tmp");
		try
		{
			serverProcess=builder.start();
			stdout=serverProcess.getInputStream();
			stdin=serverProcess.getOutputStream();
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
										if(c == '\u0007' || line.startsWith("\u001B]0;"))
										{
											//Do nothing.
										}
										else
										{
											ConsoleActivity.log(line);
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
					ConsoleActivity.log("[PE Server] Server was stopped.");
					MainActivity.stopNotifyService();
				}
			};
			tMonitor.start();
		}
		catch(Exception e)
		{
			ConsoleActivity.log("[PE Server] Unable to start "+(MainActivity.nukkitMode?"Java":"PHP")+".");
			ConsoleActivity.log(e.toString());
			MainActivity.stopNotifyService();
			killServer();
		}
		return;
	}

	final static public void setPermission()
	{
		try
		{
			if(MainActivity.nukkitMode)
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

	public static void writeCommand(String Cmd)
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

