package MyChat.Chat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.sql.*;

public class ChatServer {

	public static final int port = 1234;
	static Date messageDate = new Date();
int i;
int c;
	public static HashMap<String, String> names = new HashMap<String, String>();

	public static HashMap<String, PrintWriter> customWriters = new HashMap<String, PrintWriter>();

	public static ArrayList<String> namesAndMsg = new ArrayList<String>();

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket0 = new ServerSocket(port);

		System.out.println("Server is run");
		try {
			while (true) {
				new Handler(serverSocket0.accept()).start();
			}
		} finally {
			serverSocket0.close();

		}

	}

	public static class Handler extends Thread {
		private String myId;
		private String myName;
		private String myPassword;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private int timeConst = 30;
		private Statement myStatement;
		private Connection myConnection;
		private ResultSet myResult;
		private PreparedStatement pstms;
		private StringBuilder frendsIdBuilder = new StringBuilder();
		private String userID;

		public Handler(Socket socket) {
			this.socket = socket;
			messageDate = new Date();
		}

		private synchronized void sendTextMessage(Object input) {
			input = input.toString().substring(7);
			String msg = input.toString();
			namesAndMsg = dividingMessage(msg);

			for (int i = 0; i < namesAndMsg.size() - 1; i++) {
				String chater = namesAndMsg.get(i);
				sendChatTime(chater);
				customWriters.get(chater).println("MESSAGE" + myName + ": " + namesAndMsg.get(namesAndMsg.size() - 1));
			}
		}

		/**
		 * @param input
		 */
		private synchronized void sendEmojiMessage(String input) {
			String msg = input.substring(5);
			namesAndMsg = dividingMessage(msg);
			for (int i = 0; i < namesAndMsg.size() - 1; i++) {
				String chater = namesAndMsg.get(i);
				sendChatTime(chater);
				customWriters.get(chater).println("EMOJI" + myName + ": " + namesAndMsg.get(namesAndMsg.size() - 1));
			}

		}

		private void sendChatTime(String chater) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			String timeStr = sdf.format(cal.getTime());
			if (new Date().getTime() / 1000 - messageDate.getTime() / 1000 >= timeConst) {
				customWriters.get(chater).println("TIME" + " " + timeStr);
			}
		}

		private ArrayList<String> dividingMessage(String chatersAndMessage) {
			namesAndMsg = new ArrayList<String>();
			String[] dividingArr = chatersAndMessage.split(",");
			for (int i = 0; i < dividingArr.length; i++) {
				namesAndMsg.add(dividingArr[i]);
			}
			return namesAndMsg;
		}

		public void connectingToDataBase() {
			try {

				Class.forName("com.mysql.jdbc.Driver");
				myConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/network_chat", "root", "");
				myStatement = myConnection.createStatement();

			} catch (SQLException e) {
				System.out.println("Error" + e);
			} catch (ClassNotFoundException e) {
				System.out.println("Error" + e);
			}

		}

		public String getNameFromDataBase(String name) {
			try {
				String query = "select * from users";
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					if (myResult.getString("username").equals(name)) {
						myPassword = myResult.getString("password");
						myId = myResult.getString("id");
						return name;
					}

				}
			} catch (Exception ex) {
				System.out.println("Error " + ex);
			}
			return "-1";
		}

		public void insertUserInDataBase(String userData) {
			String[] userDataArr = userData.split(",");
			String query = "INSERT INTO users (username,password)VALUES(?, ?)";
			try {
				pstms = myConnection.prepareStatement(query);

				pstms.setString(1, userDataArr[0]);
				pstms.setString(2, userDataArr[1]);
				int i = pstms.executeUpdate();
				if (i > 0) {
					out.println("ACCOUNTCREATED");
				} else {
					out.print("FAILURE");
				}
			} catch (SQLException e) {
				System.out.println("Error" + e);
			}
		}

		public boolean acceptPassword(String password) {
			if (password.equals(myPassword)) {
				return true;
			}
			return false;
		}

		private void getDataBaseFriendID() {
			try {
				String query = "select id,UserID,FriendID from users  JOIN friends ON id=UserID and friendshipStatus=0 WHERE UserID="
						+ myId;
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					frendsIdBuilder.append(myResult.getString("FriendID"));
					frendsIdBuilder.append(",");
				}
				getFriends(frendsIdBuilder.toString());
			} catch (Exception ex) {
				System.out.println("Error " + ex);
			} finally {
				frendsIdBuilder.setLength(0);
			}

		}

		private String getFriends(String id) {
			String[] frendsIdArray = id.split(",");
			StringBuilder query = new StringBuilder();
			StringBuilder friendsBuilder = new StringBuilder();
			query.append("select * from users where users.id=" + frendsIdArray[0]);
			try {
				for (int i = 1; i < frendsIdArray.length; i++) {
					query.append(" or users.id=" + frendsIdArray[i]);
				}
				myResult = myStatement.executeQuery(query.toString());
				while (myResult.next()) {
					friendsBuilder.append(myResult.getString("username") + ",");
				}
				out.println("FRIEND" + friendsBuilder.toString());
			} catch (Exception ex) {
				out.println("FRIEND" + "�dd friends");
			}
			return "";
		}

		private String getAllUsersFromDataBase() {
			StringBuilder allUsersBuilder = new StringBuilder();
			String query = "select username from users";
			try {
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					allUsersBuilder.append(myResult.getString("username"));
					allUsersBuilder.append(",");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return allUsersBuilder.toString();
		}

		private void insertFrientToDataBase(String name) {
			String query = "select username,id from users";
			try {
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					if (myResult.getString("username").equals(name)) {
						userID = myResult.getString("id");
						break;
					}

				}
				int i = 0;
				query = "INSERT INTO friends (UserID,FriendID,friendshipStatus,friendRequest,chronology)VALUES(?,?,?,?,?)";
				while (i <= 1) {
					if (i == 0) {
						pstms = myConnection.prepareStatement(query);
						pstms.setInt(1, Integer.parseInt(myId));
						pstms.setInt(2, Integer.parseInt(userID));
						pstms.setInt(3, 1);
						pstms.setString(4, "");
						pstms.setString(5, "");
					} else {
						pstms = myConnection.prepareStatement(query);
						pstms.setInt(1, Integer.parseInt(userID));
						pstms.setInt(2, Integer.parseInt(myId));
						pstms.setInt(3, 1);
						pstms.setString(4, myName + " Friend requests");
						pstms.setString(5, "");
					}
					i += 1;
					pstms.executeUpdate();
					updateIdFriend();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public String getUserId(String name) {
			try {
				String query = "select id,username from users";
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					if (myResult.getString("username").equals(name)) {
						return myResult.getString("id");
					}
				}
			} catch (Exception ex) {
				System.out.println("Error " + ex);
			}
			return "-1";
		}

		private void delFriend(String friendName) {
			String userId = getUserId(friendName);
			String sql = "delete from friends where UserID=? and FriendID=?";
			try {
				pstms = myConnection.prepareStatement(sql);

				pstms.setInt(1, Integer.parseInt(myId));
				pstms.setInt(2, Integer.parseInt(userId));
				pstms.executeUpdate();

				pstms = myConnection.prepareStatement(sql);

				pstms.setInt(1, Integer.parseInt(userId));
				pstms.setInt(2, Integer.parseInt(myId));
				pstms.executeUpdate();

				updateIdFriend();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public void updateIdFriend() {
			String query = "ALTER TABLE `friends` DROP `idFriend`";
			try {
				pstms.executeUpdate(query);
				query = "ALTER TABLE `friends` AUTO_INCREMENT = 1";
				pstms.executeUpdate(query);
				query = "ALTER TABLE `friends` ADD `idFriend` int UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST";
				pstms.executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void checkFriendRequests() {
			String query = "select friendRequest from friends where UserId=" + myId;
			try {
				myResult = myStatement.executeQuery(query);
				while (myResult.next()) {
					out.println("REQUEST" + myResult.getString("friendRequest"));
				}
			} catch (SQLException e) {
				System.out.println("Error" + e);
			}
		}

		private void acceptRequest(int myId, int userId) {
			String sql = "update friends set friendshipStatus=? where UserId=? and FriendId=?";

			try {
				pstms = myConnection.prepareStatement(sql);

				pstms.setInt(1, 0);
				pstms.setInt(2, myId);
				pstms.setInt(3, userId);
				pstms.executeUpdate();

				pstms = myConnection.prepareStatement(sql);

				pstms.setInt(1, 0);
				pstms.setInt(2, userId);
				pstms.setInt(3, myId);
				pstms.executeUpdate();

				sql = "update friends set friendRequest=? where UserId=? and FriendId=?";
				pstms = myConnection.prepareStatement(sql);

				pstms.setString(1, "");
				pstms.setInt(2, myId);
				pstms.setInt(3, userId);
				pstms.executeUpdate();

			} catch (SQLException e) {

				e.printStackTrace();
			}
		}

		
		public void run() {
			connectingToDataBase();
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				while (true) {

					out.println("SUBMITNAME");
					myName = getNameFromDataBase(in.readLine());

					if (myName.equals("-1")) {
						// Send Message to usser - da dobavq
						out.println("CREATEACOUNT");
						insertUserInDataBase(in.readLine());
					} else {
						// Username Message - da dobavq
						out.println("CONFIMPASSWORD");
						if (acceptPassword(in.readLine())) {
							break;
						}
						out.println("PASSWORDINCORRECT");
					}

				}
				checkFriendRequests();
				getDataBaseFriendID();
				out.println("UNLUCKBAR");
				customWriters.put(myName, out);
				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}
					if (input.startsWith("MESSAGE")) {
						sendTextMessage(input);
					} else if (input.startsWith("EMOJI")) {
						sendEmojiMessage(input);
					} else if (input.startsWith("FINDFRIEND")) {
						out.println("ALLUSERS" + getAllUsersFromDataBase());
					} else if (input.startsWith("ADDFRIENT")) {
						insertFrientToDataBase(input.substring(9));
					} else if (input.startsWith("GETFRIEND")) {
						getDataBaseFriendID();
					} else if (input.startsWith("DELFRIEND")) {
						delFriend(input.substring(9));
					} else if (input.startsWith("ACCEPTREQUEST")) {
						userID = getUserId(input.substring(13));
						acceptRequest(Integer.parseInt(myId), Integer.parseInt(userID));
					} else if (input.startsWith("DELETEREQUEST")) {
						delFriend(input.substring(13));
					} else if (input.startsWith("CHECKREQUEST")) {
						checkFriendRequests();
					}
					if (new Date().getTime() / 1000 - messageDate.getTime() / 1000 >= timeConst) {
						messageDate = new Date();
					}

				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				customWriters.remove(myName, out);
				try {
					socket.close();
				} catch (IOException e) {

				}

			}
		}

	}

}
