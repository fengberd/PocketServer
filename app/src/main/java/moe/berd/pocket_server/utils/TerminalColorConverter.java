package moe.berd.pocket_server.utils;

import java.util.*;
import java.util.regex.*;

@SuppressWarnings({"WeakerAccess"})
public class TerminalColorConverter
{
	public static Pattern pattern_SGR=Pattern.compile("\u001b\\[((\\d;?)*)m");
	public static String[] ColorTable=new String[]{
		"#000000","#800000","#008000","#808000","#000080","#800080","#008080","#c0c0c0"
	}, BrightColorTable=new String[]{
		"#808080","#ff0000","#00ff00","#ffff00","#0000ff","#ff00ff","#00ffff","#ffffff"
	}, ColorTable256=new String[]{
		// Standard colors
		"#000000","#800000","#008000","#808000","#000080","#800080","#008080","#c0c0c0",
		// High-intensity colors
		"#808080","#ff0000","#00ff00","#ffff00","#0000ff","#ff00ff","#00ffff","#ffffff",
		// 216 colors
		"#000000","#00005f","#000087","#0000af","#0000d7","#0000ff","#005f00","#005f5f","#005f87",
		"#005faf","#005fd7","#005fff","#008700","#00875f","#008787","#0087af","#0087d7","#0087ff",
		"#00af00","#00af5f","#00af87","#00afaf","#00afd7","#00afff","#00d700","#00d75f","#00d787",
		"#00d7af","#00d7d7","#00d7ff","#00ff00","#00ff5f","#00ff87","#00ffaf","#00ffd7","#00ffff",
		"#5f0000","#5f005f","#5f0087","#5f00af","#5f00d7","#5f00ff","#5f5f00","#5f5f5f","#5f5f87",
		"#5f5faf","#5f5fd7","#5f5fff","#5f8700","#5f875f","#5f8787","#5f87af","#5f87d7","#5f87ff",
		"#5faf00","#5faf5f","#5faf87","#5fafaf","#5fafd7","#5fafff","#5fd700","#5fd75f","#5fd787",
		"#5fd7af","#5fd7d7","#5fd7ff","#5fff00","#5fff5f","#5fff87","#5fffaf","#5fffd7","#5fffff",
		"#870000","#87005f","#870087","#8700af","#8700d7","#8700ff","#875f00","#875f5f","#875f87",
		"#875faf","#875fd7","#875fff","#878700","#87875f","#878787","#8787af","#8787d7","#8787ff",
		"#87af00","#87af5f","#87af87","#87afaf","#87afd7","#87afff","#87d700","#87d75f","#87d787",
		"#87d7af","#87d7d7","#87d7ff","#87ff00","#87ff5f","#87ff87","#87ffaf","#87ffd7","#87ffff",
		"#af0000","#af005f","#af0087","#af00af","#af00d7","#af00ff","#af5f00","#af5f5f","#af5f87",
		"#af5faf","#af5fd7","#af5fff","#af8700","#af875f","#af8787","#af87af","#af87d7","#af87ff",
		"#afaf00","#afaf5f","#afaf87","#afafaf","#afafd7","#afafff","#afd700","#afd75f","#afd787",
		"#afd7af","#afd7d7","#afd7ff","#afff00","#afff5f","#afff87","#afffaf","#afffd7","#afffff",
		"#d70000","#d7005f","#d70087","#d700af","#d700d7","#d700ff","#d75f00","#d75f5f","#d75f87",
		"#d75faf","#d75fd7","#d75fff","#d78700","#d7875f","#d78787","#d787af","#d787d7","#d787ff",
		"#d7af00","#d7af5f","#d7af87","#d7afaf","#d7afd7","#d7afff","#d7d700","#d7d75f","#d7d787",
		"#d7d7af","#d7d7d7","#d7d7ff","#d7ff00","#d7ff5f","#d7ff87","#d7ffaf","#d7ffd7","#d7ffff",
		"#ff0000","#ff005f","#ff0087","#ff00af","#ff00d7","#ff00ff","#ff5f00","#ff5f5f","#ff5f87",
		"#ff5faf","#ff5fd7","#ff5fff","#ff8700","#ff875f","#ff8787","#ff87af","#ff87d7","#ff87ff",
		"#ffaf00","#ffaf5f","#ffaf87","#ffafaf","#ffafd7","#ffafff","#ffd700","#ffd75f","#ffd787",
		"#ffd7af","#ffd7d7","#ffd7ff","#ffff00","#ffff5f","#ffff87","#ffffaf","#ffffd7","#ffffff",
		// Grayscale colors
		"#080808","#121212","#1c1c1c","#262626","#303030","#3a3a3a","#444444","#4e4e4e","#585858",
		"#626262","#6c6c6c","#767676","#808080","#8a8a8a","#949494","#9e9e9e","#a8a8a8","#b2b2b2",
		"#bcbcbc","#c6c6c6","#d0d0d0","#dadada","#e4e4e4","#eeeeee"
	};
	
