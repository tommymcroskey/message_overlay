package csx55.overlay.node;

import csx55.overlay.wireformats.*;
import csx55.overlay.transport.*;
import csx55.overlay.util.Request;
import csx55.overlay.util.CLIMonitorThread;
import csx55.overlay.spanning.MinimumSpanningTree;
import csx55.overlay.util.AdjacencyList;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.Random;

import java.lang.IllegalArgumentException;

import java.net.Socket;

import java.io.IOException;

public class MessagingNode implements Node, Protocol {

  private ConcurrentHashMap<String, TCPSender> activeConnections;
  private BlockingQueue<Request> requests;
  private String currentInet;
  private Socket currentSocket;
  // private String clientInet; // should only be set once, do i want final?
  private String myInet;
  private int myPort;
  private String registryInet;
  private int registryPort;
  private Set<String> linkWeights;
  private MinimumSpanningTree mst;
  private AdjacencyList al;
  // private Random rand;
  private String rootKey;

  private int sendTracker;
  private int receiveTracker;
  private int relayTracker;
  private long sendSummation;
  private long receiveSummation;

  public MessagingNode(String registryInet, int registryPort) {
    this.registryInet = registryInet;
    this.registryPort = registryPort;
    this.activeConnections = new ConcurrentHashMap<>();
    this.requests = new LinkedBlockingQueue<>();
    // this.rand = new Random();
  }

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

  private void runCommand(Request.Command command, String[] args) throws IOException {
    switch (command) {
      case LIST_MESSAGING_NODES:
      case LIST_WEIGHTS:
      case SETUP_OVERLAY:
      case SEND_OVERLAY_LINK_WEIGHTS:
      case START:
        break;
      case PRINT_MST:
        this.printMST();
        break;
      case EXIT_OVERLAY:
        this.exitOverlay();
        break;
      default:
        System.out.println("Not a valid command for this node.");
    }
  }

  private void createMST() {
    MinimumSpanningTree mst = new MinimumSpanningTree(this.rootKey, this.linkWeights);
    this.mst = mst;
  }

  private void printMST() {
    mst.printMST();
  }

  public void spawnCLIMonitor() {
    CLIMonitorThread cliMonitor = new CLIMonitorThread(this.requests);
    Thread cliMonitorThread = new Thread(cliMonitor);
    cliMonitorThread.start();
  }

  public void spawnServerThread() throws IOException { // must run this before registration request
    TCPServerThread server = new TCPServerThread(0, this.requests);
    this.myPort = server.getLocalPort();
    Thread serverThread = new Thread(server);
    serverThread.start();
  }

  /*
   * Later please add logic so that we create and reference the
   * sender object in the hashmap and delete the socket return
   */
  private TCPSender connectToReturnsSender(String inet, int port) throws IOException {
    Socket socket = new Socket(inet, port);
    TCPReceiverThread receiver = new TCPReceiverThread(socket, requests);
    Thread receiverThread = new Thread(receiver);
    receiverThread.start();
    TCPSender sender = new TCPSender(socket);
    return sender;
  }

  public void register() throws IOException {
    TCPSender sender = this.connectToReturnsSender(this.registryInet, this.registryPort);

    this.myInet = sender.getClientInet();
    this.rootKey = this.myInet + ":" + this.myPort;

    int type = REGISTER_REQUEST;
    String[] args = {
      Integer.toString(type),
      this.myInet,
      Integer.toString(this.myPort),
    };
    Event event = EventFactory.getInstance().getEventObject(args);
    sender.sendData(event.getBytes());
    String key = this.registryInet + ":" + this.registryPort;
    this.activeConnections.put(key, sender);
  }

  public void deregister() throws IOException {
    TCPSender sender = this.activeConnections.get(this.registryInet + ":" + this.registryPort);
    int type = DEREGISTER_REQUEST;
    String[] args = {
      Integer.toString(type),
      this.myInet,
      Integer.toString(this.myPort),
    };
    Event event = EventFactory.getInstance().getEventObject(args);
    sender.sendData(event.getBytes());
  }

  private void exitOverlay() throws IOException {
    deregister();
    System.out.println("exited overlay");
  }

