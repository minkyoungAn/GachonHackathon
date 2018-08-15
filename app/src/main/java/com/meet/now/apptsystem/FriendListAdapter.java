package com.meet.now.apptsystem;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class FriendListAdapter extends BaseAdapter {

    private Context context;
    private List<Friend> friendList;

    public FriendListAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int i) {
        return friendList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = View.inflate(context, R.layout.friend, null);
        ImageButton userPhoto = (ImageButton) v.findViewById(R.id.userPhoto);
        TextView userNickname = (TextView) v.findViewById(R.id.userNickname);
        TextView statusMsg = (TextView) v.findViewById(R.id.statusMsg);

        String photo = friendList.get(i).getUserPhoto();
        String fnickname = friendList.get(i).getFriendNickname();
        String unickname = friendList.get(i).getUserNickname();
        String Msg = friendList.get(i).getUserStatusmsg();

        if (photo.equals("null")) {
            userPhoto.setImageResource(R.drawable.ic_person_outline_black_24dp);
        } else {
            // 사용자 이미지 추가
        }

        if(fnickname.equals("null")){
            userNickname.setText(unickname);
        }else{
            userNickname.setText(fnickname);
        }

        if (Msg.equals("null")) {
            statusMsg.setText("");
        } else {
            statusMsg.setText(Msg);
        }

        return v;
    }
}