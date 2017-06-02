// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package codeu.chat.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.*;
import java.util.Collection;
import codeu.chat.util.Time;
import codeu.chat.util.*;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.store.StoreAccessor;
import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.Relay;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
//import codeu.chat.common.Uuids;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Timeline;
import codeu.chat.util.connections.Connection;

import java.sql.ResultSet; 
import java.sql.Statement; 
import java.sql.SQLException;
import java.sql.PreparedStatement;
//import codeu.chat.util.connections.Connection;


public final class Server {

  private static final Logger.Log LOG = Logger.newLog(Server.class);

  private static final int RELAY_REFRESH_MS = 5000;  // 5 seconds

  private final Timeline timeline = new Timeline();

  private final Uuid id;
  private final byte[] secret;

  private final Model model = new Model();
  private final View view = new View(model);
  private final Controller controller;

  private final Relay relay;
  private Uuid lastSeen = Uuid.NULL;

  //added database
  private final Database database = new Database(); 


  public Server(final Uuid id, final byte[] secret, final Relay relay) {

    this.id = id;
    this.secret = Arrays.copyOf(secret, secret.length);

    this.controller = new Controller(id, model);
    this.relay = relay;

    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.info("Reading update from relay...");

          for (final Relay.Bundle bundle : relay.read(id, secret, lastSeen, 32)) {
            onBundle(bundle);
            lastSeen = bundle.id();
          }

        } catch (Exception ex) {

          LOG.error(ex, "Failed to read update from relay.");

        }

