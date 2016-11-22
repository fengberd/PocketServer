package net.fengberd.minecraftpe_server;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.security.cert.*;

import javax.net.ssl.*;

public class MainActivity extends Activity implements Handler.Callback, View.OnClickListener
{
	public static Handler actionHandler=null;
	public final static int ACTION_STOP_SERVICE=1;

	public final static int CHOOSE_PHP_CODE=1;

	public static Intent serverIntent=null;
	public static SharedPreferences config=null;

	public static boolean isStarted=false, nukkitMode=false, ansiMode=false;

	public static final String[] jenkins_nukkit=new String[]{
			"Angelic47|http://ci.angelic47.com:30001/job/Nukkit/",
			"MengCraft|http://ci.mengcraft.com:8080/job/nukkit/",
			"RegularBox|http://ci.regularbox.com/job/Nukkit/",
			"ZXDA|https://jenkins.zxda.net/job/Nukkit/"
	}, jenkins_pocketmine=new String[]{
			"Genisys (iTX Tech)|https://ci.itxtech.org/job/Genisys/",
			"Genisys (ZXDA,Not suggested)|https://jenkins.zxda.net/job/Genisys/",
			"ClearSky-PHP7 (ZXDA)|https://jenkins.zxda.net/job/ClearSky-PHP7/",
			"ClearSky-PHP5 (ZXDA)|https://jenkins.zxda.net/job/ClearSky-PHP5/",
			"PocketMine-MP (ZXDA)|https://jenkins.zxda.net/job/PocketMine-MP/",
			"PocketMine-MP (pmmp)|https://jenkins.pmmp.gq/job/PocketMine-MP/"
	};

	public static void postMessage(int arg1,int arg2,Object obj)
	{
		if(actionHandler!=null)
		{
			Message msg=new Message();
			msg.arg1=arg1;
			msg.arg2=arg2;
			msg.obj=obj;
			actionHandler.sendMessage(msg);
		}
	}

	static
	{
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
	}

	public RadioButton radio_pocketmine=null, radio_nukkit=null;
	public CheckBox check_kusud=null, check_ansi=null;
	public Button button_start=null, button_stop=null;
	public SeekBar seekbar_fontsize=null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		actionHandler=new Handler(this);
		serverIntent=new Intent(this,ServerService.class);

		config=getSharedPreferences("config",0);
		ansiMode=config.getBoolean("ANSIMode",ansiMode);
		nukkitMode=config.getBoolean("NukkitMode",nukkitMode);

		button_stop=(Button)findViewById(R.id.button_stop);
		button_stop.setOnClickListener(this);
		button_start=(Button)findViewById(R.id.button_start);
		button_start.setOnClickListener(this);
		findViewById(R.id.button_mount).setOnClickListener(this);

		check_ansi=(CheckBox)findViewById(R.id.check_ansi);
		check_ansi.setOnClickListener(this);
		check_kusud=(CheckBox)findViewById(R.id.check_kusud);
		check_kusud.setOnClickListener(this);

		radio_nukkit=(RadioButton)findViewById(R.id.radio_nukkit);
		radio_nukkit.setOnClickListener(this);
		radio_pocketmine=(RadioButton)findViewById(R.id.radio_pocketmine);
		radio_pocketmine.setOnClickListener(this);

		seekbar_fontsize=(SeekBar)findViewById(R.id.seekbar_fontsize);
		seekbar_fontsize.setProgress(config.getInt("ConsoleFontSize",16));
		seekbar_fontsize.setMax(30);
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

		check_ansi.setChecked(ansiMode);
		check_kusud.setChecked(config.getBoolean("KusudMode",false));

		radio_nukkit.setChecked(nukkitMode);
		radio_pocketmine.setChecked(!nukkitMode);

		ServerUtils.setAppDirectory(this);
		ConsoleActivity.font_size=seekbar_fontsize.getProgress();

