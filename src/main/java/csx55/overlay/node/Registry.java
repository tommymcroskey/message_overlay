package csx55.overlay.node;

import csx55.overlay.wireformats.*;
import csx55.overlay.transport.*;
import csx55.overlay.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Set;

import java.io.IOException;
import java.net.Socket;

/*
 * Node connects to Registry, registry saves socket; 
 * node reports the serversocket port and ip across 
 * the pipe, which acts as an id. now when nodes 
 * communicate they establish a connection to the 
 * serversocket, then record in their active 
 * connections the ports associated with each 
 * connection such that these ports dont necessarily
 * match the registry ports for the same nodes. 
 * Now, in order to ensure that messages get sent
 * to the right place, each node compiles a hashmap
 * of registry keys to their own specific keys to
 * the connection.
 */

 /*
  * Okay node sends in bytes, listener takes bytes
  * and takes the sourceIP and creates an object.
  * Then, it puts object in a Queue. now the main
  * thread will access the Queue of events and
  * source IPs and take the latest one, process
  * the event and finally check the source IP
  * against the request. For now, all I worry
  * about is building an accurate ConcurrentHashMap
  * table of the registeredNodes in registry, I
  * will concern myself with canonicalizing it
  * at a later step.
  */

public class Registry implements Node, Protocol {

  ConcurrentHashMap<String, TCPSender> registeredNodes;
  BlockingQueue<Request> requests;
  private String currentInet;
  private Socket currentSocket;
  private int localPort;
  private Set<String> overlay;
  private int numComplete;
  private int numExpected;
  private int currRounds;
  private String myInet; // do not use, here for polymorphism

  private int messageSentSum;
  private int messageReceiveSum;
  private long messageSentSummationSum;
  private long messageReceiveSummationSum;



  public Registry() {
    this.registeredNodes = new ConcurrentHashMap<>();
    this.requests = new LinkedBlockingQueue<>();
  }

  /*
   * This should be running on a loop to check if a request is up
   * and then complete the request while recording who registry is
   * talking to for the duration of the request, then freeing that
   * variable.
   */
  private void nextRequest() throws IOException, IllegalArgumentException, InterruptedException {
    Request request = this.requests.take();
    this.process(request);
  }

  private void process(Request request) throws IOException, IllegalArgumentException {
    if (request.isNet()) {
      byte[] bytes = request.bytes;
      this.currentInet = request.sourceInet;
      this.currentSocket = request.socket;
      Event event = EventFactory.getInstance().getEventObject(bytes);
      this.onEvent(event);
      this.currentInet = null;
      this.currentSocket = null;
    } else if (request.isCli()) {
      // handle CLI commands here
      this.runCommand(request.command, request.args);
    } else {
      throw new IllegalArgumentException("Invalid request object neither NET nor CLI");
    }
  }

  private void runCommand(Request.Command command, String[] args) throws IOException, IllegalStateException {
    switch (command) {
      case LIST_MESSAGING_NODES:
        this.listMessagingNodes();
        break;
      case LIST_WEIGHTS:
        this.listWeights();
        break;
      case SETUP_OVERLAY:
        this.setupOverlay(args);
        break;
      case SEND_OVERLAY_LINK_WEIGHTS:
        this.sendOverlayLinkWeights();
        break;
      case START:
        this.sendTaskInitiate(args);
        break;
      case PRINT_MST:
      case EXIT_OVERLAY:
      default:
        System.out.println("Not a valid command for this node.");
    }
  }

