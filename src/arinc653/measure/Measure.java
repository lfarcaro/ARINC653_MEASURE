package arinc653.measure;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import j.extensions.comm.SerialComm;

/**
 * Measurement tool.
 * 
 * @author Lu�s Fernando Arcaro
 */
public class Measure {

	// Definitions
	public static final int COMMAND_HANDSHAKE = 0x005AA500;
	public static final int COMMAND_CONFIGURE = 0x11000011;
	public static final int COMMAND_MEASURE = 0xFF0000FF;
	public static final int RESPONSE_ERROR = 0x00000000;
	public static final int RESPONSE_SUCCESS = 0x11111111;

	// Configure command flags
	public static final int COMMAND_CONFIGURE_BPRED_DIS = 0x00000001;
	public static final int COMMAND_CONFIGURE_BPRED_ENA = 0x00000002;
	public static final int COMMAND_CONFIGURE_BPRED_CLR = 0x00000004;
	public static final int COMMAND_CONFIGURE_DCACHE_DIS = 0x00000010;
	public static final int COMMAND_CONFIGURE_DCACHE_ENA = 0x00000020;
	public static final int COMMAND_CONFIGURE_DCACHE_CLR = 0x00000040;
	public static final int COMMAND_CONFIGURE_ICACHE_DIS = 0x00000100;
	public static final int COMMAND_CONFIGURE_ICACHE_ENA = 0x00000200;
	public static final int COMMAND_CONFIGURE_ICACHE_CLR = 0x00000400;

	// Serial port configuration
	public static final String SERIALPORT_NAME = "COM7";

	// Configuration flags
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_DIS | COMMAND_CONFIGURE_DCACHE_DIS | COMMAND_CONFIGURE_ICACHE_DIS;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_DIS | COMMAND_CONFIGURE_DCACHE_DIS | COMMAND_CONFIGURE_ICACHE_ENA;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_DIS | COMMAND_CONFIGURE_DCACHE_ENA | COMMAND_CONFIGURE_ICACHE_DIS;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_DIS | COMMAND_CONFIGURE_DCACHE_ENA | COMMAND_CONFIGURE_ICACHE_ENA;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_ENA | COMMAND_CONFIGURE_DCACHE_DIS | COMMAND_CONFIGURE_ICACHE_DIS;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_ENA | COMMAND_CONFIGURE_DCACHE_DIS | COMMAND_CONFIGURE_ICACHE_ENA;
//	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_ENA | COMMAND_CONFIGURE_DCACHE_ENA | COMMAND_CONFIGURE_ICACHE_DIS;
	public static final int COMMAND_CONFIGURE_FLAGS = COMMAND_CONFIGURE_BPRED_ENA | COMMAND_CONFIGURE_DCACHE_ENA | COMMAND_CONFIGURE_ICACHE_ENA;

	/**
	 * Main method.
	 * 
	 * @param args Arguments.
	 */
	public static void main(String[] args) {
		try {

			// Shows message
			System.out.println("Searching for port '" + SERIALPORT_NAME + "'...");

			// Searches for selected serial port
			SerialComm scSerialPort = null;
			SerialComm[] scCommPorts = SerialComm.getCommPorts();
			for (int i = 0; i < scCommPorts.length; i++) {
				if (SERIALPORT_NAME.equals(scCommPorts[i].getSystemPortName())) {
					scSerialPort = scCommPorts[i];
				}
			}
			if (scSerialPort == null) {

				// Shows message
				System.out.println("Serial port '" + SERIALPORT_NAME + "' not found");
				return;
			}

			// Shows message
			System.out.println("Serial port '" + SERIALPORT_NAME + "' found, configuring...");

			// Sets serial port parameters
			scSerialPort.setComPortParameters(115200, 8, SerialComm.ONE_STOP_BIT, SerialComm.NO_PARITY);
			scSerialPort.setComPortTimeouts(SerialComm.TIMEOUT_READ_BLOCKING | SerialComm.TIMEOUT_WRITE_BLOCKING, 0, 0);

			// Shows message
			System.out.println("Opening serial port...");

			// Opens serial port
			scSerialPort.openPort();
			try {

				// Shows message
				System.out.println("Serial port open");

				// Gets streams
				DataInputStream isInputStream = new DataInputStream(scSerialPort.getInputStream());
				DataOutputStream osOutputStream = new DataOutputStream(scSerialPort.getOutputStream());

				// Sends handshake command
				osOutputStream.writeInt(COMMAND_HANDSHAKE);
				if (isInputStream.readInt() != COMMAND_HANDSHAKE) {
					throw new RuntimeException("Invalid handshake");
				}

				// Shows message
				System.out.println("Handshake received");

				// Sends configuration command
				osOutputStream.writeInt(COMMAND_CONFIGURE);
				osOutputStream.writeInt(COMMAND_CONFIGURE_FLAGS);

				// Shows message
				System.out.println("Configuration applied");

				// Measuring loop
				for (int i = 0; i < 1000; i++) {

					// Sends measurement request
					osOutputStream.writeInt(COMMAND_MEASURE);
					if (isInputStream.readInt() != COMMAND_MEASURE) {
						throw new RuntimeException("Measure command rejected");
					}

					// Reads measured value
					int inValue = isInputStream.readInt();

					// Shows measurement
					System.out.println("Measured value: " + inValue);
				}
			} finally {

				// Closes serial port
				scSerialPort.closePort();
			}
		} catch (Throwable t) {

			// Prints exception
			t.printStackTrace();
		}
	}
}