  private void connectToWithLinks(Set<String> linkWeights) throws IOException {
    String[] parts;
    String myKey = this.rootKey;
    Set<String> toConnect = new HashSet<>();
    for (String lw : linkWeights) {
      parts = lw.trim().split("\\s+");
      if (parts[0].equals(myKey)) {
        toConnect.add(parts[1]);
      }
    }
    for (String key : toConnect) {
      this.connectToWithKey(key);
    }
  }

  private void connectToWithKey(String key) throws IOException {
    String[] keyParts = key.trim().split(":");
    int port = Integer.parseInt(keyParts[1]);
    TCPSender sender = this.connectToReturnsSender(keyParts[0], port);
    this.activeConnections.put(key, sender);
    this.sendHello(key);
  }

  private void sendHello(String key) throws IOException {
    String[] helloArgs = {
      Integer.toString(HELLO),
      this.myInet,
      Integer.toString(this.myPort),
    };
    EventFactory eventFactory = EventFactory.getInstance();
    Event event = eventFactory.getEventObject(helloArgs);
    TCPSender sender = this.activeConnections.get(key);
    sender.sendData(event.getBytes());
  }

  private void resetCounters() {
    this.sendTracker = 0;
    this.receiveTracker = 0;
    this.relayTracker = 0;
    this.sendSummation = 0;
    this.receiveSummation = 0;
  }

  private void start(int rounds) throws IOException {
    // need to send a message containing random number and destination key, then repeat rounds times
    this.resetCounters();

    Set<String> nds = new HashSet<>();
    String[] parts;
    for (String line : this.linkWeights) {
      parts = line.trim().split(" ");
      nds.add(parts[0]);
      nds.add(parts[1]);
    }

    ArrayList<String> nodes = new ArrayList<>(nds);
    Random rand = new Random();
    String dest = null;
    String regKey = this.registryInet + ":" + this.registryPort;
    int targetIndex = 0;
    EventFactory eventFactory = EventFactory.getInstance();
    for (int i = 0; i < rounds; i++) {
      while (dest == null || dest.equals(regKey) || dest.equals(this.rootKey)) { // this loop just gets a valid target node
        targetIndex = rand.nextInt(nodes.size());
        dest = nodes.get(targetIndex);
      }
      String path = this.mst.getPath(this.rootKey, dest);
      for (int k = 0; k < 5; k++) { // this loop creates a new message and sends it to the next node along the chain to target, 5 times per round
        int numToSend = rand.nextInt();
        this.sendSummation += numToSend;
        String[] args = {
          Integer.toString(MESSAGE),
          path,
          Integer.toString(numToSend),
        };
        Event eventToSend = eventFactory.getEventObject(args);
        // String step = this.al.next(dest);
        // TCPSender sender = this.activeConnections.get(step);
        String[] pathParts = path.trim().split(" ");
        TCPSender sender = this.activeConnections.get(pathParts[0]);
        sender.sendData(eventToSend.getBytes());
        this.sendTracker += 1;
      }
      dest = null;
    }
    this.sendTaskComplete();
  }

  private void sendTaskComplete() throws IOException {
    String[] args = {
      Integer.toString(TASK_COMPLETE),
    };
    EventFactory eventFactory = EventFactory.getInstance();
    Event event = eventFactory.getEventObject(args);
    String regKey = this.registryInet + ":" + this.registryPort;
    TCPSender sender = this.activeConnections.get(regKey);
    sender.sendData(event.getBytes());
  }

  public void onEvent(Event event) {
    try {
      int type = event.getType();
      switch (type) {
        case REGISTER_RESPONSE: this.handleRegisterResponse(event); break;
        case DEREGISTER_RESPONSE: this.handleDeregisterResponse(event); break;
        case LINK_WEIGHTS: this.handleLinkWeights(event); break;
        case TASK_INITIATE: this.handleTaskInitiate(event); break;
        case HELLO: this.handleHello(event); break;
        case MESSAGE: this.handleMessage(event); break;
        case PULL_TRAFFIC_SUMMARY: this.handleTrafficSummary(event); break;
      }
    } catch (IllegalArgumentException iae) {
      System.out.println(iae.getMessage());
    }
  }

