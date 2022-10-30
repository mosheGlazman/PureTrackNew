package com.supercom.puretrack.data.source.remote.requests;

import android.util.Log;

import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;

public class CycleManager {

    public static CycleManager instance;
    public static CycleManager getInstance() {
        if(instance==null){
            instance=new CycleManager();
        }
        return instance;
    }
private CycleManager(){

}
    private   Hashtable<UUID,String> runningTasksUid=new Hashtable<>();
    private Date start=new Date();
    private boolean hasTerminateTask;
    private int terminateTaskCounter=0;
private  UUID uid;
private int counter=0;
    public void putTask(BaseAsyncTaskRequest t) {
        terminateTaskCounter=3;
        runningTasksUid.put(t.executeUid,t.getTagName());
    }

    public boolean setTerminateTask() {
        if (hasTerminateTask) {
            return false;
        }
        terminateTaskCounter = 3;
        hasTerminateTask = true;
        return true;
    }


    public void removeTask(BaseAsyncTaskRequest t) {
        runningTasksUid.remove(t.executeUid);
    }

    public boolean canRunTerminateTask() {
        if (!runningTasksUid.isEmpty() || terminateTaskCounter > 0){
            if(runningTasksUid.isEmpty()){
                terminateTaskCounter--;
            }

            return false;
        }
        return true;
    }
    void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int startNew() {
        counter++;
        uid=UUID.randomUUID();
        hasTerminateTask=false;
        terminateTaskCounter=3;
        runningTasksUid.clear();
        start=new Date();
        return counter;
    }
}
