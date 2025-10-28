package csx55.overlay.wireformats;

import csx55.overlay.wireformats.*;

import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.lang.IllegalArgumentException;

public class EventFactory implements Protocol {

  private static final EventFactory INSTANCE = new EventFactory();

  private EventFactory() {
  }

  public static EventFactory getInstance() {
    return INSTANCE;
  }

  public Event getEventObject(byte[] marshalledBytes) throws IOException, IllegalArgumentException {

    ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
    DataInputStream din = new DataInputStream(baInputStream);
    final int type = din.readInt();

    switch (type) {
      case (REGISTER_REQUEST):      return new RegisterRequest(marshalledBytes);
      case (REGISTER_RESPONSE):     return new RegisterResponse(marshalledBytes);
      case (DEREGISTER_REQUEST):    return new DeregisterRequest(marshalledBytes);
      case (DEREGISTER_RESPONSE):   return new DeregisterResponse(marshalledBytes);
      case (MESSAGING_NODES_LIST):  return new MessagingNodesList(marshalledBytes);
      case (LINK_WEIGHTS):          return new LinkWeights(marshalledBytes);
      case (TASK_INITIATE):         return new TaskInitiate(marshalledBytes);
      case (TASK_COMPLETE):         return new TaskComplete(marshalledBytes);
      case (PULL_TRAFFIC_SUMMARY):  return new TaskSummaryRequest(marshalledBytes);
      case (TRAFFIC_SUMMARY):       return new TaskSummaryResponse(marshalledBytes);
      case (HELLO):                 return new Hello(marshalledBytes);
      case (MESSAGE):               return new Message(marshalledBytes);
      default:
        throw new IllegalArgumentException("Invalid Protocol in getEventObject(byte[]) argument: " + type);
    }
  }

  public Event getEventObject(String[] args) throws IOException, IllegalArgumentException {

    if (args == null || args.length < 1) {
      throw new IllegalArgumentException("args is null / empty");
    }

    final int type;
    try {
      type = Integer.parseInt(args[0]);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("args[0] must be an int protocol type", nfe);
    }

    switch (type) {

      case (REGISTER_REQUEST):      return new RegisterRequest(args);
      case (REGISTER_RESPONSE):     return new RegisterResponse(args);
      case (DEREGISTER_REQUEST):    return new DeregisterRequest(args);
      case (DEREGISTER_RESPONSE):   return new DeregisterResponse(args);
      case (MESSAGING_NODES_LIST):  return new MessagingNodesList(args);
      case (LINK_WEIGHTS):          return new LinkWeights(args);
      case (TASK_INITIATE):         return new TaskInitiate(args);
      case (TASK_COMPLETE):         return new TaskComplete(args);
      case (PULL_TRAFFIC_SUMMARY):  return new TaskSummaryRequest(args);
      case (TRAFFIC_SUMMARY):       return new TaskSummaryResponse(args);
      case (HELLO):                 return new Hello(args);
      case (MESSAGE):               return new Message(args);
      default:
        throw new IllegalArgumentException("Invalid Protocol in getEventObject(String[]) argument: " + type);
    }
  }
}