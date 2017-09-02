package moe.berd.pocket_server.fragment;

import android.app.*;
import android.preference.PreferenceFragment;

import moe.berd.pocket_server.activity.*;

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
	/*
		case R.id.menu_update_repos:
			processing_dialog.setCancelable(false);
			processing_dialog.setMessage(getString(R.string.message_downloading).replace("%s",""));
			processing_dialog.setIndeterminate(false);
			processing_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			processing_dialog.show();
			new Thread(new Runnable()
			{
				public void run()
				{
					downloadFile("https://raw.githubusercontent.com/fengberd/MinecraftPEServer/master/app/src/main/assets/urls.json",new File(getFilesDir(),"urls.json"),processing_dialog);
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							reloadUrls();
							processing_dialog.dismiss();
						}
					});
				}
			}).start();
			break;
	
	
		check_ansi=(CheckBox)main.findViewById(R.id.check_ansi);
		check_ansi.setOnClickListener(this);
		check_kusud=(CheckBox)main.findViewById(R.id.check_kusud);
		check_kusud.setOnClickListener(this);
		
		seekbar_fontsize=(SeekBar)main.findViewById(R.id.seekbar_fontsize);
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
				config.edit().putInt("ConsoleFontSize",p1.getProgress()).apply();
			}
		});
		
		check_ansi.setChecked(ansiMode);
		check_kusud.setChecked(config.getBoolean("KusudMode",false));
		*/
}
