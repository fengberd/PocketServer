package net.fengberd.minecraftpe_server;

import java.io.*;

import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.view.View.*;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockActivity;

public class HomeActivity extends SherlockActivity
{
	final static int FORCE_CLOSE_CODE = 143;
	final static int CONSOLE_CODE = FORCE_CLOSE_CODE + 1;

	public static Intent serverIntent=null;
	public static HomeActivity homeActivity = null;
	public static RadioButton radio_pocketmine=null,radio_nukkit=null;
	public static CheckBox check_kusud=null,check_ansi=null;
	public static Button button_start=null,button_stop=null,button_install_php=null,button_install_jre=null,button_mount=null;
	
	public static boolean isStarted = false,nukkitMode=false,ansiMode=false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		homeActivity=this;
		ServerUtils.setContext(homeActivity);
		
		button_stop=(Button) findViewById(R.id.button_stop);
		button_start=(Button) findViewById(R.id.button_start);
		button_mount=(Button) findViewById(R.id.button_mount);
		button_install_php=(Button)findViewById(R.id.button_install_php);
		button_install_jre=(Button)findViewById(R.id.button_install_jre);

		check_ansi=(CheckBox)findViewById(R.id.check_ansi);
		check_kusud=(CheckBox)findViewById(R.id.check_kusud);

		radio_nukkit=(RadioButton) findViewById(R.id.radio_nukkit);
		radio_pocketmine=(RadioButton) findViewById(R.id.radio_pocketmine);
		
		check_ansi.setChecked(ansiMode);
		
		radio_nukkit.setChecked(nukkitMode);
		radio_pocketmine.setChecked(!nukkitMode);
		
		check_ansi.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				ansiMode=check_ansi.isChecked();
			}
		});
		
		radio_nukkit.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				nukkitMode=true;
				refreshEnabled();
			}
		});
		radio_pocketmine.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				nukkitMode=false;
				refreshEnabled();
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
					toast("Done");
				}
				catch(Exception e)
				{
					toast(e.toString());
				}
			}
		});
		button_install_jre.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final ProgressDialog dialog=new ProgressDialog(homeActivity);
				dialog.setCancelable(false);
				dialog.setMessage(getString(R.string.message_installing));
				dialog.show();
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
								dialog.dismiss();
								refreshEnabled();
							}
						});
					}
				}).start();
			}
		});
		button_install_php.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final ProgressDialog dialog=new ProgressDialog(homeActivity);
				dialog.setCancelable(false);
				dialog.setMessage(getString(R.string.message_installing));
				dialog.show();
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
								dialog.dismiss();
								refreshEnabled();
							}
						});
					}
				}).start();
			}
		});
		button_start.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				isStarted=true;
				refreshEnabled();
				serverIntent=new Intent(homeActivity,ServerService.class);
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
					ServerUtils.executeCMD("stop");
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
		button_install_php.setEnabled(!isStarted);
		button_install_jre.setEnabled(!isStarted);
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
		InputStream is=homeActivity.getAssets().open(name);
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
		if(homeActivity != null && serverIntent != null)
		{
			homeActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					isStarted=false;
					refreshEnabled();
					homeActivity.stopService(serverIntent);
				}
			});
		}
	}
	
	public void toast(int text)
	{
		toast(getString(text));
	}
	
	public void toast(final String text)
	{
		if(homeActivity!=null)
		{
			homeActivity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					Toast.makeText(homeActivity,text,Toast.LENGTH_SHORT).show();
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
		menu.add(0,FORCE_CLOSE_CODE,0,getString(R.string.menu_kill));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home || item.getItemId() == 0)
		{
			return false;
		}
		if(item.getItemId() == FORCE_CLOSE_CODE)
		{
			ServerUtils.stopServer();
			if(serverIntent != null)
			{
				stopService(serverIntent);
			}
			isStarted=false;
			refreshEnabled();
		}
		else if(item.getItemId() == CONSOLE_CODE)
		{
			startActivity(new Intent(homeActivity,LogActivity.class));
		}
		return true;
	}
}