  private void handleTrafficSummary(Event event) {
    if (event instanceof TaskSummaryRequest) {
      String[] args = {
        Integer.toString(TRAFFIC_SUMMARY),
        Integer.toString(this.sendTracker),
        Integer.toString(this.receiveTracker),
        Integer.toString(this.relayTracker),
        Long.toString(this.sendSummation),
        Long.toString(this.receiveSummation),
        Integer.toString(this.myPort),
      };
      try {
        EventFactory eventFactory = EventFactory.getInstance();
        Event traffic = eventFactory.getEventObject(args);
        String regKey = this.registryInet + ":" + this.registryPort;
        this.activeConnections.get(regKey).sendData(traffic.getBytes()); // send the response to the registry
        this.resetCounters();
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    } else throw new IllegalStateException("Incorrect event type. Expected: TaskSummaryRequest.");
  }

  private void handleMessage(Event event) {
    if (event instanceof Message) {
      Message msg = (Message) event;
      String path = msg.getPath();
      ArrayList<String> pathParts = new ArrayList<>(Arrays.asList(path.trim().split(" ")));
      pathParts.remove(0);
      if (pathParts.isEmpty()) { // message meant for me
        this.receiveTracker += 1;
        this.receiveSummation += msg.getNum();
        
      } else { // message meant for someone else
        try {
          // String dest = this.al.next(msg.getDest()); // get the next node towards the target
          this.relayTracker += 1;
          String next = pathParts.get(0);
          TCPSender sender = this.activeConnections.get(next); // get the associated sender object
          StringBuilder pathBuilder = new StringBuilder();
          for (String part : pathParts) {
            pathBuilder.append(" " + part);
          }
          String pathToSend = pathBuilder.toString().trim();
          // msg.SetPath(pathToSend);
          String[] args = {
            Integer.toString(MESSAGE),
            pathToSend,
            Long.toString(msg.getNum()),
          };
          EventFactory eventFactory = EventFactory.getInstance();
          Event relay = eventFactory.getEventObject(args);
          sender.sendData(relay.getBytes());
        } catch (IOException ioe) {
          System.out.println(ioe.getMessage());
        }
      }
    } else throw new IllegalStateException("Incorrect event type. Expected: Message.");
  }

  private void handleHello(Event event) {
    if (event instanceof Hello) {
      Hello hi = (Hello) event;
      String key = hi.getInet() + ":" + hi.getPort();
      try {
        TCPSender sender = new TCPSender(currentSocket);
        this.activeConnections.put(key, sender);
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    } else throw new IllegalStateException("Incorrect event type. Expected: Hello.");
  }

  private void handleTaskInitiate(Event event) {
    if (event instanceof TaskInitiate) {
      TaskInitiate ti = (TaskInitiate) event;
      try {
        this.start(ti.getRounds());
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    } else throw new IllegalStateException("Incorrect event type. Expected: TaskInitiate.");
  }

  private void handleLinkWeights(Event event) {
    if (event instanceof LinkWeights) {
      LinkWeights lw = (LinkWeights) event;
      this.linkWeights = new HashSet<>(Arrays.asList(lw.getLinks()));
      try {
        this.connectToWithLinks(linkWeights);
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
      this.createMST();
      System.out.println("Link weights received and processed. Ready to send messages.");
    } else throw new IllegalArgumentException("Incorrect event type. Expected: LinkWeights.");
  }

  private void handleRegisterResponse(Event event) throws IllegalArgumentException {
    if (event instanceof RegisterResponse) {
      RegisterResponse rr = (RegisterResponse) event;
      System.out.println(rr.getInfo());
    } else throw new IllegalArgumentException("Incorrect event type. Expected: RegisterResponse.");
  }

  private void handleDeregisterResponse(Event event) throws IllegalArgumentException {
    if (event instanceof DeregisterResponse) {
      DeregisterResponse dr = (DeregisterResponse) event;
      System.out.println(dr.getInfo());
    } else throw new IllegalArgumentException("Incorrect event type. Expected: DeregisterResponse");
  }

  public static void main(String[] args) {
    String registerInet = args[0];
    int registerPort = Integer.parseInt(args[1]);
    MessagingNode messagingNode = new MessagingNode(registerInet, registerPort);
    try {
      messagingNode.spawnServerThread();
      messagingNode.spawnCLIMonitor();
      messagingNode.register();
      while (true) {
        messagingNode.nextRequest();
      }
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (InterruptedException ie) {
      System.out.println(ie.getMessage());
    }
  }
}
