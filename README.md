# chatalk
ChaTalk is a chatting desktop application based on Java socket programming.
ChatTalk is divided in two applications: The server and the client. Both applications communicate with each other through a TCP connection.

## How it works
The server application is a multithreading application that listens for new connections from the client on port 6789. Each time a new connection is requested by the client, it is initiated on a new thread which allows the server to support multiple clients at the same time.
All information sent by the client to the server such as messages, credentials and so on are saved by the server in a JSON file.

The client application is also a multithreading application that allows the client to connect to multiple servers at the same time.

### List of commands

|            Command            |                          Description                         |
|:-----------------------------:|:------------------------------------------------------------:|
|  login &lt;username> &lt;password&gt;  |        Allows the user to login using his credentials        |
| message &lt;recipient> &lt;content&gt; |   Allows the user to send a private message to another user  |
|      broadcast &lt;content&gt;     |        Allows the user to send a message to all users        |
|          whoseonline          |                 Returns all the online users                 |
|           WhoLastHr           |    Returns the users that were online during the past hour   |
|          block &lt;user&gt;        | User will not receive messages from the blocked user anymore |
|         ublock &lt;user&gt;         |            Allows a user to unblock a blocked user           |
|             logout            |                   Allows the user to logout                  |
|   reg &lt;username> &lt;password&gt;   |            Allows the user to create a new account           |
  
  ## Video demo
