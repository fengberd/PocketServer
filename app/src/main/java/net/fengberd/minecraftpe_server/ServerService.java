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
			Context context = getApplicationContext();
		
			Intent i = new Intent(context,MainActivity.class);
			PendingIntent pi = PendingIntent.getActivity(this,0,i,0);
			Notification note;
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setContentIntent(pi)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle((MainActivity.nukkitMode?"Nukkit":"PocketMine")+" "+MainActivity.instance.getString(R.string.message_running))
				.setContentText(MainActivity.instance.getString(R.string.message_tap_open))
				.setOngoing(true);
			note = builder.build();
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

