package p2p.forum;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;


/**
 * represents the Forum for an asked question
 * holds the participants and the contents shared in the forum
 */
public class Forum {
    public String question;
    public User owner;
    public List<User> participants = new ArrayList<>();
    public List<Message> messages = new ArrayList<>();
    Handler messageHandler;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Forum forum = (Forum) o;

        return question.equals(forum.question);

    }

    @Override
    public int hashCode() {
        return question.hashCode();
    }

    public boolean addMember(User u) {
        if( ! participants.contains(u))
        {
            participants.add(u);
            return true;
        }
        return false;
    }

    public void addMessage(String chatMessage, User sender) {
        Message m = new Message(chatMessage, sender);
        messages.add(m);
        if(messageHandler !=null)
        {
            android.os.Message mm = new android.os.Message();
            mm.obj = m;
            messageHandler.sendMessage(mm);
        }
    }

    public void setMessageHandler(Handler handler) {
        this.messageHandler = handler;
    }
}
