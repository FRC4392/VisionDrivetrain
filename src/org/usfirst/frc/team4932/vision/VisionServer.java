package org.usfirst.frc.team4932.vision;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import edu.wpi.first.wpilibj.Timer;

import org.usfirst.frc.team4932.vision.messages.VisionMessage;
import org.usfirst.frc.team4932.vision.messages.HeartbeatMessage;
import org.usfirst.frc.team4932.vision.messages.OffWireMessage;

public class VisionServer implements Runnable {
	
	static int tempPort = 8254;
	
	private static VisionServer server_instance = null;
	private ServerSocket server_socket;
	private boolean running = true;
	private int m_port;
	private ArrayList<VisionUpdateReceiver> receivers = new ArrayList<>();
	AdbBridge adb = new AdbBridge();
	double last_message_received_time = 0;
	private boolean use_java_time = false;
	private ArrayList<ServerThread> server_threads = new ArrayList<>();
	private volatile boolean want_app_restart = false;
	private boolean is_connected = false;
	
	public static VisionServer getInstance() {
		if (server_instance == null) {
			server_instance = new VisionServer(tempPort);
		}
		
		return server_instance;
	}
	
	public boolean isConnected() {
		return is_connected;
	}
	
	public void requestAppRestart() {
		want_app_restart = true;
	}
	
	protected class ServerThread implements Runnable {
		private Socket m_socket;
		
		public ServerThread(Socket socket) {
			m_socket = socket;
		}
		
		public void sendMessage(VisionMessage message) {
			String to_send = message.toJson() + "\n";
			if (m_socket != null && m_socket.isConnected()) {
				try {
					OutputStream os = m_socket.getOutputStream();
					os.write(to_send.getBytes());
				} catch (IOException e) {
					System.err.println("Vision Server: Could not send data to socket");
				}
			}
		}
		
		public void handleMessage(VisionMessage message, double timestamp) {
			if ("targets".equals(message.getType())) {
				VisionUpdate update = VisionUpdate.generateFromJsonString(timestamp, message.getMessage());
				receivers.removeAll(Collections.singleton(null));
				if(update.isValid()) {
					for (VisionUpdateReceiver receiver : receivers){
						receiver.gotUpdate(update);
					}
				}
				
			}
			if ("heartbeat".equals(message.getType())) {
				sendMessage(HeartbeatMessage.getInstance());
			}
		}
		
		public boolean isAlive() {
			return (m_socket != null && m_socket.isConnected() && !m_socket.isClosed());
		}
		
		@Override
		public void run() {
			if(m_socket == null) {
				return;
			}
			
			try {
				InputStream is = m_socket.getInputStream();
				byte[] buffer = new byte[2048];
				int read;
				while (m_socket.isConnected() && (read = is.read(buffer)) != -1) {
					double timestamp = getTimestamp();
					last_message_received_time = timestamp;
					String messageRaw = new String(buffer, 0, read);
					String[] messages = messageRaw.split("\n");
					for (String message : messages) {
						OffWireMessage parsedMessage = new OffWireMessage(message);
						if (parsedMessage.isValid()) {
							handleMessage(parsedMessage, timestamp);
						}
					}
					
				}
				System.err.println("Socket disconnected");
			} catch (IOException e) {
				System.err.println("Could not talk to socket");
			}
			
			if (m_socket != null) {
				try {
					m_socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private VisionServer (int port) {
		try {
			adb = new AdbBridge();
			m_port = port;
			server_socket = new ServerSocket(port);
			adb.start();
			adb.reversePortForward(port, port);
			try {
				String useJavaTime = System.getenv("USE_JAVA_TIME");
				use_java_time = "true".equals(useJavaTime);
			} catch (NullPointerException e) {
				use_java_time = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
		new Thread(new AppMaintainanceThread()).start();
	}
	
	public void restartAdb() {
		adb.restartAdb();
		adb.reversePortForward(m_port, m_port);
	}
	
	public void addVisionUpdateReceiver(VisionUpdateReceiver receiver) {
		if (!receivers.contains(receiver)) {
			receivers.add(receiver);
		}
	}
	
	public void removeVisionUpdateReceiver(VisionUpdateReceiver receiver) {
		if (receivers.contains(receiver)) {
			receivers.remove(receiver);
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				Socket p = server_socket.accept();
				ServerThread s = new ServerThread(p);
				new Thread(s).start();
				server_threads.add(s);
			} catch (IOException e) {
				System.err.println("Issue accepting socket connection!");
			} finally {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private double getTimestamp() {
		if (use_java_time) {
			return System.currentTimeMillis();
		} else {
			return Timer.getFPGATimestamp();
		}
	}
	
	private class AppMaintainanceThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (getTimestamp() - last_message_received_time > .1) {
					adb.reversePortForward(m_port, m_port);
					is_connected = false;
				} else {
					is_connected = true;
				}
				if (want_app_restart) {
					adb.restartApp();
					want_app_restart = false;
				} try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
