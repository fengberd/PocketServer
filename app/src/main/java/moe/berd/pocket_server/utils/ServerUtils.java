package moe.berd.pocket_server.utils;

import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.os.*;

import java.io.*;
import java.lang.Process;
import java.net.*;
import java.security.cert.*;
import java.util.*;

import javax.net.ssl.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.fragment.*;

@SuppressWarnings({"ResultOfMethodCallIgnored","UnusedReturnValue","WeakerAccess"})
public class ServerUtils
{
	private static File appDirectory=null, appFilesDirectory=null;
	private static File nukkitDataDirectory=new File(Environment.getExternalStorageDirectory(),"Nukkit"), pocketmineDataDirectory=new File(Environment
		.getExternalStorageDirectory(),"PocketMine");
	
	private static Process serverProcess=null;
	private static InputStreamReader stdout=null;
	private static OutputStreamWriter stdin=null;
	
	private static long startTime=0;
	
	@SuppressLint("SdCardPath")
	@SuppressWarnings("SpellCheckingInspection")
	public static void init(Context ctx)
	{
		appFilesDirectory=ctx.getFilesDir();
		if(appFilesDirectory==null)
		{
			ContextWrapper wrapper=new ContextWrapper(ctx);
			if(wrapper.getApplicationInfo().dataDir!=null)
			{
				appFilesDirectory=new File(wrapper.getApplicationInfo().dataDir,"files");
			}
			else
			{
				// Add this line to compatible with some strange devices
				appFilesDirectory=new File("/data/user/0/net.fengberd.minecraftpe_server/files/");
			}
		}
		appDirectory=appFilesDirectory.getParentFile();
	}
	
	public static File getAppDirectory()
	{
		return appDirectory;
	}
	
