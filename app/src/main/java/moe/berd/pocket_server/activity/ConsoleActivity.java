package moe.berd.pocket_server.activity;

import android.app.*;
import android.content.*;
import android.content.ClipboardManager;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

import moe.berd.pocket_server.utils.*;

public class ConsoleActivity extends Activity implements Handler.Callback
{
	public static Handler logUpdateHandler=null;

	public ScrollView scroll_log;
	public Button button_command=null;
	public TextView label_log=null;
	public EditText edit_command=null;

	public static float font_size=16.0f;
	public static SpannableStringBuilder currentLog=new SpannableStringBuilder();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);

		logUpdateHandler=new Handler(this);

		label_log=(TextView)findViewById(R.id.label_log);
		edit_command=(EditText)findViewById(R.id.edit_command);
		scroll_log=(ScrollView)findViewById(R.id.logScrollView);
		button_command=(Button)findViewById(R.id.button_send);

		label_log.setTextSize(font_size);

		edit_command.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View p1,int keyCode,KeyEvent p3)
			{
				if(keyCode==KeyEvent.KEYCODE_ENTER)
				{
					sendCommand();
					return true;
				}
				return false;
			}
		});

		button_command.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				sendCommand();
			}
		});
		postAppend(currentLog);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.console,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_clear:
			currentLog=new SpannableStringBuilder();
			label_log.setText("");
			break;
		case R.id.menu_copy:
			((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("test",currentLog));
			Toast.makeText(this,R.string.message_copied,Toast.LENGTH_SHORT).show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		label_log.append((CharSequence)msg.obj);
		scroll_log.fullScroll(ScrollView.FOCUS_DOWN);
		return true;
	}

	private void sendCommand()
	{
		log("> " + edit_command.getText());
		ServerUtils.writeCommand(edit_command.getText().toString());
		edit_command.setText("");
	}

	public static boolean postAppend(CharSequence data)
	{
		if(logUpdateHandler!=null)
		{
			Message msg=new Message();
			msg.obj=data;
			logUpdateHandler.sendMessage(msg);
			return true;
		}
		return false;
	}

	public static void log(String line)
	{
		if(MainActivity.ansiMode)
		{
			line=TerminalColorConverter.control2html(line.replace("&","&amp;")
				.replace("<","&lt;")
				.replace(">","&gt;")
				.replace(" ","&nbsp;")
				.replace("\u001b[1G","")
				.replace("\u001b[K","")) + "<br />";
		}
		CharSequence result=MainActivity.ansiMode ? Html.fromHtml(line) : (line + "\n");
		currentLog.append(result);
		postAppend(result);
	}
}
