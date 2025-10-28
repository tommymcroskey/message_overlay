package csx55.overlay.wireformats;

import csx55.overlay.wireformats.*;

import java.io.IOException;

public class TestWireformats implements Protocol{

  /*
   * Ensure expected outcomes from EventFactory calls and ensure thread safety
   * for EventFactory class
   */
  public static void main(String[] args) {
    testRegisterRequestArgs();
    testRegisterRequestBytes();
    testRegisterResponseArgs();
    testRegisterResponseBytes();
    testDeregisterRequestArgs();
    testDeregisterRequestBytes();
    testDeregisterResponseArgs();
    testDeregisterResponseBytes();
    // testLinkWeightsArgs();
    // testLinkWeightsBytes();
    testMessage();
  }
  
  public static void testRegisterRequestArgs() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = REGISTER_REQUEST;
    String inet = "localhost";
    int port = 4567;

    String[] myArgs = {
      Integer.toString(type),
      inet,
      Integer.toString(port),
    };
    try {
      Event event = eventFactory.getEventObject(myArgs);
      RegisterRequest expected = new RegisterRequest(myArgs);
      boolean matches = event.equals(expected);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testRegisterRequestBytes() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = REGISTER_REQUEST;
    String inet = "localhost";
    int port = 4567;

    String[] myArgs = {
      Integer.toString(type),
      inet,
      Integer.toString(port),
    };
    try {
      RegisterRequest RR = new RegisterRequest(myArgs);
      byte[] bytes = RR.getBytes();
      Event event = eventFactory.getEventObject(bytes);
      boolean matches = event.equals(RR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testRegisterResponseArgs() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = REGISTER_RESPONSE;
    byte code = (byte) 255;
    String info = "infotest";

    String[] myArgs = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    try {
      Event event = eventFactory.getEventObject(myArgs);
      RegisterResponse RR = new RegisterResponse(myArgs);
      boolean matches = event.equals(RR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testRegisterResponseBytes() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = REGISTER_RESPONSE;
    byte code = (byte) 255;
    String info = "infotest";

    String[] myArgs = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    try {
      RegisterResponse RR = new RegisterResponse(myArgs);
      byte[] bytes = RR.getBytes();
      Event event = eventFactory.getEventObject(bytes);
      boolean matches = event.equals(RR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testDeregisterRequestArgs() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = DEREGISTER_REQUEST;
    String inet = "localhost";
    int port = 4567;

    String[] myArgs = {
      Integer.toString(type),
      inet,
      Integer.toString(port),
    };
    try {
      Event event = eventFactory.getEventObject(myArgs);
      DeregisterRequest expected = new DeregisterRequest(myArgs);
      boolean matches = event.equals(expected);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testDeregisterRequestBytes() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = DEREGISTER_REQUEST;
    String inet = "localhost";
    int port = 4567;

    String[] myArgs = {
      Integer.toString(type),
      inet,
      Integer.toString(port),
    };
    try {
      DeregisterRequest DR = new DeregisterRequest(myArgs);
      byte[] bytes = DR.getBytes();
      Event event = eventFactory.getEventObject(bytes);
      boolean matches = event.equals(DR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testDeregisterResponseArgs() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = DEREGISTER_RESPONSE;
    byte code = (byte) 255;
    String info = "infotest";

    String[] myArgs = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    try {
      Event event = eventFactory.getEventObject(myArgs);
      DeregisterResponse RR = new DeregisterResponse(myArgs);
      boolean matches = event.equals(RR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  public static void testDeregisterResponseBytes() {
    EventFactory eventFactory = EventFactory.getInstance();

    int type = DEREGISTER_RESPONSE;
    byte code = (byte) 255;
    String info = "infotest";

    String[] myArgs = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    try {
      DeregisterResponse RR = new DeregisterResponse(myArgs);
      byte[] bytes = RR.getBytes();
      Event event = eventFactory.getEventObject(bytes);
      boolean matches = event.equals(RR);

      System.out.println("Expected: " + myArgs[0] + " " + myArgs[1] + " " + myArgs[2] + ". Output: " + event.toString() + ". Success: " + matches);
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  // public void testLinkWeightsArgs() {
  //   EventFactory eventFactory = EventFactory.getInstance();

  //   int type = LINK_WEIGHTS;
  //   int numLinks = 4;
  //   String[] links = {
  //     "link1",
  //     "link2",
  //     "link3",
  //     "link4",
  //   };
  //   String[] args = new String[numLinks + 2];
  //   args[0] = Integer.toString(type);
  //   args[1] = Integer.toString(numLinks);
  //   for (int i = 0; i < numLinks; i++) {
  //     args[i + 2] = links[i];
  //   }

  //   try {
  //     Event event = eventFactory.getEventObject(args);
  //     LinkWeights lw = new LinkWeights(args);
  //     boolean matching = event.equals(lw);
  //   } catch (IOException ioe) {
  //     System.out.println(ioe.getMessage());
  //   }
  // }

  public static void testMessage() {
    String[] args = {
      Integer.toString(MESSAGE),
      "ABCD:1234",
      Integer.toString(Integer.MAX_VALUE),
    };
    EventFactory eventFactory = EventFactory.getInstance();
    try {
      Event event = eventFactory.getEventObject(args);
      Event rcvEvent = eventFactory.getEventObject(event.getBytes());
      if (rcvEvent instanceof Message) {
        Message msg = (Message) rcvEvent;
        // System.out.println(msg.getType() + " " + msg.getDest() + " " + msg.getNum());
      }
    } catch (Exception e){
    }
  }
}