	@SuppressWarnings({"UnusedAssignment","unused"})
	public static String control2html(String input)
	{
		String foreground="", background="";
		Matcher match=pattern_SGR.matcher(input);
		StringBuffer sb=new StringBuffer(input.length());
		// TODO: dim and reverse
		boolean need_close=false, bright=false, dim=false, italic=false, underscore=false, reverse=false;
		while(match.find())
		{
			String[] codes=match.group(1).split(";");
			for(int i=0;i<codes.length;i++)
			{
				int icode=codes[i].isEmpty() ? 0 : Integer.parseInt(codes[i]);
				switch(icode)
				{
				case 0:
					foreground="";
					background="";
					bright=dim=italic=underscore=reverse=false;
					break;
				case 1:
					bright=true;
					break;
				case 2:
					dim=true;
					break;
				case 3:
					italic=true;
					break;
				case 4:
					underscore=true;
					break;
				case 7:
					reverse=true;
					break;
				case 38: // RGB foreground
					if(i<codes.length - 1)
					{
						if(Integer.parseInt(codes[++i])==5)
						{
							foreground=ColorTable256[Integer.parseInt(codes[++i])];
						}
					}
					break;
				case 48: // RGB background
					if(i<codes.length - 1)
					{
						if(Integer.parseInt(codes[++i])==5)
						{
							background=ColorTable256[Integer.parseInt(codes[++i])];
						}
					}
					break;
				default:
					if(30<=icode && icode<=37)
					{
						foreground=(bright ? BrightColorTable : ColorTable)[icode - 30];
					}
					else if(40<=icode && icode<=47)
					{
						background=(bright ? BrightColorTable : ColorTable)[icode - 40];
					}
					break;
				}
			}
			match.appendReplacement(sb,String.format("%s<font color='%s' style" +
				"='background: %s%s%s;'>",(need_close ? "</font>" : ""),foreground,background,(italic ? ";font-style: italic" : ""),(underscore ? ";text-decoration: underline" : "")));
			need_close=true;
		}
		match.appendTail(sb);
		if(need_close)
		{
			sb.append("</font>");
		}
		return sb.toString();
	}
	
	public static int parseInt(final String s)
	{
		final int length=s.length();
		int num=0, i=0;
		while(i<length)
		{
			num=num * 10 + '0' - s.charAt(i++);
		}
		return -num;
	}
	
	public boolean modfied=false;
	
	public StringBuilder sequence(final String escape)
	{
		StringTokenizer st=new StringTokenizer(escape,";");
		int tokens=st.countTokens();
		if(tokens==0)
		{
			return new StringBuilder(0);
		}
		String foreground=null;
		StringBuilder result=new StringBuilder();
		if(modfied)
		{
			result.append("</font>");
		}
		result.append("<font style='");
		boolean bright=false;
		for(int i=0;i<tokens;i++)
		{
			int icode=parseInt(st.nextToken());
			if(30<=icode && icode<=37)
			{
				foreground=(bright ? BrightColorTable : ColorTable)[icode - 30];
				continue;
			}
			if(40<=icode && icode<=47)
			{
				result.append("background:")
					.append((bright ? BrightColorTable : ColorTable)[icode - 40])
					.append(';');
				continue;
			}
			switch(icode)
			{
			case 0:
				result=new StringBuilder("</font>");
				bright=false;
				break;
			case 1:
				bright=true;
				break;
			case 2:
				// dim
				break;
			case 3:
				result.append("font-style:italic;");
				break;
			case 4:
				result.append("text-decoration: underline;");
				break;
			case 7:
				result.append("direction: rtl; unicode-bidi: bidi-override;");
				break;
			case 38: // RGB foreground
				if(i<tokens - 1)
				{
					i++;
					if(parseInt(st.nextToken())==5)
					{
						i++;
						foreground=ColorTable256[parseInt(st.nextToken())];
					}
				}
				break;
			case 48: // RGB background
				if(i<tokens - 1)
				{
					i++;
					if(parseInt(st.nextToken())==5)
					{
						i++;
						result.append("background:")
							.append(ColorTable256[parseInt(st.nextToken())])
							.append(';');
					}
				}
				break;
			}
		}
		if(foreground!=null)
		{
			result.append("' color='").append(foreground);
		}
		modfied=true;
		return result.append("'>");
	}
	
	public StringBuilder parseLine(CharSequence input)
	{
		modfied=false;
		int index=0, length=input.length();
		StringBuilder sb=new StringBuilder(input);
		while(index<length)
		{
			char c=sb.charAt(index);
			if(c=='\u001b')
			{
				index+=2;
				StringBuilder escape=new StringBuilder(20);
				while(index<length && (c=sb.charAt(index++))!='m' && escape.length()<=20)
				{
					escape.append(c);
				}
				int offset=index - 3 - escape.length();
				escape=sequence(escape.toString());
				sb.delete(offset,index).insert(offset,escape);
				index=offset + escape.length();
				length=sb.length();
			}
			else
			{
				index++;
			}
		}
		return sb;
	}
	
	public String parseMultiLines(String input)
	{
		StringBuilder sb=new StringBuilder();
		StringTokenizer st=new StringTokenizer(input,"\n");
		while(st.hasMoreTokens())
		{
			sb.append(parseLine(st.nextToken()));
		}
		return sb.toString();
	}
}
