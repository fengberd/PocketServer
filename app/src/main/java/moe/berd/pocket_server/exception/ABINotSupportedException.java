package moe.berd.pocket_server.exception;

import java.util.*;

public class ABINotSupportedException extends RuntimeException
{
	public String binaryName="";
	public ArrayList<String> supportedABIS=null;
	
	public ABINotSupportedException(String binaryName,ArrayList<String> supportedABIS)
	{
		this.binaryName=binaryName;
		this.supportedABIS=supportedABIS;
	}
}
