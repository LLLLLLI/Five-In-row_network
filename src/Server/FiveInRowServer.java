package Server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

public class FiveInRowServer {
    private Server server;
    private LinkedBlockingQueue<String> messageQueue;
    private SqlManager sqlManager;

    private FiveInRowServer(){
        System.out.println("Initialization...");
        sqlManager = new SqlManager();
        try {
            sqlManager.Connect();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        server = new Server();
        messageQueue = server.StartListen();

        System.out.println("Succeeded initialization!");
        new QueueProcessor().start();

        Scanner in = new Scanner(System.in);
        while(true)
        {
            String read = in.nextLine();
            try {
                messageQueue.put(read);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class QueueProcessor extends Thread{

        @Override
        public void run(){
            while(true){
                if(!messageQueue.isEmpty()){
                    try {
                        String currentMessage = messageQueue.take();
                        System.out.println(currentMessage);
                        new MessageProcessor(currentMessage).start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class MessageProcessor extends Thread{
        String message;

        MessageProcessor(String s){message = s;}

        @Override
        public void run(){
            if(message.contains("Register"))
                registerAction(message);
            else if(message.contains("Login"))
                loginAction(message);
            else if(message.contains("RequireVS"))
                requireVSAction(message);
            else if(message.contains("AcceptVS"))
                acceptVSAction(message);
            else if(message.contains("ChessMove"))
                chessMoveAction(message);
            else if(message.contains("StopMatch"))
                stopMatchAction(message);
            else if(message.contains("Win"))
                winAction(message);
            else if(message.contains("RequireUpdate"))
                update(message);
            else if(message.contains("WatchChess"))
                watchChess(message);
            else if(message.contains("QuitWatch"))
                quitWatch(message);
            else if(message.contains("QuitGame"))
                quitGame(message);
            else if(message.contains("SignOut"))
                offline(message);
            else
                System.out.println("Illegal requirement");
        }
    }

    private void quitWatch(String message) {

    }


    private void registerAction(String s){
        String[] strArr = s.split("/");
        String ip = strArr[1];
        String id = strArr[3];
        String password = strArr[4];

        try {
            sqlManager.Adduser(id, password, ip);
            System.out.println("Succeeded register");
            server.ConfirmRegister(ip, "YES");
        } catch (SQLException e) {
            System.out.println("Failed register:id already existed!");
            server.ConfirmRegister(ip, "NO");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loginAction(String s){
        String[] strArr = s.split("/");
        String ip = strArr[1];
        String id = strArr[3];
        String password = strArr[4];

        try {
            if(sqlManager.checkPassword(id, password)){
                if(sqlManager.userOnLine(id, ip)){
                    System.out.println("Succeeded Login");
                    server.ConfirmLogin(ip, "YES");
                    server.SendOnlineUser(ip, sqlManager.getOnlineUser());
                    server.SendPlayingGame(ip, sqlManager.getOnlineGame());
                }else{
                    System.out.println("Fail Login:password wrong!");
                    server.ConfirmLogin(ip, "NO");
                }
            }else{
                System.out.println("Fail Login:password wrong!");
                server.ConfirmLogin(ip, "NO");
            }
        } catch (SQLException e) {
            System.out.println("Fail Login:userid wrong!");
            server.ConfirmLogin(ip, "NO");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void requireVSAction(String s){
        String[] strArr = s.split("/");
        String ip = strArr[1];
        String playerA = strArr[3];
        String password = strArr[4];
        String playerB = strArr[5];

        try {
            if(sqlManager.checkOnline(playerB)) {
                if (sqlManager.checkPassword(playerA, password)) {
                    String ipB = sqlManager.getIP(playerB);
                    server.RequireVS(ipB, playerA);
                }
            } else{
                server.AcceptVS(ip, playerB, "NO");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void acceptVSAction(String s){
        String[] strArr = s.split("/");
        String ip = strArr[1];
        String playerA = strArr[3];
        String password = strArr[4];
        String playerB = strArr[5];
        String info = strArr[6];

        try {
            if(sqlManager.checkPassword(playerB, password)){
                if(info.equals("YES")){
                    String ipA = sqlManager.getIP(playerA);
                    server.AcceptVS(ipA, playerB, "YES");
                    if(server.SendSucceed())
                    {
                        String gameid = playerA + "VS" + playerB;
                        sqlManager.Addgame(gameid, playerA, playerB);
                        System.out.println("Succeeded starting a new game!");
                    }
                }
                else if(info.equals("NO")){
                    String ipA = sqlManager.getIP(playerB);
                    server.AcceptVS(ipA, playerB, "NO");
                    System.out.println("Refuse to start a new game!");
                }
            }
            else{
                System.out.println("Wrong password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void chessMoveAction(String s){
        String[] strArr = s.split("/");
        String ipA = strArr[1];
        String playerA = strArr[3];
        String password = strArr[4];
        String playerB = strArr[5];
        String n = strArr[6];
        String color = strArr[7];

        try {
            if(sqlManager.checkPassword(playerA, password)){
                String chessID;
                if(color.equals("1")){
                    chessID = playerA + "VS" + playerB;
                }else{
                    chessID = playerB + "VS" + playerA;
                }
                sqlManager.updateChessBoard(chessID, Integer.parseInt(n), color);
                String ipB = sqlManager.getIP(playerB);
                server.ChessMove(ipB, Integer.parseInt(n), Integer.parseInt(color));
                ArrayList<String> allVisitors = sqlManager.getAllVisitors(chessID);
                if(!allVisitors.isEmpty()){
                    for (String allVisitor : allVisitors) {
                        System.out.print(allVisitor);
                        server.ChessMoveforVisitor(sqlManager.getIP(allVisitor), Integer.parseInt(n), Integer.parseInt(color));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void stopMatchAction(String s){

    }

    private void winAction(String s){
        String[] strArr = s.split("/");
        String ipA = strArr[1];
        String playerA = strArr[3];
        String password = strArr[4];
        String playerB = strArr[5];
        String color = strArr[6];

        try {
            if(sqlManager.checkPassword(playerA, password)){
                String chessID;
                if(color.equals("1")){
                    chessID = playerA + "VS" + playerB;
                }else{
                    chessID = playerB + "VS" + playerA;
                }
                sqlManager.deleteGame(chessID);
                System.out.println("Succeeded ending a game!");
                sqlManager.updateTimes(playerA, true);
                sqlManager.updateTimes(playerB, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void update(String s){
        String[] strArr = s.split("/");
        String ipA = strArr[1];
        String userA = strArr[3];
        String password = strArr[4];

        try {
            if(sqlManager.checkPassword(userA, password)){
                server.SendOnlineUser(ipA, sqlManager.getOnlineUser());
                server.SendPlayingGame(ipA, sqlManager.getOnlineGame());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void watchChess(String s){
        String[] strArr = s.split("/");
        String ipA = strArr[1];
        String username = strArr[3];
        String password = strArr[4];
        String chessID = strArr[5];

        try {
            if(sqlManager.checkPassword(username, password)){
                String info = "";
                if(sqlManager.addVistor(chessID, username)){
                    info = sqlManager.getChessBoard(chessID);
                }
                else{
                    info = "NO";
                }
                server.SendChessBoard(ipA, info);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void offline(String s){
        String[] strArr = s.split("/");
        String username = strArr[3];
        String password = strArr[4];

        try {
            if(sqlManager.checkPassword(username, password)){
                sqlManager.userOffLine(username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void quitGame(String s){
        String[] strArr = s.split("/");
        String username = strArr[3];
        String password = strArr[4];
        String userB = strArr[5];
    }

    public static void main(String args[]){
        new FiveInRowServer();
    }
}
