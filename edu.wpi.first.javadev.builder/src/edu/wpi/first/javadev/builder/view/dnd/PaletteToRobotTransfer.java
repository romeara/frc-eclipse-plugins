package edu.wpi.first.javadev.builder.view.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/** 
 * Transfer class between palette and robot views.  Transfers the internal model id
 * to be used to lookup the corresponding element
 * @author Ryan O'Meara
 */
public class PaletteToRobotTransfer extends ByteArrayTransfer{
	private static PaletteToRobotTransfer instance = new PaletteToRobotTransfer();
	private static final String TYPE_NAME = "treeobject-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton gadget transfer instance.
	 */
	public static PaletteToRobotTransfer getInstance() {
		return instance;
	}
	
	/**
	 * Avoid explicit instantiation
	 */
	private PaletteToRobotTransfer() {}

	@Override
	protected int[] getTypeIds() {
		return new int[]{TYPEID};
	}

	@Override
	protected String[] getTypeNames() {
		return new String[]{TYPE_NAME};
	}
	
	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if(object instanceof String[]){
			super.javaToNative(toByteArray((String[])object), transferData);
		}
	}
	   
	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[])super.nativeToJava(transferData);
	    return fromByteArray(bytes);
	}
	
	/** Converts a string array to a byte array */
	protected byte[] toByteArray(String[] input){
		/*Writing protocol:
		 * Number strings
		 * strings
		 */
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		
		try{
			out.writeInt(input.length);
			
			for(String current : input){
				out.writeUTF(current);
			}
			
			byte[] bytes = byteOut.toByteArray();
			
			return bytes;
		
		}catch(Exception e){
			return null;
		}finally{
			try {
				out.close();
				byteOut.close();
			} catch (Exception e) {e.printStackTrace();}
			
		}
	}
	
	/** converts a byte array to a string array */
	protected String[] fromByteArray(byte[] input){
		if(input == null){return null;}
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(input));
		
		try{
			int num = in.readInt();
			
			String[] retArray = new String[num];
			
			for(int i = 0; i < num; i++){
				retArray[i] = in.readUTF();
			}
			
			return retArray;
			
		}catch(Exception e){
			return null;
		}finally{
			try {
				in.close();
			} catch (Exception e) {e.printStackTrace();}
		}
	}
}
