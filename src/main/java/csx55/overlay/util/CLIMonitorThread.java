package csx55.overlay.util;

import csx55.overlay.util.Request;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Scanner;

public class CLIMonitorThread implements Runnable {

  Scanner scanner;
  BlockingQueue<Request> requests;

  
  public CLIMonitorThread(BlockingQueue<Request> requests) {
    this.scanner = new Scanner(System.in);
    this.requests = requests;
  }

  public void process(String command) {
    String[] parts = command.trim().split("\\s+"); // so that there can be more than one space before args
    String verb = parts[0];

    Request.Command cmd;
    switch (verb) {
      case "list-messaging-nodes":
        cmd = Request.Command.LIST_MESSAGING_NODES;
        break;
      case "list-weights":
        cmd = Request.Command.LIST_WEIGHTS;
        break;
      case "setup-overlay":
        cmd = Request.Command.SETUP_OVERLAY;
        break;
      case "send-overlay-link-weights":
        cmd = Request.Command.SEND_OVERLAY_LINK_WEIGHTS;
        break;
      case "start":
        cmd = Request.Command.START;
        break;
      case "print-mst":
        cmd = Request.Command.PRINT_MST;
        break;
      case "exit-overlay":
        cmd = Request.Command.EXIT_OVERLAY;
        break;
      default:
        throw new IllegalArgumentException("Invalid Command: " + verb);
    }

    String[] args = null;
    if (cmd == Request.Command.SETUP_OVERLAY || cmd == Request.Command.START) {
      if (parts.length < 2) throw new IllegalArgumentException("Missing argument");
      args = new String[] { parts[1] };
    }

    Request request = Request.cli(cmd, args);
    this.requests.add(request);
  }

  public void run() {
    while (true) {
      try {
        if (scanner.hasNextLine()) {
          String command = scanner.nextLine();
          this.process(command);
        }
      } catch (IllegalArgumentException iae) {
        System.out.println(iae.getMessage());
      }
    }
  }
}