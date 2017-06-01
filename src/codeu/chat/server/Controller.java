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

import java.util.Collection;
import java.util.Random;
import java.util.Arrays;

import codeu.chat.common.*;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import java.sql.*;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;


public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);


  private final Encryption encryption = new RsaAesSha();

  //database URL
  private static final String SERVER_URL = "jdbc:sqlite:data.db";

  private final Model model;
  private final Uuid.Generator uuidGenerator;
 // private final Database database;
  

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
    
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, int pass, String body) {
    return newMessage(createId(), author, conversation, pass, body, Time.now());
  }

  @Override
  public User newUser(String name, String password) {
    return newUser(createId(), name, Time.now(), password);

  }

  @Override
  public Conversation newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, int pass, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final Conversation foundConversation = model.conversationById().first(conversation);
    final int userPass = model.passBytext().get(foundUser.name);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id) && pass == userPass) {


      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);

      model.add(message);
      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {

        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;

      if (!foundConversation.users.contains(foundUser)) {
        foundConversation.users.add(foundUser.id);
      }
    }

    return message;
  }

  // We assume the Relay is being truthful and do not have a pass based security on new messages from it.
  public Message newRelayMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final Conversation foundConversation = model.conversationById().first(conversation);
    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) { //DO IT HERE

      message = new Message(id, Uuids.NULL, Uuids.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuids.equals(foundConversation.lastMessage, Uuids.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuids.equals(foundConversation.firstMessage, Uuids.NULL) ?
              message.id :
              foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;

      if (!foundConversation.users.contains(foundUser)) {
        foundConversation.users.add(foundUser.id);
      }
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime, String password) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime, password.getBytes());
      model.add(user, encryption.hash(password.getBytes()));

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  public User newHashedUser(Uuid id, String name, Time creationTime, byte[] hashedPassword) {

    User user = null;
    

    if (isIdFree(id)) {

      user = new User(id, name, creationTime, hashedPassword);
      model.add(user, hashedPassword);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public Conversation newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    Conversation conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new Conversation(id, owner, creationTime, title);
      model.add(conversation);

      LOG.info("Conversation added: " + conversation.id);
    }

    return conversation;
  }

  public int loginUser(String name, String password) {

    LOG.info(name + " " + password);

    byte[] trueHashedPassword = model.hashByText().first(name);
    int currentPass = model.passBytext().get(name);

    if(trueHashedPassword.equals(encryption.hash(password.getBytes()))) { LOG.info("Password and hashed pass match"); }

    if(encryption.hash(password.getBytes()).equals(encryption.hash(password.getBytes()))) { LOG.info("********************");}

    if (currentPass == 0 && Arrays.equals(trueHashedPassword, (encryption.hash(password.getBytes())))) {
      int newPass = createPass();
      model.setNewPass(name, newPass);
      LOG.info("Someone logged in.");
      return newPass;
    } else {
      return 0;
    }
  }

  public boolean logoutUser(String name, int pass) {

    int currentPass = model.passBytext().get(name);

    if(currentPass != 0 && pass == currentPass) {
      model.setNewPass(name, 0);
      LOG.info("Someone logged out.");
      return true;
    } else if (currentPass == 0) {
      LOG.info("Someone 'logged out' of an account that was not logged in.");
      return true;
    } else {
      return false;
    }
  }

  private int createPass() {
    Random rand = new Random();
    int upperBnd = 100000;
    int pass = rand.nextInt(upperBnd) + 1;
    return pass;
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

}
