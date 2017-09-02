package moe.berd.pocket_server.exception;

public class ABINotSupportedException extends RuntimeException
{
	public String binaryName="";
	
	public ABINotSupportedException(String binaryName)
	{
		this.binaryName=binaryName;
	}
}
