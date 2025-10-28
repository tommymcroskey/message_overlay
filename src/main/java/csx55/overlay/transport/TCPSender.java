package csx55.overlay.transport;

import java.net.Socket;

import java.io.DataOutputStream;
import java.io.IOException;

public class TCPSender {

  private Socket socket;
  private DataOutputStream dout;
  private String clientInet;
  
  public TCPSender(Socket socket) throws IOException {
    this.socket = socket;
    this.dout = new DataOutputStream(socket.getOutputStream());
    this.clientInet = socket.getLocalAddress().getHostAddress();
  }

  public String getClientInet() {
    return this.clientInet;
  }

  public void sendData(byte[] dataToSend) throws IOException {
    int dataLength = dataToSend.length;
    dout.writeInt(dataLength);
    dout.write(dataToSend, 0, dataLength);
    dout.flush();
  }

  public void cleanup() throws IOException {
    this.dout.close();
    this.socket.close();
  }
}
