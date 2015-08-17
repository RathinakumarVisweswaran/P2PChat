package p2p.forum;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import p2p.ask.R;

/**
 * holds the forum discussion fragment, provides access to the application service
 */

public class ForumActivity extends Activity {

    ApplicationService applicationService;
    DiscussionFragment cmFragment;
    Forum forum;
    String topic;
    public Handler messageHandler;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            applicationService = ((ApplicationService.Binder) binder).getService();
            forum = applicationService.getForumBy(topic);
            forum.setMessageHandler(messageHandler);
            cmFragment.addMessagesToRoom(forum.messages);
            Toast.makeText(ForumActivity.this, "Service Connection Success", Toast.LENGTH_SHORT)
                    .show();
        }

        public void onServiceDisconnected(ComponentName className) {
            applicationService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discuss_question);
        Intent intent= new Intent(this, ApplicationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        cmFragment = new DiscussionFragment();

        topic = getIntent().getStringExtra("question");
        if(getFragmentManager().findFragmentByTag("forums") == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.forum_container, cmFragment).commit();
            getFragmentManager().executePendingTransactions();
        }

        messageHandler = new Handler(){
            @Override
            public void handleMessage(android.os.Message message){
                Message m = (Message)message.obj;
                ((DiscussionFragment)cmFragment).addMessageToRoom(m);
            }
        };
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
