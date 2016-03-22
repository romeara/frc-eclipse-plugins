package edu.wpi.first.javadev.builder.util;

/**
 * Utilities relating to String operations
 * 
 * @author Ryan O'Meara
 *
 */
public class StringUtil {
	
	/**
	 * Replaces a given string within another string (not a regex)
	 * @param original String to replace within
	 * @param replace String to replace
	 * @param replaceWith String to replace the given string with
	 * @return The new string, or original if any argument was null or string to 
	 * replace was not found within the original sting
	 */
	public static String replaceFirst(String original, String replace, String replaceWith){
		if((original == null)
				||(replace == null)
				||(replaceWith == null)){
			return original;
		}
		
		int indexOf = original.indexOf(replace);
		
		if(indexOf == -1){return original;}
		
		String before = original.substring(0, indexOf);
		String after = original.substring(indexOf + replace.length());
		
		return before + replaceWith + after;
	}
}
