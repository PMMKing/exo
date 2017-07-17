package com.haolb.client.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haolb.client.R;
import com.haolb.client.adapter.utils.QSimpleAdapter;

import static com.haolb.client.domain.response.MessageResult.Message;

public class MessageAdp extends QSimpleAdapter<Message> {


    public MessageAdp(Context context) {
        super(context);
    }

    @Override
    protected View newView(Context context, ViewGroup parent) {
        return inflate(R.layout.activity_msg_item, null, false);
    }

    @Override
    protected void bindView(View view, Context context, Message item,
                            int position) {
        View unReadMsg =   view.findViewById(R.id.unread_msg_number);
        TextView tvMessageType = (TextView) view.findViewById(R.id.tv_message_type);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
        TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
        tvMessageType.setText(getDesByType(item.type));
        tvName.setText(item.community);
        tvTitle.setText(item.title);
        tvDate.setText(item.createtime);
        unReadMsg.setVisibility(View.GONE);
        tvTitle.setText(item.title);
        if(item.isnew){
            tvTitle.setTextColor(context.getResources().getColor(R.color.common_color_black));
//            unReadMsg.setVisibility(View.VISIBLE);
        }else {
            tvTitle.setTextColor(context.getResources().getColor(R.color.common_color_light_gray));
//            unReadMsg.setVisibility(View.GONE);
        }
    }

    /*
    1 通知 2 活动 3 公告

    * */
    public String getDesByType(int type) {
        if (type == 1) {
            return "【通知】";
        } else if (type == 2) {
            return "【活动】";
        } else if (type == 3) {
            return "【公告】";
        }else {
            return "【消息】";
        }
    }

}
