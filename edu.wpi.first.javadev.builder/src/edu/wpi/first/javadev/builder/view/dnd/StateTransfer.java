package edu.wpi.first.javadev.builder.view.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

public class StateTransfer extends ByteArrayTransfer {

	private static final String		typeName	= "WPI_State";
	private static final int		typeID		= registerType(typeName);
	private static StateTransfer	instance	= new StateTransfer();

	private StateTransfer() {}

	public static StateTransfer getInstance() {
		return instance;
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		if(!validate(object) || !isSupportedType(transferData)) {
			DND.error(DND.ERROR_INVALID_DATA);
		}
		try {
			StateTransferType state = (StateTransferType)object;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			byte[] buffer = state.state.getFullyQualifiedName().getBytes();
			writeOut.writeInt(buffer.length);
			writeOut.write(buffer);
			writeOut.writeInt(state.offsetX);
			writeOut.writeInt(state.offsetY);
			buffer = out.toByteArray();
			writeOut.close();
			super.javaToNative(buffer, transferData);
		} catch(IOException exception) {}
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		StateTransferType state = null;
		if(isSupportedType(transferData)) {
			byte[] buffer = (byte[])super.nativeToJava(transferData);
			if(buffer == null) {
				System.out.println("NULL Return");
				return null;
			}
			try {
				ByteArrayInputStream in = new ByteArrayInputStream(buffer);
				DataInputStream readIn = new DataInputStream(in);
				int nameLength = readIn.readInt();
				byte[] name = new byte[nameLength];
				readIn.read(name);
				//TODO make this use correct constructors
				/*state = new StateTransferType(new FRCRState(new String(name)));
				state.offsetX = readIn.readInt();
				state.offsetY = readIn.readInt();*/
				readIn.close();
			} catch(IOException exception) {
				exception.printStackTrace();
				return null;
			}
		}
		return state;
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
		return object != null && object instanceof StateTransferType;
	}
}
