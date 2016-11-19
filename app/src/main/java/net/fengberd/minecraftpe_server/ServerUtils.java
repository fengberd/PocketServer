package net.fengberd.minecraftpe_server;

import android.content.*;
import android.os.*;

import java.io.*;
import java.lang.Process;

public class ServerUtils
{
	private static File appDirectory=null;
	private static File nukkitDataDirectory=new File(Environment.getExternalStorageDirectory(),"Nukkit"),pocketmineDataDirectory=new File(Environment.getExternalStorageDirectory(),"PocketMine");

	private static Process serverProcess=null;
	private static InputStreamReader stdout=null;
	private static OutputStreamWriter stdin=null;

	public static void setAppDirectory(Context ctx)
	{
		appDirectory=ctx.getFilesDir().getParentFile();
	}

	public static File getAppDirectory()
	{
		return appDirectory;
	}

	public static File getDataDirectory()
	{
		File dir=MainActivity.nukkitMode ? nukkitDataDirectory : pocketmineDataDirectory;
		dir.mkdirs();
		return dir;
	}

	public static void killServer()
	{
		try
		{
			// TODO: This might kill other processes?
			Runtime.getRuntime()
					.exec(getAppDirectory() + "/busybox killall -9 " + (MainActivity.nukkitMode ? "java" : "php"))
					.waitFor();
		}
		catch(Exception ignored)
		{

		}
	}

	public static boolean isRunning()
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

	public static void runServer()
	{
		File f=new File(getDataDirectory(),"tmp");
		if(!f.isDirectory() && f.exists())
		{
			f.delete();
		}
		f.mkdirs();
		setPermission();
		String file=MainActivity.nukkitMode ? "/Nukkit.jar" : (new File(getDataDirectory(),"/PocketMine-MP.phar")
				.exists() ? "/PocketMine-MP.phar" : "/src/pocketmine/PocketMine.php");
		File ini=new File(getDataDirectory() + "/php.ini");
		if(!MainActivity.nukkitMode && !ini.exists())
		{
			try
			{
				ini.createNewFile();
				FileOutputStream os=new FileOutputStream(ini);
				os.write("phar.readonly=0\nphar.require_hash=1\ndate.timezone=Asia/Shanghai\nshort_open_tag=0\nasp_tags=0\nopcache.enable=1\nopcache.enable_cli=1\nopcache.save_comments=1\nopcache.fast_shutdown=0\nopcache.max_accelerated_files=4096\nopcache.interned_strings_buffer=8\nopcache.memory_consumption=128\nopcache.optimization_level=0xffffffff"
						.getBytes("UTF8"));
				os.close();
			}
			catch(Exception ignored)
			{

			}
		}
		String[] args=null;
		if(MainActivity.nukkitMode)
		{
			args=new String[]{
					getAppDirectory() + "/java/jre/bin/java",
					"-jar",
					getDataDirectory() + file,
					MainActivity.ansiMode ? "enable-ansi" : "disable-ansi",
					"disable-jline"
			};
		}
		else
		{
			args=new String[]{
					getAppDirectory() + "/php",
					"-c",
					getDataDirectory() + "/php.ini",
					getDataDirectory() + file,
					MainActivity.ansiMode ? "--enable-ansi" : "--disable-ansi"
			};
		}
		ProcessBuilder builder=new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(getDataDirectory());
		builder.environment().put("TMPDIR",getDataDirectory() + "/tmp");
		try
		{
			serverProcess=builder.start();
			stdout=new InputStreamReader(serverProcess.getInputStream(),"UTF-8");
			stdin=new OutputStreamWriter(serverProcess.getOutputStream(),"UTF-8");
			Thread tMonitor=new Thread()
			{
				public void run()
				{
					BufferedReader br=new BufferedReader(stdout);
					while(isRunning())
					{
						try
						{
							int size=0;
							char[] buffer=new char[8192];
							StringBuilder s=new StringBuilder();
							while((size=br.read(buffer,0,buffer.length))!=-1)
							{
								s.setLength(0);
								for(int i=0;i<size;i++)
								{
									char c=buffer[i];
									switch(c)
									{
									case '\r':
										continue;
									case '\n':
										String line=s.toString();
										if(!line.startsWith("\u001B]0;"))
										{
											ConsoleActivity.log(line);
										}
									case '\u0007':
										s.setLength(0);
										break;
									default:
										s.append(c);
										break;
									}
								}
							}
						}
						catch(IOException ignored)
						{

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
					MainActivity.postMessage(MainActivity.ACTION_STOP_SERVICE,0,null);
				}
			};
			tMonitor.start();
		}
		catch(Exception e)
		{
			ConsoleActivity.log("[PE Server] Unable to start " + (MainActivity.nukkitMode ? "Java" : "PHP") + ".");
			ConsoleActivity.log(e.toString());
			MainActivity.postMessage(MainActivity.ACTION_STOP_SERVICE,0,null);
			killServer();
		}
	}

	public static void setPermission()
	{
		try
		{
			if(MainActivity.nukkitMode)
			{
				Runtime.getRuntime()
						.exec("./busybox chmod -R 755 java",new String[0],appDirectory)
						.waitFor();
			}
			else
			{
				new File(appDirectory,"php").setExecutable(true,true);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static boolean writeCommand(String cmd)
	{
		try
		{
			stdin.write(cmd + "\r\n");
			stdin.flush();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
}
