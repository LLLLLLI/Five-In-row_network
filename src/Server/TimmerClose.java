package Server;

import java.util.concurrent.TimeUnit;

/**
 * Created by 王赣宇 on 2017/10/15.
 */

public class TimmerClose extends Thread {

    private int seconds;
    private Thread thread;


    TimmerClose(Thread thread, int seconds){
        this.seconds = seconds;
        this.thread = thread;
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(seconds);
            if(thread.isAlive()){
                thread.interrupt();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
