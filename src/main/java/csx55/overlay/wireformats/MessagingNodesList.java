package csx55.overlay.wireformats;

import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MessagingNodesList implements Event, Protocol {

  private byte[] bytes;
  private int type;
  private int numLinks;
  private String[] links;

  public byte[] getBytes() {
    return this.bytes;
  }

  public int getType() {
    return this.type;
  }

  public MessagingNodesList(String[] args) throws IOException {
    this.type = Integer.parseInt(args[0]);
    this.numLinks = Integer.parseInt(args[1]);
    this.links = new String[numLinks];
    for (int i = 0; i < numLinks; i++) {
      this.links[i] = args[i + 2];
    }
    this.bytes = this.marshallBytes();
  }

  public MessagingNodesList(byte[] marshalledBytes) throws IOException {
    this.bytes = marshalledBytes;
    this.unmarshallBytes();
  }

  private byte[] marshallBytes() throws IOException {
    byte[] marshalledBytes = null;
    ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
    DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

    dout.writeInt(this.type);
    dout.writeInt(this.numLinks);

    int linkLength;
    byte[] linkBytes;
    for (int i = 0; i < this.numLinks; i++) {
      linkLength = this.links[i].length();
      linkBytes = this.links[i].getBytes();

      dout.writeInt(linkLength);
      dout.write(linkBytes, 0, linkLength);
    }

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
    this.numLinks = din.readInt();

    int linkLength;
    this.links = new String[numLinks];
    byte[] linkBytes;
    for (int i = 0; i < this.numLinks; i++) {
      linkLength = din.readInt();
      linkBytes = new byte[linkLength];
      din.readFully(linkBytes, 0, linkLength);
      String link = new String(linkBytes);
      links[i] = link;
    }

    baInputStream.close();
    din.close();
  }
}
