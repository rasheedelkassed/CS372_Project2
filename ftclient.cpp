/**
* Author: Rasheed El Kassed
* Last Modified: 10/27/2018
* OSU email address: elkasser@oregonstate.edu
* Course number/section: CS372_400
* Project Number 1                
* Due Date: 10/28/2018
* Description: 
*
* Heavily influenced by Beej's Guide to Network Programming. It's essentially a modularized
* version of an example.
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

#define PORT "5423"

#define MAXDATASIZE 500

/**
* Returns a linked list with an address and a port
*/
struct addrinfo* createAddressInfo(char *input_addr, char *port){
	struct addrinfo hints;
	struct addrinfo *res;
	
	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	
	int status = getaddrinfo(input_addr, port, &hints, &res);
	if(status != 0){
		exit(1);
	}
	
	return res;
}

/**
* Creats a socket using and address and port paired within a linked list
*/
int createSocket(struct addrinfo *res){
	int sockfd = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
	if (sockfd == -1){
		exit(1);
	}
	return sockfd;
}

/**
* Connects the socket using 
*/
void connectSocket(int sockfd, struct addrinfo *res){
	int status;
	status = connect(sockfd, res->ai_addr, res->ai_addrlen);
	if(status == -1){
		exit(1);
	}
}

/**
* Returns a username found on the command line. 
*/
void getUserName(char *name){
	fgets(name, 10, stdin);
}

/**
* Takes two arguments: an adress and a port in the form of strings.
* Most of the chat functionality comes from here as I wasn't sure how
* to modularize it. 
*/
int main(int argCount, char *address[]) {
	struct addrinfo *res = createAddressInfo(address[1], address[2]);
	int sockfd = createSocket(res);
	connectSocket(sockfd, res);
	printf("Server Connected\n");
	
	int status;
	char output[500];
	char input[500];
	char userName[10];
	char toSend[510];
	
	memset(input,0,sizeof(input));
	memset(output,0,sizeof(output));
	memset(userName,0,sizeof(userName));
	memset(toSend,0,sizeof(toSend));
	toSend[0] = '\0';
	
	printf("Enter your username: ");
	getUserName(userName);
	
	while(true){
		fgets(input, 500, stdin);
		
		if(strcmp(input, "\\quit\n") == 0){
			break;
		}
		
		strcat(toSend, userName);
		toSend[strcspn(toSend, "\n")] = '>';  //strcspn is a godsend!
		strcat(toSend, input);
		
		
		send(sockfd, toSend, sizeof(toSend), 0);
		
		status = recv(sockfd, output, 500, 0);		
		if (status == -1){
			fprintf(stderr, "Error when receiving data from host\n");
			exit(1);
		}else if (status == 0){ 
			printf("Connection closed by server\n");
			break;
		}else if(strcmp(output, "\\quit") == 0){
			printf("Connection closed by server\n");
		}else{
			printf("%s", output);
		}
		
		
		memset(input,0,sizeof(input));
		memset(output,0,sizeof(output));
		memset(toSend,0,sizeof(toSend));
		toSend[0] = '\0';
	}
	close(sockfd);
	freeaddrinfo(res);
	
	return 0;
}