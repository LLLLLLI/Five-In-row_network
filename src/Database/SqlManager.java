package Database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SqlManager {

    private Connection connection;

    private static InputStream in  = SqlManager.class.getClassLoader().getResourceAsStream("config.properties");
    private static Properties prop = new Properties();

    public void Connect() throws ClassNotFoundException, SQLException {
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String driver = prop.getProperty("jdbc.driverClassName");
        String url = prop.getProperty("jdbc.url");
        String user = prop.getProperty("jdbc.username");
        String password = prop.getProperty("jdbc.password");

        Class.forName(driver);
        connection = DriverManager.getConnection(url,user,password);
    }

    //添加新用户
    public void addUser(String id, String password, String ip) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        PreparedStatement psql;    //用来执行mysql语句
        psql = connection.prepareStatement("insert into user (id, password, playtimes, wintimes, scores, ip, state) "
                + "values(?,?,0,0,0,?,'f')");
        psql.setString(1,id);
        psql.setString(2,password);
        psql.setString(3, ip);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded adding a user!");
    }

    public Boolean checkPassword(String id, String password) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String correctPassword = rs.getString("password");
        return password.equals(correctPassword);
    }

    public String getIP(String id) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        return rs.getString("ip");
    }

    public String getOnlineUser() throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where state = 'o'";
        ResultSet rs = statement.executeQuery(sql);
        StringBuilder onlineUser = new StringBuilder();
        while(rs.next())    // 太强大了 自动把不优雅的写法变优雅
            onlineUser.append(rs.getString("id")).append(" ").append(rs.getInt("scores")).append("/");
        return onlineUser.toString();
    }

    public String getOnlineGame() throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming";
        ResultSet rs = statement.executeQuery(sql);
        StringBuilder onlineGame = new StringBuilder();
        while(rs.next())
            onlineGame.append(rs.getString("id")).append("/");
        return onlineGame.toString();
    }

    public void updateTimes(String id, Boolean win, Boolean dogfall) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        int playTimes = rs.getInt("playtimes") + 1;
        int wintTimes = rs.getInt("wintimes");
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set playtimes = ? where id = ?");
        psql.setInt(1, playTimes);
        psql.setString(2, id);
        psql.executeUpdate();
        if(win){
            psql = connection.prepareStatement("update user set wintimes = ? where id = ?");
            psql.setInt(1, wintTimes+1);
            psql.setString(2, id);
            psql.executeUpdate();
        }
        psql.close();
        System.out.println("Succeeded updating the playing times!");
        updateScore(id, win, false, dogfall);
        userOnLine(id, getIP(id));
    }

    private void userStateChange(String id, String state) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set state = ? where id = ?");
        psql.setString(1, state);
        psql.setString(2, id);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded Changing user state!");
    }

    public void updateScore(String user, Boolean win, Boolean quit, Boolean dofdall) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + user + "\"";
        ResultSet rs = statement.executeQuery(sql);
        int score;
        rs.next();
        score = rs.getInt("scores");
        if(win){
            score += 3;
        }
        else{
            score -= 2;
        }
        if(quit)
            score -= 3;
        if(dofdall)
            score -= 2;
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set scores = ? where id = ?");
        psql.setInt(1, score);
        psql.setString(2, user);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded updating the scores!");
    }

    public void userOnLine(String id, String ip) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set ip = ? where id = ?");
        psql.setString(1, ip);
        psql.setString(2, id);
        psql.executeUpdate();
        psql.close();
        userStateChange(id, "o");
        System.out.println("Database:Succeeded online!");
    }

    public Boolean checkState(String id, String state) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        return state.equals(rs.getString("state"));
    }

    public void userOffLine(String id) throws SQLException, ClassNotFoundException {
        userStateChange(id, "f");
    }

    private void userGaming(String id) throws SQLException, ClassNotFoundException {
        userStateChange(id, "g");
    }

    //添加新棋局
    public void Addgame(String id, String playerA, String playerB) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        PreparedStatement psql;    //用来执行mysql语句
        psql = connection.prepareStatement("insert into gaming (id, playerA, playerB, chessboard, visitorA, visitorB, visitorC, visitorD, visitorE) "
                + "values(?,?,?,?,null,null,null,null,null)");
        psql.setString(1, id);
        psql.setString(2, playerA);
        psql.setString(3, playerB);
        String board = "";
        for(int i = 0; i < 225; i++)
            board = board.concat("0");
        psql.setString(4, board);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded adding a game!");
        userGaming(playerA);
        userGaming(playerB);
    }

    //更新棋局信息
    public void updateChessBoard(String id, int n, String color) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String chess = rs.getString("chessboard");
        StringBuilder strb = new StringBuilder(chess);
        strb.replace(n, n+1, color);
        chess = strb.toString();
        PreparedStatement psql;
        psql = connection.prepareStatement("update gaming set chessboard = ? where id = ?");
        psql.setString(1, chess);
        psql.setString(2, id);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded updating the chess board!");
    }


    public String getChessBoard(String id) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        return rs.getString("chessboard");
    }

    public ArrayList<String> getAllVisitors(String id) throws SQLException, ClassNotFoundException {
        ArrayList<String> allVisitors = new ArrayList<>();
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String[] dict = {"A", "B", "C", "D", "E"};
        for(int i = 0; i < 5; i++){
            String visitor = rs.getString("visitor" + dict[i]);
            if(visitor != null)
                allVisitors.add(rs.getString("visitor" + dict[i]));
        }
        return allVisitors;
    }

    public void deleteVisitors(String chessID, String visitID) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming where id = \"" + chessID + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String dict = "ABCDE";
        for(int i = 0; i < 5; i++){
            String s = rs.getString("visitor" + dict.charAt(i));
            if(s != null && s.equals(visitID)){
                PreparedStatement psql;
                psql = connection.prepareStatement("update gaming set visitor" + dict.charAt(i) + " = null where id = ?");
                psql.setString(1, chessID);
                psql.executeUpdate();
                psql.close();
                System.out.println("delete visitor"+dict.charAt(i) + ":" + visitID);
                break;
            }
        }
    }

    //添加观战者
    public Boolean addVisitor(String id, String visitorID) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        String dic = "ABCDE";
        while(rs.next()){
            for(int i = 0; i < 6; i++){
                if(i == 5){
                    System.out.println("Failed adding a visitor!");
                    System.out.println("Failed adding a visitor!");
                }
                String v = rs.getString("visitor"+dic.charAt(i));
                if(v == null){
                    PreparedStatement psql;
                    psql = connection.prepareStatement("update gaming set visitor" + dic.charAt(i) +" = ? where id = ?");
                    psql.setString(1, visitorID);
                    psql.setString(2, id);
                    psql.executeUpdate();
                    psql.close();
                    System.out.println("Succeeded adding a visitor:" + visitorID + "!");
                    return true;
                }
                if(v.equals(visitorID)) {
                    System.out.println("Failed adding a visitor!");
                    return false;
                }
            }
        }
        System.out.println("Failed adding a visitor!");
        return false;
    }

    //删除对局
    public void deleteGame(String id) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        PreparedStatement psql;
        psql = connection.prepareStatement("delete from gaming where id = ?");
        psql.setString(1, id);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded deleting the Game!");
    }

    public static void main(String args[]) {
        SqlManager sqlManager = new SqlManager();
        try {
            sqlManager.Connect();
            System.out.println("sleep");
            TimeUnit.MILLISECONDS.sleep(5000);
            System.out.println("wake up");
            //sqlManager.close();
            //sqlManager.Adduser("GJDW4", "HEHE", "192.169.1.1");
            //sqlManager.Addgame("1234", "GJDW", "FUCKER");
            //sqlManager.updateChessBoard("1234", 0, 11, "1");
            //sqlManager.addVistor("1234", "JINGYUl");
            //sqlManager.deleteGame("1234");
            //sqlManager.updateTimes("GJDW", true);
            //sqlManager.userOnLine("GJDW", "1.0.0.0");
            //sqlManager.userOnLine("GJDW2", "1.0.0.0");
            //sqlManager.userOnLine("GJDW3", "1.0.0.0");
            //sqlManager.userOnLine("GJDW4", "1.0.0.0");
            //sqlManager.Addgame("12345", "gjdw", "fucker");
            //System.out.println(sqlManager.getOnlineUser());
            //System.out.println(sqlManager.getOnlineGame());
            //sqlManager.usergaming("GJDW");
            //sqlManager.userOffLine("GJDW");
            sqlManager.addVisitor("W1VSyjch", "yjch");
            sqlManager.addVisitor("W1VSyjch", "sb");
            sqlManager.deleteVisitors("W1VSyjch", "sb");
            sqlManager.deleteVisitors("W1VSyjch", "yjch");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
