/**
* A class that implements the DatabaseInt inteface
*/
package codeu.chat.server;

import java.util.Collection;
import codeu.chat.server.DatabaseInt;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.util.*;
import java.util.*;
import codeu.chat.util.Logger;
import java.sql.ResultSet; 
import java.sql.Statement; 
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.DriverManager;


//The SQL database is initialized such that there is a table for: Users, Conversations, Messages
// This was to best mimick the stoarage of and attributes of the current stencil code's User, Conversation, and Message objects


//SUSAN TODO: Figure out elegant way to close the conn? Handle signals?

public final class Database implements DatabaseInt {
	private final String url;
	private final Connection conn;
	// private boolean isUsersEmpty = false;
	// private boolean isConversationsEmpty = false;


	public Database() {

		this.url = "jdbc:sqlite:data.db";

		Connection aconn = null;
        try {
            // db parameters

            aconn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite database has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } 

        this.conn = aconn;

		//initiatize user and conversation tables if they do not exist
		String userString = "CREATE TABLE if not exists Users" +
							  "(ID VARCHAR(16)," +
							  "NAME VARCHAR(254)," +
							  "CREATION BIGINT);";

		String convoString =  "CREATE TABLE if not exists Convos" +
								"(ID VARCHAR(254)," +
								"OWNER VARCHAR(254)," +
								"CREATION BIGINT," +
								"TITLE VARCHAR(254));";

		String messageString =  "CREATE TABLE if not exists Msgs" +
								"(ID VARCHAR(254)," +
								"AUTHOR VARCHAR(254)," +
								"CONVERSATION VARCHAR(254)," +
								"BODY TEXT," +
								"CREATION BIGINT);";

		 try {
		      Statement stmt = conn.createStatement();
		      stmt.executeUpdate(convoString);
		      stmt.executeUpdate(userString);
		      stmt.executeUpdate(messageString);


		  //    stmt.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

	    }
	    System.out.println("Table created successfully");
	}

// @table input : name of the table in SQL database
// Returns boolean indicating if table is empty
	@Override
	public Boolean isTableEmpty(String table) {
		//return stuff based on booleans lol

  		try {
  			Statement statement = conn.createStatement();
  			ResultSet result = statement.executeQuery("SELECT * FROM " + table + ";");
  			return !result.next();
  		} catch (SQLException e) {
            System.out.println("error in finding emptiness" + e.getMessage());
        } 

        return null;
	}



//Adds user to SQL database
	@Override
  	public boolean addUser(Uuid id, Time creation, String name) {
  		try {
  			PreparedStatement preparedStat = conn.prepareStatement("INSERT INTO Users (ID, NAME, CREATION)" +
	  						  				 "VALUES (?, ?, ?);");
  											 long msTime = creation.inMs();
  											 String stringId = id.toString();
  											 stringId = stringId.replace("[UUID:","");
  											 stringId = stringId.replace("]","");
  											 System.out.println("isUuid? " + stringId);
	  						  				 preparedStat.setString(1, stringId);
	  						  				 preparedStat.setString(2, name);
	  						  				 preparedStat.setLong(3, msTime);
	  						  				 int count = preparedStat.executeUpdate();
											 System.out.println("isUpdated? " + count);
			} catch (Exception e) {
				System.err.println("failed addUserupdate :"  + e.getMessage());
				return false;
			}
			return true;
  	}


    //adds conversation to SQL databse
  	@Override
  	public boolean addConvo(Uuid id, Uuid owner, Time creation, String title) {
  		try { 
  			PreparedStatement preparedStat = conn.prepareStatement("INSERT INTO Convos (ID, OWNER, CREATION, TITLE)" +
	  						  				 "VALUES (?, ?, ?, ?);");
  											 long msTime = creation.inMs();
  											 String stringId = id.toString();
  											 stringId = stringId.replace("[UUID:","");
  											 stringId = stringId.replace("]","");
  											 String stringOwner = owner.toString();
  											 stringOwner = stringOwner.replace("[UUID:","");
  											 stringOwner = stringOwner.replace("]", "");
  											 //strigified version of uuid id
  											 preparedStat.setString(1, stringId);
  											 //stringified version of uuid string owner
  											 preparedStat.setString(2, stringOwner);
  											 // long version of creation
  											 preparedStat.setLong(3, msTime);
  											 //title enterred as is
  											 preparedStat.setString(4, title);
  											 int count = preparedStat.executeUpdate();
											 System.out.println("Convo added: " + count);
  		} catch (Exception e) {

				System.err.println("failed addConvoupdate :"  + e.getMessage());
				return false;
			}

		return true;
  	}


    //adds messages to SQL databse
  	@Override
  	public boolean addMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

  		try { 
  			PreparedStatement preparedStat = conn.prepareStatement("INSERT INTO Msgs (ID, AUTHOR, CONVERSATION, BODY, CREATION)" +
	  						  				 "VALUES (?, ?, ?, ?, ?);");
  											 long msTime = creationTime.inMs();
  											 String stringId = id.toString();
  											 stringId = stringId.replace("[UUID:","");
  											 stringId = stringId.replace("]","");
  											 String stringAuthor = author.toString();
  											 stringAuthor = stringAuthor.replace("[UUID:","");
  											 stringAuthor = stringAuthor.replace("]", "");
  											 String stringConvo = conversation.toString();
  											 stringConvo = stringConvo.replace("[UUID:","");
  											 stringConvo = stringConvo.replace("]", "");
  											 //strigified version of uuid id
  											 preparedStat.setString(1, stringId);
  											 //stringified version of uuid string owner
  											 preparedStat.setString(2, stringAuthor);
  											 //stringified convo id
  											 preparedStat.setString(3, stringConvo);
  											 //body enterred as is
  											 preparedStat.setString(4, body);
  											 // long version of creation
  											 preparedStat.setLong(5, msTime);
  											 
  											 int count = preparedStat.executeUpdate();
											 System.out.println("Convo added: " + count);
											
  		} catch (Exception e) {

				System.err.println("failed addConvoupdate :"  + e.getMessage());
				return false;
			}

		return true;
  	}

    // returns ResultSet of Users
  	@Override
  	public ResultSet getUsers() {
  		System.out.println("getUsers");

  		Statement statement = null;

  		try {

  			statement = conn.createStatement();
  			ResultSet result = statement.executeQuery("SELECT * FROM Users;");

  			return result;
  			
			} catch (Exception e) {

				System.err.println("failed to get User RS :"  + e.getMessage());
				System.exit(0);
			}
			return null;
  	}


    // returns ResultSet of Conversatio SQL table entries
  	@Override
    public ResultSet getConvos() {

    	try {

    		Statement statement = conn.createStatement();
    		ResultSet result = statement.executeQuery("SELECT * FROM Convos;");

    		return result;

    	} catch (SQLException e) {
    		System.err.println("failed to get Convo RS :"  + e.getMessage());
			System.exit(0);
    	}

    	return null;
    }


    // returns ResultSet of Msg SQL table entries
  	@Override
  	public ResultSet getMsgs() {

  		Statement statement = null;

  		try {
  			statement = conn.createStatement();
  			ResultSet result = statement.executeQuery("SELECT * FROM Msgs;");

  			return result;
  			
			} catch (Exception e) {

				System.err.println("failed to get Message RS :"  + e.getMessage());
				System.exit(0);
			}

			return null;
  	}


}