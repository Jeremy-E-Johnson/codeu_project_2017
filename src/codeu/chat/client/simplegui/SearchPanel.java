package codeu.chat.client.simplegui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import codeu.chat.client.ClientContext;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Message;

@SuppressWarnings("serial")
public final class SearchPanel extends JPanel {

  private final DefaultListModel<String> messageListModel = new DefaultListModel<>();
  private final JLabel currentSearch = new JLabel("no search term", JLabel.RIGHT);
  private String sTerm = "";

  private final ClientContext clientContext;

  public SearchPanel(ClientContext clientContext) {
    super(new GridBagLayout());
    this.clientContext = clientContext;
    initialize();
  }

  /** Update function for conversation changes. */
  public void update(ConversationSummary owningConversation) {
    if(!sTerm.equals(""))
      searchAndDisplay(owningConversation, sTerm);
    else {
      currentSearch.setText("no search term");
      messageListModel.clear();
    }
  }

  /** Sets up JPanel objects within SearchPanel that produce,
   * from top to bottom, a Title bar, a panel for displaying
   * messeges, and a button bar.
   */
  private void initialize() {
    initializeTitle();
    initializeWindow();
    initializeButton();
  }

  private void initializeTitle() {
    // The title bar, also includes searched term.
    final JPanel titlePanel = new JPanel(new GridBagLayout());
    final GridBagConstraints titlePanelC = new GridBagConstraints();

    final JLabel titleLabel = new JLabel("Search", JLabel.LEFT);
    final GridBagConstraints titleLabelC = new GridBagConstraints();
    titleLabelC.gridx = 0;
    titleLabelC.gridy = 0;
    titleLabelC.anchor = GridBagConstraints.PAGE_START;

    final GridBagConstraints titleGapC = new GridBagConstraints();
    titleGapC.gridx = 1;
    titleGapC.gridy = 0;
    titleGapC.fill = GridBagConstraints.HORIZONTAL;
    titleGapC.weightx = 0.9;

    currentSearch.setAlignmentX(Component.RIGHT_ALIGNMENT);
    final GridBagConstraints currentSearchC = new GridBagConstraints();
    currentSearchC.gridx = 2;
    currentSearchC.gridy = 0;
    currentSearchC.anchor = GridBagConstraints.LINE_END;

    titlePanel.add(titleLabel, titleLabelC);
    titlePanel.add(Box.createHorizontalGlue(), titleGapC);
    titlePanel.add(currentSearch, currentSearchC);
    titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Place title bar at top of SearchPanel.
    titlePanelC.gridx = 0;
    titlePanelC.gridy = 0;
    titlePanelC.gridwidth = 10;
    titlePanelC.gridheight = 1;
    titlePanelC.fill = GridBagConstraints.HORIZONTAL;
    titlePanelC.anchor = GridBagConstraints.FIRST_LINE_START;

    this.add(titlePanel, titlePanelC);
  }

  private void initializeWindow() {
    // Message display window.
    final JPanel listShowPanel = new JPanel();
    final GridBagConstraints listPanelC = new GridBagConstraints();

    final JList<String> userList = new JList<>(messageListModel);
    userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    userList.setVisibleRowCount(15);
    userList.setSelectedIndex(-1);

    final JScrollPane userListScrollPane = new JScrollPane(userList);
    listShowPanel.add(userListScrollPane);
    userListScrollPane.setMinimumSize(new Dimension(500, 100));
    userListScrollPane.setPreferredSize(new Dimension(500, 100));

    // Place message window under title bar.
    listPanelC.gridx = 0;
    listPanelC.gridy = 1;
    listPanelC.gridwidth = 10;
    listPanelC.gridheight = 5;
    listPanelC.fill = GridBagConstraints.BOTH;
    listPanelC.anchor = GridBagConstraints.FIRST_LINE_START;
    listPanelC.weighty = 0.8;

    this.add(listShowPanel, listPanelC);
  }

  private void initializeButton() {
    // Button panel.
    final JPanel buttonPanel = new JPanel();
    final GridBagConstraints buttonPanelC = new GridBagConstraints();

    final JButton searchButton = new JButton("Search");
    buttonPanel.add(searchButton);

    // Place button panel at bottom of SearchPanel.
    buttonPanelC.gridx = 0;
    buttonPanelC.gridy = 7;
    buttonPanelC.gridwidth = 10;
    buttonPanelC.gridheight = 1;
    buttonPanelC.fill = GridBagConstraints.HORIZONTAL;
    buttonPanelC.anchor = GridBagConstraints.FIRST_LINE_START;

    this.add(buttonPanel, buttonPanelC);

    // Setup method for button click.
    // User clicks Search button - prompt for search term, search and display.
    searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!clientContext.user.hasCurrent()) {
          JOptionPane.showMessageDialog(SearchPanel.this, "You are not signed in.");
        } else if (!clientContext.conversation.hasCurrent()) {
          JOptionPane.showMessageDialog(SearchPanel.this, "You must select a conversation.");
        } else {
          final String searchTerm = (String) JOptionPane.showInputDialog(
              SearchPanel.this, "Enter search term:", "Search", JOptionPane.PLAIN_MESSAGE,
              null, null, "");
          if (searchTerm != null && searchTerm.length() > 0)
            SearchPanel.this.searchAndDisplay(clientContext.conversation.getCurrent(), searchTerm);
          }
        }
      }
    );
  }
  public void Query(ClientContext myContext, String myquery) {  
    ClientMessage message = myContext.message;
    ClientConversation convo = myContext.conversation;
    List<Message> contents = message.getConversationContents(convo.currentSummary);
    String[]searchWords = myquery.split(\p{Space}|\p{Punct});
    for (Message m : contents) {
        String[]tokens = m.content.split(\p{Space}|\p{Punct});
        for (String searchw : searchWords) {
          if (tokens.contains(searchw)) {

          }
        }
      }

      console.log
 }

  // TODO(Maitreyee) implement the search calls and remove stubby test search code.
  // Clears panel, sends search call, displays results, updates current search.
  private void searchAndDisplay(ConversationSummary conversation, String searchTerm) {
    currentSearch.setText("Looking for: '" + searchTerm + "'");
    sTerm = searchTerm;

    messageListModel.clear();

    // Here we need to call a search function that returns a ListViewable object of the messages to display.
    // This then replaces the conversation contents call in the for loop.

    for (final Message m : clientContext.message.getConversationContents(conversation)) {
      // Stubby inclusion check for search.
      if (m.content.toLowerCase().contains(searchTerm.toLowerCase())) {
        // Display author name if available.  Otherwise display the author UUID.
        final String authorName = clientContext.user.getName(m.author);

        final String displayString = String.format("%s: [%s]: %s",
            ((authorName == null) ? m.author : authorName), m.creation, m.content);

        messageListModel.addElement(displayString);
      }
    }
  }
}
