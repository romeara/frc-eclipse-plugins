package edu.wpi.first.javadev.sunspotfrcsdk.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import edu.wpi.first.codedev.output.FRCConsole;

/**
 * Provides methods to interact with the file system, including deleting
 * directories which contain files and comparing file locations
 * 
 * @author Ryan O'Meara
 */
public class FileOperations {
	/**
	 * Compares two file location to determine if they are they same.  Accounts
	 * for differences in separators used with different operating systems.
	 * {@code compareFieldLocation("usr\temp\dir", "usr/temp/dir")} evaluates to 
	 * true
	 * @param fileLoc1 {@link String} path to the first file location
	 * @param fileLoc2 {@link String} path to the second file location
	 * @return true if the two locations are the same, false otherwise
	 */
	public static boolean sameFileLocation(String fileLoc1, String fileLoc2){
		fileLoc1= fileLoc1.replace("/", ".");
		fileLoc1 = fileLoc1.replace("\\", ".");
		fileLoc2 = fileLoc2.replace("/", ".");
		fileLoc2 = fileLoc2.replace("\\", ".");
		
		return fileLoc1.equalsIgnoreCase(fileLoc2);
	}
	
	/**
	 * Deletes the given directory or file, including files within if the given 
	 * file is a directory
	 * @param targetFile {@link File} representing the directory or file to be 
	 * deleted
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean deleteFile(File targetFile){
		if(!targetFile.exists()){return true;}
	
		try{targetFile.setWritable(true);}catch(Exception e){}
		
		if (targetFile.isDirectory()) { 
			String[] children = targetFile.list(); 
			for (int i=0; i<children.length; i++) { 
				if(!deleteFile(new File(targetFile, children[i]))){
					return false;
				}
			} 
		} 
		
		return targetFile.delete();
	}
	
	/**
	 * Copies the given directory and sub directories
	 * @param src The file to copy from (must exist)
	 * @param dest The file to copy to
	 * @return true if successful, false otherwise
	 */
	public static boolean copyDirectory(File src, File dest){
		//make sure source exists
    	if(!src.exists()){
           FRCConsole.writeToConsole("Directory does not exist");
           return false;
        }else{
 
           try{
        	copyFolder(src,dest);
           }catch(Exception e){
        	e.printStackTrace();
        	 return false;
           }
        }
 
    	return true;
	}
	
	/**
	 * Recursively copies a folder and contained files
	 * @param src The File to copy
	 * @param dest The file to copy to
	 * @throws Exception If an error occurs with IO
	 */
	private static void copyFolder(File src, File dest)
	    	throws Exception{
	 
	    	if(src.isDirectory()){
	 
	    		//if directory not exists, create it
	    		if(!dest.exists()){
	    		   dest.mkdir();
	    		   FRCConsole.writeToConsole("Directory copied from " 
                           + src + "  to " + dest);
	    		}
	 
	    		//list all the directory contents
	    		String files[] = src.list();
	 
	    		for (String file : files) {
	    		   //construct the src and dest file structure
	    		   File srcFile = new File(src, file);
	    		   File destFile = new File(dest, file);
	    		   //recursive copy
	    		   copyFolder(srcFile,destFile);
	    		}
	 
	    	}else{
	    		//if file, then copy it
	    		//Use bytes stream to support all file types
	    		InputStream in = new FileInputStream(src);
	    	        OutputStream out = new FileOutputStream(dest); 
	 
	    	        byte[] buffer = new byte[1024];
	 
	    	        int length;
	    	        //copy the file content in bytes 
	    	        while ((length = in.read(buffer)) > 0){
	    	    	   out.write(buffer, 0, length);
	    	        }
	 
	    	        in.close();
	    	        out.close();
	    	        FRCConsole.writeToConsole("File copied from " + src + " to " + dest);
	    	}
	    }
}
