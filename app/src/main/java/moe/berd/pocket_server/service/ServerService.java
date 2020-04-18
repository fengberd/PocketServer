package moe.berd.pocket_server.service;

import android.app.*;
import android.content.*;
import android.os.*;

import android.support.v4.app.*;

import net.fengberd.minecraftpe_server.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.utils.*;

public class ServerService extends Service
{
	@Override
	public int onStartCommand(Intent intent,int flags,int startId)
	{
		String channelId = "";
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			channelId = "running";
			NotificationChannel channel=new NotificationChannel(channelId,"Server Running",NotificationManager.IMPORTANCE_LOW);
			channel.setDescription("Make sure the service won't die");
			NotificationManager notificationManager=getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
		startForeground(1,new NotificationCompat.Builder(getApplicationContext(), channelId).setOngoing(true)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentText(getString(R.string.message_tap_open))
			.setContentTitle((MainActivity.nukkitMode ? "Nukkit " : "PocketMine ") + getString(R.string.message_running))
			.setContentIntent(PendingIntent.getActivity(this,0,new Intent(getApplicationContext(),MainActivity.class),0))
			.build());
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy()
	{
		ServerUtils.killServer();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
}
