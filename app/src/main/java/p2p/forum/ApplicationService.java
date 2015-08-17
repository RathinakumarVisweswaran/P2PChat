package p2p.forum;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a bind service, used to maintain the state of the overall application
 * Stores the Forums and Users part of the application
 * Also interacts with the Network Helper for all network Activities
 */
public class ApplicationService extends Service {

    User localUser;
    List<User> users;
    List<Forum> forums;
    private final IBinder cBinder = new Binder();
    String localForumListBroadcast ="";
    private OnServiceListener mOnServiceListener = null;
    NetworkHelper helper;

    public ApplicationService()
    {
        initialize();
    }

    private void initialize()
    {
        helper = new NetworkHelper();
        users = new ArrayList<>();
        forums = new ArrayList<>();
    }

    public void setLocalUser(String userName, String address) {
        localUser = new User();
        localUser.name = userName;
        try {
            localUser.inetAddress = InetAddress.getByName(address);
            NetworkHelper.localAddress = localUser.inetAddress;
            localForumListBroadcast = userName+"^";
            NetworkHelper.initiate(this);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    private boolean isUserPresent(User user)
    {
        return (users.contains(user))? true:false;
    }

    public interface OnServiceListener{
        void onDataReceived(Forum forum);
    }

    public void setOnServiceListener(OnServiceListener serviceListener){
        mOnServiceListener = serviceListener;
    }

    public class Binder extends android.os.Binder {
        ApplicationService getService() {
            return ApplicationService.this;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cBinder;
    }

    public Forum askQuestion(String question){
        Forum forum = new Forum();
        forum.question = question;
        forum.owner = localUser;

        if(forums.contains(forum)) {
            Log.i("Chat Service", "Question already asked");
            return null;
        }
        else
        {
            forums.add(forum);
            localForumListBroadcast += forum.question +"^";
            return forum;
        }
    }

    public void updateForumListFromBroadCast(Forum forum) {
        forums.add(forum);
        if(mOnServiceListener != null) {
            mOnServiceListener.onDataReceived(forum);
        }
    }

    public void handleForumMessage(String topic, String discussionMessage, String userName, InetAddress senderAddress) {
        Forum forum = getForumBy(topic);
        User sender = new User();
        sender.inetAddress = senderAddress;
        sender.name = userName;
        forum.addMessage(discussionMessage, sender);
        if(forum.owner.equals(localUser))
        {
            if( ! isUserPresent(sender))
                users.add(sender);
            forum.addMember(sender);
            String msg = topic+"^"+discussionMessage+"^"+sender.name;
            broadcastToOtherMembers(forum, sender, msg);
        }
        else
            if(senderAddress.equals(localUser.inetAddress)) {
                String msg = topic+"^"+discussionMessage+"^"+localUser.name;
                NetworkHelper.sendMessage(forum.owner.inetAddress, msg);
            }
    }

    public void broadcastToOtherMembers(Forum forum, User sender, String message)
    {
        for(User member: forum.participants)
            if( ! (member.equals(localUser) || member.equals(sender)) )
                NetworkHelper.sendMessage(member.inetAddress, message);
    }

    Forum getForumBy(String topic)
    {
        for(Forum forum : forums)
            if(forum.question.equals(topic))
                return forum;
        return null;
    }


}
