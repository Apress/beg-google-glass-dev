//
//  ViewController.m
//  SocketiOSApp
//
//  Created by Jeff Tang on 5/26/14.
//  Copyright (c) 2014 Jeff Tang. All rights reserved.
//

#import "ViewController.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UILabel *lblInfo;

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)serverTapped:(id)sender {
    [self runSocketServer];
}

- (IBAction)clientTapped:(id)sender {
    [self runSocketClient];
}

- (void) runSocketServer {
    int listenfd = 0;
    __block int connfd = 0;
    struct sockaddr_in serv_addr;
    
    __block char sendBuff[1024];
    __block NSString *info;
    
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    memset(&serv_addr, '0', sizeof(serv_addr));
    memset(sendBuff, '0', sizeof(sendBuff));
    
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(6604);
    
    bind(listenfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr));
    
    listen(listenfd, 10);
    
    
    _lblInfo.text = @"Waiting for client on port 6606";
    
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        struct sockaddr_in client_addr;
        socklen_t addrlen=sizeof(client_addr);
        connfd = accept(listenfd, (struct sockaddr*)&client_addr, &addrlen);
        write(connfd, "2014\n", 5);
        char recvBuff[1024];
        int n = read(connfd, recvBuff, sizeof(recvBuff)-1);
        recvBuff[n] = '\0';
        
        struct sockaddr_in localAddress;
        socklen_t addressLength = sizeof(localAddress);
        getsockname(connfd, (struct sockaddr*)&localAddress, &addressLength);
        
        info = [NSString stringWithFormat:@"SERVER IP: %@, connected from %@, received: %s",
                [NSString stringWithCString:inet_ntoa(localAddress.sin_addr) encoding:NSUTF8StringEncoding],
                [NSString stringWithCString:inet_ntoa(client_addr.sin_addr) encoding:NSUTF8StringEncoding],
                recvBuff];
        close(connfd);
        close(listenfd);
        
        
        dispatch_async(dispatch_get_main_queue(), ^{
            _lblInfo.text = [NSString stringWithFormat:@"%@\n%@", _lblInfo.text, info];
        });
    });
}

- (void) runSocketClient {
    
    
    int sockfd = 0, n = 0;
    char recvBuff[1024];
    struct sockaddr_in serv_addr;
    
    
    memset(recvBuff, '0',sizeof(recvBuff));
    if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        printf("unable to create socket\n");
        return;
    }
    
    memset(&serv_addr, '0', sizeof(serv_addr));
    
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(6604);
    
    if(inet_pton(AF_INET, "10.0.0.6", &serv_addr.sin_addr)<=0)
    {
        printf("inet_pton error occured\n");
        return;
    }
    
    if( connect(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
    {
        printf("unable to connect\n");
    }
    
    if ( (n = read(sockfd, recvBuff, sizeof(recvBuff)-1)) > 0)
    {
        recvBuff[n] = 0;
        char result[1024];
        sprintf(result, "%d\n", atoi(recvBuff)+50);
        write(sockfd, result, 5);
        
        struct sockaddr_in localAddress;
        socklen_t addressLength = sizeof(localAddress);
        getsockname(sockfd, (struct sockaddr*)&localAddress, &addressLength);
        
        _lblInfo.text = [NSString stringWithFormat:@"CLIENT IP: %@, connected to %@, received: %s",
                         [NSString stringWithCString:inet_ntoa(localAddress.sin_addr) encoding:NSUTF8StringEncoding],
                         [NSString stringWithCString:inet_ntoa(serv_addr.sin_addr) encoding:NSUTF8StringEncoding],
                         recvBuff];
        
        close(sockfd);
    }
    
}



@end
