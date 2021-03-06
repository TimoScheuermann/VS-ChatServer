package ChatSystem.Frontend.Frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import ChatSystem.Entities.Contact;
import ChatSystem.Entities.Contact.ContactType;
import ChatSystem.Entities.Message;
import ChatSystem.Entities.ServerMessage;
import ChatSystem.Frontend.ChatFrameListener;
import ChatSystem.Frontend.ChatManager;
import ChatSystem.Frontend.ComponentFactory;
import ChatSystem.Frontend.Emoji.Emoji;
import ChatSystem.Frontend.Emoji.EmojiChatManager;
import ChatSystem.Packets.SendMessagePacket;

@SuppressWarnings("serial")
public class ChatFrame extends JFrame {

	private Contact currentContact = null;
	private JLabel chatTitle = ComponentFactory.getLabel("Open a Chat", 203, 6, 300, 25);
	private JPanel contactWrapper = ComponentFactory.getPanel(null);
	private JTextPane chatPane = ComponentFactory.getTextPane();
	private JTextField textField = ComponentFactory.getTextField(278, 334, 211, 25);
	private ChatManager manager;
	private JLabel background = new JLabel(new ImageIcon("./assets/chat.png"));
	private JLabel spinner = ComponentFactory.getLabel("", 275, 175, 50, 50);
	private JLabel spinner_info = ComponentFactory.getLabel("Reconnecting...", 200, 225, 200, 50);

	/**
	 * Opens a new chat frame when client has received login confirmation
	 * 
	 * @param manager Chatmanager
	 */
	public ChatFrame(ChatManager manager) {

		this.manager = manager;

		setTitle("VS Chatsystem - Signed in as " + manager.user.name);
		setSize(600, 400);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		add(background);
		background.setLayout(null);

		background.add(ComponentFactory.getButton(new ImageIcon("./assets/contact.png"), false, 6, 6, 25, 25, (e) -> {
			manager.contactPopUp.open(false);
		}));

		background.add(chatTitle);

		JLabel contactsTitle = ComponentFactory.getLabel("Contacts", 3, 6, 186, 25);
		contactsTitle.setHorizontalAlignment(SwingConstants.CENTER);
		background.add(contactsTitle);

		background.add(ComponentFactory.getScrollPane(this.chatPane, 200, 40, 385, 285));

		this.contactWrapper.setLayout(new BoxLayout(this.contactWrapper, BoxLayout.Y_AXIS));
		background.add(ComponentFactory.getScrollPane(this.contactWrapper, 0, 35, 192, 327));

		spinner.setIcon(new ImageIcon("./assets/loading.gif"));
		spinner_info.setHorizontalAlignment(SwingConstants.CENTER);
		background.add(spinner);
		background.add(spinner_info);

		updateSpinner(false);
		setVisible(true);

		addWindowListener(new ChatFrameListener(manager));
	}

	/**
	 * send typed message to server
	 */
	public void sendMessage() {
		String message = textField.getText();

		// check if message is not null
		if (message.length() == 0) {
			return;
		}

		// send message and clear input
		manager.client.sendMessage(new ServerMessage("message",
				new SendMessagePacket(manager.user.getContact(), currentContact, message, true)));
		textField.setText("");
	}

	/**
	 * Insert Emoji to inputfield
	 * 
	 * @param emoji Emoji
	 */
	public void insertEmoji(Emoji emoji) {
		textField.setText(textField.getText() + ":" + emoji + ":");
	}

	/**
	 * remove all messages
	 */
	public void removeAllMessages() {
		this.chatPane.setText("");
	}

	/**
	 * insert messages
	 * 
	 * @param list List<Message>
	 */
	public void setMessages(List<Message> list) {
		list.stream().forEach(this::addMessage);
	}

