package edu.csulb;

import cecs429.text.BasicTokenProcessor;

public class TempClass {
	public static void main(String[] args)
	{
		BasicTokenProcessor proc = new BasicTokenProcessor();
		
		for(String s: proc.enhancedProcessToken("Hewlett-Packard-Computing"))
		{
			System.out.println(s);
		}
		
	}
}
