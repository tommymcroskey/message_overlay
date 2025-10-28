package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TaskInitiate implements Event, Protocol {
  private byte[] bytes;
  private int type;
  private int rounds;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public int getRounds() {
    return this.rounds;
  }

  public TaskInitiate(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.rounds = Integer.parseInt(args[1]);
    this.bytes = this.marshallBytes();
  }

  public TaskInitiate(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  private byte[] marshallBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(this.type);
    dout.writeInt(this.rounds);

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
    this.rounds = din.readInt();

    baInputStream.close();
    din.close();
  }
}
