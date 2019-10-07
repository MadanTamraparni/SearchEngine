package edu.csulb;

import cecs429.text.BasicTokenProcessor;

public class TempClass {
	// This class is mean to test function before making adjustment to the main
	public static void main(String[] args)
	{
		BasicTokenProcessor proc = new BasicTokenProcessor();
		

//		for(String s: proc.enhancedProcessToken("Hewlett-Packard-Computing"))
//=======
		for(String s: proc.enhancedProcessToken("$%%marine $#@#Near coastal$#@$"))
//>>>>>>> 67febae0b1cac8a4b5b3924b42517f4d7384f8ca
		{
			System.out.println(s);
		}
		
	}
}
