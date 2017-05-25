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
import codeu.chat.common.Time;
import codeu.chat.common.User;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.util.Logger;
import java.sql.*;

//TODO: Figure out elegant way to close the conn?

public final class Database implements DatabaseInt {
	private final String url;
	private final Connection conn;

	public Database() {

		this.url = "jdbc:sqlite:data.db";

		Connection aconn = null;
        try {
            // db parameters
  
            //setting conn to be conenction
            aconn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite database has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } 

        this.conn = aconn;

		//initiatize user and conversation tables if they do not exist
		String userString = "CREATE TABLE if not exists Users" +
							  "(ID VARCHAR(254)," +
							  "NAME VARCHAR(254)," +
							  "CREATION BIGINT);";

		String convoString =  "CREATE TABLE if not exists Convos" +
								"(ID VARCHAR(254)," +
								"OWNER VARCHAR(254)," +
								"CREATION BIGINT," +
								"TITLE TEXT);";

		 try {
		      Statement stmt = conn.createStatement();
		      stmt.executeUpdate(convoString);
		      stmt.executeUpdate(userString);
		  //    stmt.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );

	    }
	    System.out.println("Table created successfully");
	}

	@Override
  	public Boolean addUser(Uuid id, Time creation, String name) {

  		try {
  			PreparedStatement preparedStat = conn.prepareStatement("INSERT INTO Users (ID, NAME, CREATION)" +
	  						  				 "VALUES (?, ?, ?);");
  											 long msTime = creation.inMs();
  											 String stringId = id.toString();
  											 stringId = stringId.replace("[UUID:","");
  											 stringId = stringId.replace("]","");
  											 System.out.println("isUuid? " + stringId);
	  						  				 preparedStat.setString(1, "'" + stringId + "'");
	  						  				 preparedStat.setString(2, name);
	  						  				 preparedStat.setLong(3, msTime);
	  						  				 int count = preparedStat.executeUpdate();
											 System.out.println("isUpdated? " + count);
			} catch (Exception e) {

				System.err.println("failed addUseupdate :"  + e.getMessage());
				return false;
			}

			return true;

  	}

  	@Override
  	public ResultSet getUsers() {
  		System.out.println("getUsers");

  			Statement statement = null;

  		try {

  			statement = conn.createStatement();
  			ResultSet result = statement.executeQuery("SELECT * FROM Users;");

  			// while(result.next()) {
  			// 	String name = result.getString("NAME");
  			// 	System.out.println(name);
  			// }
  		//	statement.close();
  			return result;
  			
			} catch (Exception e) {

				System.err.println("failed addUseupdate :"  + e.getMessage());
				System.exit(0);
			}

			return null;

  	}


}