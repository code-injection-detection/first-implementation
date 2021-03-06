import java.util.*;
import java.io.*;
/*
 * This program takes as input (has hardcoded name of file in it). The file
 * is a .asm / assembly file hence, it is in text format.
 * It inserts nop instructions (which will be replaced with keyshares) between
 * instructions. 
 * It does this by some not-very-clean parsing, (but we could refine it later)
 * (The point is that parsing an assembly file is still way easier than parsing
 * a C file.)
 * To handle the jumps over the instructions, we add labels. 
 
 Note: This works with 16-bit TASM code
 */
public class Secure_Assembly {
	
	public static void main(String[] args) throws Exception
	{
		String filename = "C:/asm programs/add2.asm";
		Scanner sc = new Scanner(new File(filename));
		ArrayList<String> list = new ArrayList<String>();
		sc.useDelimiter("\n");
		String ulabel = "unique";
		int n= 2;
		int counter = 0;
		int i = 0;
		int  k = 2;
		
		// This puts the file into the ArrayList and looks for the start of the code
		// which is ".code"
		
		while (sc.hasNext())
		{
			String line = sc.next();
			line = removeNewlines(line);
			list.add(line);
			if (removeSpaces(line).indexOf(".code")!=-1)
			{
				
				break;
			}			
			
		}
		
		// Adding these NOPs help identify the beginning of code for the Secure_Machine_code program
		list.add("NOP");
		list.add("NOP");
		
		// This inserts the jumps and NOPs in the code.
		// It breaks at the end of the code ("end")
		while(sc.hasNext())
		{
			String line = sc.next();
			line = removeNewlines(line);
			if (removeSpaces(line) == "")
			{
				
				continue;
			}
			if (removeSpaces(line).startsWith("end"))
			{
				
				list.add("jmp end_of_program_label");
				
				//for reasons of efficiency, we force the end of file canary
				// to be followed by as many nops as the number of keyshares
				for(int m=0;m<k;m++)	 
				{   	                 
					list.add("nop");
				}
				
				
				list.add("end_of_program_label: mov ah, 4ch");
				list.add("int 21h");
				list.add(line);
				break;
			}
			
			// this adds the jmps and the nops between n lines of assembly code
		
			if (i == n)
			{
				list.add(" jmp " + ulabel + counter);
				for (int j = 0; j < k; j++)
					list.add("NOP");
				list.add(ulabel + counter + ": " + line);
				
				i = 0;
				counter++;
				continue;
			}
			
			i++;
			list.add(line);
			
		}
		
		
		for (String s: list)
		{
			System.out.println(s);
		}
		
		// This write the modified lines into a new ASM
		// You can use TASM to assemble this ASM into machine code
		String finalfile = "";
		String newfilename = filename.substring(0,filename.length()-4) + "_sec.asm";
		System.out.println(newfilename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(newfilename));
		for (String line: list)
		{
			bw.write(line);
			bw.newLine();
		}
		
		bw.write(finalfile);
		bw.flush();

		
	}
	//this removes the spaces in a string i.e. line of assembly code (for parsing purposes)
	static String removeSpaces(String abc)
	{
		String line = "";
		for(int i=0;i<abc.length();i++)
		{
			if(abc.charAt(i) == ' ' || abc.charAt(i)=='\t' || abc.charAt(i) == '\n' || abc.charAt(i) == '\r')
			{
				
			}
			else
			{
				line = line+(abc.charAt(i)+"").toLowerCase();
			}
		}
		
		return line;
	}
	//this removes the new line characters at the end of a string (line of assembly code)
	static String removeNewlines(String s)
	{
		String line = "";
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) != '\r' && s.charAt(i) != '\n')
				line += s.charAt(i);
		}
		return line;
	}

}
