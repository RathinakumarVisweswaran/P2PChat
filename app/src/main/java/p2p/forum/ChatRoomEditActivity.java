package p2p.forum;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import p2p.ask.R;

/**
 * lists the available forums and let you add new questions
 * through access to application service
 */
public class ChatRoomEditActivity extends Activity implements ApplicationService.OnServiceListener {

    private ChatRoomsFragmentList availableChatRooms;
    private DiscussionFragment conversationFragment;
    Button chatRoomCreateBtn;
    TextView newChatRoomName;
    ApplicationService applicationService;
    ChatRoomsFragmentList fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_page);
        availableChatRooms = new ChatRoomsFragmentList();
        conversationFragment = new DiscussionFragment();
        chatRoomCreateBtn = (Button)findViewById(R.id.ask_question_btn);
        newChatRoomName = (TextView) findViewById(R.id.chat_room_name_editText);
        Intent intent= new Intent(this, ApplicationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        View.OnClickListener createBtnListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newChatRoomName.getText().toString().length()<1)
                    Toast.makeText(ChatRoomEditActivity.this, "Chat already exists", Toast.LENGTH_SHORT).show();
                else
                {
                    Forum newForum = applicationService.askQuestion(newChatRoomName.getText().toString());
                    if( newForum != null)
                    {
                        fragment = (ChatRoomsFragmentList) getFragmentManager()
                                .findFragmentByTag("forums");
                        ChatRoomsFragmentList.ChatRoomsAdapter adapter = ((ChatRoomsFragmentList.ChatRoomsAdapter) fragment
                                .getListAdapter());
                        adapter.add(newForum);
                        adapter.notifyDataSetChanged();
                    }
                    else
                        Toast.makeText(ChatRoomEditActivity.this, "Chat already exists", Toast.LENGTH_SHORT).show();
                }
            }
        };
        chatRoomCreateBtn.setOnClickListener(createBtnListner);

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            ApplicationService.Binder cBinder = (ApplicationService.Binder) binder;
            applicationService = cBinder.getService();
            applicationService.setOnServiceListener(ChatRoomEditActivity.this);
            Toast.makeText(ChatRoomEditActivity.this, "Service Connection Success", Toast.LENGTH_SHORT)
                    .show();
            if(getFragmentManager().findFragmentByTag("forums") == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.chat_rooms_container, availableChatRooms, "forums").commit();
                getFragmentManager().executePendingTransactions();
            }

            fragment = (ChatRoomsFragmentList) getFragmentManager()
                    .findFragmentByTag("forums");
            ChatRoomsFragmentList.ChatRoomsAdapter adapter = ((ChatRoomsFragmentList.ChatRoomsAdapter) fragment
                    .getListAdapter());
            adapter.addAll(applicationService.forums);
            adapter.notifyDataSetChanged();

        }

        public void onServiceDisconnected(ComponentName className) {
            applicationService = null;
        }
    };

    @Override
    public void onDataReceived(final Forum forum) {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run() {
                (fragment).pushMessage(forum);
            }
        });

    }
}
