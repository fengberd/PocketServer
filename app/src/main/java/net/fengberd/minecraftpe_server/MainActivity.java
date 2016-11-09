package net.fengberd.minecraftpe_server;

import java.io.*;
import java.net.*;
import java.security.cert.*;

import javax.net.ssl.*;

import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.view.View.*;

import org.json.*;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockActivity;
import java.security.cert.*;

public class MainActivity extends SherlockActivity
{
	final static int FORCE_CLOSE_CODE = 143,
		CONSOLE_CODE = FORCE_CLOSE_CODE + 1,
		INSTALL_PHP_CODE = CONSOLE_CODE + 1,
		INSTALL_PHP7_CODE = INSTALL_PHP_CODE + 1,
		INSTALL_JAVA_CODE = INSTALL_PHP7_CODE + 1,
		DOWNLOAD_SERVER_CODE = INSTALL_JAVA_CODE + 1;

	public static Intent serverIntent=null;
	public static MainActivity instance = null;
	public static RadioButton radio_pocketmine=null,radio_nukkit=null;
	public static CheckBox check_kusud=null,check_ansi=null;
	public static Button button_start=null,button_stop=null,button_mount=null;
	public static SeekBar seekbar_fontsize=null;
	public static MenuItem menu_install_php=null,menu_install_java=null,menu_download_server=null;
	public static SharedPreferences config=null;
	
	public static boolean isStarted = false,nukkitMode=false,ansiMode=false;

