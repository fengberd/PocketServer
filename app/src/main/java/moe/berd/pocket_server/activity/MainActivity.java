package moe.berd.pocket_server.activity;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.security.cert.*;

import javax.net.ssl.*;

import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.fragment.*;
import moe.berd.pocket_server.service.*;
import moe.berd.pocket_server.utils.*;

public class MainActivity extends Activity implements Handler.Callback, View.OnClickListener
{
	public static Handler actionHandler=null;
	public final static int ACTION_STOP_SERVICE=1;
	
	public final static int CHOOSE_PHP_CODE=1;
	
	public static Intent serverIntent=null;
	
	public static boolean isStarted=false, nukkitMode=false, ansiMode=false;
	
	public static String[] jenkins_nukkit, jenkins_pocketmine;
	
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
	
	public Fragment currentFragment=null;
	public MainFragment fragment_main=new MainFragment();
	public ConsoleFragment fragment_console=new ConsoleFragment();
	public SettingsFragment fragment_settings=new SettingsFragment();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ConfigProvider.init(getSharedPreferences("config",0));
		nukkitMode=ConfigProvider.getBoolean("NukkitMode",nukkitMode);
		
		ServerUtils.init(this);
		try
		{
			ServerUtils.installBusybox(this);
		}
		catch(ABINotSupportedException e)
		{
			alertABIWarning(e.binaryName,null);
		}
		catch(Exception e)
		{
			toast(e.getMessage());
		}
		
		actionHandler=new Handler(this);
		serverIntent=new Intent(this,ServerService.class);
		
		switchFragment(fragment_main);
		
		reloadUrls();
		/*
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
				// todo: write config
				config.edit().putInt("ConsoleFontSize",p1.getProgress()).apply();
			}
		});
		
		check_ansi.setChecked(ansiMode);
		check_kusud.setChecked(config.getBoolean("KusudMode",false));
		
		radio_nukkit.setChecked(nukkitMode);
		radio_pocketmine.setChecked(!nukkitMode);
		
		ServerUtils.init(this);
		
		reloadUrls();
		refreshEnabled();
		*/
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
								fragment_main.refreshEnabled();
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
	/*
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
	*/
	
	@Override
	public boolean handleMessage(Message msg)
	{
		switch(msg.arg1)
		{
		case ACTION_STOP_SERVICE:
			stopService(serverIntent);
			fragment_main.refreshEnabled();
			break;
		}
		return false;
	}
	
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		/*
		case R.id.check_ansi:
			ansiMode=check_ansi.isChecked();
			config.edit().putBoolean("ANSIMode",ansiMode).apply();
			break;
		case R.id.check_kusud:
			config.edit().putBoolean("KusudMode",check_kusud.isChecked()).apply();
			break;
			*/
		default:
			return;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_kill:
			ServerUtils.killServer();
			stopService(serverIntent);
			fragment_main.refreshEnabled();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onBackPressed()
	{
		if(currentFragment!=null && (currentFragment instanceof ConsoleFragment || currentFragment instanceof SettingsFragment))
		{
			switchFragment(fragment_main);
			return;
		}
		super.onBackPressed();
	}
	
	public void switchFragment(Fragment target)
	{
		getFragmentManager().beginTransaction()
			.setCustomAnimations(R.animator.enter,R.animator.exit)
			.replace(R.id.layout_main,target)
			.commit();
		currentFragment=target;
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
	
	public void reloadUrls()
	{
		try
		{
			File jfile=new File(getFilesDir(),"urls.json");
			if(!jfile.exists())
			{
				copyAsset("urls.json",jfile);
			}
			FileInputStream fis=new FileInputStream(jfile);
			byte[] data=new byte[(int)jfile.length()];
			fis.read(data);
			fis.close();
			if(data.length<2)
			{
				jfile.delete();
				reloadUrls();
				return;
			}
			JSONObject json=new JSONObject(new String(data,"UTF-8"));
			JSONObject jenkins=json.getJSONObject("jenkins");
			{
				JSONArray nukkit=jenkins.getJSONArray("nukkit");
				{
					jenkins_nukkit=new String[nukkit.length()];
					for(int i=0;i<jenkins_nukkit.length;i++)
					{
						jenkins_nukkit[i]=nukkit.getString(i);
					}
				}
				JSONArray pocketmine=jenkins.getJSONArray("pocketmine");
				{
					jenkins_pocketmine=new String[pocketmine.length()];
					for(int i=0;i<jenkins_pocketmine.length;i++)
					{
						jenkins_pocketmine[i]=pocketmine.getString(i);
					}
				}
			}
		}
		catch(Exception e)
		{
			toast(getString(R.string.message_install_fail) + "\n" + e.toString());
		}
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
	
	public void alertABIWarning(final String name,final DialogInterface.OnClickListener onclick)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				new AlertDialog.Builder(MainActivity.this).setTitle(R.string.dialog_abi_title)
					.setCancelable(false)
					.setMessage(getString(R.string.dialog_abi_message).replace("%binary",name))
					.setNegativeButton(R.string.dialog_abi_exit,new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int which)
						{
							finish();
						}
					})
					.setPositiveButton(R.string.dialog_abi_ignore,onclick)
					.create()
					.show();
			}
		});
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
