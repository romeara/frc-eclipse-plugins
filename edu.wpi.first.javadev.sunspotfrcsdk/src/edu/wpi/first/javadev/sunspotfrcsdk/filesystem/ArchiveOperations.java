package edu.wpi.first.javadev.sunspotfrcsdk.filesystem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.wpi.first.codedev.output.FRCConsole;

/**
 * ArchiveOperations provides methods to extract a single file or an entire 
 * archive to a designated location
 * 
 * @author Ryan O'Meara
 */
public class ArchiveOperations {
	
	/**
	 * Extracts a ".zip" archive located at source to the location given by 
	 * location.  The destination does not have to exist beforehand.
	 * @param source {@link File} representing the location of the zip archive to be 
	 * extracted
	 * @param destination {@link File} representing the location to extract the archive
	 * to in the file system
	 * @return true if the operation completed successfully, false otherwise
	 */
	public static boolean extractArchive(File source, File destination){
		//Source file must be a zip archive
		if(!(source.getAbsolutePath().endsWith(".zip"))){return false;}
		
		//Set the source to be readable to allow necessary operations
		source.setReadable(true);
		
		//Make sure destination exists and is writable
		if(!destination.exists()){destination.mkdir();}
		
		destination.setWritable(true);
		
		
		//Enumeration to store all entries in the archive
		Enumeration<? extends ZipEntry> entries;
		
		//Holds entry currently being processed
		ZipEntry processing;
		
		//Get a complete list of archive entries
		try{
			ZipFile archive = new ZipFile(source);
			entries = archive.entries();
		
		
			//Loop through all entries.  On first pass, only create directory 
			//structure as the order of the entries cannot be predicted accurately
			File directory;
			
			FRCConsole.writeToConsole("Creating directory structure");
			while(entries.hasMoreElements()){
				processing = (ZipEntry)entries.nextElement();
				
				if(processing.isDirectory()){
					directory = new File(destination.getAbsolutePath() 
							+ File.separator + processing.getName());
					
					if(directory.mkdirs()){
						directory.setWritable(true);
					}else{return false;}
				}
			}
			
			//Reset entries list, processing entry, free last file
			entries = archive.entries();
			processing = null;
			directory = null;
			
			
			//Populate directory structure
			while(entries.hasMoreElements()){
				processing = (ZipEntry)entries.nextElement();
				
				FRCConsole.writeToConsole("Extracting " + processing.getName());
				
				if(!processing.isDirectory()){
					copyStreams(archive.getInputStream(processing), 
							new BufferedOutputStream(new FileOutputStream(
									destination.getAbsolutePath() + File.separator + processing.getName())));
				}
			}
			
			
		}catch(Exception e){return false;}
			
		
		return true;
	}
	
	/**
	 * Extracts a given file from the given archive to the given location.  Will 
	 * only extract the first file found with the given name
	 * @param archive {@link File} representing the archive to extract the desired
	 * file from
	 * @param destination {@link File} pointing to directory to extract the 
	 * file to, doesn't have to exist prior
	 * @param fileName {@link String} of file name to extract, relative to 
	 * the directory immediately containing it
	 * @return {@link File} representing extracted file, or null if failed
	 */
	public static File extractFile(File sourceArchive, File destination, String fileName){
		//Check if archive is valid
		if(!sourceArchive.getAbsolutePath().endsWith(".zip")){return null;}
		
		sourceArchive.setReadable(true);
		
		if(!destination.exists()){
			destination.mkdirs();
			destination.setWritable(true);
		}
		
		try{
			//Create necessary handles to various archive pieces
			ZipFile archive = new ZipFile(sourceArchive);
			Enumeration<? extends ZipEntry> entries = archive.entries();
			ZipEntry processing;
			
			while(entries.hasMoreElements()){
				processing = (ZipEntry)entries.nextElement();
				
				if(!((processing.isDirectory())||(processing.getName().indexOf(fileName) == -1))){
					copyStreams(archive.getInputStream(processing), 
							new BufferedOutputStream(new FileOutputStream(destination.getAbsolutePath() 
									+ File.separator + fileName)));
					return new File(destination.getAbsolutePath() + File.separator + fileName);
				}
			}
		}catch(Exception e){return null;}
		
		return null;
	}
	
	/**
	 * Copies a file using input and output streams
	 * @param source {@link InputStream} to copy data from
	 * @param destination  {@link OutputStream} to copy data to
	 * @throws Exception On error reading or writing from the given streams
	 */
	private static void copyStreams(InputStream source, OutputStream destination) throws Exception{
		byte[] buffer = new byte[1024];
		int len;
		
		while((len = source.read(buffer)) >= 0){
			destination.write(buffer,0,len);
		}
		
		destination.close();
		source.close();
	}
}
