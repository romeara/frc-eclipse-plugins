package edu.wpi.first.cppdev.ui.util;

import java.io.File;
import java.util.StringTokenizer;

/**
 * String utilities for the plug-in, including replacing delimited Strings,
 * and converting package directory notation to file system notation
 * 
 * @author Ryan O'Meara
 */
public class StringUtil {
	 
	/**
	 * Replaces a delimited string - will not pull strings from contiguous delimited strings
	 * 
	 * Example:  Calling replaceDelimitedString("aNonDelimitedString", "Non", "OK"); will result in:
	 * "aNonDemilimitedString"
	 * 
	 * Calling replaceDelimitedString("A Non Delimited String", "Non", "Well"); will result in:
	 * "A Well Delimited String"
	 * 
	 * The function uses the delimiter set " ():.,;\"\t\n\r\f"
	 * 
	 * @param inputString  The string containing the string to be replaced
	 * @param replaceStr  The string to replace in inputString
	 * @param replaceWith  The string to replace replaceStr with
	 * @return The string created when replaceWith is substituted for replaceStr in inputString
	 */
	public static String replaceDelimitedString(String inputString, String replaceStr, String replaceWith){
		StringTokenizer inputTokens = new StringTokenizer(inputString, " ():.,;\"\t\n\r\f", true);
		int currentIndex, startIndex;
		boolean resolved;
		String retString = inputString;
		String temp, holder;
		String front, end;
		
		resolved = false;
		currentIndex = startIndex = 0;
		
		//Loop through all tokens in the input string
		while(inputTokens.hasMoreTokens()){
			temp = inputTokens.nextToken();
			//if this token starts the string being replaced, start seeing if this is the string bein replaced
			if(replaceStr.indexOf(temp) == 0){ 
				//If this token is already the fully built replace string, replace it
				if(replaceStr.equalsIgnoreCase(temp)){
					front = retString.substring(0, startIndex);
					end = retString.substring(startIndex + replaceStr.length(), retString.length());
					retString = front + replaceWith + end;
					currentIndex += replaceWith.length();
					startIndex = currentIndex;
				}else{
					//built the temp string until it is determined whether it is the string being searched for,
					//or the tokenizer runs out of tokens
					while(!resolved){
						if(temp.length() <= replaceStr.length()){
							//if the string has been built, replace it in the string being returned
							if(replaceStr.equalsIgnoreCase(temp)){
								front = retString.substring(0, startIndex);
								end = retString.substring(startIndex + replaceStr.length(), retString.length());
								retString = front + replaceWith + end;
								resolved = true;
								currentIndex -= replaceStr.length();
								currentIndex += replaceWith.length();
								startIndex = currentIndex;
							}else{
								if(inputTokens.hasMoreTokens()){
									holder = inputTokens.nextToken();
									currentIndex += holder.length();
									temp += holder;
								}else{
									resolved = true;
								}
							}
						}else{
							resolved = true;
							startIndex = currentIndex;
						}
					}
					
					//reset the condition flag
					resolved = false;
				}
			}else{
				currentIndex += temp.length();
				startIndex = currentIndex;
			}
		}
		
		return retString;
	}
	
	/**
	 * Converts package-format string into its corresponding file directory string representation
	 * Ex:  "this.is.package.format" would be converted to "this/is/package/format" using the 
	 * system's File separator type
	 * @param packageString String to convert
	 * @return Converted string
	 */
	public static String convertPackageToDirectory(String packageString){
		packageString = replaceDelimitedString(packageString, ".", File.separator);
		return packageString;
	}
}
