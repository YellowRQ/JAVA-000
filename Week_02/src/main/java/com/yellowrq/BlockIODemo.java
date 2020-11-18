package com.yellowrq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClassName:BlockIODemo
 * Package:com.yellowrq
 * Description:
 *
 * @author:YellowRQ
 * @data:2020/10/28 16:48
 */
public class BlockIODemo {

    public static void main(String[] args) throws IOException {
        int portNumber = 0;
        //创建一个新的ServerSocket，用以监听指定端口的连接请求
        ServerSocket serverSocket = new ServerSocket(portNumber);
        //对accept()方法的调用将被阻塞，直到一个连接建立
        Socket clientSocket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //这些流对象都派生于 该套接字的流对象
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String request, response;
        while ((request = in.readLine()) != null) {
            if ("Done".equals(request)) {
                break;
            }
            response = processReqiest(request);
            out.println(response);
        }

    }

    private static String processReqiest(String request) {
        return null;
    }
}
