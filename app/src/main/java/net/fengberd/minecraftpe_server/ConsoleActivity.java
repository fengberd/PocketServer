package net.fengberd.minecraftpe_server;

import android.support.v7.app.AppCompatActivity;

import android.app.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.view.View.*;

public class ConsoleActivity extends AppCompatActivity
{
	final static int CLEAR_CODE = 143;
	final static int COPY_CODE = CLEAR_CODE + 1;
	
	public static ConsoleActivity instance=null;
	public static ScrollView scroll_log;
	public static SpannableStringBuilder currentLog = new SpannableStringBuilder();
	public static Button button_command=null;
	public static TextView label_log=null;
	public static EditText edit_command=null;
	public static float font_size=16.0f;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		
		instance=this;
		
		label_log=(TextView)findViewById(R.id.label_log);
		edit_command=(EditText)findViewById(R.id.edit_command);
		scroll_log=(ScrollView)findViewById(R.id.logScrollView);
		button_command=(Button)findViewById(R.id.button_send);
		
		label_log.setText(currentLog);
		label_log.setTextSize(font_size);
		
		edit_command.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View p1,int keyCode,KeyEvent p3)
			{
				if(keyCode==KeyEvent.KEYCODE_ENTER)
				{
					button_command.callOnClick();
					return true;
				}
				return false;
			}
		});
		
		button_command.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0)
			{
				log("> "+edit_command.getText());
				ServerUtils.writeCommand(edit_command.getText().toString());
				edit_command.setText("");
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == CLEAR_CODE)
		{
			currentLog=new SpannableStringBuilder();
			label_log.setText("");
			return true;
		}
		else if(item.getItemId() == COPY_CODE)
		{
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(currentLog);
			Toast.makeText(this,R.string.message_copied,Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0,COPY_CODE,0,getString(R.string.menu_copy))
			.setIcon(R.drawable.content_copy)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0,CLEAR_CODE,0,getString(R.string.menu_clear))
			.setIcon(R.drawable.content_discard)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	public static void log(String line)
	{
		if(MainActivity.ansiMode)
		{
			line="<font>" + line.replace("&","&amp;")
				.replace("<","&lt;")
				.replace(">","&gt;")
				.replace(" ","&nbsp;")
				.replace("\u001b[m","</font>")
				.replace("\u001b[0m","</font>")
				.replace("\u001b[1m","</font><font style=\"font-weight:bold\">")
				.replace("\u001b[3m","</font><font style=\"font-style:italic\">")
				.replace("\u001b[4m","</font><font style=\"text-decoration:underline\">")
				.replace("\u001b[8m","</font><font>")
				.replace("\u001b[9m","</font><font style=\"text-decoration:line-through\">");
			if(MainActivity.nukkitMode)
			{
				line=line.replace("\u001b[0;30m","</font><font color=\"#000000\">")
					.replace("\u001b[0;34m","</font><font color=\"#0000AA\">")
					.replace("\u001b[0;32m","</font><font color=\"#00AA00\">")
					.replace("\u001b[0;36m","</font><font color=\"#00AAAA\">")
					.replace("\u001b[0;31m","</font><font color=\"#AA0000\">")
					.replace("\u001b[0;35m","</font><font color=\"#AA00AA\">")
					.replace("\u001b[0;33m","</font><font color=\"#FFAA00\">")
					.replace("\u001b[0;37m","</font><font color=\"#AAAAAA\">")
					.replace("\u001b[30;1m","</font><font color=\"#555555\">")
					.replace("\u001b[34;1m","</font><font color=\"#5555FF\">")
					.replace("\u001b[32;1m","</font><font color=\"#55FF55\">")
					.replace("\u001b[36;1m","</font><font color=\"#55FFFF\">")
					.replace("\u001b[31;1m","</font><font color=\"#FF5555\">")
					.replace("\u001b[35;1m","</font><font color=\"#FF55FF\">")
					.replace("\u001b[33;1m","</font><font color=\"#FFFF55\">")
					.replace("\u001b[37;1m","</font><font color=\"#FFFFFF\">");
			}
			else
			{
				line=line.replace("\u001b[38;5;16m","</font><font color=\"#000000\">")
					.replace("\u001b[38;5;19m","</font><font color=\"#0000AA\">")
					.replace("\u001b[38;5;34m","</font><font color=\"#00AA00\">")
					.replace("\u001b[38;5;37m","</font><font color=\"#00AAAA\">")
					.replace("\u001b[38;5;124m","</font><font color=\"#AA0000\">")
					.replace("\u001b[38;5;127m","</font><font color=\"#AA00AA\">")
					.replace("\u001b[38;5;214m","</font><font color=\"#FFAA00\">")
					.replace("\u001b[38;5;145m","</font><font color=\"#AAAAAA\">")
					.replace("\u001b[38;5;59m","</font><font color=\"#555555\">")
					.replace("\u001b[38;5;63m","</font><font color=\"#5555FF\">")
					.replace("\u001b[38;5;83m","</font><font color=\"#55FF55\">")
					.replace("\u001b[38;5;87m","</font><font color=\"#55FFFF\">")
					.replace("\u001b[38;5;203m","</font><font color=\"#FF5555\">")
					.replace("\u001b[38;5;207m","</font><font color=\"#FF55FF\">")
					.replace("\u001b[38;5;227m","</font><font color=\"#FFFF55\">")
					.replace("\u001b[38;5;231m","</font><font color=\"#FFFFFF\">");
			}
			line=line+ "</font><br />";
		}
		final CharSequence result=MainActivity.ansiMode?Html.fromHtml(line):(line+"\n");
		currentLog.append(result);
		if(instance != null)
		{
			instance.runOnUiThread(new Runnable() 
			{
				public void run()
				{
					label_log.append(result);
					scroll_log.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}
}

