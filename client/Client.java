/*The purpose of this program is to showcase networking and using a TCP-chat to display that
 * multiple clients can connect to a server and chat with each other.
 * 
 * LAST CHANGE: 30-05-2023
 * AUTHOR: Solman
 * 
 * */
package client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client extends JFrame implements ActionListener, KeyListener, MouseListener{
	
	/*Width and height of frame*/
	private final int WIDTH = 500;
	private final int HEIGHT = 500;
	/*Width and height of buttons*/
	private final int B_WIDTH = 100;
	private final int B_HEIGHT = 50;
	
	//Ensuring mutiple connections does not go through on the same client
	private boolean joined = false;

	// Implemented classes used locally followed with swing components
	private Socket client;
	private String username = null;
	
	private JPanel panel;
	private JTextArea ta;
	private JScrollPane scroll;
	private JTextField tf;
	private JLabel label;
	private JButton join, disconnect, changeNick, _send;
	
	// Constructor to initiate chat-frame
	Client(){
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setTitle("GroupTalking");
		this.setSize(WIDTH, HEIGHT);
		
		panel = new JPanel();
		panel.setSize(WIDTH, HEIGHT);
		panel.setLayout(null);
		
		ta = new JTextArea();
		ta.setEditable(false);
		scroll = new JScrollPane(ta);
		scroll.setBounds(5, B_HEIGHT+10, WIDTH-25, HEIGHT-155);
		panel.add(scroll);
		
		tf = new JTextField();
		tf.setBounds(5, HEIGHT-B_HEIGHT*2+10, WIDTH-B_WIDTH-25, B_HEIGHT);
		tf.addKeyListener(this);
		panel.add(tf);
		
		label = new JLabel();
		label.setBounds(B_WIDTH+10, B_HEIGHT/2-5, 150, 25);
		panel.add(label);
		
		changeNick = new JButton();
		changeNick.setBounds(5, 5, B_WIDTH, B_HEIGHT);
		changeNick.setText("Nickname");
		changeNick.setFocusable(false);
		changeNick.addActionListener(this);
		panel.add(changeNick);
		
		join = new JButton();
		join.setBounds((WIDTH-B_WIDTH*2-20), 5, B_WIDTH, B_HEIGHT);
		join.setText("Join");
		join.setFocusable(false);
		join.addActionListener(this);
		join.addMouseListener(this);
		panel.add(join);
		
		disconnect = new JButton();
		disconnect.setBounds((WIDTH-B_WIDTH-20), 5, B_WIDTH, B_HEIGHT);
		disconnect.setText("Disconnect");
		disconnect.setFocusable(false);
		disconnect.addActionListener(this);
		disconnect.addMouseListener(this);
		panel.add(disconnect);
		
		_send = new JButton();
		_send.setBounds((WIDTH-B_WIDTH-20), HEIGHT-B_HEIGHT*2+10, B_WIDTH, B_HEIGHT);
		_send.setText("Send");
		_send.setFocusable(false);
		_send.addActionListener(this);
		panel.add(_send);
		
		this.add(panel);
		this.setVisible(true);
	}
	
	// Function to connect to the server, IP and PORT is predefined
	public void connectToChat() throws UnknownHostException, IOException {
		client = new Socket("127.0.0.1", 5000);
		
		//Forcing client-sided username check
		while(username == null) {
			username = JOptionPane.showInputDialog("Enter nickname");
			if(username.isBlank() || !(username.length() > 3 && username.length() < 13)) {username = null;}
		}
		
		//Displaying username at chat-frame
		label.setText(username);
		joined = true;
		
		//If connection goes through and username is set, message of connection is pushed to all connected users
		String message = username.concat(" has connected to the chat\n");
		writeToChat(message);

		//Seperate thread is created on clients side to listen to incoming messages and append them to TextArea
        Thread listener = new Thread(new Runnable() {
        	@Override
        	public void run() {
        		try {
        			InputStream in = client.getInputStream();
        			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
        			String buffer;
        			while ((buffer = bf.readLine()) != null) {
        				String message = buffer;
        				ta.append(message + "\n");
        			}
        		} catch (IOException e) {
        			e.printStackTrace();
        		}        		
        	}
        });
        listener.start();
	}
	
	//Function to change nickname i.e username
	public void changeNick() {
		String preUsername = username;
		username = JOptionPane.showInputDialog("Enter new nickname");
		
		//If legal check is failed name change is canceled
		if(username.isBlank() || !(username.length() > 3 && username.length() < 13)) {
			username = preUsername;
		}else {
			//Otherwise name change is pushed to all users connected
			String message = (preUsername + " is now " + username + "\n");
			writeToChat(message);
		}
	}
	
	//Function to write to outputstream
	public void writeToChat(String message) {
		try {
			OutputStream out = client.getOutputStream();
			out.write(message.getBytes());
			out.flush();
			tf.setText("");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	
	/*-------------------------------------------------------------------------------------------
	 * Following are functions to handle different events, button clicks, key presses and mouse events*/
	
	@Override
	public void keyPressed(KeyEvent e) {
		//Sending message from textfield via Enter key
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			writeToChat(username.concat(": " + tf.getText() + "\n"));
		}
	}
	
	//Function checking weather any button is pushed
	@Override
	public void actionPerformed(ActionEvent e) {
		//Nickname change
		if(e.getSource() == changeNick) {
			changeNick();
		}
		//Sending message from textfield via button for more flavour
		if(e.getSource() == _send) {
			writeToChat(username.concat(": " + tf.getText() + "\n"));
		}
		//Joining chat server 
		if(e.getSource() == join) {
			try {
				if(!joined) {
					connectToChat();					
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//Disconnecting from the server and terminating the program
		if(e.getSource() == disconnect) {
			try {
				String message = username.concat(" has disconnected from the chat\n");
				writeToChat(message);
				client.close();
				System.exit(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	//Hover effect for buttons: join, disconnect '#80ff80'-green '#E52B50'-red
	@Override
	public void mouseEntered(MouseEvent e) {
		if(e.getSource() == join) {
			join.setBackground(Color.decode("#80ff80"));			
		}else if (e.getSource() == disconnect) {
			disconnect.setBackground(Color.decode("#E52B50"));
		}
	}
	
	//Resetting color when mouse no longer hovers the buttons
	@Override
	public void mouseExited(MouseEvent e) {
		join.setBackground(null);
		disconnect.setBackground(null);
	}
	
	//Driver function i.e entry-point
	public static void main(String[] args) throws IOException {
		new Client();
	}
	
	
	/*--------Unused functions through implementation---------*/
	
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
