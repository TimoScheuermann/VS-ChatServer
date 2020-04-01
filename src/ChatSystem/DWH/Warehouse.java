package ChatSystem.DWH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ChatSystem.Entities.Group;
import ChatSystem.Entities.Message;
import ChatSystem.Entities.User;

public class Warehouse {

	private static List<Message> messages = new ArrayList<Message>();
	private static List<User> users = new ArrayList<User>();
	private static List<Group> groups = new ArrayList<Group>();
	private static String[] files = new String[] {"messages", "users", "groups"};


	public static void saveFiles() {
		for (String fileName : files) {
			String name = "./" + fileName + ".dat";
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(name));
				System.out.println("Saving " + fileName + " in " + name);
				if (fileName.equals("messages")) {
					out.writeObject(getMessages());
				} else if (fileName.equals("groups")) {
					out.writeObject(getGroups());

				} else if (fileName.equals("users")) {
					out.writeObject(getUsers());
				}
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "resource" })
	public static void loadFiles() {
		for (String fileName : files) {
			String name = "./" + fileName + ".dat";
			File f = new File(name);
			if (!f.exists())
				continue;
			System.out.println("Loading file: " + f.getAbsolutePath());

			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(name));
				if (fileName.equals("messages")) {
					messages = (List<Message>) in.readObject();
				} else if (fileName.equals("groups")) {
					groups = (List<Group>) in.readObject();
				} else if (fileName.equals("users")) {
					users = (List<User>) in.readObject();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void addMessage(Message m) {
		getMessages().add(m);
	}

	public static void addUser(User u) {
		System.out.println("Trying to create User " + u);
		if (doesUserExist(u)) {
			System.err.println("User already exists");
			return;
		}
		System.out.println("User created and stored in DB");
		getUsers().add(u);
	}

	public static void addGroup(Group g) {
		getGroups().add(g);
	}

	public static List<Message> getMessages() {
		return messages;
	}

	public static List<User> getUsers() {
		return users;
	}

	public static List<Group> getGroups() {
		return groups;
	}

	public static User getUser(String name) {
		List<User> usersFound = getUsers().stream().filter(x -> x.name.equals(name)).collect(Collectors.toList());
		if (usersFound.size() == 0)
			return null;
		return usersFound.get(0);
	}

	public static void addUserToGroup(User u, Group g) {
		if (g == null)
			return;
		if (g.members.contains(u))
			return;
		g.members.add(u);
	}

	public static List<Group> getGroupsOfUser(User u) {
		if (u == null)
			return new ArrayList<Group>();

		return getMessages().stream().filter(x -> x.toGroup != null && x.toGroup.members.contains(u))
				.map(x -> x.toGroup).collect(Collectors.toList());
	}

	public static List<Message> getChatMessagesSorted(User a, User b) {
		return getMessages().stream()
				.filter(x -> ((x.from.equals(a) && x.toUser.equals(b)) || (x.from.equals(b) && x.toUser.equals(a))))
				.sorted().collect(Collectors.toList());
	}

	public static boolean doesMessageExist(Message m) {
		return getMessages().stream().filter(x -> x.equals(m)).count() > 0;
	}
	public static boolean doesUserExist(User u) {
		return getUsers().stream().filter(x -> x.equals(u)).count() > 0;
	}

	public static boolean doesGroupExsits(Group g) {
		return getGroups().stream().filter(x -> x.equals(g)).count() > 0;
	}

	public static List<Message> getGroupMessages(Group g) {
		return getMessages().stream().filter(x -> x.toGroup != null && x.toGroup.equals(g))
				.collect(Collectors.toList());
	}
}