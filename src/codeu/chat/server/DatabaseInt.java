/**
* An interface for the database
*/
package codeu.chat.server;

import java.util.Collection;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RawController;
import codeu.chat.common.Time;
import codeu.chat.common.User;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.util.Logger;
 
import java.sql.*;

public interface DatabaseInt {

	
    /**
    * Returns boolean if successfully stored user 
    **/
   // public Boolean addConvo(Uuid id, Uuid owner, Time creation, String title);

    /**
    * Returns boolean if successfully stored user 
    **/
    public Boolean addUser(Uuid id, Time creation, String name);

    /**
    * Returns boolean if successfully stored message into conversation
    */
    //public Boolean addMessage(Message message, Conversation conversation);


    /**
    * Returns 
    **/
  //  public ClientConversation[] getConversations();

    /**
    * Returns Result list of users or None if operation was unsuccessful
    **/
    public ResultSet getUsers();
}

