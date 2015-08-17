package p2p.forum;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import p2p.ask.R;

/**
 * A simple ListFragment that shows the available services as published by the
 * peers
 */
public class ChatRoomsFragmentList extends ListFragment {

    ChatRoomsAdapter listAdapter = null;
    DiscussionFragment chatFragment = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forum_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new ChatRoomsAdapter(this.getActivity(),
                android.R.layout.simple_list_item_2, android.R.id.text1,
                new ArrayList<Forum>());
        setListAdapter(listAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        ((TextView) v.findViewById(android.R.id.text2)).setText("Connecting");
        Intent intent= new Intent(getActivity(), ForumActivity.class);
        intent.putExtra("question", ((Forum) l.getItemAtPosition(position)).question);
        startActivity(intent);
    }

    public void pushMessage(Forum forum) {
        listAdapter.add(forum);
        listAdapter.notifyDataSetChanged();
    }

    public class ChatRoomsAdapter extends ArrayAdapter<Forum> {

        private List<Forum> rooms = new ArrayList<>();

        public ChatRoomsAdapter(Context context, int resource,
                                int textViewResourceId, List<Forum> rooms) {
            super(context, resource, textViewResourceId, rooms);
            this.rooms = rooms;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }
            Forum room = rooms.get(position);
            if (room != null) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);

                if(room.question != null)
                {
                    nameText.setText(room.question);
                }
                /*TextView statusText = (TextView) v
                        .findViewById(android.R.id.text2);
                statusText.setText(getDeviceStatus(room.device.status));*/
            }
            return v;
        }

    }

    public static String getDeviceStatus(int statusCode) {
        switch (statusCode) {
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

}
