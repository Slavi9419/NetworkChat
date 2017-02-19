package MyChat.Chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class NetworkClient extends ButtonGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedReader in;
	private PrintWriter out;
	private JFrame frame = new JFrame("Academic");
	private JPanel messagePanel = new JPanel();
	private JTextPane textPane = new JTextPane();
	private JButton emojiBtn = emojiBtn();
	private JTextField textField = getField();
	private JScrollPane messageScrolPane;
	private JPanel emojiPanel = getEmojiPanel();
	private JScrollPane emojiScroolPane = getEmojiScrollPanel();
	private String myName;
	private String myPassword;
	private Socket socket;
	private boolean isMinimize = false;
	private JPanel frendPanel = getFrendPanel();
	private JButton frendsBtn = getFrendsBtn();
	private JButton findFrendsBtn = getFindFrendsBtn();
	private JButton notificationBtn = getNotificationBtn();
	private JTextPane frendPane = getFrendsPane();
	private final int emojiConst = 60;
	private final String serverAdress = "localhost";
	private JButton fixEmojiBtn = new JButton();
	private StringBuilder sendMessageTo = new StringBuilder();
	private ArrayList<String> notificationRequestList = new ArrayList<String>();
	private String[] myFriendsArray;
	private String myFriends = "null";

	public NetworkClient() {
		frendPanel.add(findFrendsBtn);
		frendPanel.add(frendsBtn);
		frendPanel.add(notificationBtn);
		frendPanel.add(frendPane);
		messagePanel.setLayout(new BorderLayout());
		textPane.setEditable(false);
		textField.setEnabled(false);
		messageScrolPane = new JScrollPane(textPane);
		messageScrolPane.setPreferredSize(new Dimension(350, 400));
		messageScrolPane.setMinimumSize(new Dimension(350, 400));
		messageScrolPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		messagePanel.add(messageScrolPane, BorderLayout.NORTH);
		messagePanel.add(textField, BorderLayout.CENTER);
		messagePanel.add(emojiBtn, BorderLayout.EAST);
		frame.getContentPane().add(messagePanel, "Center");
		frame.getContentPane().add(frendPanel, "West");
		frame.getContentPane().add(emojiScroolPane, "South");
		frame.pack();
		frame.setResizable(false);
		frame.setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/logo.png")).getImage());
		frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent arg0) {
				if (isMinimize == false) {
					isMinimize = true;
				} else {
					isMinimize = false;
				}
			}
		});
		fixEmojiBtn.setPreferredSize(new Dimension(0, 0));
	}

	private JPanel getEmojiPanel() {
		JPanel emojiPanel = new JPanel();
		for (int i = 1; i <= emojiConst; i++) {

			final JButton jb = new JButton();
			if (i < 10) {
				jb.setName("emoji0" + Integer.toString(i));
			} else {
				jb.setName("emoji" + Integer.toString(i));
			}
			jb.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/i" + i + ".png")));
			jb.setBackground(Color.WHITE);
			jb.setPreferredSize(new Dimension(30, 30));
			jb.addActionListener(new ActionListener() {

				
				public void actionPerformed(ActionEvent e) {
					out.println("EMOJI" + sendMessageTo + jb.getName());
					if (sendMessageTo.length() > 0) {
						appendMyEmoji(jb.getName());
					} else {
						textField.setEnabled(false);
					}
				}
			});
			Border emptyBorder = BorderFactory.createCompoundBorder();
			jb.setBorder(emptyBorder);
			emojiPanel.add(jb);
		}
		emojiPanel.setPreferredSize(new Dimension(540, 140));
		emojiPanel.setBackground(Color.WHITE);
		return emojiPanel;
	}

	public JScrollPane getEmojiScrollPanel() {
		JScrollPane emojiScroolPane = new JScrollPane(emojiPanel);
		emojiScroolPane.setViewportView(emojiPanel);
		emojiScroolPane.setPreferredSize(new Dimension(540, 110));
		emojiScroolPane.setMinimumSize(new Dimension(540, 110));
		emojiScroolPane.setMaximumSize(new Dimension(540, 110));
		emojiScroolPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return emojiScroolPane;
	}

	private JButton getFindFrendsBtn() {
		JButton findFrendsBtn = new JButton();
		findFrendsBtn.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent arg0) {
				sendMessageTo.setLength(0);
				out.println("FINDFRIEND");

			}
		});
		findFrendsBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/addFriendIcon.png")));
		findFrendsBtn.setPreferredSize(new Dimension(55, 49));
		Border emptyBorder = BorderFactory.createEmptyBorder();
		findFrendsBtn.setBorder(emptyBorder);
		return findFrendsBtn;
	}

	private JButton getFrendsBtn() {
		JButton frendsBtn = new JButton();
		frendsBtn.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent e) {
				sendMessageTo.setLength(0);
				out.println("GETFRIEND");
			}
		});
		frendsBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/friends.png")));
		frendsBtn.setPreferredSize(new Dimension(55, 55));
		Border emptyBorder = BorderFactory.createEmptyBorder();
		frendsBtn.setBorder(emptyBorder);
		return frendsBtn;
	}

	private JTextPane getFrendsPane() {
		JTextPane frendsArea = new JTextPane();
		frendsArea.setPreferredSize(new Dimension(180, 340));
		frendsArea.setMinimumSize(new Dimension(180, 340));
		frendsArea.setEditable(false);
		return frendsArea;
	}

	private JPanel getFrendPanel() {
		JPanel frendpanel = new JPanel();
		frendpanel.setPreferredSize(new Dimension(180, 300));
		frendpanel.setMinimumSize(new Dimension(180, 300));
		frendpanel.setMaximumSize(new Dimension(180, 300));
		frendpanel.setBackground(Color.WHITE);
		return frendpanel;
	}

	private JTextField getField() {
		final JTextField textField = new JTextField();
		textField.setPreferredSize(new Dimension(22, 35));
		textField.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent e) {
				if (textField.getText().equals("")) {
					return;
				}
				out.println("MESSAGE" + sendMessageTo + textField.getText());
				if (sendMessageTo.length() > 0) {
					appendMyMsg(textField.getText());
				} else {
					textField.setEnabled(false);
				}
				textField.setText("");
			}
		});

		return textField;
	}

	private String getUsername() {
		do {
			myName = JOptionPane.showInputDialog(frame, "Enther your name", "Screen name selection",
					JOptionPane.PLAIN_MESSAGE);
		} while (myName.equals(""));
		return myName;
	}

	private String createNewAcount() {
		JTextField username = new JTextField();
		JTextField password = new JPasswordField();
		Object[] message = { "Username:", username, "Password:", password };
		int option = JOptionPane.showConfirmDialog(frame, message, "Login", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			String acaountData = username.getText() + "," + password.getText();
			return acaountData;
		}
		return "-1";
	}

	private String getPassword() {
		do {
			myPassword = JOptionPane.showInputDialog(frame, "Enther your password", "Screen name selection",
					JOptionPane.PLAIN_MESSAGE);
		} while (myPassword.equals(""));

		return myPassword;
	}

	private JButton emojiBtn() {
		JButton emojiBtn = new JButton();
		emojiBtn.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent e) {
				out.println("EMOJI" + sendMessageTo + "$iemoji");
				appendMyEmoji("$iemoji");

			}
		});
		emojiBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/$emojiBtnIcon.png")));
		emojiBtn.setBackground(Color.white);
		emojiBtn.setPreferredSize(new Dimension(40, 40));
		return emojiBtn;
	}

	private JButton getNotificationBtn() {
		final JButton ntfBtn = new JButton();
		ntfBtn.setPreferredSize(new Dimension(55, 55));
		Border emptyBorder = BorderFactory.createEmptyBorder();
		ntfBtn.setBorder(emptyBorder);
		ntfBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/ntfBtn.png")));
		ntfBtn.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent arg0) {
				frendPane.setText("");
				StyledDocument doc = frendPane.getStyledDocument();
				Style style = doc.addStyle("StyleName", null);

				try {
					if (notificationRequestList.isEmpty()) {
						out.println("CHECKREQUEST");
					}
					for (int i = notificationRequestList.size() - 1; i >= 0; i--) {

						final JButton acceptBtn = new JButton("Accept");
						final JButton dellBtn = new JButton("Delete");
						acceptBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/confimBtn.png")));
						acceptBtn.setPreferredSize(new Dimension(2, 25));
						acceptBtn.setBackground(Color.white);
						acceptBtn.setName(notificationRequestList.get(i).substring(0,
								notificationRequestList.get(i).length() - 17));
						dellBtn.setName(notificationRequestList.get(i).substring(0,
								notificationRequestList.get(i).length() - 17));
						dellBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/delBtn.png")));
						dellBtn.setPreferredSize(new Dimension(2, 25));
						dellBtn.setBackground(Color.white);

						acceptBtn.addActionListener(new ActionListener() {

							
							public void actionPerformed(ActionEvent e) {
								out.println("ACCEPTREQUEST" + acceptBtn.getName());
								acceptBtn.setEnabled(false);
								dellBtn.setEnabled(false);
								notificationRequestList = new ArrayList<String>();
							}
						});

						dellBtn.addActionListener(new ActionListener() {

							
							public void actionPerformed(ActionEvent e) {
								out.println("DELETEREQUEST" + dellBtn.getName());
								acceptBtn.setEnabled(false);
								dellBtn.setEnabled(false);
								notificationRequestList = new ArrayList<String>();
							}
						});
						doc.insertString(doc.getLength(), notificationRequestList.get(i), style);
						frendPane.insertComponent(acceptBtn);
						frendPane.insertComponent(dellBtn);
						doc.insertString(doc.getLength(), "\n \n", style);
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				ntfBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/ntfBtn.png")));

			}
		});
		return ntfBtn;
	}

	private void playMsgSound() {
		try {

			AudioInputStream stream;
			AudioFormat format;
			DataLine.Info info;
			Clip clip;

			stream = AudioSystem.getAudioInputStream(this.getClass().getClassLoader().getResource("notificationSound/msgSound.wav"));
			format = stream.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(stream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setAllUsersInPane(String[] users) {
		frendPane.setText("");
		StyledDocument doc = frendPane.getStyledDocument();
		Style style = doc.addStyle("StyleName", null);
		for (int i = 0; i < users.length; i++) {
			if (!users[i].equals(myName) && !myFriends.contains(users[i])) {
				final JButton jb = new JButton(users[i]);
				jb.setBackground(Color.WHITE);
				Border emptyBorder = BorderFactory.createRaisedBevelBorder();
				jb.setBorder(emptyBorder);
				jb.setName(users[i]);
				jb.setPreferredSize(new Dimension(250, 25));
				jb.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/addUser.png")));
				jb.addActionListener(new ActionListener() {

					
					public void actionPerformed(ActionEvent e) {
						int i = JOptionPane.showConfirmDialog(frame, "Add " + jb.getName() + " to frends?", "?",
								JOptionPane.YES_NO_OPTION);
						if (i == 0) {
							out.println("ADDFRIENT" + jb.getName());
						}
					}
				});
				frendPane.insertComponent(jb);
				try {
					doc.insertString(doc.getLength(), "\n", style);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void setFriendInFriendPane(String friends) {
		frendPane.setText("");
		myFriends = friends;
		if (myFriends.startsWith("�dd friends")) {
			frendPane.setText(myFriends);
			return;
		}
		myFriendsArray = myFriends.split(",");
		for (int i = 0; i < myFriendsArray.length; i++) {
			final JToggleButton jb = new JToggleButton(myFriendsArray[i]);
			jb.setBackground(Color.WHITE);
			Border emptyBorder = BorderFactory.createLoweredBevelBorder();
			jb.setBorder(emptyBorder);
			jb.setName(myFriendsArray[i]);
			jb.setPreferredSize(new Dimension(100, 25));
			jb.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/user.png")));
			add(jb);
			jb.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					int key = e.getButton();
					if (key == 1) {
						setSelected(jb.getModel(), true);
						textField.setEnabled(true);
						sendMessageTo.setLength(0);
						sendMessageTo.append(jb.getName() + ",");
						//UIManager.put("ToggleButton.select", Color.white);
						//SwingUtilities.updateComponentTreeUI(jb);

					} else if (key == 3) {
						int i = JOptionPane.showConfirmDialog(frame, "Delete " + jb.getName() + " from frends?", "?",
								JOptionPane.YES_NO_OPTION);
						if (i == 0) {
							deleteFriend(jb.getName());
							frendsBtn.doClick();
						}
					}
				}
			});
			frendPane.insertComponent(jb);
			StyledDocument doc = frendPane.getStyledDocument();
			Style style = doc.addStyle("StyleName", null);

			try {
				doc.insertString(doc.getLength(), "\n", style);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}

		}
	}

	private void deleteFriend(String friend) {
		out.println("DELFRIEND" + friend);
	}

	private void notificationRequest(String notification) {
		if (notification.length() <= 1) {
			return;
		}
		notificationBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("btnIcons/ntfBtnR.png")));
		notificationRequestList.add(notification + "\n");
	}

	private void run() throws UnknownHostException, IOException, BadLocationException {
		socket = new Socket(serverAdress, 1234);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		while (true) {

			String line = in.readLine();

			if (line.startsWith("SUBMITNAME")) {
				out.println(getUsername());
			} else if (line.startsWith("CONFIMPASSWORD")) {
				out.println(getPassword());
			} else if (line.startsWith("UNLUCKBAR")) {
				textField.setEnabled(true);
			} else if (line.startsWith("MESSAGE")) {
				appendMsgFromUser(line.substring(7) + "\n");
			} else if (line.startsWith("EMOJI")) {
				sendEmoji(line);
			} else if (line.startsWith("TIME")) {
				sendChatTime(line);
			} else if (line.startsWith("CREATEACOUNT")) {
				out.println(createNewAcount());
			} else if (line.startsWith("ACCOUNTCREATED")) {
				JOptionPane.showMessageDialog(frame, "Account created");
			} else if (line.startsWith("FAILURE")) {
				JOptionPane.showMessageDialog(frame, "FAILURE");
			} else if (line.startsWith("PASSWORDINCORRECT")) {
				JOptionPane.showMessageDialog(frame, "PASSWORD INCORRECT");
			} else if (line.startsWith("FRIEND")) {
				setFriendInFriendPane(line.substring(6));
			} else if (line.startsWith("ALLUSERS")) {
				setAllUsersInPane(line.substring(8).split(","));
			} else if (line.startsWith("REQUEST")) {
				notificationRequest(line.substring(7));
			}
		}
	}

	private void sendChatTime(String line) throws BadLocationException {
		line = line.substring(4);
		StyledDocument doc = (StyledDocument) textPane.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		StyleConstants.setForeground(center, Color.orange);
		doc.setParagraphAttributes(doc.getLength(), 1, center, false);
		doc.insertString(doc.getLength(), line, center);
		doc.insertString(doc.getLength(), "\n", center);

	}

	private void appendMyEmoji(String emoji) {

		StyledDocument doc = (StyledDocument) textPane.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);

		doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		if (emoji.equals("emoji32") || emoji.equals("$iemoji")) {
			StyleConstants.setIcon(right, new ImageIcon(this.getClass().getClassLoader().getResource("emojiIcons/"+emoji.trim() + ".gif")));
		} else {
			StyleConstants.setIcon(right, new ImageIcon(this.getClass().getClassLoader().getResource("emojiIcons/"+emoji.trim() + ".png")));
		}
		try {
			doc.insertString(doc.getLength(), "\n", right);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textPane.remove(fixEmojiBtn);
	}

	private void sendEmoji(String line) {

		StyledDocument doc = (StyledDocument) textPane.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);

		String nameAndEmoji = line.substring(5).trim();
		Style style = doc.addStyle("StyleName", null);

		String emoji = nameAndEmoji.substring(nameAndEmoji.length() - 7, nameAndEmoji.length()).trim();
		String name = nameAndEmoji.substring(0, nameAndEmoji.length() - 7).trim();
		StyleConstants.setForeground(left, Color.GRAY);
		doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), name + " ", style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		if (emoji.equals("emoji32") || emoji.equals("$iemoji")) {
			StyleConstants.setIcon(left, new ImageIcon(this.getClass().getClassLoader().getResource("emojiIcons/"+emoji + ".gif")));
		} else {
			StyleConstants.setIcon(left, new ImageIcon(this.getClass().getClassLoader().getResource("emojiIcons/"+emoji + ".png")));
		}
		textPane.insertComponent(fixEmojiBtn);
		try {
			doc.insertString(doc.getLength(), "\n", left);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		textPane.remove(fixEmojiBtn);
		if (isMinimize) {
			playMsgSound();
		}
	}

	private void appendMyMsg(String message) {
		message = message + "\n";
		StyledDocument doc = textPane.getStyledDocument();

		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(right, Color.BLUE);
		try {
			doc.setParagraphAttributes(doc.getLength(), 1, right, false);
			doc.insertString(doc.getLength(), message, right);
			StyleConstants.setForeground(right, Color.GRAY);
		} catch (BadLocationException e) {
			e.printStackTrace();

		}
	}

	public void appendMsgFromUser(String line) {
		StyledDocument doc = textPane.getStyledDocument();

		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.GRAY);
		try {
			doc.setParagraphAttributes(doc.getLength(), 1, left, false);
			doc.insertString(doc.getLength(), line, left);
		} catch (BadLocationException e) {
			e.printStackTrace();

		} finally {
			if (isMinimize) {
				playMsgSound();
			}
		}

		textPane.setEditable(false);

	}

	public static void main(String[] args) throws UnknownHostException, IOException, BadLocationException {
		NetworkClient client = new NetworkClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.frame.setLocationRelativeTo(null);
		client.run();
	}
}