		refreshEnabled();
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		switch(requestCode)
		{
		case CHOOSE_PHP_CODE:
			if(data==null)
			{
				return;
			}
			final Uri choosed=data.getData();
			final ProgressDialog processing_dialog=new ProgressDialog(this);
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_installing));
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						File inside=new File(ServerUtils.getAppDirectory(),"php");
						inside.delete();
						OutputStream os=new FileOutputStream(inside);
						InputStream is=getContentResolver().openInputStream(choosed);
						assert is!=null;
						int cou=0;
						byte[] buffer=new byte[8192];
						while((cou=is.read(buffer))!=-1)
						{
							os.write(buffer,0,cou);
						}
						is.close();
						os.close();
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								processing_dialog.dismiss();
								refreshEnabled();
								toast(R.string.message_install_success);
							}
						});
					}
					catch(Exception e)
					{
						final String ex=e.getMessage();
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								processing_dialog.dismiss();
								toast(getString(R.string.message_install_fail) + ex);
							}
						});
					}
				}
			}).start();
			break;
		default:
			super.onActivityResult(requestCode,resultCode,data);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main,menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.menu_install_php).setEnabled(!isStarted);
		menu.findItem(R.id.menu_install_php_manually).setEnabled(!isStarted);
		menu.findItem(R.id.menu_install_java).setEnabled(!isStarted);
		menu.findItem(R.id.menu_download_server).setEnabled(!isStarted);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final ProgressDialog processing_dialog=new ProgressDialog(this);
		switch(item.getItemId())
		{
		case R.id.menu_kill:
			ServerUtils.killServer();
			postMessage(ACTION_STOP_SERVICE,0,null);
			refreshEnabled();
			break;
		case R.id.menu_console:
			startActivity(new Intent(this,ConsoleActivity.class));
			break;
		case R.id.menu_install_php:
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
						copyAsset("php",new File(ServerUtils.getAppDirectory(),"/php"));
						toast(R.string.message_install_success);
					}
					catch(Exception e)
					{
						toast(getString(R.string.message_install_fail) + "\n" + e.toString());
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
		case R.id.menu_install_php_manually:
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.alert_install_php_title)
					.setMessage(R.string.alert_install_php_message)
					.setPositiveButton(R.string.button_ok,new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int which)
						{
							chooseFile(CHOOSE_PHP_CODE,getString(R.string.message_choose_php));
						}
					})
					.setNegativeButton(R.string.button_cancel,null)
					.show();
			break;
		case R.id.menu_install_java:
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_installing));
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						File libData=new File(Environment.getExternalStorageDirectory()
								.toString() + "/nukkit_library.tar.gz");
						if(!libData.exists())
						{
							toast(getString(R.string.message_install_fail_path) + " " + Environment.getExternalStorageDirectory()
									.toString());
						}
						else
						{
							File inside=new File(ServerUtils.getAppDirectory() + "/java/nukkit_library.tar.gz");
							inside.delete();
							new File(ServerUtils.getAppDirectory() + "/java").mkdirs();
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
							Runtime.getRuntime()
									.exec("../busybox tar zxf nukkit_library.tar.gz",new String[0],new File(ServerUtils
											.getAppDirectory() + "/java"))
									.waitFor();
							inside.delete();
							toast(R.string.message_install_success);
						}
					}
					catch(Exception e)
					{
						toast(getString(R.string.message_install_fail) + "\n" + e.toString());
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
		case R.id.menu_download_server:
			AlertDialog.Builder download_dialog_builder=new AlertDialog.Builder(this);
			String[] jenkins=nukkitMode ? jenkins_nukkit : jenkins_pocketmine, values=new String[jenkins.length];
			for(int i=0;i<jenkins.length;i++)
			{
				String[] split=jenkins[i].split("\\|",2);
				values[i]=split[0];
			}
			download_dialog_builder.setTitle(getString(R.string.message_select_repository).replace("%s",nukkitMode ? "Nukkit" : "PocketMine"));
			download_dialog_builder.setItems(values,new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface p1,final int p2)
				{
					p1.dismiss();
					processing_dialog.setCancelable(false);
					processing_dialog.setMessage(getString(R.string.message_downloading).replace("%s",nukkitMode ? "Nukkit.jar" : "PocketMine-MP.phar"));
					processing_dialog.setIndeterminate(false);
					processing_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					processing_dialog.show();
					new Thread(new Runnable()
					{
						public void run()
						{
							String[] wtf=nukkitMode ? jenkins_nukkit : jenkins_pocketmine;
							wtf=wtf[p2].split("\\|");
							downloadServer(wtf[1],new File(ServerUtils.getDataDirectory() + "/" + (nukkitMode ? "Nukkit.jar" : "PocketMine-MP.phar")),processing_dialog);
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
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		switch(msg.arg1)
		{
		case ACTION_STOP_SERVICE:
			isStarted=false;
			refreshEnabled();
			stopService(serverIntent);
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.button_start:
			isStarted=true;
			startService(serverIntent);
			ServerUtils.runServer();
			break;
		case R.id.button_stop:
			if(ServerUtils.isRunning())
			{
				ServerUtils.writeCommand("stop");
			}
			break;
		case R.id.button_mount:
			try
			{
				String binary="su", busybox=ServerUtils.getAppDirectory() + "/busybox ";
				if(((CheckBox)findViewById(R.id.check_kusud)).isChecked())
				{
					binary="ku.sud";
				}
				Runtime.getRuntime().exec(binary + " -c " + busybox + "mount -o rw,remount /").waitFor();
				if(!new File("/lib").exists())
				{
					Runtime.getRuntime().exec(binary + " -c " + busybox + "mkdir /lib").waitFor();
				}
				else
				{
					Runtime.getRuntime().exec(binary + " -c " + busybox + "umount /lib").waitFor();
				}
				Runtime.getRuntime()
						.exec(binary + " -c " + busybox + "mount -o bind " + ServerUtils.getAppDirectory() + "/java/lib /lib")
						.waitFor();
				toast(R.string.message_done);
			}
			catch(Exception e)
			{
				toast(e.toString());
			}
			break;
		case R.id.radio_pocketmine:
			nukkitMode=false;
			config.edit().putBoolean("NukkitMode",nukkitMode).apply();
			break;
		case R.id.radio_nukkit:
			nukkitMode=true;
			config.edit().putBoolean("NukkitMode",nukkitMode).apply();
			break;
		case R.id.check_ansi:
			ansiMode=check_ansi.isChecked();
			config.edit().putBoolean("ANSIMode",ansiMode).apply();
			break;
		case R.id.check_kusud:
			config.edit().putBoolean("KusudMode",check_kusud.isChecked()).apply();
			break;
		default:
			return;
		}
		refreshEnabled();
	}

	public void installBusybox() throws Exception
	{
		File busybox=new File(ServerUtils.getAppDirectory() + "/busybox");
		if(busybox.exists())
		{
			return;
		}
		copyAsset("busybox",busybox);
		busybox.setExecutable(true,true);
	}

	public void refreshEnabled()
	{
		radio_nukkit.setEnabled(!isStarted);
		radio_pocketmine.setEnabled(!isStarted);
		check_ansi.setEnabled(!isStarted);
		findViewById(R.id.button_mount).setEnabled(!isStarted);
		if(nukkitMode && !new File(ServerUtils.getAppDirectory(),"java/jre/bin/java").exists())
		{
			button_start.setEnabled(false);
		}
		else if(!nukkitMode && !new File(ServerUtils.getAppDirectory(),"php").exists())
		{
			button_start.setEnabled(false);
		}
		else
		{
			button_start.setEnabled(!isStarted);
		}
		button_stop.setEnabled(isStarted);
	}

	public void chooseFile(int code,String title)
	{
		Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent sIntent=new Intent("com.sec.android.app.myfiles.PICK_DATA");
		sIntent.addCategory(Intent.CATEGORY_DEFAULT);
		Intent chooserIntent;
		if(getPackageManager().resolveActivity(sIntent,0)!=null)
		{
			chooserIntent=Intent.createChooser(sIntent,title);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,new Intent[]{intent});
		}
		else
		{
			chooserIntent=Intent.createChooser(intent,title);
		}
		try
		{
			startActivityForResult(chooserIntent,code);
		}
		catch(ActivityNotFoundException e)
		{
			toast("No suitable file chooser.");
		}
	}

	public String getInternetString(String url)
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
			toast(e.toString());
		}
		return null;
	}

	public void toast(int text)
	{
		toast(getString(text));
	}

	public void toast(final String text)
	{
		final MainActivity instance=this;
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				Toast.makeText(instance,text,Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void downloadServer(String jenkins,File saveTo,final ProgressDialog dialog)
	{
		try
		{
			JSONObject json=new JSONObject(getInternetString(jenkins + "lastSuccessfulBuild/api/json"));
			JSONArray artifacts=json.getJSONArray("artifacts");
			if(artifacts.length()<=0)
			{
				throw new Exception(getString(R.string.message_no_artifacts));
			}
			json=artifacts.getJSONObject(0);
			downloadFile(jenkins + "lastSuccessfulBuild/artifact/" + json.getString("relativePath"),saveTo,dialog);
		}
		catch(Exception e)
		{
			toast(e.getMessage());
		}
	}

	public void copyAsset(String name,File target) throws Exception
	{
		target.delete();
		OutputStream os=new FileOutputStream(target);
		InputStream is=getAssets().open(name);
		int cou=0;
		byte[] buffer=new byte[8192];
		while((cou=is.read(buffer))!=-1)
		{
			os.write(buffer,0,cou);
		}
		is.close();
		os.close();
	}

	public URLConnection openNetConnection(String url) throws Exception
	{
		final SSLContext sc=SSLContext.getInstance("SSL");
		sc.init(null,new TrustManager[]{
				new X509TrustManager()
				{
					@Override
					@SuppressLint("TrustAllX509TrustManager")
					public void checkClientTrusted(X509Certificate[] p1,String p2) throws CertificateException
					{

					}

					@Override
					@SuppressLint("TrustAllX509TrustManager")
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

	public void downloadFile(String url,File saveTo,final ProgressDialog dialog)
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
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						dialog.setMax((int)max / 1024);
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
					final int temp=(int)(read / 1000);
					runOnUiThread(new Runnable()
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
			toast(R.string.message_done);
		}
		catch(Exception e)
		{
			toast(e.getMessage());
		}
		finally
		{
			try
			{
				if(output!=null)
				{
					output.close();
				}
				if(input!=null)
				{
					input.close();
				}
			}
			catch(Exception ignored)
			{

			}
		}
	}
}
