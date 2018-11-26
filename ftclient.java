/**
 * Author: Rasheed El Kassed
 * Last Modified: 10/27/2018
 * OSU email address: elkasser@oregonstate.edu
 * Course number/section: CS372_400
 * Project Number 1
 * Due Date: 10/28/2018
 * Description:
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

    private void connectToServer(String address, int serverPort){
        try{
            this.clientSocket = new Socket(address, serverPort);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    private void listenForConnection(int dataPort){
        try{
            this.listenSocket = new ServerSocket(dataPort);
            this.dataSocket = listenSocket.accept();
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

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

    private void closeSocket(Closeable s){
        try{
            if(s != null){
                s.close();
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public ftclient(String address, int serverPort, String command, int dataPort){
        this.serverAddress = address;
        this.dataPort = dataPort;
        connectToServer(address, serverPort);
        sendCommand(command, dataPort);
        receiveFileList(dataPort);
    }

    public ftclient(String address, int serverPort, String command, int dataPort, String fileName){
        connectToServer(address, serverPort);
        sendCommand(command, dataPort, fileName);
        getFile(dataPort, fileName);

    }

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
            //ftclient client = new ftclient(args[0], Integer.parseInt(args[1]));
            //ftclient client1 = new ftclient("127.0.0.1", 25000, "-l", 14643);
            //ftclient client2 = new ftclient("127.0.0.1", 25000, "-g", 14643, "fiename.txt");
        }catch(NumberFormatException e){
            System.out.println(e.getMessage());
        }
    }
}