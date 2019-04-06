# chatalk
ChaTalk is a chatting desktop application based on Java socked programming.
ChatTalk is divided in two applications: The server and the client. Both applications communicate with each other through a TCP connection.

|            Command            |                          Description                         |
|:-----------------------------:|:------------------------------------------------------------:|
|  login <username> <password>  |        Allows the user to login using his credentials        |
| message <recipient> <content> |   Allows the user to send a private message to another user  |
|      broadcast <content>      |        Allows the user to send a message to all users        |
|          whoseonline          |                 Returns all the online users                 |
|           WhoLastHr           |    Returns the users that were online during the past hour   |
|          block <user>         | User will not receive messages from the blocked user anymore |
|         ublock <user>         |            Allows a user to unblock a blocked user           |
|             logout            |                   Allows the user to logout                  |
|   reg <username> <password>   |            Allows the user to create a new account           |
