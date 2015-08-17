package p2p.forum;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import p2p.ask.R;

/**
 * used to display the dynamic content of the forum.
 * The messages sent by other users are displayed and new messages from the
 * current user are received
 */

public class DiscussionFragment extends Fragment {
    private View view;

    private TextView chatLine;
    private ListView listView;
    ChatMessageAdapter adapter = null;
    private List<Message> items = new ArrayList<Message>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.forum_layout, container, false);
        chatLine = (TextView) view.findViewById(R.id.txtChatLine);
        listView = (ListView) view.findViewById(android.R.id.list);
        adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
                items);
        listView.setAdapter(adapter);

        view.findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String text = chatLine.getText().toString();
                        ((ForumActivity)getActivity()).applicationService.handleForumMessage(((ForumActivity) getActivity()).topic, text,
                                ((ForumActivity) getActivity()).applicationService.localUser.name,
                                ((ForumActivity) getActivity()).applicationService.localUser.inetAddress);
                        chatLine.setText("");
                        chatLine.clearFocus();
                    }
                });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addMessagesToRoom(List<Message> messages) {

        for(Message message : messages)
            adapter.add(message);
        adapter.notifyDataSetChanged();
    }

    public void addMessageToRoom(Message readMessage) {
        adapter.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    public class ChatMessageAdapter extends ArrayAdapter<Message> {

        List<String> messages = null;

        public ChatMessageAdapter(Context context, int textViewResourceId,
                                  List<Message> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            Message message = items.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);

                if (nameText != null) {
                    nameText.setText(message.getDisplayText());
                }
            }
            return v;
        }
    }
}
