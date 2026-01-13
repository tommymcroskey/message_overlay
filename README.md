# message_overlay
## Description:

This is a class project for CS455 Distributed Systems. The goal of the assignment is to create a k-regular network of nodes, whose edges are sustained TCP connections for the duration of the program execution. MessagingNodes send register requests to the registry. Once all nodes have been registered/deregistered as desired on the RT CLI, the registry can send a request to all MessagingNodes currently registered to connect to a set of other registered nodes, first initializing the k-regular network. Each link is established with a seeded random weight, which is then used to compute an MST overlay and sent to the MessagingNodes, once the registry CLIThread is given the command to do so. When the registry finally receives the start command from the CLI, it sends a TaskInitiate message to all registered nodes. The MessagingNodes send messages along the MST according to the directive and finally send back message diagnostics to the registry, where it is finally displayed to the user.



This project was a fun exploration of parallel and network programming. There are many improvements which can be made in a later attempt, when I migrate this to C++ for performance comparison, especially when using manual memory management.
