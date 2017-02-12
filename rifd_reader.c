#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>

#define SPORT_NAME "/dev/ttyAMA0"

int set_interface_attributes(int filedescriptor, int speed){
	struct termios tty;
	memset(&tty, 0, sizeof(tty));
	if (tcgetattr(filedescriptor,tty) !=0){
		printf("Error %d from tcgetattr\n", errno);
		return -1;
	}

	cfsetospeed(&tty, speed);
	cfsetispeed(&tty, speed); //set the speed of the interface

	tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8; //8 bit characters

	tty.c_iflag &= ~IGNBREAK;
	tty.c_lflag = 0; //no signaling characters, no echo

	tty.c_oflag = 0; //no remapping, no delays
	tty.c_cc[VMIN] = 1; //should block								THIS SHOULD BE TURNED OFF FOR DEBUG. a debug needs to be run to see how many chars come from the RFID tag
	tty.c_cc[VTIME] = 10; //1 second timeout

	tty.c_iflag &= ~(IXON | IXOFF | IXANY); //shut off xon/xoff control

	tty.c_cflag |= (CLOCAL | CREAD); //ignore modem controls //enable reading

	tty.c_cflag &= ~(PARENB | PAROD); //no parity

	tty.c_cflag &= ~CSTOPB;
	tty.c_cflag &= ~CRTSCTS; //no hardware flow control

	if (tcsetattr(filedescriptor, TCSANOW, &tty) != 0) {
		printf("error %d from tcsetattr\n", errno);
		return -1
	}

	return 0

}

int main(int argc, char const *argv[])
{
	
	char *serialPortName = SPORT_NAME;

	int sport_fd = open (serialPortName, O_RDWR | O_NOCTTY | O_SYNC);
	if (sport_fd < 0){
		printf("error %d from file open\n", errno);
	}
	set_interface_attributes(sport_fd, B9600); //setup the attributes

	char *fifoName = "/var/run/RFID_FIFO"; //this needs to be the same in the java program

	mkfifo(*fifoName, 0666);
	int fifo_fd = open(fifoName, O_WRONLY)

	//Do stuff with the fifo:
	//write(fifo_fd, "test", sizeof("test"));


	close(fifo_fd);
	unlink(fifoName);
	return 0;
}