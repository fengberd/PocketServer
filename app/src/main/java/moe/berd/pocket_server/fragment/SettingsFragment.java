package moe.berd.pocket_server.fragment;

import android.app.*;
import android.content.*;
import android.preference.*;

import net.fengberd.minecraftpe_server.*;

import java.io.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.exception.*;
import moe.berd.pocket_server.utils.*;

public class SettingsFragment extends PreferenceFragment
{
	public MainActivity main=null;
	
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
	public void onStart()
	{
		getPreferenceManager().setSharedPreferencesName("config");
		addPreferencesFromResource(R.xml.config);
		findPreference("ANSIMode").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference,Object newValue)
			{
				MainActivity.ansiMode=(boolean)newValue;
				return true;
			}
		});
		findPreference("button_update_repos").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				final ProgressDialog processing_dialog=new ProgressDialog(main);
				processing_dialog.setCancelable(false);
				processing_dialog.setMessage(getString(R.string.message_downloading).replace("%s",""));
				processing_dialog.setIndeterminate(false);
				processing_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				processing_dialog.show();
				new Thread(new Runnable()
				{
					public void run()
					{
						main.downloadFile("https://raw.githubusercontent.com/fengberd/MinecraftPEServer/master/app/src/main/assets/urls.json",new File(ServerUtils
							.getAppFilesDirectory(),"urls.json"),processing_dialog);
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
				return false;
			}
		});
		findPreference("button_install_php").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				final ProgressDialog processing_dialog=new ProgressDialog(main);
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
							}
						});
					}
				}).start();
				return false;
			}
		});
		findPreference("button_install_php_manually").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				new AlertDialog.Builder(main).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.alert_install_php_title)
					.setMessage(R.string.alert_install_php_message)
					.setPositiveButton(R.string.button_ok,new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog,int which)
						{
							main.chooseFile(MainActivity.CHOOSE_PHP_CODE,getString(R.string.message_choose_php));
						}
					})
					.setNegativeButton(R.string.button_cancel,null)
					.show();
				return false;
			}
		});
		findPreference("button_install_java").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
		{
			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				main.chooseFile(MainActivity.CHOOSE_JAVA_CODE,getString(R.string.message_choose_java));
				return false;
			}
		});
		super.onStart();
	}
}
