/**
 * Author: Rasheed El Kassed
 * Last Modified: 11/25/2018
 * OSU email address: elkasser@oregonstate.edu
 * Course number/section: CS372_400
 * Project Number: 2
 * Due Date: 11/25/2018
 * Description: A client designed to work with ftserver.cpp. This client sends a command to ftserver that then
 * results in ftserver sending data to this program. Depending on the command, ftclient will either receive and 
 * print out a directory list, or ftclient will "download" a .txt file from ftserver.
 *
 * There's lots of repeat from project 1's chatserve.java. Almost everything was derived from there.
 * This stackoverflow forum helped a little bit when I forgot to flush the writes:
 * https://stackoverflow.com/questions/39994596/unable-to-write-data-through-bufferedwriter-on-socket
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ftclient{
    private String serverAddress;
    private int dataPort;
    private Socket clientSocket;
    private ServerSocket listenSocket;
    private Socket dataSocket;


    private OutputStream out;
    private OutputStreamWriter outWriter;
    private BufferedWriter bufferedOutWriter;

    private InputStream in;
    private InputStreamReader inReader;
    private BufferedReader bufferedInReader;

	/**
	* Connect to a socket specified by address and serverPort.
	*/
    private void connectToServer(String address, int serverPort){
        try{
            this.clientSocket = new Socket(address, serverPort);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
	
	/**
	* Listen for a connection and open a connection with dataSocket when a
	* connection is received.
	*/
    private void listenForConnection(int dataPort){
        try{
            this.listenSocket = new ServerSocket(dataPort);
            this.dataSocket = listenSocket.accept();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

	/**
	* Sends a command and data port through clientSocket
	*/
    private void sendCommand(String command, int dataPort){
        try{
            this.out = clientSocket.getOutputStream();
            this.outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            this.bufferedOutWriter = new BufferedWriter(this.outWriter);
            this.bufferedOutWriter.write(command + "\0" + dataPort + "\0");
            this.bufferedOutWriter.flush();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

    }

	/**
	* Sends a command, data port, and file name through clientSocket
	*/
    private void sendCommand(String command, int dataPort, String fileName){
        try{
            this.out = clientSocket.getOutputStream();
            this.outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            this.bufferedOutWriter = new BufferedWriter(this.outWriter);
            this.bufferedOutWriter.write(command + "\0" + dataPort + "\0" + fileName + "\0");
            this.bufferedOutWriter.flush();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }

    }

	/**
	* Listens for a connection on dataPort and then prints out the received
	* data. The data will be a directory structure.
	*/
    private void receiveFileList(int dataPort){
        try{
            listenForConnection(dataPort);
            String receivedData = "";
            this.in = this.dataSocket.getInputStream();
            this.inReader = new InputStreamReader(this.in);
            this.bufferedInReader = new BufferedReader(this.inReader);
            System.out.println("Receiving directory structure from " + this.serverAddress + ":" + this.dataPort);
            while((receivedData = this.bufferedInReader.readLine()) != null){
                System.out.println(receivedData);
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }finally{
            closeSocket(this.clientSocket);
            closeSocket(this.listenSocket);
            closeSocket(this.dataSocket);
        }
    }

	/**
	* Listens for a connection on dataPort and then prints out the received
	* data into a file named after fileName. The data will be from a text file.
	*/
	//https://docs.oracle.com/javase/7/docs/api/java/io/Writer.html
    private void getFile(int dataPort, String fileName){
        try{
            listenForConnection(dataPort);
            String receivedData = "";
            String dataToWrite = "";
            Writer newFile = null;
            this.in = this.dataSocket.getInputStream();
            this.inReader = new InputStreamReader(this.in);
            this.bufferedInReader = new BufferedReader(this.inReader);
            while((receivedData = this.bufferedInReader.readLine()) != null){
                dataToWrite += receivedData;
            }
            if(dataToWrite.trim().equals("ERROR: File not found")){
                System.out.println(dataToWrite);
            }else{
                newFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
                newFile.write(dataToWrite);
                newFile.flush();
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }finally{
            closeSocket(this.clientSocket);
            closeSocket(this.listenSocket);
            closeSocket(this.dataSocket);
        }
    }

	/**
	* A helper function that closes a socket "s" if it is open.
	*/
	//https://stackoverflow.com/questions/7224658/java-try-finally-block-to-close-stream
    private void closeSocket(Closeable s){
        try{
            if(s != null){
                s.close();
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

	/**
	* Sends a command for a directory list and then prints that list.
	*/
    public ftclient(String address, int serverPort, String command, int dataPort){
        this.serverAddress = address;
        this.dataPort = dataPort;
        connectToServer(address, serverPort);
        sendCommand(command, dataPort);
        receiveFileList(dataPort);
    }

	/**
	* Sends a command to get a file and the "downloads" it.
	*/
    public ftclient(String address, int serverPort, String command, int dataPort, String fileName){
        connectToServer(address, serverPort);
        sendCommand(command, dataPort, fileName);
        getFile(dataPort, fileName);

    }

	/**
	* The main function only calls the constructor with the correct arguments.
	*/
    //args = {address, serverPort, command, dataPort}
    //OR
    //args = {address, serverPort, command, fileName, dataPort}
    public static void main(String[] args) {
        try{
            if(args.length == 4){
                ftclient client = new ftclient(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
            }else if(args.length == 5){
                ftclient client = new ftclient(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), args[4]);
            }else{
                System.out.println("Incorrect number of arguments.");
            }
        }catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
    }
}