	/**
	 * insert a message
	 * 
	 * @param m Message
	 */
	public void addMessage(Message m) {

		// Generate message's prefix
		String prefix = "";
		if (m.sender.type.equals(ContactType.SYSTEM)) {
			prefix = "[" + m.sender.name + "] ";
		} else if (m.receiver.type.equals(ContactType.GROUP) || true) {
			prefix = m.sender.name + ": ";
		}
		if (m.sender.equals(manager.user.getContact())) {
			prefix = "You: ";
		}

		// Determine color
		Color c = new Color(0, 136, 255);
		if (prefix.equalsIgnoreCase("You: ")) {
			c = new Color(37, 202, 73);
		} else if (m.sender.type.equals(ContactType.SYSTEM)) {
			c = new Color(231, 76, 60);
		}
		String msg = m.message + "\n";

		// Update chatPane
		chatPane.setEditable(true);

		// Set colors and insert prefix
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Alignment,
				StyleConstants.ALIGN_JUSTIFIED);
		aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Arial");

		aset = sc.addAttribute(aset, StyleConstants.Foreground, c);
		aset = sc.addAttribute(aset, StyleConstants.Bold, true);

		chatPane.setCaretPosition(chatPane.getDocument().getLength());
		chatPane.setCharacterAttributes(aset, false);
		chatPane.replaceSelection("\n[" + getDate(m.timestamp) + "] " + prefix);

		// Set colors and insert message
		aset = sc.addAttribute(aset, StyleConstants.Foreground, Color.WHITE);
		aset = sc.addAttribute(aset, StyleConstants.Bold, false);

		chatPane.setCaretPosition(chatPane.getDocument().getLength());
		chatPane.setCharacterAttributes(aset, false);
		chatPane.replaceSelection("\n" + msg);

		// Search for inserted emoji and replace it with its icon
		EmojiChatManager.changed(chatPane);
		chatPane.setEditable(false);

		// unselect chat
		chatPane.selectAll();
		int x = chatPane.getSelectionEnd();
		chatPane.select(x, x);
	}

	/**
	 * update contactlist
	 * 
	 * @param chatData
	 */
	public void updateContacts(HashMap<Contact, List<Message>> chatData) {
		contactWrapper.removeAll();
		for (Contact c : chatData.keySet()) {

			Message m = manager.getLatestMessageWith(c);

			if (m == null || m.sender == null)
				continue;
			String name = (m.sender.name.equals(manager.user.name) ? "You: " : "");

			contactWrapper.add(ComponentFactory.getContact(c.type.equals(ContactType.GROUP) ? c.shortName : c.name,
					name + m.message, false, c.equals(currentContact), (e) -> {
						manager.openChatWith(c);
					}));
			contactWrapper.add(ComponentFactory.getContactSpacer());
		}
	}

	/**
	 * open a chat
	 * 
	 * @param c Chat partner
	 */
	public void setChat(Contact c) {

		// First time a chat has been opened, add buttons, inputs, and chatpane
		if (this.currentContact == null) {
			background.add(
					ComponentFactory.getButton(new ImageIcon("./assets/addToGroup.png"), true, 555, 6, 25, 25, (e) -> {
						manager.contactPopUp.open(true);
					}));
			background.add(ComponentFactory.getLabel("Message", 203, 334, 70, 25));

			textField.addActionListener((e) -> {
				sendMessage();
			});
			background.add(textField);

			background.add(ComponentFactory.getButton("Send", true, 495, 334, 50, 25, (e) -> {
				sendMessage();
			}));

			background.add(ComponentFactory.getButton(new ImageIcon("./emojis/" + Emoji.GRINNING_FACE.name), false, 550,
					334, 25, 25, (e) -> {
						manager.emojiPopUp.open(this);
					}));
			background.updateUI();
		}

		// update chat partner and chat title
		this.currentContact = c;
		if (c.type.equals(ContactType.GROUP)) {
			chatTitle.setText("Group: " + c.shortName);
		} else {
			chatTitle.setText("Chatting with: " + c.name);
		}
		this.removeAllMessages();
	}

	/**
	 * Returns readable date
	 * 
	 * @param time timestamp
	 * @return String
	 */
	public String getDate(long time) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.YY HH:mm");
		return format.format(new Date(time));
	}

	/**
	 * Display spinner gif or not, disable input as needed
	 * 
	 * @param state visible
	 */
	public void updateSpinner(boolean state) {
		textField.setEditable(!state);
		spinner.setVisible(state);
		spinner_info.setVisible(state);
	}

}
