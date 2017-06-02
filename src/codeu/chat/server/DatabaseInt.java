/**
* An interface for the database
*/
package codeu.chat.server;

import java.util.Collection;
import java.util.*;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import java.sql.ResultSet; 
import java.sql.Statement; 
import java.sql.Connection;
import codeu.chat.util.*;

//The SQL database is initialized such that there is a table for: Users, Conversations, Messages
// This was to best mimick the stoarage of and attributes of the current stencil code's User, Conversation, and Message objects

public interface DatabaseInt {


    public Boolean isTableEmpty(String table);


    /**
    * Returns boolean if successfully stored user 
    **/
    public boolean addUser(Uuid id, Time creation, String name, byte[] hashedPass);


    /**
    * Returns boolean if successfully stored conversation
    */
   public boolean addConvo(Uuid id, Uuid owner, Time creation, String title);

    /**
    * Returns boolean if successfully stored message
    */
    public boolean addMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime);

    /**
    * Returns Result list of users or None if operation was unsuccessful
    **/
    public ResultSet getUsers();

    /**
    * Returns Result list of convos or None if operation was unsuccessful
    **/
    public ResultSet getConvos();

     /**
    * Returns Result list of messages or None if operation was unsuccessful
    **/
    public ResultSet getMsgs();
}

