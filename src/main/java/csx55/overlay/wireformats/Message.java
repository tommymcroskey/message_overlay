package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Message implements Event, Protocol {

  private int type;
  private byte[] bytes;
  private String path;
  private int num;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public String getPath() {
    return this.path;
  }

  public void SetPath(String path) {
    this.path = path;
  }

  public int getNum() {
    return this.num;
  }

  public Message(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.path = args[1];
    this.num = Integer.parseInt(args[2]);
    this.bytes = this.marshallBytes();
  }

  public Message(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  private byte[] marshallBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(this.type);

    int destLength = this.path.length();
    byte[] destBytes = this.path.getBytes();

    dout.writeInt(destLength);
    dout.write(destBytes, 0, destLength);

    dout.writeInt(this.num);

    dout.flush();
    marshalledBytes = baOutputStream.toByteArray();

    baOutputStream.close();
    dout.close();

    return marshalledBytes;
  }

  private void unmarshallBytes() throws IOException {
    ByteArrayInputStream baInputStream = new ByteArrayInputStream(this.bytes);
    DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

    this.type = din.readInt();

    int destLength = din.readInt();
    byte[] destBytes = new byte[destLength];

    din.readFully(destBytes, 0, destLength);
    this.path = new String(destBytes);

    this.num = din.readInt();

    baInputStream.close();
    din.close();
  }
}
