import java.util.*;
import java.io.*;
/*
 * This code reads a compiled exe file and then locates the nop instructions
 * and replaces them with the key share values.
 Here, the .exe file is the result of assembling a TASM .exe file.
 * 
 */

public class Secure_Machine_Code {
	public static void main(String[] args) throws Exception
	{
		String filename = "C:/asm programs/add2_sec.exe"; 
		String newfilename = filename.substring(0,filename.length()-4)+"_sec.exe";
	
		
		int k = 2;
		int numberofkeys = k-1;
	
		FileInputStream fr = new FileInputStream(new File(filename));
		FileOutputStream fw = new FileOutputStream(new File(newfilename));
		ArrayList<Byte> list = new ArrayList<Byte>();
		ArrayList[] keys = new ArrayList[numberofkeys]; // keeps track of the different keys
		for(int i=0;i<numberofkeys;i++)
		{
			keys[i] =new  ArrayList<Byte>(); //initializes the values
		}
		
		byte[] b =  new byte[1];
		while(fr.available()>0) 
		{
			fr.read(b);
			list.add(b[0]);   // add bytes to the list
		}
	    byte[] arr = new byte[list.size()];
	    for(int i=0;i<list.size();i++)
	    {
	    	arr[i] = list.get(i);
	    }
	    int n = arr.length;
	    
	    
	    // This loop goes from the end of the file to the beginning, looking for
	    // The instruction opcodes for "B4 4C CD 21" which marks the signal to end the program
	    // We search for the first "EB 0k NOP NOP .. (k+1 times)" immediately before "B4 4C CD 21"
	    // Then we change that to "EB 0k 97 NOP NOP (k times)". Here 97 acts as a canary
	    // B4 = -76, 4C = 76, -51 = CD, 33 = 21, -21 = EB
	    
	    boolean end_canary_loop = false;
	    for(int i=arr.length-4;i>=0;i--)
	    {
	    	if(arr[i]== -76  && arr[i+1] == 76 && arr[i+2] == -51  && arr[i+3] == 33 )
	    	{
	    		for(int j=i; j> 0 && (!end_canary_loop); j--)
	    		{
	    			if(arr[j] == -21) 
	    			{
	    				arr[j+2] = -105; 
	    				end_canary_loop = true;
	    				break;
	    			}
	    		}
	    	}
	    	if (end_canary_loop == true)
	    		break;
	    }
	    
	    
	    // this loop goes from the start of the code section till the end
	    // it searches for bytes "EB 0k NOP NOP (k+1 times)"
	    // it inserts the key share values so that it looks like "EB 0k 27 x y z" where x, y, z are the key-shares
	    for(int i=0;i<n-(2+k);i++)
	    {
	    	
	    	
	    	if(arr[i]==-21 && (arr[i+1] == (byte)k)) 
	    	{
	    		
	    		boolean knops =  true;   
	    		for(int x= i+2; x<i+2+k ; x++)
		    	{
		    		if (arr[x] != -112)
		    			knops = false;
		    	}
	    		
	    		if (knops == false)
	    			continue;
	    		
	    		arr[i+2] = 39; 
	    		for(int j=0;j<numberofkeys;j++)
	    		{
	    			byte temp = randomByte();
	    			keys[j].add(temp);
	    			arr[i+3+j] = temp;
	    		}
	    	}
	    }
		
	    //printing out data just to confirm correctness
	    for(int i=0;i<numberofkeys;i++)
	    {
	    	System.out.printf("Keyshare %d : 0x%02X \n",i, xor(keys[i]));
	    }
	    
	    fw.write(arr);
	    fw.flush();
	    byte t = (byte)0xeb;
		System.out.println(randomByte() + " "+(t == 107 )+" "+t);
	}
	static byte xor(ArrayList<Byte> list)
	{
		byte b = list.get(0);
		for(int i=1;i<list.size();i++)
		{
			b = (byte)(b^list.get(i));
		}
		return b;
	}
	static byte randomByte()
	{
		return (byte)(Math.random()*128); 
	}

}
