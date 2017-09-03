package moe.berd.pocket_server.fragment;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

import moe.berd.pocket_server.activity.*;
import moe.berd.pocket_server.utils.*;

import static moe.berd.pocket_server.activity.MainActivity.*;

public class ConsoleFragment extends Fragment implements Handler.Callback
{
	public MainActivity main=null;
	
	private static final int MESSAGE_APPEND=1, MESSAGE_UPDATE_LINE=2;
	
	public static Handler logUpdateHandler=null;
	public static CharSequence currentLine="";
	public static SpannableStringBuilder currentLog=new SpannableStringBuilder();
	
	public Button button_command=null;
	public TextView label_log=null, label_current=null;
	public EditText edit_command=null;
	public ScrollView scroll_log=null;
	
	public ConsoleFragment()
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
		return inflater.inflate(R.layout.fragment_console,container,false);
	}
	
	@Override
	public void onStart()
	{
		logUpdateHandler=new Handler(this);
		
		label_log=(TextView)main.findViewById(R.id.label_log);
		label_current=(TextView)main.findViewById(R.id.label_current);
		edit_command=(EditText)main.findViewById(R.id.edit_command);
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
		scroll_log=(ScrollView)main.findViewById(R.id.logScrollView);
		button_command=(Button)main.findViewById(R.id.button_send);
		button_command.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				sendCommand();
			}
		});
		
		label_log.setTextSize(ConfigProvider.getInt("ConsoleFontSize",16));
		label_current.setTextSize(ConfigProvider.getInt("ConsoleFontSize",16));
		
		label_log.setText(currentLog);
		label_current.setText(currentLine);
		
		super.onStart();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		scrollToBottom();
	}
	
	@Override
	public void onStop()
	{
		logUpdateHandler=null;
		super.onStop();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu,inflater);
		inflater.inflate(R.menu.console,menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_clear:
			currentLog=new SpannableStringBuilder();
			currentLine="";
			label_log.setText("");
			label_current.setText("");
			break;
		case R.id.menu_copy:
			((android.content.ClipboardManager)main.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData
				.newPlainText("PocketServer_ConsoleLog",currentLog));
			main.toast(R.string.message_copied);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	
	@Override
	public boolean handleMessage(Message msg)
	{
		switch(msg.arg1)
		{
		case MESSAGE_APPEND:
			label_log.append((CharSequence)msg.obj);
			label_log.post(new Runnable()
			{
				@Override
				public void run()
				{
					scrollToBottom();
				}
			});
			break;
		case MESSAGE_UPDATE_LINE:
			label_current.setText(currentLine);
			break;
		}
		return true;
	}
	
	public void scrollToBottom()
	{
		scroll_log.smoothScrollBy(0,scroll_log.getChildAt(scroll_log.getChildCount() - 1).getBottom());
	}
	
	private void sendCommand()
	{
		logLine("> " + edit_command.getText());
		ServerUtils.writeCommand(edit_command.getText().toString());
		edit_command.setText("");
	}
	
	public static boolean postAppend(CharSequence data)
	{
		if(logUpdateHandler!=null)
		{
			Message msg=new Message();
			msg.arg1=MESSAGE_APPEND;
			msg.obj=data;
			logUpdateHandler.sendMessage(msg);
			return true;
		}
		return false;
	}
	
	public static boolean postNewLine()
	{
		if(logUpdateHandler!=null)
		{
			Message msg=new Message();
			msg.arg1=MESSAGE_UPDATE_LINE;
			logUpdateHandler.sendMessage(msg);
			return true;
		}
		return false;
	}
	
	public static void logLine(String line)
	{
		if(!currentLine.equals(""))
		{
			currentLog.append(ansiMode ? Html.fromHtml("<br />") : "\n").append(currentLine);
			postAppend(ansiMode ? Html.fromHtml("<br />") : "\n");
			postAppend(currentLine);
		}
		if(ansiMode)
		{
			int index=0;
			while((index=line.indexOf("\u001b[1G"))!=-1)
			{
				line=line.substring(index + 4);
			}
			line=TerminalColorConverter.control2html(line.replace("&","&amp;")
				.replace("<","&lt;")
				.replace(">","&gt;")
				.replace(" ","&nbsp;")
				.replace("\u001b[1G","")
				.replace("\u001b[K",""));
		}
		currentLine=ansiMode ? Html.fromHtml(line) : line;
		postNewLine();
	}
}
