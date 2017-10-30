package Network;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 王赣宇 on 2017/10/11.
 */

public class ServerConnections {
    //	 messagequeue
    private LinkedBlockingQueue<String> messageQueue;
    private ServerSocket serverSocket;
    private int port;
    //  initial
    ServerConnections(){

        messageQueue = new LinkedBlockingQueue<String>();
        serverSocket = null;
    }

    //  return message queue
    public LinkedBlockingQueue<String> startListen(int port){
        this.port = port;
        new MainThread().start();
        return messageQueue;
    }

    //  start main thread
    private class MainThread extends Thread{
        @Override
        public void run() {
            try {
                //listing
                serverSocket = new ServerSocket(port);
                //System.out.println("the server has turned on");
                //阻塞部分
                while (true){
                    Socket socket = serverSocket.accept();
                    new ListenThread(socket).start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try {
                    serverSocket.close();
                }catch (IOException e){e.printStackTrace();}
            }
        }
    }

    //    listening thread    offer in queue
    private class ListenThread extends Thread{
        private Socket s;
        String data = "";

        ListenThread(Socket socket){
            s = socket;
        }

        @Override
        public void run() {
            InputStream inputStream = null;
            try {
                inputStream = s.getInputStream();

                byte[] b = new byte[1024];
                int n = inputStream.read(b);
                data = new String(b,0,n);
                //System.out.println(data);
                messageQueue.add(s.getInetAddress() + data);

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try {
                    inputStream.close();
                    s.close();
                }catch (IOException e){e.printStackTrace();}
            }
        }
    }
}
