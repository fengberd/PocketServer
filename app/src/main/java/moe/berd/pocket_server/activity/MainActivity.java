package moe.berd.pocket_server.activity;

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

import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.fragment.*;
import moe.berd.pocket_server.service.*;
import moe.berd.pocket_server.utils.*;

public class MainActivity extends Activity implements Handler.Callback
{
	public static Handler actionHandler=null;
	public final static int ACTION_STOP_SERVICE=1;
	
	public final static int CHOOSE_PHP_CODE=1, CHOOSE_JAVA_CODE=2;
	
	public static Intent serverIntent=null;
	
	public static boolean nukkitMode=false, ansiMode=false;
	
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
	
	public static void postStopService()
	{
		postMessage(ACTION_STOP_SERVICE,0,null);
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
		ansiMode=ConfigProvider.getBoolean("ANSIMode",nukkitMode);
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
		
		switchFragment(fragment_main,R.string.activity_main);
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		if(data==null)
		{
			return;
		}
		final Uri choose=data.getData();
		final ProgressDialog processing_dialog=new ProgressDialog(this);
		processing_dialog.setCancelable(false);
		processing_dialog.setMessage(getString(R.string.message_installing));
		processing_dialog.show();
		switch(requestCode)
		{
		case CHOOSE_PHP_CODE:
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						File inside=new File(ServerUtils.getAppDirectory(),"php");
						inside.delete();
						ServerUtils.copyStream(getContentResolver().openInputStream(choose),new FileOutputStream(inside));
						runOnUiThread(new Runnable()
						{
							public void run()
							{
								processing_dialog.dismiss();
								fragment_main.refreshElements();
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
		case CHOOSE_JAVA_CODE:
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						File inside=new File(ServerUtils.getAppDirectory() + "/java/nukkit_library.tar.gz");
						inside.delete();
						inside.getParentFile().mkdirs();
						ServerUtils.copyStream(getContentResolver().openInputStream(choose),new FileOutputStream(inside));
						Runtime.getRuntime()
							.exec("../busybox tar zxf nukkit_library.tar.gz",new String[0],new File(ServerUtils
								.getAppDirectory() + "/java"))
							.waitFor();
						inside.delete();
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
						}
					});
				}
			}).start();
			break;
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
			fragment_main.refreshElements();
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
			switchFragment(fragment_main,R.string.activity_main);
			return;
		}
		super.onBackPressed();
	}
	
	@Override
	public boolean handleMessage(Message msg)
	{
		switch(msg.arg1)
		{
		case ACTION_STOP_SERVICE:
			stopService(serverIntent);
			fragment_main.refreshElements();
			break;
		default:
			return false;
		}
		return true;
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
	
	public void switchFragment(Fragment target,int title)
	{
		getFragmentManager().beginTransaction()
			.setCustomAnimations(R.animator.enter,R.animator.exit)
			.replace(R.id.layout_main,target)
			.commit();
		currentFragment=target;
		setTitle(title);
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
			URLConnection connection=ServerUtils.openNetConnection(url);
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
	
	public String getInternetString(String url)
	{
		try
		{
			BufferedReader reader=new BufferedReader(new InputStreamReader(ServerUtils.openNetConnection(url)
				.getInputStream()));
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
			if(artifacts.length()>1)
			{
				// F**k PMMP
				for(int i=0;i<artifacts.length();i++)
				{
					String check=artifacts.getJSONObject(i).getString("fileName").toLowerCase();
					if(check.contains("pocketmine"))
					{
						json=artifacts.getJSONObject(i);
						break;
					}
				}
				if(!json.has("relativePath"))
				{
					throw new Exception(getString(R.string.message_no_artifacts));
				}
			}
			else
			{
				json=artifacts.getJSONObject(0);
			}
			downloadFile(jenkins + "lastSuccessfulBuild/artifact/" + json.getString("relativePath"),saveTo,dialog);
		}
		catch(Exception e)
		{
			toast(e.toString());
		}
	}
	
	public void copyAsset(String name,File target) throws Exception
	{
		target.delete();
		ServerUtils.copyStream(getAssets().open(name),new FileOutputStream(target));
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
}