	public static final String[] jenkins_nukkit=new String[]
	{
		"Angelic47|http://ci.angelic47.com:30001/job/Nukkit/",
		"MengCraft|http://ci.mengcraft.com:8080/job/nukkit/",
		"RegularBox|http://ci.regularbox.com/job/Nukkit/",
		"ZXDA|https://jenkins.zxda.net/job/Nukkit/"
	},jenkins_pocketmine=new String[]
	{
		"Genisys (iTX Tech)|https://ci.itxtech.org/job/Genisys/",
		"Genisys (ZXDA,Not suggested)|https://jenkins.zxda.net/job/Genisys/",
		"ClearSky-PHP7 (ZXDA)|https://jenkins.zxda.net/job/ClearSky-PHP7/",
		"ClearSky-PHP5 (ZXDA)|https://jenkins.zxda.net/job/ClearSky-PHP5/",
		"PocketMine-MP (ZXDA)|https://jenkins.zxda.net/job/PocketMine-MP/"
		"PocketMine-MP (pmmp)|https://jenkins.pmmp.gq/job/PocketMine-MP/"
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		
		instance=this;
		config=getSharedPreferences("config",0);
		ServerUtils.setContext(instance);
		
		ansiMode=config.getBoolean("ANSIMode",ansiMode);
		nukkitMode=config.getBoolean("NukkitMode",nukkitMode);
		
		button_stop=(Button) findViewById(R.id.button_stop);
		button_start=(Button) findViewById(R.id.button_start);
		button_mount=(Button) findViewById(R.id.button_mount);
		
		check_ansi=(CheckBox)findViewById(R.id.check_ansi);
		check_kusud=(CheckBox)findViewById(R.id.check_kusud);

		radio_nukkit=(RadioButton) findViewById(R.id.radio_nukkit);
		radio_pocketmine=(RadioButton) findViewById(R.id.radio_pocketmine);
		
		seekbar_fontsize=(SeekBar)findViewById(R.id.seekbar_fontsize);
		
		seekbar_fontsize.setProgress(config.getInt("ConsoleFontSize",16));
		seekbar_fontsize.setMax(30);
		
		ConsoleActivity.font_size=seekbar_fontsize.getProgress();
		
		check_ansi.setChecked(ansiMode);
		check_kusud.setChecked(config.getBoolean("KusudMode",false));
		
		radio_nukkit.setChecked(nukkitMode);
		radio_pocketmine.setChecked(!nukkitMode);
		
		check_ansi.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ansiMode=check_ansi.isChecked();
				config.edit().putBoolean("ANSIMode",ansiMode).apply();
			}
		});
		check_kusud.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				config.edit().putBoolean("KusudMode",check_kusud.isChecked()).apply();
			}
		});
		
		radio_nukkit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				nukkitMode=true;
				config.edit().putBoolean("NukkitMode",nukkitMode).apply();
				refreshEnabled();
			}
		});
		radio_pocketmine.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				nukkitMode=false;
				config.edit().putBoolean("NukkitMode",nukkitMode).apply();
				refreshEnabled();
			}
		});
		
		seekbar_fontsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar p1,int p2,boolean p3)
			{
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar p1)
			{
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar p1)
			{
				ConsoleActivity.font_size=p1.getProgress();
				config.edit().putInt("ConsoleFontSize",p1.getProgress()).apply();
			}
		});
		
		button_mount.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					String binary="su",busybox=ServerUtils.getAppDirectory()+"/busybox ";
					if(((CheckBox)findViewById(R.id.check_kusud)).isChecked())
					{
						binary="ku.sud";
					}
					Runtime.getRuntime().exec(binary+" -c "+busybox+"mount -o rw,remount /").waitFor();
					if(!new File("/lib").exists())
					{
						Runtime.getRuntime().exec(binary+" -c "+busybox+"mkdir /lib").waitFor();
					}
					else
					{
						Runtime.getRuntime().exec(binary+" -c "+busybox+"umount /lib").waitFor();
					}
					Runtime.getRuntime().exec(binary+" -c "+busybox+"mount -o bind "+ServerUtils.getAppDirectory()+"/java/lib /lib").waitFor();
					toast(R.string.message_done);
				}
				catch(Exception e)
				{
					toast(e.toString());
				}
			}
		});
		button_start.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				isStarted=true;
				refreshEnabled();
				serverIntent=new Intent(instance,ServerService.class);
				startService(serverIntent);
				ServerUtils.runServer();
			}
		});
		button_stop.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				if(ServerUtils.isRunning())
				{
					ServerUtils.writeCommand("stop");
				}
			}
		});
		refreshEnabled();
	}
	
	public static void installBusybox() throws Exception
	{
		copyAsset("busybox",ServerUtils.getAppDirectory()+"/busybox");
		new File(ServerUtils.getAppDirectory()+"/busybox").setExecutable(true,true);
	}
	
	public static void refreshEnabled()
	{
		radio_nukkit.setEnabled(!isStarted);
		radio_pocketmine.setEnabled(!isStarted);
		button_mount.setEnabled(!isStarted);
		check_ansi.setEnabled(!isStarted);
		if(menu_install_php!=null)
		{
			menu_install_php.setEnabled(!isStarted);
			menu_install_java.setEnabled(!isStarted);
			menu_download_server.setEnabled(!isStarted);
		}
		if(nukkitMode && !new File(ServerUtils.getAppDirectory()+"/java/jre/bin/java").exists())
		{
			button_start.setEnabled(false);
		}
		else if(!nukkitMode && !new File(ServerUtils.getAppDirectory()+"/php").exists())
		{
			button_start.setEnabled(false);
		}
		else
		{
			button_start.setEnabled(!isStarted);
		}
		button_stop.setEnabled(isStarted);
	}
	
	public static void copyAsset(String name,String target) throws Exception
	{
		File tmp=new File(target);
		tmp.delete();
		OutputStream os=new FileOutputStream(tmp);
		InputStream is=instance.getAssets().open(name);
		int cou=0;
		byte[] buffer=new byte[8192];
		while((cou=is.read(buffer))!=-1)
		{
			os.write(buffer,0,cou);
		}
		is.close();
		os.close();
	}

	public static void stopNotifyService()
	{
		if(instance != null && serverIntent != null)
		{
			instance.runOnUiThread(new Runnable()
			{
				public void run()
				{
					isStarted=false;
					refreshEnabled();
					instance.stopService(serverIntent);
				}
			});
		}
	}
	
	public static String getInternetString(String url)
	{
		try
		{
			BufferedReader reader=new BufferedReader(new InputStreamReader(openNetConnection(url).getInputStream()));
			StringBuilder sb=new StringBuilder();
			String line=null;
			while((line=reader.readLine())!=null)
			{
				sb.append(line).append('\r');
			}
			reader.close();
			return sb.toString();
		}
		catch(Exception e)
		{
			instance.toast(e.toString());
		}
		return null;
	}

	public static void downloadServer(String jenkins,File saveTo,final ProgressDialog dialog)
	{
		try
		{
			JSONObject json=new JSONObject(getInternetString(jenkins+"lastSuccessfulBuild/api/json"));
			JSONArray artifacts=json.getJSONArray("artifacts");
			if(artifacts.length()<=0)
			{
				throw new Exception(instance.getString(R.string.message_no_artifacts));
			}
			json=artifacts.getJSONObject(0);
			downloadFile(jenkins+"lastSuccessfulBuild/artifact/"+json.getString("relativePath"),saveTo,dialog);
		}
		catch(Exception e)
		{
			instance.toast(e.getMessage());
		}
	}
	
	public static void downloadFile(String url,File saveTo,final ProgressDialog dialog)
	{
		OutputStream output=null;
		InputStream input=null;
		try
		{
			if(saveTo.exists())
			{
				saveTo.delete();
			}
			URLConnection connection=openNetConnection(url);
			input=new BufferedInputStream(connection.getInputStream());
			output=new FileOutputStream(saveTo);
			int count=0;
			long read=0;
			if(dialog!=null)
			{
				final long max=connection.getContentLength();
				instance.runOnUiThread(new Runnable()
				{
					public void run()
					{
						dialog.setMax((int)max/1024);
					}
				});
			}
			byte[] buffer=new byte[4096];
			while((count=input.read(buffer))>=0)
			{
				output.write(buffer,0,count);
				read+=count;
				if(dialog!=null)
				{
					final int temp=(int)(read/1000);
					instance.runOnUiThread(new Runnable()
					{
						public void run()
						{
							dialog.setProgress(temp);
						}
					});
				}
			}
			output.close();
			input.close();
			instance.toast(R.string.message_done);
		}
		catch(Exception e)
		{
			instance.toast(e.getMessage());
		}
		finally
		{
			try
			{
				output.close();
				input.close();
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	public static URLConnection openNetConnection(String url) throws Exception
	{
		final SSLContext sc=SSLContext.getInstance("SSL");
		sc.init(null,new TrustManager[]
		{
			new X509TrustManager()
			{
				@Override
				public void checkClientTrusted(X509Certificate[] p1,String p2) throws CertificateException
				{
					
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] p1,String p2) throws CertificateException
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
	
	public void toast(int text)
	{
		toast(getString(text));
	}
	
	public void toast(final String text)
	{
		if(instance!=null)
		{
			instance.runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(instance,text,Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,CONSOLE_CODE,0,getString(R.string.menu_console))
			.setIcon(R.drawable.hardware_dock)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu_install_php=menu.add(0,INSTALL_PHP_CODE,0,getString(R.string.menu_install_php));
		menu_install_php=menu.add(0,INSTALL_PHP7_CODE,0,getString(R.string.menu_install_php7));
		menu_install_java=menu.add(0,INSTALL_JAVA_CODE,0,getString(R.string.menu_install_java));
		menu_download_server=menu.add(0,DOWNLOAD_SERVER_CODE,0,getString(R.string.menu_download));
		menu.add(0,FORCE_CLOSE_CODE,0,getString(R.string.menu_kill));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final ProgressDialog processing_dialog=new ProgressDialog(instance);
		switch(item.getItemId())
		{
		case 0:
		case android.R.id.home:
			return false;
		case FORCE_CLOSE_CODE:
			ServerUtils.killServer();
			if(serverIntent != null)
			{
				stopService(serverIntent);
			}
			isStarted=false;
			refreshEnabled();
			break;
		case CONSOLE_CODE:
			startActivity(new Intent(instance,ConsoleActivity.class));
			break;
		case INSTALL_PHP_CODE:
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_installing));
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						installBusybox();
						copyAsset("php",ServerUtils.getAppDirectory()+"/php");
						toast(R.string.message_install_success);
					}
					catch(Exception e)
					{
						toast(getString(R.string.message_install_fail)+"\n"+e.toString());
					}
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							processing_dialog.dismiss();
							refreshEnabled();
						}
					});
				}
			}).start();
			break;
		case INSTALL_PHP7_CODE:
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_downloading).replace("%s","php7.tar.gz"));
			processing_dialog.setIndeterminate(false);
			processing_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					downloadFile("https://raw.githubusercontent.com/FENGberd/MinecraftPEServer/master/_DOWNLOAD/php7.tar.gz",new File(ServerUtils.getAppDirectory()+"/php7.tar.gz"),processing_dialog);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							processing_dialog.dismiss();
							final ProgressDialog processing_dialog=new ProgressDialog(instance);
							processing_dialog.setCancelable(false);
							processing_dialog.setMessage(getString(R.string.message_installing));
							processing_dialog.show();
							new Thread(new Runnable()
							{
								public void run()
								{
									try
									{
										installBusybox();
										Runtime.getRuntime().exec("./busybox tar zxf php7.tar.gz",new String[0],new File(ServerUtils.getAppDirectory())).waitFor();
										new File(ServerUtils.getAppDirectory()+"/php7.tar.gz").delete();
										toast(R.string.message_install_success);
									}
									catch(Exception e)
									{
										toast(getString(R.string.message_install_fail)+"\n"+e.toString());
									}
									runOnUiThread(new Runnable()
									{
										public void run()
										{
											processing_dialog.dismiss();
											refreshEnabled();
										}
									});
								}
							}).start();
						}
					});
				}
			}).start();
			break;
		case INSTALL_JAVA_CODE:
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_installing));
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						File libData=new File(Environment.getExternalStorageDirectory().toString()+"/nukkit_library.tar.gz");
						if(!libData.exists())
						{
							toast(getString(R.string.message_install_fail_path)+" "+Environment.getExternalStorageDirectory().toString());
						}
						else
						{
							File inside=new File(ServerUtils.getAppDirectory()+"/java/nukkit_library.tar.gz");
							inside.delete();
							new File(ServerUtils.getAppDirectory()+"/java").mkdirs();
							OutputStream os=new FileOutputStream(inside);
							InputStream is=new FileInputStream(libData);
							int cou=0;
							byte[] buffer=new byte[8192];
							while((cou=is.read(buffer))!=-1)
							{
								os.write(buffer,0,cou);
							}
							is.close();
							os.close();
							installBusybox();
							Runtime.getRuntime().exec("../busybox tar zxf nukkit_library.tar.gz",new String[0],new File(ServerUtils.getAppDirectory()+"/java")).waitFor();
							inside.delete();
							toast(R.string.message_install_success);
						}
					}
					catch(Exception e)
					{
						toast(getString(R.string.message_install_fail)+"\n"+e.toString());
					}
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							processing_dialog.dismiss();
							refreshEnabled();
						}
					});
				}
			}).start();
			break;
		case DOWNLOAD_SERVER_CODE:
			AlertDialog.Builder download_dialog_builder=new AlertDialog.Builder(this);
			String[] jenkins=nukkitMode?jenkins_nukkit:jenkins_pocketmine,values=new String[jenkins.length];
			for(int i=0;i<jenkins.length;i++)
			{
				String[] split=jenkins[i].split("\\|",2);
				values[i]=split[0];
			}
			download_dialog_builder.setTitle(getString(R.string.message_select_repository).replace("%s",nukkitMode?"Nukkit":"PocketMine"));
			download_dialog_builder.setItems(values,new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1,final int p2)
				{
					p1.dismiss();
					processing_dialog.setCancelable(false);
					processing_dialog.setMessage(getString(R.string.message_downloading).replace("%s",nukkitMode?"Nukkit.jar":"PocketMine-MP.phar"));
					processing_dialog.setIndeterminate(false);
					processing_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					processing_dialog.show();
					new Thread(new Runnable()
					{
						public void run()
						{
							String[] wtf=nukkitMode?jenkins_nukkit:jenkins_pocketmine;
							wtf=wtf[p2].split("\\|");
							downloadServer(wtf[1],new File(ServerUtils.getDataDirectory()+"/"+(nukkitMode?"Nukkit.jar":"PocketMine-MP.phar")),processing_dialog);
							runOnUiThread(new Runnable()
							{
								public void run()
								{
									processing_dialog.dismiss();
								}
							});
						}
					}).start();
				}
			});
			download_dialog_builder.show();
			break;
		}
		return true;
	}
}

