package Server;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 王赣宇 on 2017/10/15.
 */

public class Server {

    private ClientConnection clientConnection;
    private ServerConnections serverConnections;

    private int ServerPort;
    private int ClientPort;

    Server(){
        //setting port
        ServerPort = 33000;
        ClientPort = 33001;
        clientConnection = new ClientConnection();
        serverConnections = new ServerConnections();
    }

    public LinkedBlockingQueue<String> StartListen(){
        return serverConnections.startListen(ServerPort);
    }

    public boolean SendSucceed(){
        return clientConnection.sendSucceed();
    }

    public void ConfirmRegister(String ClientAddr, String confirmInformation){
        String[] tmp = {"ConfirmRegister", confirmInformation};
        SendtoClient(ClientAddr, Combine(tmp));
    }

    public void ConfirmLogin(String ClientAddr, String confirmInformation){
        String[] tmp = {"ConfirmLogin", confirmInformation};
        SendtoClient(ClientAddr, Combine(tmp));
    }

    //这个需再考虑，若用户过多则一次无法全部加载，分批传输较麻烦，可传输好友？或积分相近的。
    public void SendOnlineUser(String ClientAddr, String data){
        String[] tmp = {"SendOnlineUser", data};
        SendtoClient(ClientAddr, Combine(tmp));
    }

    public void SendPlayingGame(String ClientAddr, String data){
        String[] tmp = {"SendPlayingGame", data};
        SendtoClient(ClientAddr, Combine(tmp));
    }

    public void RequireVS(String ClientAddrUserB, String UserA){
        String[] tmp = {"RequireVS",UserA};
        SendtoClient(ClientAddrUserB, Combine(tmp));
    }

    //    accept or not : info =  YES or NO
    public void AcceptVS(String ClientAddrUserA, String UserB, String info){
        String[] tmp = {"AcceptVS", UserB, info};
        SendtoClient(ClientAddrUserA, Combine(tmp));
    }

    public void ChessMove(String ClientAddrUserA, int n, int color){
        String[] tmp = {"ChessMove",String.valueOf(n), String.valueOf(color)};
        SendtoClient(ClientAddrUserA, Combine(tmp));
    }

    //    info: = RUNAWAY or PLAYEROFFLINE
    public void StopMatch(String ClientAddrUserB, String UserA, String info){
        String[] tmp = {"StopMatch", UserA, info};
        SendtoClient(ClientAddrUserB, Combine(tmp));
    }

    public void YouLose(String ClientAddrUserB, String UserA) {
        String[] tmp = {"StopMatch", UserA};
        SendtoClient(ClientAddrUserB, Combine(tmp));
    }

    // 一场棋局最多有五个观战者，若ChessBoard里为NO则说明观战名额以满，否则里面为棋盘信息 255个0、1、2,  0为空 1为黑 2为白
    public void SendChessBoard(String ClientAddrUserA, String ChessBoard){
        String[] tmp = {"SendChessBoard", ChessBoard};
        SendtoClient(ClientAddrUserA, Combine(tmp));
    }

    //给观战者提供棋子信息，约定与ChessMove一致
    public void ChessMoveforVisitor(String ClientAddrUserA, int n, int color){
        String[] tmp = {"ChessMove", String.valueOf(n), String.valueOf(color)};
        SendtoClient(ClientAddrUserA, Combine(tmp));
    }

    //general send
    public void SendtoClient(String ClientAddr, String data){
        System.out.println(data);
        clientConnection.Send(ClientAddr, ClientPort, data);
    }

    //string combine
    private String Combine(String[] args){
        String data = "";
        for (String a : args) {
            data = data.concat("/" + a);
        }
        return data;
    }

}
