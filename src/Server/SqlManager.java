package Server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

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
    public Boolean Adduser(String id, String password, String ip) throws SQLException, ClassNotFoundException {
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
        return true;
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
        if(password.equals(correctPassword))
            return true;
        else
            return false;
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
        String ip = rs.getString("ip");
        return ip;
    }

    public Boolean checkOnline(String username) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + username + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String state = rs.getString("state");
        return state.equals("o");
    }

    public String getOnlineUser() throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where state = 'o'";
        ResultSet rs = statement.executeQuery(sql);
        String onlineUser = "";
        while(rs.next())
            onlineUser +=  rs.getString("id") + " " + rs.getInt("scores") + "/";
        return onlineUser;
    }

    public String getOnlineGame() throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from gaming";
        ResultSet rs = statement.executeQuery(sql);
        String onlineGame = "";
        while(rs.next())
            onlineGame = onlineGame + rs.getString("id") + "/";
        return onlineGame;
    }

    public Boolean updateTimes(String id, Boolean win) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        int playtimes = rs.getInt("playtimes") + 1;
        int wintimes = rs.getInt("wintimes");
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set playtimes = ? where id = ?");
        psql.setInt(1, playtimes);
        psql.setString(2, id);
        psql.executeUpdate();
        if(win){
            psql = connection.prepareStatement("update user set wintimes = ? where id = ?");
            psql.setInt(1, wintimes+1);
            psql.setString(2, id);
            psql.executeUpdate();
        }
        psql.close();
        System.out.println("Succeeded updating the playing times!");
        userOnLine(id, getIP(id));
        return true;
    }

    private Boolean userStateChange(String id, String state) throws SQLException, ClassNotFoundException {
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
        return true;
    }

    public Boolean userOnLine(String id, String ip) throws SQLException, ClassNotFoundException {
        if(connection.isClosed()){
            Connect();
            System.out.println("Reconnecting to the Database!");
        }
        Statement statement = connection.createStatement();
        String sql = "select * from user where id = \"" + id + "\"";
        ResultSet rs = statement.executeQuery(sql);
        rs.next();
        String  state = rs.getString("state");
        if(state.equals("o"))
            return false;
        PreparedStatement psql;
        psql = connection.prepareStatement("update user set ip = ? where id = ?");
        psql.setString(1, ip);
        psql.setString(2, id);
        psql.executeUpdate();
        psql.close();
        if(userStateChange(id, "o")) {
            System.out.println("Database:Succeeded online!");
            return true;
        }
        return false;
    }

    public Boolean userOffLine(String id) throws SQLException, ClassNotFoundException {
        if(userStateChange(id, "f")) {
            System.out.println("Database:Succeeded offline!");
            return true;
        }else{
            System.out.println("Database:Failed offline!");
            return false;
        }
    }

    private Boolean usergaming(String id) throws SQLException, ClassNotFoundException {
        if(userStateChange(id, "g")) {
            System.out.println("Database:Succeeded gaming!");
            return true;
        }else{
            System.out.println("Database:Failed gaming!");
            return false;
        }
    }

    //添加新棋局
    public Boolean Addgame(String id, String playerA, String playerB) throws SQLException, ClassNotFoundException {
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
        usergaming(playerA);
        usergaming(playerB);
        return true;
    }

    //更新棋局信息
    public Boolean updateChessBoard(String id, int n, String color) throws SQLException, ClassNotFoundException {
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
        return true;
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
        String[] dict = {"A", "B", "C", "D", "E"};
        for(int i = 0; i < 5; i++){
            if(!rs.getString("visitor" + dict[i]).equals(visitID)){

            }
        }
    }

    //添加观战者
    public Boolean addVistor(String id, String visitorID) throws SQLException, ClassNotFoundException {
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
                    System.out.println("Succeeded adding a visitor!");
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
    public boolean deleteGame(String id) throws SQLException {
        if(connection.isClosed()){
            System.out.println("Failed deleting the Game!");
            return false;
        }
        PreparedStatement psql;
        psql = connection.prepareStatement("delete from gaming where id = ?");
        psql.setString(1, id);
        psql.executeUpdate();
        psql.close();
        System.out.println("Succeeded deleting the Game!");
        return true;
    }

    public void close() throws SQLException {
        connection.close();
    }

    public static void main(String args[]) {
        SqlManager sqlManager = new SqlManager();
        try {
            sqlManager.Connect();
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
            System.out.println(sqlManager.checkOnline("GJDW"));
            System.out.println(sqlManager.checkOnline("GJDW2"));
            //sqlManager.Addgame("12345", "gjdw", "fucker");
            //System.out.println(sqlManager.getOnlineUser());
            //System.out.println(sqlManager.getOnlineGame());
            //sqlManager.usergaming("GJDW");
            sqlManager.userOffLine("GJDW");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
