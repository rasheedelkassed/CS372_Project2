/**
* Author: Rasheed El Kassed
* Last Modified: 11/25/2018
* OSU email address: elkasser@oregonstate.edu
* Course number/section: CS372_400
* Project Number: 2                
* Due Date: 11/25/2018
* Description: A file transfer server that receives a command and a port number and uses that to either send
* a file directory back or an actual text file back. Files sent using this program must have the .txt extension.
*
* Heavily influenced by Beej's Guide to Network Programming. It's essentially a modularized
* version of the "A Simple Stream Server" example modified to send txt files and text instead
* of just text.
*
* Lots of repeat functions taken from the chatclient.cpp file from project 1. Some are slightly
* modified versions of previous code from project 1.
*/

#include <string.h>
#include <sys/types.h>
#include <iostream>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>

#define PORT "25000"
#define BACKLOG 5

// Straight out of Beej's guide
// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa){
	if (sa->sa_family == AF_INET) {
		return &(((struct sockaddr_in*)sa)->sin_addr);
	}
	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}


/**
* Returns a linked list with the host address and a port.
*/
struct addrinfo* createAddressInfo(char *port){
	struct addrinfo hints;
	struct addrinfo *serverinfo;
	
	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	
	int status = getaddrinfo(NULL, port, &hints, &serverinfo);
	if(status != 0){
		fprintf(stderr, "getaddrinfo error: %s\n", gai_strerror(status));
		exit(1);
	}
	
	return serverinfo;
}

/**
* Returns a linked list with an address and a port.
*/
struct addrinfo* createAddressInfo(char *input_addr, char *port){
	struct addrinfo hints;
	struct addrinfo *res;
	
	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	
	int status = getaddrinfo(input_addr, port, &hints, &res);
	if(status != 0){
		fprintf(stderr, "getaddrinfo error: %s\n", gai_strerror(status));
		exit(1);
	}
	
	return res;
}

/**
* Creats a socket using and address and port paired within a linked list.
*/
int createSocket(struct addrinfo *res){
	int sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
	if (sockfd == -1){
		exit(1);
	}
	return sockfd;
}

/**
* Connects the socket.
*/
void connectSocket(int sockfd, struct addrinfo *res){
	int status;
	status = connect(sockfd, res->ai_addr, res->ai_addrlen);
	if(status == -1){
		exit(1);
	}
}

/**
* Binds the provided socket.
*/
void bindSocket(int sockfd, struct addrinfo *serverinfo){
	int status = bind(sockfd, serverinfo->ai_addr, serverinfo->ai_addrlen);
	if(status == -1){
		close(sockfd);
		fprintf(stderr, "bind error: %s\n", gai_strerror(status));
		exit(1);
	}
}

void listenSocket(int sockfd){
	int status = listen(sockfd, BACKLOG);
	if(status == -1){
		close(sockfd);
		fprintf(stderr, "listen error: %s\n", gai_strerror(status));
		exit(1);
	}
}

/**
* Sends the file directory the program is in through datasockfd.
*/
//https://www.geeksforgeeks.org/c-program-list-files-sub-directories-directory/
void sendFileList(int datasockfd, char address[], char dataPort[]){
	char fileList[1024];
	memset(fileList,0,sizeof(fileList));
	
	struct dirent *de;
    DIR *dr = opendir("."); 
    if (dr == NULL){ 
        printf("Could not open current directory" ); 
        return; 
    } 
	
    while ((de = readdir(dr)) != NULL) {
		strcat(fileList, de->d_name);
		strcat(fileList, "\n");
	}     
	strcat(fileList, "\0");
  
    closedir(dr);
	printf("Sending directory contents to %s:%s\n", address, dataPort);
	send(datasockfd, fileList, sizeof(fileList), 0);
    return; 
}