	public static File getAppFilesDirectory()
	{
		return appFilesDirectory;
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
			ConsoleFragment.logLine("[PocketServer] Killing server...");
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
	
	@SuppressWarnings("SpellCheckingInspection")
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
				os.write(("zend.enable_gc=On\nzend.assertions=-1\n\nenable_dl=On\nallow_url_fopen=On\nmax_execution_time=0\nregister_argc_argv=On\n\nerror_reporting=-1\ndisplay_errors=stderr\ndisplay_startup_errors=On\n\ndefault_charset=\"UTF-8\"\n\nphar.readonly=Off\nphar.require_hash=On\n\nopcache.enable=1\nopcache.enable_cli=1\nopcache.save_comments=1\nopcache.load_comments=1\nopcache.fast_shutdown=0\nopcache.memory_consumption=128\nopcache.interned_strings_buffer=8\nopcache.max_accelerated_files=4000\nopcache.optimization_level=0xffffffff")
					.getBytes("UTF8"));
				os.close();
			}
			catch(Exception ignored)
			{
			
			}
		}
		String[] args;
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
				getAppDirectory() + "/php",
				"-c",
				getDataDirectory() + "/php.ini",
				getDataDirectory() + file,
				MainActivity.ansiMode ? "--enable-ansi" : "--disable-ansi",
				"--no-wizard"
			};
		}
		ProcessBuilder builder=new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(getDataDirectory());
		builder.environment().put("TMPDIR",getDataDirectory() + "/tmp");
		try
		{
			startTime=System.currentTimeMillis();
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
							int size;
							char[] buffer=new char[8192];
							StringBuilder s;
							while((size=br.read(buffer,0,buffer.length))!=-1)
							{
								s=new StringBuilder();
								for(int i=0;i<size;i++)
								{
									char c=buffer[i];
									switch(c)
									{
									case '\r':
										continue;
									case '\n':
										ConsoleFragment.logLine(s.toString());
									case '\u0007':
										s=new StringBuilder();
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
					if(System.currentTimeMillis() - startTime<3000)
					{
						ConsoleFragment.logLine("[PocketServer] Server start failed!");
						MainActivity.postStartFailedWarning();
					}
					else
					{
						ConsoleFragment.logLine("[PocketServer] Server was stopped.");
					}
					MainActivity.postStopService();
				}
			};
			tMonitor.start();
		}
		catch(Exception e)
		{
			ConsoleFragment.logLine("[PocketServer] Unable to start " + (MainActivity.nukkitMode ? "Java" : "PHP") + ".");
			ConsoleFragment.logLine(e.toString());
			MainActivity.postStopService();
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
	
	public static void walkSetPermission(File folder)
	{
		folder.setReadable(true,true);
		folder.setExecutable(true,true);
		File[] files=folder.listFiles();
		if(files==null)
		{
			return;
		}
		for(File file : files)
		{
			file.setReadable(true,true);
			file.setExecutable(true,true);
			if(file.isDirectory())
			{
				walkSetPermission(file);
			}
		}
	}
	
	public static void setPermission()
	{
		try
		{
			if(MainActivity.nukkitMode)
			{
				walkSetPermission(new File(appDirectory,"java"));
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
	
	public static void copyStream(InputStream is,OutputStream os) throws IOException
	{
		copyStream(is,os,true);
	}
	
	public static void copyStream(InputStream is,OutputStream os,boolean close) throws IOException
	{
		int cou;
		byte[] buffer=new byte[8192];
		while((cou=is.read(buffer))!=-1)
		{
			os.write(buffer,0,cou);
		}
		if(close)
		{
			is.close();
			os.close();
		}
	}
	
	public static URLConnection openNetConnection(String url) throws Exception
	{
		final SSLContext sc=SSLContext.getInstance("SSL");
		sc.init(null,new TrustManager[]{
			new X509TrustManager()
			{
				@Override
				@SuppressLint("TrustAllX509TrustManager")
				public void checkClientTrusted(X509Certificate[] p1,String p2)
				{
				
				}
				
				@Override
				@SuppressLint("TrustAllX509TrustManager")
				public void checkServerTrusted(X509Certificate[] p1,String p2)
				{
				
				}
				
				@Override
				public X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}
			}
		},new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
		{
			@SuppressLint("BadHostnameVerifier")
			public boolean verify(String hostname,SSLSession session)
			{
				return true;
			}
		});
		URL req=new URL(url);
		URLConnection connection=req.openConnection();
		connection.connect();
		return connection;
	}
	
	@SuppressWarnings("deprecation")
	public static void installBinary(File target,Context ctx,String filename,String friendlyName) throws Exception
	{
		AssetManager assets=ctx.getAssets();
		ArrayList<String> ABIS=new ArrayList<>(), supportedABIS=new ArrayList<>();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
		}
		if(data==null)
		{
			throw new ABINotSupportedException(friendlyName,supportedABIS);
		}
		target.delete();
		File writeTo=compressed ? new File(target + ".tar.xz") : target;
		copyStream(data,new FileOutputStream(writeTo));
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
	
	public static void mountJavaLibrary() throws Exception
	{
		String prefix=(ConfigProvider.getBoolean("KusudMode",false) ? "ku.sud" : "su") + " -c " + ServerUtils
			.getAppDirectory() + "/busybox ";
		Runtime.getRuntime().exec(prefix + "mount -o rw,remount /").waitFor();
		if(!new File("/lib").exists())
		{
			Runtime.getRuntime().exec(prefix + "mkdir /lib").waitFor();
		}
		else
		{
			Runtime.getRuntime().exec(prefix + "umount /lib").waitFor();
		}
		Runtime.getRuntime()
			.exec(prefix + "mount -o bind " + ServerUtils.getAppDirectory() + "/java/lib /lib")
			.waitFor();
		if(!mountedJavaLibrary())
		{
			throw new RuntimeException("Mount failed.");
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
	
	public static boolean mountedJavaLibrary()
	{
		String[] list=new File("/lib").list();
		if(list!=null)
		{
			for(String f : list)
			{
				if(f.contains("ld-linux.so"))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean installedServerSoftware()
	{
		if(MainActivity.nukkitMode)
		{
			return new File(getDataDirectory(),"Nukkit.jar").exists();
		}
		else
		{
			return new File(getDataDirectory(),"PocketMine-MP.phar").exists() || new File(getDataDirectory(),"src")
				.exists();
		}
	}
}
