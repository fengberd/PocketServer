package net.fengberd.minecraftpe_server;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockActivity;

import android.os.*;
import android.text.*;
import android.widget.*;
import android.view.View;
import android.view.View.OnClickListener;

public class LogActivity extends SherlockActivity
{
	final static int CLEAR_CODE = 143;
	final static int COPY_CODE = CLEAR_CODE + 1;
	
	public static LogActivity logActivity=null;
	public static ScrollView sv;
	public static SpannableStringBuilder currentLog = new SpannableStringBuilder();
	public static Button button_command=null;
	public static TextView label_log=null;
	public static EditText edit_command=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		logActivity=this;
		label_log=(TextView) findViewById(R.id.label_log);
		label_log.setText(currentLog);
		edit_command=(EditText)findViewById(R.id.edit_command);
		sv=(ScrollView) findViewById(R.id.logScrollView);
		button_command = (Button)findViewById(R.id.button_send);
		button_command.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0)
			{
				log(">"	+ edit_command.getText());
				ServerUtils.executeCMD(edit_command.getText().toString());
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

	public static void log(final String line)
	{
		final CharSequence result=HomeActivity.ansiMode?Html.fromHtml("<font>" + line.replace("&","&amp;")
			.replace("<","&lt;")
			.replace(">","&gt;")
			.replace(" ","&nbsp;")
			.replace("\u001b[1m","</font><font style=\"font-weight:bold\">")
			.replace("\u001b[3m","</font><font style=\"font-style:italic\">")
			.replace("\u001b[4m","</font><font style=\"text-decoration:underline\">")
			.replace("\u001b[9m","</font><font style=\"text-decoration:line-through\">")
			.replace("\u001b[m","")
			.replace("\u001b[38;5;16m","</font><font color=\"#000000\">")
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
			.replace("\u001b[38;5;231m","</font><font color=\"#FFFFFF\">") + "</font><br />"):(line+"\n");
		currentLog.append(result);
		if(logActivity != null)
		{
			logActivity.runOnUiThread(new Runnable() 
			{
				public void run()
				{
					label_log.append(result);
					sv.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
		}
	}
}