        timeline.scheduleIn(RELAY_REFRESH_MS, this);
      }
    });
  }

  //Susan: transfers Users stored in the SQL database into the server database
  private void userDBTransfer() {
    //only consider updating server's user central database with sql data if the sql user database is not-empty

              StoreAccessor<Time, User> currUsers = model.userByTime();
              //start updating userlist in central database
              ResultSet result = database.getUsers();

          try {

              long firstTimeLong = result.getLong("CREATION");
            //  System.out.println(firstTimeLong);
              Time firstTime = Time.fromMs(firstTimeLong);
              //will return null if the central server db is empty...could not find a better way to check emptiness given the interface
              User firstUser = currUsers.first(firstTime);
              //update everything if db is empty in central server
                 if (firstUser == null) {
              // //iterate through list of existing users stored in database and update gui accordingly
            
                  while (result.next()) {
                      
                      String name = result.getString("NAME");
                      String stringId = result.getString("ID");
                      long longTime = result.getLong("CREATION");
                      byte[] hashedPass = result.getBytes("HASHEDPASS");
                      System.out.println(name);

                      Time creation = Time.fromMs(longTime);
                      //creation = creation.fromMs(longTime);
                      Uuid id = Uuid.parse(stringId);
                    
                      //adding the new user to the server database so the user sees it (when they press update)
                      controller.transferUser(id, name, creation, hashedPass);
                    }
                  }
                } catch (Exception e) {
                  LOG.error(e, "Exception while handling transfer from sql db to server db.");
                }
            
  }


  //Susan: Transfers messages from SQL db to server db
  private void convoDBTransfer(){

          StoreAccessor<Uuid, Conversation> currConvos = model.conversationById();
       
            try {
              ResultSet result = database.getConvos();
              String firstUuidString = result.getString("ID");
            //  System.out.println(firstTimeLong);
              Uuid firstId = Uuid.parse(firstUuidString);
              //will return null if the central server db is empty...could not find a better way to check emptiness given the interface
              Conversation firstConvo = currConvos.first(firstId);

              //update everything if db is empty in central server
              if (firstConvo == null) {

                // //iterate through list of existing users stored in database and update gui accordingly
                while (result.next()) {
                  //start updating userlist in central database

                    String stringOwner = result.getString("OWNER");
                    String stringId = result.getString("ID");
                    long longTime = result.getLong("CREATION");
                    String title = result.getString("TITLE");
                    Time creation = Time.fromMs(longTime);
                    //creation = creation.fromMs(longTime);
                    Uuid id = Uuid.parse(stringId);
                    Uuid owner = Uuid.parse(stringOwner);
                  
                    final Conversation conversation = controller.newConversation(id, title, owner, creation);
                }
              }
            } catch (Exception e) {
              LOG.error(e, "Exception while handling transfer of conversations from SQL to server db.");
            }
  }


  //Susan: transfers messages stored in the SQL database into the server database
  private void msgDBTransfer(){

        StoreAccessor<Uuid, Message> currMsgs = model.messageById();
        
          try {
            ResultSet result = database.getMsgs();
            String firstUuidString = result.getString("ID");
          //  System.out.println(firstTimeLong);
            Uuid firstId = Uuid.parse(firstUuidString);
            //will return null if the central server db is empty...could not find a better way to check emptiness given the interface
            Message firstMsg = currMsgs.first(firstId);

            //update everything if db is empty in central server
            if (firstMsg == null) {

              // //iterate through list of existing users stored in database and update gui accordingly
              while (result.next()) {
                //start updating userlist in central database

                  String stringAuthor = result.getString("AUTHOR");
                  String stringId = result.getString("ID");
                  String stringConvId = result.getString("CONVERSATION");
                  long longTime = result.getLong("CREATION");
                  String body = result.getString("BODY");

                  Time creation = Time.fromMs(longTime);
                  //creation = creation.fromMs(longTime);
                  Uuid id = Uuid.parse(stringId);
                  Uuid author = Uuid.parse(stringAuthor);
                  Uuid conversation = Uuid.parse(stringConvId);

                  final Message message = controller.transferedMessage(id, author, conversation, body, creation);
              }

            }
          } catch (Exception e) {
            LOG.error(e, "Exception while handling transfer of conversations from SQL to server db.");
          }
      
  }


  public void handleConnection(final Connection connection) {
    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.info("Handling connection...");

          // Susan : the following if statements handle the transfer of users, conversations, messages from SQL database to central server database
          // 

            if (!database.isTableEmpty("Users")) {
                LOG.info("Transferring users out of database.");
                userDBTransfer();
            }
            
            //Repeating above process for conversations
            if (!database.isTableEmpty("Convos")) {
                convoDBTransfer(); 
            }

            //Repeating above process for messages. First check is sql table for messages is empty
            if (!database.isTableEmpty("Msgs")) {
              msgDBTransfer();
            }

          final boolean success = onMessage(
              connection.in(),
              connection.out());

          LOG.info("Connection handled: %s", success ? "ACCEPTED" : "REJECTED");

        } catch (Exception ex) {

          LOG.error(ex, "Exception while handling connection.");

        }

        try {
          connection.close();
        } catch (Exception ex) {
          LOG.error(ex, "Exception while closing connection.");
        }

      }
    });
  }

  private boolean onMessage(InputStream in, OutputStream out) throws IOException {

    final int type = Serializers.INTEGER.read(in);

    if (type == NetworkCode.NEW_MESSAGE_REQUEST) {


      final Uuid author = Uuid.SERIALIZER.read(in);
      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final int pass = Serializers.INTEGER.read(in);
      final String content = Serializers.STRING.read(in);

      final Message message = controller.newMessage(author, conversation, pass, content);

      Serializers.INTEGER.write(out, NetworkCode.NEW_MESSAGE_RESPONSE);
      Serializers.nullable(Message.SERIALIZER).write(out, message);

      if (message != null) {
        database.addMessage(message.id, author, conversation, message.content, message.creation);
        timeline.scheduleNow(createSendToRelayEvent(
            author,
            conversation,
            message.id));
      }

    } else if (type == NetworkCode.NEW_USER_REQUEST) {

      final String name = Serializers.STRING.read(in);
      final String password = Serializers.STRING.read(in);

      final User user = controller.newUser(name, password);

      //add to SQL database
      database.addUser(user.id, user.creation, user.name, user.hashedPassword);

      Serializers.INTEGER.write(out, NetworkCode.NEW_USER_RESPONSE);
      Serializers.nullable(User.SERIALIZER).write(out, user);


    } else if (type == NetworkCode.NEW_CONVERSATION_REQUEST) {

      final String title = Serializers.STRING.read(in);
      final Uuid owner = Uuid.SERIALIZER.read(in);

      final Conversation conversation = controller.newConversation(title, owner);

      //add to Convo database
      database.addConvo(conversation.id, conversation.owner, conversation.creation, conversation.title);

      Serializers.INTEGER.write(out, NetworkCode.NEW_CONVERSATION_RESPONSE);
      Serializers.nullable(Conversation.SERIALIZER).write(out, conversation);

    } else if (type == NetworkCode.GET_USERS_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsers(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_BY_ID_RESPONSE);
      Serializers.collection(User.SERIALIZER).write(out, users);

    } else if (type == NetworkCode.GET_ALL_CONVERSATIONS_REQUEST) {

      final Collection<ConversationSummary> conversations = view.getAllConversations();

      Serializers.INTEGER.write(out, NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE);
      Serializers.collection(ConversationSummary.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Conversation> conversations = view.getConversations(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_MESSAGES_BY_ID_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Message> messages = view.getMessages(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_ID_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.GET_USER_GENERATION_REQUEST) {

      Serializers.INTEGER.write(out, NetworkCode.GET_USER_GENERATION_RESPONSE);
      Uuid.SERIALIZER.write(out, view.getUserGeneration());

    } else if (type == NetworkCode.GET_USERS_EXCLUDING_REQUEST) {

      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsersExcluding(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_EXCLUDING_RESPONSE);
      Serializers.collection(User.SERIALIZER).write(out, users);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TIME_REQUEST) {

      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Conversation> conversations = view.getConversations(startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TIME_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TITLE_REQUEST) {

      final String filter = Serializers.STRING.read(in);

      final Collection<Conversation> conversations = view.getConversations(filter);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TITLE_RESPONSE);
      Serializers.collection(Conversation.SERIALIZER).write(out, conversations);

    } else if (type == NetworkCode.GET_MESSAGES_BY_TIME_REQUEST) {

      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Message> messages = view.getMessages(conversation, startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_TIME_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.GET_MESSAGES_BY_RANGE_REQUEST) {

      final Uuid rootMessage = Uuid.SERIALIZER.read(in);
      final int range = Serializers.INTEGER.read(in);

      final Collection<Message> messages = view.getMessages(rootMessage, range);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE);
      Serializers.collection(Message.SERIALIZER).write(out, messages);

    } else if (type == NetworkCode.LOGIN_USER_REQUEST) {

      final String name = Serializers.STRING.read(in);
      final String password = Serializers.STRING.read(in);

      final int pass = controller.loginUser(name, password);

      Serializers.INTEGER.write(out, NetworkCode.LOGIN_USER_RESPONSE);
      Serializers.INTEGER.write(out, pass);

    } else if (type == NetworkCode.LOGOUT_USER_REQUEST) {

      final String name = Serializers.STRING.read(in);
      final int pass = Serializers.INTEGER.read(in);

      final boolean response = controller.logoutUser(name, pass);

      Serializers.INTEGER.write(out, NetworkCode.LOGOUT_USER_RESPONSE);
      Serializers.BOOLEAN.write(out, response);

    } else {

      // In the case that the message was not handled make a dummy message with
      // the type "NO_MESSAGE" so that the client still gets something.

      Serializers.INTEGER.write(out, NetworkCode.NO_MESSAGE);

    }

    return true;
  }

  private void onBundle(Relay.Bundle bundle) {

    final Relay.Bundle.Component relayUser = bundle.user();
    final Relay.Bundle.Component relayConversation = bundle.conversation();
    final Relay.Bundle.Component relayMessage = bundle.user();

    User user = model.userById().first(relayUser.id());

    if (user == null) {
      user = controller.transferUser(relayUser.id(), relayUser.text(), relayUser.time(), relayUser.hashedPassword());
    }

    Conversation conversation = model.conversationById().first(relayConversation.id());

    if (conversation == null) {

      // As the relay does not tell us who made the conversation - the first person who
      // has a message in the conversation will get ownership over this server's copy
      // of the conversation.
      conversation = controller.newConversation(relayConversation.id(),
                                                relayConversation.text(),
                                                user.id,
                                                relayConversation.time());
    }

    Message message = model.messageById().first(relayMessage.id());

    if (message == null) {
      message = controller.transferedMessage(relayMessage.id(),
                                      user.id,
                                      conversation.id,
                                      relayMessage.text(),
                                      relayMessage.time());
    }
  }

  private Runnable createSendToRelayEvent(final Uuid userId,
                                          final Uuid conversationId,
                                          final Uuid messageId) {
    return new Runnable() {
      @Override
      public void run() {
        final User user = view.findUser(userId);
        final Conversation conversation = view.findConversation(conversationId);
        final Message message = view.findMessage(messageId);
        relay.write(id,
                    secret,
                    relay.pack(user.id, user.name, user.creation, user.hashedPassword),
                    relay.pack(conversation.id, conversation.title, conversation.creation, null),
                    relay.pack(message.id, message.content, message.creation, null));
      }
    };
  }
}
