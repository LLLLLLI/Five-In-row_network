package Network;

import java.io.*;
import java.net.*;

/**
 * Created by wgy
 * on 2017/10/11.
 */

public class ClientConnection{
    // static String TAG = "ClientConnection";

    //the connection thread
    private SocketClient socketClient;
    private String RemoteIP;
    private String MyIP;

    ClientConnection(){
        socketClient = null;
        RemoteIP = "";
        MyIP = "";
    }

    boolean Send(String ServerAddr, int ServerPort, String data){

        socketClient = new SocketClient(ServerAddr, ServerPort, data);
        socketClient.start();
        try {
            socketClient.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return !RemoteIP.equals("");
    }

    // public String getRemoteIP(){return RemoteIP.substring(1);}
    // public String getMyIP(){return MyIP.substring(1);}
    boolean sendSucceed(){return !RemoteIP.equals("");}


    private class SocketClient extends Thread{

        private Socket socket = null;
        private OutputStream outputStream;
        private String data;
        private String addr;
        private int port;


        SocketClient(String tmpaddr, int tmpport,String tmpdata){
            data = tmpdata;
            addr = tmpaddr;
            port = tmpport;
        }

        @Override
        public void run(){
            //Log.d(TAG, "connect start");
            //initialize
            socket = null;
            outputStream = null;
            //server IP
            String serverIP = addr;
            //setting 5 second to interrupt the socketclient thread
            new TimmerClose(this, 5);

            try{
                //Log.d(TAG, "start try");

                socket = new Socket(serverIP, port);

                //Log.d(TAG, "connected to " + serverIP);
                MyIP = socket.getLocalAddress().toString();
                RemoteIP = socket.getInetAddress().toString();
                //Log.d(TAG, socket.getLocalAddress().toString() + " " + RemoteIP );
                //get OutputStream and write
                outputStream = socket.getOutputStream();
                outputStream.write(data.getBytes());

            }catch (IOException e1){
                e1.printStackTrace();
            }finally {
                try {
                    outputStream.close();
                    socket.close();

                } catch (Exception e2){e2.printStackTrace();}
            }
        }
    }

}
