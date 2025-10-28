package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Hello implements Event, Protocol {
  private byte[] bytes;
  
  private int type;
  private String inet;
  private int port;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public String getInet() {
    String inetCopy = this.inet;
    return inetCopy;
  }

  public int getPort() {
    int portCopy = this.port;
    return portCopy;
  }

  public Hello(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  public Hello(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.inet = args[1];
    this.port = Integer.parseInt(args[2]);
    this.bytes = marshallBytes();
  }

  private void unmarshallBytes() throws IOException { // should only ever be called from the receive-side constructor
    ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
    DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

    this.type = din.readInt();
    
    int inetLength = din.readInt();
    byte[] inetBytes = new byte[inetLength];
    din.readFully(inetBytes, 0, inetLength);

    this.inet = new String(inetBytes);
    this.port = din.readInt();

    baInputStream.close();
    din.close();
  }

  private byte[] marshallBytes() throws IOException { // should only ever be called from the sender-side constructor
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(HELLO);

    byte[] inetBytes = inet.getBytes();
    int inetLength = inetBytes.length;

    dout.writeInt(inetLength);
    dout.write(inetBytes);
    dout.writeInt(port);

    dout.flush();
    marshalledBytes = baOutputStream.toByteArray();

    baOutputStream.close();
    dout.close();

    return marshalledBytes;
  }
}
