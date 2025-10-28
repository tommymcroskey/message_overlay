package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TaskSummaryResponse implements Event, Protocol {
  private int type;
  private byte[] bytes;
  private int sendTracker;
  private int receiveTracker;
  private int relayTracker;
  private long sendSummation;
  private long receiveSummation;
  private int port;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public int getSendTracker() {
    return this.sendTracker;
  }

  public int getReceiveTracker() {
    return this.receiveTracker;
  }

  public int getRelayTracker() {
    return this.relayTracker;
  }

  public long getSendSummation() {
    return this.sendSummation;
  }

  public long getReceiveSummation() {
    return this.receiveSummation;
  }

  public int getPort() {
    return this.port;
  }

  public TaskSummaryResponse(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.sendTracker = Integer.parseInt(args[1]);
    this.receiveTracker = Integer.parseInt(args[2]);
    this.relayTracker = Integer.parseInt(args[3]);
    this.sendSummation = Long.parseLong(args[4]);
    this.receiveSummation = Long.parseLong(args[5]);
    this.port = Integer.parseInt(args[6]);
    this.bytes = this.marshallBytes();
  }

  public TaskSummaryResponse(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  private byte[] marshallBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(this.type);
    dout.writeInt(this.sendTracker);
    dout.writeInt(this.receiveTracker);
    dout.writeInt(this.relayTracker);
    dout.writeLong(this.sendSummation);
    dout.writeLong(this.receiveSummation);
    dout.writeInt(this.port);

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
    this.sendTracker = din.readInt();
    this.receiveTracker = din.readInt();
    this.relayTracker = din.readInt();
    this.sendSummation = din.readLong();
    this.receiveSummation = din.readLong();
    this.port = din.readInt();

    baInputStream.close();
    din.close();
  }
}
