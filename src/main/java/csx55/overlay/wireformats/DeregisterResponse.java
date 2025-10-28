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
 *  Message Type (int): DEREGISTER_REQUEST
 *  STATUS CODE (byte): code
 *  Additional Info (String): info
 */

public class DeregisterResponse implements Event, Protocol {

  private byte[] bytes;
  
  private int type;
  private byte code;
  private String info;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public byte getCode() {
    byte codeCopy = this.code;
    return codeCopy;
  }

  public String getInfo() {
    String infoCopy = this.info;
    return infoCopy;
  }

  public DeregisterResponse(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  public DeregisterResponse(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.code = Byte.parseByte(args[1]);
    this.info = args[2];
    this.bytes = marshallBytes();
  }

  private void unmarshallBytes() throws IOException { // should only ever be called from the receive-side constructor
    ByteArrayInputStream baInputStream = new ByteArrayInputStream(bytes);
    DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

    this.type = din.readInt();
    this.code = din.readByte();

    int infoLength = din.readInt();
    byte[] infoBytes = new byte[infoLength];
    din.readFully(infoBytes, 0, infoLength);

    this.info = new String(infoBytes);

    baInputStream.close();
    din.close();
  }

  private byte[] marshallBytes() throws IOException { // should only ever be called from the sender-side constructor
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(DEREGISTER_RESPONSE);
    dout.writeByte(code);

    byte[] infoBytes = info.getBytes();
    int infoLength = infoBytes.length;

    dout.writeInt(infoLength);
    dout.write(infoBytes);

    dout.flush();
    marshalledBytes = baOutputStream.toByteArray();

    baOutputStream.close();
    dout.close();

    return marshalledBytes;
  }

  // Testing methods
  @Override
  public String toString() {
    return type + " " + code + " " + info;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DeregisterResponse)) return false;
    DeregisterResponse that = (DeregisterResponse) o;
    return this.type == that.type
        && this.code == that.code
        && java.util.Objects.equals(this.info, that.info);
  }
}
