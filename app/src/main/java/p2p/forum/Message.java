package p2p.forum;

/**
 * Created by rathinakumar on 8/11/15.
 * used to represent the messages shared in the forum
 */
public class Message {
    public String getDisplayText() {
        return sender.name+ " : "+text;
    }


    public String text;
    public User sender;

    public Message(String text, User sender) {
        this.text = text;
        this.sender = sender;
    }

    public boolean isEmpty()
    {
        if(text.isEmpty())
            return true;
        return false;
    }


}
