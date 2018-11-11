package app;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public class RegionGrow extends Algorithm{
    @Autowired SocketHandler handler;

    @Override
    void run() {

    }

    private void updateClient(){
        //handler.send();
    }
}
