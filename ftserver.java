/**
* Author: Rasheed El Kassed
* Last Modified: 10/27/2018
* OSU email address: elkasser@oregonstate.edu
* Course number/section: CS372_400
* Project Number 1                
* Due Date: 10/28/2018
* Description: 
*
* Heavily influenced by the lessons found at https://docs.oracle.com/javase/tutorial/networking/sockets/index.html
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;



/**
* chatserve takes a port and waits for a connection attempt to that port.
* To run on port 5000 for example, type the following and then press enter:
* java chatserve 5000
*/
public class chatserve{

    private Socket clientSocket;			
    private ServerSocket serverSocket;

    private Scanner messageToSend;			//Data being taken from the command line to send to the client
    private BufferedReader dataIn;          //Data being received from the client
    private PrintWriter dataOut;            //Data to be sent to the client
	private String userName;				//The user name that will be sent alongside communications


	/**
	* Starts the server at the specified port.
	* Changes the values of this.serverSocket
	*/
    private void startServer(int port){
        try{
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started");
        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
    }


	/**
	* Waits for a response using ServerSocket.accept()
	* Changes the values of this.clientSocket
	*/
    private void waitForResponse(){
        try{
            System.out.println("Waiting for client...");
            this.clientSocket = this.serverSocket.accept();
            System.out.println("Client connected");
        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
    }

	/**
	* Initializes all the variables needed to read and write data.
	* Also gets user input and puts it into messageToSend.
	* Changes the values of this.dataIn, this.dataOut, and this.messageToSend
	*/	
    private void initializeVariables(){
        try{
            this.dataIn = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.dataOut = new PrintWriter(clientSocket.getOutputStream(), true);
            this.messageToSend = new Scanner(System.in);

        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }
    }
	
	/**
	* Prompts the user for the username that will prepend all their messages
	* Changes the value of this.userName
	*/	
	private void getUserName(){
		System.out.print("Enter your username: ");
		userName = messageToSend.nextLine();
	}

	/**
	* The constructor takes a port and uses the above helper functions to create
	* the chat server.
	* Changes all values;
	*/	
    public chatserve(int port) {
        startServer(port);
        waitForResponse();
        initializeVariables();
		getUserName();
		
		String receivedData;
		String sentData;
        while(true){
		receivedData = "";
		sentData = "";
            try{
                receivedData = dataIn.readLine();
				//Check if connection ended
                if(receivedData == null){
					System.out.println("Client disconnected");
					dataOut.close();
                    dataIn.close();
                    clientSocket.close();
                    break;
				}else{
					System.out.println(receivedData);
				}
				sentData = messageToSend.nextLine();
				//Check if you need to quit
				if(sentData.equals("\\quit")){
					System.out.println("Quitting...");
					dataOut.close();
                    dataIn.close();
                    clientSocket.close();
                    break;
				}
				sentData = userName + ">" + sentData;
				dataOut.println(sentData);


            }catch (IOException e){
                System.out.println(e.getMessage());
                return;
            }
        }

        try {
            clientSocket.close();
            dataIn.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
            return;
        }

    }
	
	/**
	* The main function only attempts to make a chatserve object
	* will fail if the incorrect number of args is used.
	*/
    public static void main(String args[]){
		try{
			chatserve server = new chatserve(Integer.parseInt(args[0]));
		}catch (NumberFormatException e){
			System.out.println(e.getMessage());
		}
    }
}