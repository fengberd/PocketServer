package moe.berd.pocket_server.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

import org.json.*;

import java.io.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.utils.*;

import static moe.berd.pocket_server.activity.MainActivity.*;

public class MainFragment extends Fragment implements View.OnClickListener
{
	public MainActivity main=null;
	
	public String[] jenkins_nukkit, jenkins_pocketmine;
	
	public TextView label_path_tip=null;
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
	public void onStart()
	{
		label_path_tip=(TextView)main.findViewById(R.id.label_path_tip);
		
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
		
		reloadUrls();
		refreshElements();
		
		super.onStart();
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
		menu.findItem(R.id.menu_download_server).setEnabled(!running);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final ProgressDialog processing_dialog=new ProgressDialog(main);
		switch(item.getItemId())
		{
		case R.id.menu_console:
			main.switchFragment(main.fragment_console,R.string.activity_console);
			break;
		case R.id.menu_settings:
			main.switchFragment(main.fragment_settings,R.string.activity_settings);
			break;
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
									if(processing_dialog.isShowing())
									{
										processing_dialog.dismiss();
									}
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
		refreshElements();
	}
	
	public void reloadUrls()
	{
		try
		{
			File file=new File(ServerUtils.getAppFilesDirectory(),"urls.json");
			if(!file.exists())
			{
				main.copyAsset("urls.json",file);
			}
			FileInputStream fis=new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
			fis.read(data);
			fis.close();
			if(data.length<2)
			{
				file.delete();
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
			main.toast(e.toString());
		}
	}
	
	public void refreshElements()
	{
		boolean running=ServerUtils.isRunning();
		button_stop.setEnabled(running);
		button_mount.setEnabled(!running);
		radio_nukkit.setEnabled(!running);
		radio_pocketmine.setEnabled(!running);
		label_path_tip.setText("");
		
		if(nukkitMode)
		{
			button_mount.setVisibility(View.VISIBLE);
			if(!new File(ServerUtils.getDataDirectory(),"Nukkit.jar").exists())
			{
				label_path_tip.setText(R.string.label_nukkit_to);
			}
			if(!ServerUtils.installedJava())
			{
				running=true;
			}
		}
		else
		{
			button_mount.setVisibility(View.GONE);
			if(!new File(ServerUtils.getDataDirectory(),"PocketMine-MP.phar").exists() && !new File(ServerUtils
				.getDataDirectory(),"src").exists())
			{
				label_path_tip.setText(R.string.label_pocketMine_to);
			}
			if(!ServerUtils.installedPHP())
			{
				running=true;
			}
		}
		button_start.setEnabled(!running);
	}
}
