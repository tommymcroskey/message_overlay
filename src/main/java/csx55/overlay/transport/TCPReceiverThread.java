package csx55.overlay.transport;

import csx55.overlay.util.Request;

import java.net.Socket;
import java.net.SocketException;

import java.io.DataInputStream;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;

public class TCPReceiverThread implements Runnable {
  
  private Socket socket;
  private DataInputStream din;
  private BlockingQueue<Request> requests;

  public TCPReceiverThread(Socket socket, BlockingQueue<Request> requests) throws IOException {
    this.socket = socket;
    this.din = new DataInputStream(socket.getInputStream());
    this.requests = requests;
  }

  public void run() {
    int dataLength;
    while (this.socket != null) {
      try {
        dataLength = this.din.readInt();

        byte[] data = new byte[dataLength];
        this.din.readFully(data, 0, dataLength);

        Request request = Request.net(data, this.socket);
        this.requests.put(request);

      } catch (SocketException se) {
        System.out.println(se.getMessage());
        break;

      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
        break;

      } catch (InterruptedException ie) {
        System.out.println(ie.getMessage());
        break;
      }
    } // end while
  } // end run()
}
