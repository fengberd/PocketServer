package net.fengberd.minecraftpe_server;

import android.os.*;
import android.app.*;
import android.content.*;

public class ServerService extends Service
{
	private boolean isRunning = false;

	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		run();
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		stop();
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}

	private void run()
	{
		if(!isRunning)
		{
			isRunning=true;
			Context context = getApplicationContext();
			Notification note = new Notification(R.drawable.ic_launcher,(HomeActivity.nukkitMode?"Nukkit":"PocketMine")+" is running",System.currentTimeMillis());
			Intent i = new Intent(context,HomeActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
			note.setLatestEventInfo(this,(HomeActivity.nukkitMode?"Nukkit":"PocketMine")+" "+HomeActivity.homeActivity.getString(R.string.message_running),HomeActivity.homeActivity.getString(R.string.message_tap_open),pi);
			note.flags|=Notification.FLAG_NO_CLEAR;
			startForeground(1337,note);
		}
	}

	private void stop()
	{
		if(isRunning)
		{
			isRunning=false;
			stopForeground(true);
		}
	}
}

