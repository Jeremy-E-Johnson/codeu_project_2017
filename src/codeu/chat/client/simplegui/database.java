/**
* An interface for the database
*/
package codeu.chat.client;

import codeu.chat.client.ClientConversation;
import codeu.chat.client.ClientMessage;
import codeu.chat.client.ClientUser;
import codeu.chat.client.Controller;
import codeu.chat.client.View;

public interface database {

	/**
	* Returns client context
	*/
    // public clientContext getContext();

    /**
    * Returns list of conversations given a context for the user
    **/
    public ClientConversation[] getConversations();

    /**
    * Returns list of users
    **/
    public ClientUser[] getUsers();
}

//user, date, message, message #?