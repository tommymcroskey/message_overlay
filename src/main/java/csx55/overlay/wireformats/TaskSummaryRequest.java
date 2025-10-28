package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TaskSummaryRequest implements Event, Protocol {
  private int type;
  private byte[] bytes;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public TaskSummaryRequest(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.bytes = this.marshallBytes();
  }

  public TaskSummaryRequest(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  private byte[] marshallBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(this.type);
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

    baInputStream.close();
    din.close();
  }
}
