package edu.csulb;

import cecs429.text.BasicTokenProcessor;

public class TempClass {
	public static void main(String[] args)
	{
		BasicTokenProcessor proc = new BasicTokenProcessor();
		
		for(String s: proc.enhancedProcessToken("192.168.1"))
		{
			System.out.println(s);
		}
		
	}
}
