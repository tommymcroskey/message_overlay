package csx55.overlay.wireformats;

public interface Protocol {

  int REGISTER_REQUEST = 1; // Register.java
  int REGISTER_RESPONSE = 2; // Register.java
  int DEREGISTER_REQUEST = 3; // Deregister.java
  int DEREGISTER_RESPONSE = 4; // Deregister.java
  int MESSAGING_NODES_LIST = 5; // MessagingNodesList.java
  int LINK_WEIGHTS = 6; // LinkWeights.java
  int TASK_INITIATE = 7; // TaskInitiate.java
  int TASK_COMPLETE = 8; // TaskComplete.java
  int PULL_TRAFFIC_SUMMARY = 9; // TaskSummaryRequest.java
  int TRAFFIC_SUMMARY = 10; // TaskSummaryResponse.java
  int HELLO = 11;
  int MESSAGE = 12;

}
