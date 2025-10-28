package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/*
 * Message Body:
 *  Message Type (int): REGISTER_REQUEST
 *  IP Address (String): inet
 *  Port (int): port
 */

public class RegisterRequest implements Event, Protocol {
  
  private byte[] bytes;
  
  private int type;
  private String inet; // ensure always set in the form: /127.0.0.1 - for later comparison safety, will rely on node and registry behavior
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

  public RegisterRequest(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  public RegisterRequest(String[] args) throws IOException {
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

    dout.writeInt(REGISTER_REQUEST);

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

  // Testing methods
  @Override
  public String toString() {
    return type + " " + inet + " " + port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RegisterRequest)) return false;
    RegisterRequest that = (RegisterRequest) o;
    return this.type == that.type
        && this.port == that.port
        && java.util.Objects.equals(this.inet, that.inet);
  }
}