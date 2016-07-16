package net.fengberd.minecraftpe_server;

import android.os.*;
import android.app.*;
import android.content.*;
import android.support.v4.app.*;

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
			NotificationCompat.Builder builder=new NotificationCompat.Builder(getApplicationContext());
			builder.setContentTitle((MainActivity.nukkitMode?"Nukkit":"PocketMine")+" "+MainActivity.instance.getString(R.string.message_running));
			builder.setContentText(MainActivity.instance.getString(R.string.message_tap_open));
			builder.setOngoing(true);
			builder.setSmallIcon(R.drawable.ic_launcher);
			builder.setContentIntent(PendingIntent.getActivity(this,0,new Intent(getApplicationContext(),MainActivity.class),0));
			startForeground(1337,builder.getNotification());
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

