package edu.wpi.first.javadev.builder.view.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * This class represents the "default state badge"
 * 
 * @author Joe Grinstead
 */
public class BadgeTransfer extends ByteArrayTransfer {

	private static final String		typeName	= "WPI_DefaultStateBadge";
	private static final int		typeID		= registerType(typeName);
	private static BadgeTransfer	instance	= new BadgeTransfer();

	private BadgeTransfer() {}

	public static BadgeTransfer getInstance() {
		return instance;
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if (!validate(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			byte[] buffer = "Badge".getBytes();
			writeOut.writeInt(buffer.length);
			writeOut.write(buffer);
			buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch (IOException exception) {}
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		String badge = null;
		if (isSupportedType(transferData)) {
			byte[] buffer = (byte[]) super.nativeToJava(transferData);
			if (buffer != null) {
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(buffer);
					DataInputStream readIn = new DataInputStream(in);
					int nameLength = readIn.readInt();
					byte[] name = new byte[nameLength];
					readIn.read(name);
					badge = new String(name);
					readIn.close();
				} catch (IOException exception) {}
			}
		}
		return badge;
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { typeID };
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { typeName };
	}

	@Override
	protected boolean validate(Object object) {
		return object != null && object.equals("Badge");
	}
}
