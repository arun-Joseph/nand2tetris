//Assembler, nand2tetris Project 6
/* by, Arun Joseph, NITC */

import java.io.*;
import java.util.*;

public class Assembler{
	public static void main(String args[]){
		int numfile=args.length;
		for(int t=0;t<numfile;t++){
			HashMap <String,Integer> table= new HashMap <String,Integer>();
			int lineno=0,var=16;
			char[] machlang=new char[16];

			try{
				File asmfile=new File(args[t]);
				Scanner td=new Scanner(asmfile);
				String aline;

				while(td.hasNextLine()){
					aline=td.nextLine();
					aline=aline.trim();
					if((aline.length()>0)&&(aline.charAt(0)=='(')){
						aline=aline.substring(1,aline.length()-1);
						table.put(aline,lineno);
					}
					else if((aline.length()>0)&&(aline.charAt(0)!='/')&&(aline.charAt(0)!='('))
						lineno++;
				}

				Scanner sc=new Scanner(asmfile);
				String hack=args[t].substring(0,args[t].length()-4) +  ".hack";
				File hackfile=new File(hack);
				FileOutputStream dos=new FileOutputStream(hackfile);
				BufferedWriter db = new BufferedWriter(new OutputStreamWriter(dos));

				while(sc.hasNextLine()){
					aline=sc.nextLine();
					if(aline.indexOf('/')!=-1)
						aline=aline.substring(0,aline.indexOf('/'));
					aline=aline.trim();

					if((aline.length()>0)&&(aline.charAt(0)=='@')){
						machlang[0]='0';
						aline=aline.substring(1);
						String temp=aline;
						aline=reskwd(aline);

						if(aline==""){
							if(Character.isLetter(temp.charAt(0))){
								if(table.containsKey(temp)){
									int k=table.get(temp);
									aline=Integer.toString(k);
								}
								else{
									table.put(temp,var);
									aline=Integer.toString(var);
									var++;
								}		
							}
							else
								aline=temp;
						}
						aline=toBinary(aline);
	System.out.println(aline);
						for(int i=0;i<15;i++)
							machlang[i+1]=aline.charAt(i);
						for(int i=0;i<16;i++)
							db.write(machlang[i]);
						db.newLine();
					}

					else if((aline.length()>0)&&(aline.charAt(0)!='/')&&(aline.charAt(0)!='(')){
						machlang[0]=machlang[1]=machlang[2]='1';
						if(aline.indexOf('=')>0)
							aline=assign(aline);
						else
							aline=jump(aline);
						for(int i=0;i<13;i++)
							machlang[i+3]=aline.charAt(i);
						for(int i=0;i<16;i++)
							db.write(machlang[i]);
						db.newLine();
					}
				}
			db.close();
			}
			catch(Exception e){ }
		}
	}
	
	public static String reskwd(String line){
		line=line.trim();
		switch(line.trim()){
			case "R0":
			case "SP": return "0";
			case "R1":
			case "LCL": return "1";
			case "R2":
			case "ARG": return "2";
			case "R3":
			case "THIS": return "3";
			case "R4":
			case "THAT": return "4";
			case "R5": return "5";
			case "R6": return "6";
			case "R7": return "7";
			case "R8": return "8";
			case "R9": return "9";
			case "R10": return "10";
			case "R11": return "11";
			case "R12": return "12";
			case "R13": return "13";
			case "R14": return "14";
			case "R15": return "15";
			case "SCREEN": return "16384";
			case "KBD": return "24376";
			default: return "";
		}
	}

	public static String toBinary(String line){
		line=line.trim();
		String output="";
		int dec=Integer.parseInt(line);
		double power=0;
		for(int i=14;i>=0;i--){
			power=Math.pow(2,i);
			if(dec>=power){
				output=output.concat("1");
				dec-=power;
			}
			else
				output=output.concat("0");
		}
		return output;
	}

	public static String assign(String line){
		line=line.trim();
		int p=line.indexOf('=');
		String output=findComp(line.substring(p+1)) + findDest(line.substring(0,p)) + "000";
		return output;
	}

	public static String jump(String line){
		line=line.trim();
		int p=line.indexOf(';');
		String output=findComp(line.substring(0,p)) + "000" + findJump(line.substring(p+1));
		return output;
	}

	public static String findComp(String line){
		line=line.trim();
		String output="";
		switch(line){
			case "0": output="101010";
				break;
			case "1": output="111111";
				break;
			case "-1": output="111010";
				break;
			case "D": output="001100";
				break;
			case "A":
			case "M": output="110000";
				break;
			case "!D": output="001101";
				break;
			case "!A":
			case "!M": output="110001";
				break;
			case "-D": output="001111";
				break;
			case "-A":
			case "-M": output="110011";
				break;
			case "D+1": output="011111";
				break;
			case "A+1":
			case "M+1": output="110111";
				break;
			case "D-1": output="001110";
				break;
			case "A-1":
			case "M-1": output="110010";
				break;
			case "D+A":
			case "D+M": output="000010";
				break;
			case "D-A":
			case "D-M": output="010011";
				break;
			case "A-D":
			case "M-D": output="000111";
				break;
			case "D&A":
			case "D&M": output="000000";
				break;
			case "D|A":
			case "D|M": output="010101";
				break;
		}
		if(line.indexOf('M')!=-1)
			output="1"+output;
		else
			output="0"+output;
		return output;
	}

	public static String findDest(String line){
		line=line.trim();
		String output="";
		if(line.indexOf('A')!=-1)
			output=output.concat("1");
		else
			output=output.concat("0");
		if(line.indexOf('D')!=-1)
			output=output.concat("1");
		else
			output=output.concat("0");
		if(line.indexOf('M')!=-1)
			output=output.concat("1");
		else
			output=output.concat("0");
		return output;
	}

	public static String findJump(String line){
		line=line.trim();
		switch(line){
			case "JGT": return "001";
			case "JEQ": return "010";
			case "JGE": return "011";
			case "JLT": return "100";
			case "JNE": return "101";
			case "JLE": return "110";
			case "JMP": return "111";
			default: return "000";
		}
	}
}