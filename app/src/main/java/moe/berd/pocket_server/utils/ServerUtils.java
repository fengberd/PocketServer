package moe.berd.pocket_server.utils;

import android.content.*;
import android.content.res.*;
import android.os.*;

import java.io.*;
import java.lang.Process;
import java.util.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.fragment.*;

public class ServerUtils
{
	private static File appDirectory=null;
	private static File nukkitDataDirectory=new File(Environment.getExternalStorageDirectory(),"Nukkit"), pocketmineDataDirectory=new File(Environment
		.getExternalStorageDirectory(),"PocketMine");
	
	private static Process serverProcess=null;
	private static InputStreamReader stdout=null;
	private static OutputStreamWriter stdin=null;
	
	public static void init(Context ctx)
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
			if(serverProcess!=null)
			{
				serverProcess.exitValue();
			}
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
				getAppDirectory() + "/java/jre/bin/java","-Djline.terminal=off","-jar",
				getDataDirectory() + file,MainActivity.ansiMode ? "enable-ansi" : "disable-ansi"
			};
		}
		else
		{
			args=new String[]{
				getAppDirectory() + "/php","-c",getDataDirectory() + "/php.ini",getDataDirectory() + file,MainActivity.ansiMode ? "--enable-ansi" : "--disable-ansi"
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
										// TODO: lol.
										if(line.startsWith("\u001b]0;"))
										{
											ConsoleFragment.postTitle(line.substring(8));
										}
										else
										{
											ConsoleFragment.log(line);
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
					ConsoleFragment.log("[PE Server] Server was stopped.");
					MainActivity.postMessage(MainActivity.ACTION_STOP_SERVICE,0,null);
				}
			};
			tMonitor.start();
		}
		catch(Exception e)
		{
			ConsoleFragment.log("[PE Server] Unable to start " + (MainActivity.nukkitMode ? "Java" : "PHP") + ".");
			ConsoleFragment.log(e.toString());
			MainActivity.postMessage(MainActivity.ACTION_STOP_SERVICE,0,null);
			killServer();
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
	
	public static void setPermission()
	{
		try
		{
			if(MainActivity.nukkitMode)
			{
				Runtime.getRuntime().exec("./busybox chmod -R 755 java",new String[0],appDirectory).waitFor();
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
	
	@SuppressWarnings("deprecation")
	public static void installBinary(File target,Context ctx,String filename,String friendlyName) throws Exception
	{
		AssetManager assets=ctx.getAssets();
		List<String> ABIS=new ArrayList<>(), supportedABIS=new ArrayList<>();
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
		{
			Collections.addAll(ABIS,Build.SUPPORTED_ABIS);
		}
		else
		{
			ABIS.add(Build.CPU_ABI);
			if(Build.CPU_ABI2!=null && !Build.CPU_ABI.equals(Build.CPU_ABI2))
			{
				ABIS.add(Build.CPU_ABI2);
			}
		}
		Collections.addAll(supportedABIS,assets.list(filename));
		InputStream data=null;
		boolean compressed=false;
		for(String ABI : ABIS)
		{
			if(supportedABIS.contains(ABI + ".tar.xz"))
			{
				compressed=true;
				data=assets.open(filename + "/" + ABI + ".tar.xz");
				break;
			}
			if(supportedABIS.contains(ABI))
			{
				data=assets.open(filename + "/" + ABI);
				break;
			}
			if(ABI.startsWith("armeabi") || ABI.startsWith("arm64"))
			{
				if(supportedABIS.contains("armeabi.tar.xz"))
				{
					compressed=true;
					data=assets.open(filename + "/armeabi.tar.xz");
				}
				else if(supportedABIS.contains("armeabi"))
				{
					data=assets.open(filename + "/armeabi");
				}
				break;
			}
			if(ABI.startsWith("x86"))
			{
				if(supportedABIS.contains("i686.tar.xz"))
				{
					compressed=true;
					data=assets.open(filename + "/i686.tar.xz");
				}
				else if(supportedABIS.contains("i686"))
				{
					data=assets.open(filename + "/i686");
				}
				break;
			}
		}
		if(data==null)
		{
			throw new ABINotSupportedException(friendlyName);
		}
		target.delete();
		File writeTo=compressed ? new File(target + ".tar.xz") : target;
		OutputStream os=new FileOutputStream(writeTo);
		int cou=0;
		byte[] buffer=new byte[8192];
		while((cou=data.read(buffer))!=-1)
		{
			os.write(buffer,0,cou);
		}
		os.close();
		data.close();
		if(compressed)
		{
			Runtime.getRuntime().exec("./busybox tar -xf " + writeTo,new String[0],appDirectory).waitFor();
			writeTo.delete();
		}
		target.setExecutable(true,true);
	}
	
	public static void installPHP(Context ctx,String version) throws Exception
	{
		installBinary(new File(getAppDirectory(),"php"),ctx,"php" + version,"PHP" + version);
	}
	
	public static void installBusybox(Context ctx) throws Exception
	{
		File target=new File(getAppDirectory(),"busybox");
		if(!target.exists())
		{
			installBinary(target,ctx,"busybox","Busybox");
		}
	}
	
	public static boolean installedPHP()
	{
		return new File(getAppDirectory(),"php").exists();
	}
	
	public static boolean installedJava()
	{
		return new File(getAppDirectory(),"java/jre/bin/java").exists();
	}
}
