#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

const int size = 4096;
bool hunter;

char* calMove(char* stream) {
	static char tosend[size];
	int remain, gameNum, tickNum;
	sscanf(stream, "%d %d %d", &remain, &gameNum, &tickNum);

	//TODO: change strategy here
	if (hunter) {
		if (rand() % 80 == 0) {
			sprintf(tosend, "%d %d 0 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20", gameNum, tickNum);
		}
		else {
			int x = rand() % 50;
			if (x > 3) {
				sprintf(tosend, "%d %d 0", gameNum, tickNum);
			}
			else {
				sprintf(tosend, "%d %d %d", gameNum, tickNum, x + 1);
			}
		}
	}
	else {
		int x = rand() % 3 - 1;
		int y = rand() % 3 - 1;
		sprintf(tosend, "%d %d %d %d", gameNum, tickNum, x, y);
	}
	return tosend;
}

int main(int argc, char *argv[]) {
	srand(time(NULL));

	int port;
	sscanf(argv[1], "%d", &port);

	int sock = socket(AF_INET, SOCK_STREAM, 0);
	struct sockaddr_in saddr;
	memset(&saddr, 0, sizeof(saddr));
	saddr.sin_family = AF_INET;
	saddr.sin_port = htons(port);
	saddr.sin_addr.s_addr = inet_addr("127.0.0.1");

	if (connect(sock, (struct sockaddr*)&saddr, sizeof(saddr)) < 0) {
		printf("connect fails\n");
		exit(1);
	}

	char buf[size];
	memset(buf, 0, sizeof(buf));
	char stream[size];
	memset(stream, 0, sizeof(stream));
	char tosend[size];
	memset(tosend, 0, sizeof(tosend));
	char pre[size];
	memset(pre, 0, sizeof(pre));

	bool flag;
	bool flag2 = false;
	while (true) {
		if (flag2) {
			flag2 = false;
		}
		else {
			recv(sock, buf, sizeof(buf), 0);
			if (strchr(buf, '\n') != strrchr(buf, '\n')) {
				flag2 = true;
			}
			printf("%s", buf);
			const char s[2] = "\n";
			char* token;
			token = strtok(buf, s);
			strcat(stream, token);
			token = strtok(NULL, s);
			if (token != NULL) {
				//printf("%s", token);
				strcpy(pre, token);
			}
			memset(buf, 0, sizeof(buf));
		}
		printf("received: %s\n", stream);		
		usleep(100);
		flag = false;
		if (strcmp(stream, "done") == 0) {
			break;
		}
		else if (strcmp(stream, "hunter") == 0) {
			hunter = true;
		}
		else if (strcmp(stream, "prey") == 0) {
			hunter = false;
		}
		else if (strcmp(stream, "sendname") == 0) {
			//TODO: change client name here
			strcpy(tosend, "random_player_c++");
			flag = true;
		}
		else if (strncmp(stream, "error", 5) == 0) {

		}
		else {
			strcpy(tosend, calMove(stream));
			flag = true;
		}
		strcpy(stream, pre);
		memset(pre, 0, sizeof(pre));

		if (flag) {
			strcat(tosend, "\n");
			printf("sending: %s", tosend);
			send(sock, tosend, strlen(tosend), 0);
			memset(tosend, 0, sizeof(tosend));
		}
	}
	return 0;
}