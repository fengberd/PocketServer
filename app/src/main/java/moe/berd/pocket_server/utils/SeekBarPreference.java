package moe.berd.pocket_server.utils;

import android.content.*;
import android.content.res.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import net.fengberd.minecraftpe_server.*;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener
{
	private int progress;
	
	public SeekBarPreference(Context context,AttributeSet attrs)
	{
		super(context,attrs);
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a,int index)
	{
		return a.getInt(index,0);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		setLayoutResource(R.layout.preference_seekbar);
		return super.onCreateView(parent);
	}
	
	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);
		SeekBar seekbar=(SeekBar)view.findViewById(R.id.preference_seekbar_seekbar);
		if(seekbar!=null)
		{
			seekbar.setProgress(progress);
			seekbar.setOnSeekBarChangeListener(this);
			seekbar.setMax(32);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue,Object defaultValue)
	{
		setValue(restoreValue ? getPersistedInt(progress) : (Integer)defaultValue);
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser)
	{
		if(!fromUser)
		{
			return;
		}
		setValue(progress);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
		
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
		if(seekBar.getProgress()!=progress)
		{
			setValue(progress);
		}
	}
	
	public void setValue(int value)
	{
		if(shouldPersist())
		{
			persistInt(value);
		}
		if(value!=progress)
		{
			progress=value;
			notifyChanged();
		}
	}
}
