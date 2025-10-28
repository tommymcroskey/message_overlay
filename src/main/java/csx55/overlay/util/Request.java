package csx55.overlay.util;

import java.net.Socket;

public class Request {
  
  enum RequestType {
    NET,
    CLI,
  }

  public enum Command {
    LIST_MESSAGING_NODES,
    LIST_WEIGHTS,
    SETUP_OVERLAY,
    SEND_OVERLAY_LINK_WEIGHTS,
    START,
    PRINT_MST,
    EXIT_OVERLAY,
  }

  private RequestType type;

  // net only
  public final byte[] bytes;
  public final Socket socket;
  public final String sourceInet;

  // cmd only
  public final Command command;
  public final String[] args;

  private Request(RequestType type, byte[] bytes, Socket socket, String sourceInet, Command command, String[] args) {
    this.type = type;
    this.bytes = bytes;
    this.socket = socket;
    this.sourceInet = sourceInet;
    this.command = command;
    this.args = args;
  }

  public static Request net(byte[] bytes, Socket socket) {
    String sourceInet = socket.getInetAddress().getHostAddress();
    return new Request(RequestType.NET, bytes, socket, sourceInet, null, null);
  }

  public static Request cli(Command command, String[] args) {
    return new Request(RequestType.CLI, null, null, null, command, args);
  }

  public boolean isNet() {
    return this.type == RequestType.NET;
  }

  public boolean isCli() {
    return this.type == RequestType.CLI;
  }
}