  /* not thread safe. only ever have one thread 
  * running this at one time, otherwise lock the
  * interactingWith field until interacting with 
  * someone else or find a way to have each
  * thread have its own variable it can access to
  * retrieve accurate info
  */
  public void onEvent(Event event) {
    try {
      int type = event.getType();
      switch (type) {
        case REGISTER_REQUEST: this.register(event); break;
        case DEREGISTER_REQUEST: this.deregister(event); break;
        case TASK_COMPLETE: this.handleTaskComplete(event); break;
        case TRAFFIC_SUMMARY: this.handleTrafficSummary(event); break;
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  private void handleTrafficSummary(Event event) {
    if (event instanceof TaskSummaryResponse) {
      TaskSummaryResponse tsr = (TaskSummaryResponse) event;
      System.out.println(this.currentInet + ":" 
                         + tsr.getPort() 
                         + " " + tsr.getSendTracker() 
                         + " " + tsr.getReceiveTracker() 
                         + " " + tsr.getSendSummation() + ".00"
                         + " " + tsr.getReceiveSummation() + ".00"
                         + " " + tsr.getRelayTracker());
    this.messageSentSum += tsr.getSendTracker();
    this.messageReceiveSum += tsr.getReceiveTracker();
    this.messageSentSummationSum += tsr.getSendSummation();
    this.messageReceiveSummationSum += tsr.getReceiveSummation();
    this.numComplete += 1;
    if (numComplete == numExpected) {
      System.out.println("sum " + messageSentSum + " "
                         + messageReceiveSum + " "
                         + messageSentSummationSum + ".00 " 
                         + messageReceiveSummationSum + ".00");
    }
    };
  }

  private void handleTaskComplete(Event event) {
    if (event instanceof TaskComplete) {
      if (event.getType() == TASK_COMPLETE) this.numComplete += 1; // not necessary to check type but i wanted to use my work lol
      if (this.numComplete == this.numExpected) {
        System.out.println(this.currRounds + " rounds completed");
        try {
          Thread.sleep(5000); // wait a little to let messages settle after being sent
          EventFactory eventFactory = EventFactory.getInstance();
          String[] args = {
            Integer.toString(PULL_TRAFFIC_SUMMARY),
          };
          Event trafficRequest = eventFactory.getEventObject(args);
          Set<String> regNodes = this.registeredNodes.keySet();
          for (String node : regNodes) {
            this.registeredNodes.get(node).sendData(trafficRequest.getBytes());
          }
          this.numComplete = 0;
          this.messageSentSum = 0;
          this.messageReceiveSum = 0;
          this.messageSentSummationSum = 0;
          this.messageReceiveSummationSum = 0;
        } catch (IOException ioe) {
          System.out.println(ioe.getMessage());
        } catch (InterruptedException ie) {
          System.out.println(ie.getMessage());
        }
      }
    } else throw new IllegalStateException("Incorrect event type. Expected: TaskComplete.");
  }

  private void sendTaskInitiate(String[] cliArgs) throws IOException, IllegalArgumentException {
    Set<String> regNodes = this.registeredNodes.keySet();
    String[] eventArgs = {
      Integer.toString(TASK_INITIATE),
      cliArgs[0],
    };
    this.currRounds = Integer.parseInt(cliArgs[0]);
    EventFactory eventFactory = EventFactory.getInstance();
    byte[] startBytes = eventFactory.getEventObject(eventArgs).getBytes();
    this.numExpected = 0;
    for (String node : regNodes) {
      this.numExpected += 1;
      this.registeredNodes.get(node).sendData(startBytes);;
    }
    this.numComplete = 0;
  }

  private void setupOverlay(String[] args) {
    int k = Integer.parseInt(args[0]);
    OverlayCreator oc = new OverlayCreator(this.registeredNodes.keySet(), k);
    oc.computeOverlay();
    this.overlay = oc.getOverlaySetString();
    System.out.println("setup completed with " + this.overlay.size() + " connections");
  }

  private void listWeights() {
    if (this.overlay == null) throw new IllegalStateException("Overlay not set or is empty");
    for (String key : this.overlay) {
      String[] keyParts = key.toString().split(" ");
      System.out.println(keyParts[0] + ", " + keyParts[1] + ", " + keyParts[2]);
    }
  }
  
  public void spawnCLIMonitor() {
    CLIMonitorThread cliMonitor = new CLIMonitorThread(this.requests);
    Thread cliMonitorThread = new Thread(cliMonitor);
    cliMonitorThread.start();
  }

  public void spawnTCPServerThread(int port) throws IOException {
    TCPServerThread server = new TCPServerThread(port, this.requests);
    this.localPort = server.getLocalPort();
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

  private void register(Event event) throws IOException, IllegalArgumentException {

    if (event instanceof RegisterRequest) {
      RegisterRequest rr = (RegisterRequest) event;

      String inet = rr.getInet();
      int port = rr.getPort();
      String key = inet + ":" + port;
      String[] args;

      TCPSender sender = new TCPSender(this.currentSocket);

      if (!inet.equals(this.currentInet)) {
        args = registerInvalidSourceHelper();

      } else if (registeredNodes.containsKey(key)) {
        args = registerAlreadyAddedHelper();

      } else {
        registeredNodes.put(key, sender);
        args = registerSuccessHelper();
      }

      byte[] dataToSend = EventFactory.getInstance().getEventObject(args).getBytes();
      sender.sendData(dataToSend);

    } else throw new IllegalArgumentException("Incorrect event type. Expected: RegisterRequest.");
  }
  
  private void deregister(Event event) throws IOException, IllegalArgumentException {
    if (event instanceof DeregisterRequest) {
      DeregisterRequest dr = (DeregisterRequest) event;

      String inet = dr.getInet();
      int port = dr.getPort();
      String key = inet + ":" + port;
      String[] args;
      TCPSender sender = new TCPSender(this.currentSocket);
      boolean success = false;

      if (!inet.equals(this.currentInet)) {
        args = deregisterInvalidSourceHelper();

      } else if (!registeredNodes.containsKey(key)) {
        args = deregisterNotAddedHelper();

      } else {
        registeredNodes.remove(key);
        args = deregisterSuccessHelper();
        success = true;
      }

      byte[] dataToSend = EventFactory.getInstance().getEventObject(args).getBytes();
      sender.sendData(dataToSend);

      if (success) {
        sender.cleanup();
      }

    } else throw new IllegalArgumentException("Incorrect event type. Expected: DeregisterRequest.");
  }

  private void sendOverlayLinkWeights() throws IOException, IllegalStateException {
    EventFactory eventFactory = EventFactory.getInstance();
    Set<String> regNodes = this.registeredNodes.keySet();

    if (regNodes.isEmpty()) 
      throw new IllegalStateException("No registered nodes present in overlay");
    if (this.overlay == null || this.overlay.isEmpty()) 
      throw new IllegalStateException("Overlay has not been set up or is empty");

    int linksLength = this.overlay.size();
    String[] args = new String[linksLength + 2];
    args[0] = Integer.toString(LINK_WEIGHTS);
    args[1] = Integer.toString(linksLength);
    int i = 2;
    for (String link : this.overlay) {
      args[i++] = link;
    }
    byte[] eventBytes = eventFactory.getEventObject(args).getBytes();

    for (String node : regNodes) {
      this.registeredNodes.get(node).sendData(eventBytes);;
    }
    System.out.println("link weights assigned");
  }

  private void listMessagingNodes() {
    Set<String> keys = this.registeredNodes.keySet();
    for (String key : keys) {
      System.out.println(key);
    }
  }
  
  private String registerFormatInfo(int code) {
    String update = "The number of messaging nodes currently constituting the overlay is (%d)\n";
    String prefix;
    switch (code) {
      case 0:
        prefix = "Registration request successful.";
        break;
      case 1:
        prefix = "Registration request unsuccessful. Node already registered.";
        break;
      case 2:
        prefix = "Registration request unsuccessful. Source IP mismatch.";
        break;
      default:
        prefix = "Registration request unsuccessful. Reason unknown.";
    };
    return String.format(prefix + update, registeredNodes.size());
  }

  private String deregisterFormatInfo(int code) {
    String update = "The number of messaging nodes currently constituting the overlay is (%d)\n";
    String prefix;
    switch (code) {
      case 0:
        prefix = "Deregistration request successful.";
        break;
      case 1:
        prefix = "Deregistration request unsuccessful. Node not registered.";
        break;
      case 2:
        prefix = "Deregistration request unsuccessful. Source IP mismatch.";
        break;
      default:
        prefix = "Deregistration request unsuccessful. Reason unknown.";
        break;
    };
    return String.format(prefix + update, registeredNodes.size());
  }
  
  private String[] registerSuccessHelper() {
    int type = REGISTER_RESPONSE;
    byte code = 1;
    String info = this.registerFormatInfo(0);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }
  
  private String[] registerAlreadyAddedHelper() {
    int type = REGISTER_RESPONSE;
    byte code = 0;
    String info = this.registerFormatInfo(1);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }
  
  private String[] registerInvalidSourceHelper() {
    int type = REGISTER_RESPONSE;
    byte code = 0;
    String info = this.registerFormatInfo(2);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }

  private String[] deregisterSuccessHelper() {
    int type = DEREGISTER_RESPONSE;
    byte code = 1;
    String info = this.deregisterFormatInfo(0);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }

  private String[] deregisterNotAddedHelper() {
    int type = DEREGISTER_RESPONSE;
    byte code = 0;
    String info = this.deregisterFormatInfo(1);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }

  private String[] deregisterInvalidSourceHelper() {
    int type = DEREGISTER_RESPONSE;
    byte code = 0;
    String info = this.deregisterFormatInfo(2);
    String[] args = {
      Integer.toString(type),
      Byte.toString(code),
      info,
    };
    return args;
  }


  public static void main(String[] args) {
    if (args.length != 1) System.exit(1);
    Registry registry = new Registry();
    int port = Integer.parseInt(args[0]);
    try {
      registry.spawnTCPServerThread(port);
      registry.spawnCLIMonitor();
      while (true) {
        registry.nextRequest();
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    } catch (InterruptedException ie) {
      System.out.println(ie.getMessage());
    }
  }
}