/**
* Sends the specified file in chunks through datasockfd.
*/
//http://www.cplusplus.com/reference/cstdio/fopen/
//https://www.tutorialspoint.com/c_standard_library/c_function_fopen.htm
void sendFile(int datasockfd, char filename[], char address[], char dataPort[]){
	char fileChunk[128];
	FILE *pFile;
	pFile = fopen(filename, "r");
	if(pFile == NULL){
		printf("File not found. Sending error message to %s:%s\n", address, dataPort);
		send(datasockfd, "ERROR: File not found", sizeof("ERROR: File not found"), 0);
		return;
	}
	printf("Sending \"%s\" to %s:%s\n", filename, address, dataPort);
	while(!feof(pFile)){
		memset(fileChunk,0,sizeof(fileChunk));
		fread(fileChunk, sizeof(fileChunk)-1, 1, pFile);
		send(datasockfd, fileChunk, sizeof(fileChunk), 0);
	}
	
	fclose(pFile);
	
}

/**
* This is the bulk of the program. Uses the connection to send and receive data through various created sockets.
*/
//Almost straight out of Beej's guide
void useConnection(int sockfd){
	struct sockaddr_storage their_addr;
	socklen_t addr_size;
	int new_fd;
	char s[INET_ADDRSTRLEN];
	
	char command[16];
	char dataPort[16];
	memset(command,0,sizeof(command));
	memset(dataPort,0,sizeof(dataPort));
	
	while(1){
		addr_size = sizeof their_addr;
		new_fd = accept(sockfd, (struct sockaddr *)&their_addr, &addr_size);
		if(new_fd == -1){
			perror("accept");
			continue;
		}
		inet_ntop(their_addr.ss_family, get_in_addr((struct sockaddr *)&their_addr), s, sizeof s); //From Beej's guide
		printf("Connection from %s\n", s);
		
		//First, get the command
		int commandStatus = recv(new_fd, command, 3, 0);
		if (commandStatus == -1){
			fprintf(stderr, "Error receiving data from host\n");
			exit(1);
		}else if (commandStatus == 0){ 
			printf("Connection closed by server\n");
			break;
		}
		
		//Then, get the data port
		int dataPortStatus = recv(new_fd, dataPort, 6, 0);
		if (dataPortStatus == -1){
			fprintf(stderr, "Error receiving data from host\n");
			exit(1);
		}else if (dataPortStatus == 0){ 
			printf("Connection closed by server\n");
			break;
		}
		
		//If the client asked for a list, send the file list
		if(strncmp(command, "-l", 2) == 0){
			printf("List directory requested on port %s\n", dataPort);
			struct addrinfo *datainfo = createAddressInfo(s, dataPort);
			int datasockfd = createSocket(datainfo);
			connectSocket(datasockfd, datainfo);
			sendFileList(datasockfd, s, dataPort);
			close(datasockfd);
			freeaddrinfo(datainfo);
		//If the client asked to get a file, get the filename and then send if it exists
		}else if(strcmp(command, "-g") == 0){
			char fileName[128];
			memset(fileName, 0, sizeof(fileName));
			
			int fileNameStatus = recv(new_fd, fileName, sizeof(fileName), 0);
			if (fileNameStatus == -1){
				fprintf(stderr, "Error receiving data from host\n");
				exit(1);
			}else if (fileNameStatus == 0){ 
				printf("Connection closed by server\n");
				break;
			}
			
			printf("File \"%s\" requested on port %s\n", fileName, dataPort);
			struct addrinfo *datainfo = createAddressInfo(s, dataPort);
			int datasockfd = createSocket(datainfo);
			connectSocket(datasockfd, datainfo);
			sendFile(datasockfd, fileName, s, dataPort);
			close(datasockfd);
			freeaddrinfo(datainfo);
		}
		//else{
		//	sendError();
		//}
		
		close(new_fd);
		
	}	
}

/**
* Calls all the above functions and then closes the sockets.
* This will never return as long as no errors occur.
*/
int main(int argCount, char *address[]){
	struct addrinfo *serverinfo = createAddressInfo(address[1]);
	int serversockfd = createSocket(serverinfo);
	bindSocket(serversockfd, serverinfo);
	listenSocket(serversockfd);
	printf("Server open on port %s\n", address[1]);
	useConnection(serversockfd);
	
	printf("One\n");
	
	close(serversockfd);
	freeaddrinfo(serverinfo);
}