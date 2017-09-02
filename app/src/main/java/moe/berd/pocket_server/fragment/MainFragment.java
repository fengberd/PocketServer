package moe.berd.pocket_server.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

import java.io.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.utils.*;

import static moe.berd.pocket_server.activity.MainActivity.*;

public class MainFragment extends Fragment implements View.OnClickListener
{
	public MainActivity main=null;
	
	public Button button_start=null, button_stop=null, button_mount=null;
	public RadioButton radio_pocketmine=null, radio_nukkit=null;
	
	public MainFragment()
	{
		
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		if(!(activity instanceof MainActivity))
		{
			throw new RuntimeException("Invalid activity attach event.");
		}
		main=(MainActivity)activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_main,container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		button_stop=(Button)main.findViewById(R.id.button_stop);
		button_stop.setOnClickListener(this);
		button_start=(Button)main.findViewById(R.id.button_start);
		button_start.setOnClickListener(this);
		button_mount=(Button)main.findViewById(R.id.button_mount);
		button_mount.setOnClickListener(this);
		
		radio_nukkit=(RadioButton)main.findViewById(R.id.radio_nukkit);
		radio_nukkit.setChecked(nukkitMode);
		radio_nukkit.setOnClickListener(this);
		radio_pocketmine=(RadioButton)main.findViewById(R.id.radio_pocketmine);
		radio_pocketmine.setChecked(!nukkitMode);
		radio_pocketmine.setOnClickListener(this);
		
		refreshEnabled();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu,inflater);
		inflater.inflate(R.menu.main,menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		boolean running=ServerUtils.isRunning();
		menu.findItem(R.id.menu_install_php).setEnabled(!running);
		menu.findItem(R.id.menu_install_php_manually).setEnabled(!running);
		menu.findItem(R.id.menu_install_java).setEnabled(!running);
		menu.findItem(R.id.menu_download_server).setEnabled(!running);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final ProgressDialog processing_dialog=new ProgressDialog(main);
		switch(item.getItemId())
		{
		case R.id.menu_console:
			main.switchFragment(main.fragment_console);
			break;
		case R.id.menu_settings:
			main.switchFragment(main.fragment_settings);
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
						ServerUtils.installPHP(main,"7");
						main.toast(R.string.message_install_success);
					}
					catch(ABINotSupportedException e)
					{
						main.alertABIWarning(e.binaryName,null);
					}
					catch(Exception e)
					{
						main.toast(getString(R.string.message_install_fail) + "\n" + e.toString());
					}
					main.runOnUiThread(new Runnable()
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
		/*case R.id.menu_install_php_manually:
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
						File libData=new File(Environment.getExternalStorageDirectory().toString() + "/nukkit_library.tar.gz");
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
							fragment_main.refreshEnabled();
						}
					});
				}
			}).start();
			break;*/
		case R.id.menu_download_server:
			AlertDialog.Builder download_dialog_builder=new AlertDialog.Builder(main);
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
							main.downloadServer(wtf[1],new File(ServerUtils.getDataDirectory() + "/" + (nukkitMode ? "Nukkit.jar" : "PocketMine-MP.phar")),processing_dialog);
							main.runOnUiThread(new Runnable()
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
			return main.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.button_start:
			main.startService(serverIntent);
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
				main.toast(R.string.message_done);
			}
			catch(Exception e)
			{
				main.toast(e.toString());
			}
			break;
		case R.id.radio_pocketmine:
			ConfigProvider.set("NukkitMode",nukkitMode=false);
			break;
		case R.id.radio_nukkit:
			ConfigProvider.set("NukkitMode",nukkitMode=true);
			break;
		default:
			return;
		}
		refreshEnabled();
	}
	
	public void refreshEnabled()
	{
		boolean running=ServerUtils.isRunning();
		button_stop.setEnabled(running);
		button_mount.setEnabled(!running);
		radio_nukkit.setEnabled(!running);
		radio_pocketmine.setEnabled(!running);
		
		// Fake running state to prevent user starting server without Java/PHP installed
		if(nukkitMode)
		{
			if(!ServerUtils.installedJava())
			{
				running=true;
			}
		}
		else if(!ServerUtils.installedPHP())
		{
			running=true;
		}
		button_start.setEnabled(!running);
	}
}
