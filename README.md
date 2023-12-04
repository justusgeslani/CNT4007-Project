# CNT4007-Project

## Project Group 34
Team Members: Kevin Zhang, John Midel Miclat, Justus Geslani


### Starting the Peer Process
Our program is able to correctly read in Common.cfg and PeerInfo.cfg, storing the related variables to their respective positions.
The Common config is shared across all peers, and in our demo we are ablw to show that the PeerInfo is being read in differently for each peer ID.

In our demo, we clearly show that each peer will make TCP connections to all the peers that started before it, i.e., if 1005 joins last, it will connect to 1004, 1003, 1002, and 1001, etc.

When a peer is connected to at least one peer, it will try to exchange pieces as described in the protocol description section. However, the message type was not able to be correctly read in, so the peers were never able to start the process of file exchange.

Peer termination is implemented, however, it was not able to make in the demo, because the conditions, of completing file downloads, was never met.

### After Connection
Handshake messages are being sent to establish connection between two peers. Each of the peers of the connection sends to the other one a handshake message before sending each other messages. This functionality is portrayed in our demo.

In our demo, we also exchange the bitfield message, as well as sending either 'interested' or 'not interested' messages.

**Logging** is implemented, and shows the behavior of each peer. Through logging, we are able to see the incorrect behavior of the message type being 53, which is out of bounds of all the message types. 

### What did not work
In our program, we are not able to thoroughly implement file exchange. Therefore, we are not able to meet the 'stop service correctly' requirement, as well as correctly updating the corresponding peer's bitfields.

Choking/Unchoking and Optimistic Choking algorithm is also impelemented, but it was not thoroughly shown in the demo because of another issue, the incorrect message types, blocking it.

### Running the Project
Ensure that you are in the 'p2pFileSharing' directory.
Compile the project using `javac PeerDriver.java`
Run the code using `java PeerDriver ${peer_id)`, and replace the peer ID with the given configuration, e.g. `1001`, etc.
