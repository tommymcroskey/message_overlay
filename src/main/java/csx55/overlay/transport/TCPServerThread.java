package csx55.overlay.transport;

import csx55.overlay.transport.TCPReceiverThread;
import csx55.overlay.util.Request;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;

public class TCPServerThread implements Runnable {
  
  private ServerSocket serverSocket;
  private BlockingQueue<Request> requests;


  private int localPort;

  public TCPServerThread(int port, BlockingQueue<Request> requests) throws IOException, IllegalStateException {
    this.serverSocket = new ServerSocket(port);
    this.localPort = this.serverSocket.getLocalPort();
    this.requests = requests;
  }

  public int getLocalPort() {
    return this.localPort;
  }

  public void run() {
    while (true) {
      try {
        Socket socket = serverSocket.accept();
        TCPReceiverThread receiver = new TCPReceiverThread(socket, this.requests);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();
        
      } catch (SocketException se) {
        System.out.println(se.getMessage());
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    }
  }
}
