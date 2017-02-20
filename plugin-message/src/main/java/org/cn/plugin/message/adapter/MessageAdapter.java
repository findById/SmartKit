package org.cn.plugin.message.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cn.plugin.message.MessageActivity;
import org.cn.plugin.message.R;
import org.cn.plugin.message.databinding.ItemMessageBinding;
import org.cn.plugin.message.model.Message;
import org.cn.plugin.message.model.MessageType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenning on 17-1-18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private LayoutInflater mInflater;

    private List<Message> mData = new ArrayList<>();

    private RecyclerView mRecyclerView;

    public MessageAdapter(Context ctx, RecyclerView view) {
        mInflater = LayoutInflater.from(ctx);
        mRecyclerView = view;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemMessageBinding bind = DataBindingUtil.inflate(mInflater, R.layout.item_message, parent, false);
        return new ViewHolder(bind);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mData.get(position);
        holder.mBind.setMessage(message);
        //holder.mBind
        holder.onBindData(message);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void add(Message message) {
        mData.add(message);
        notifyItemInserted(getItemCount());
        mRecyclerView.smoothScrollToPosition(getItemCount());
    }

    public void addAll(List<Message> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }
        int position = getItemCount();
        mData.addAll(messageList);
        notifyItemRangeChanged(position, getItemCount());
        mRecyclerView.smoothScrollToPosition(getItemCount());
    }

    public void setData(List<Message> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(getItemCount());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemMessageBinding mBind;

        public ViewHolder(ItemMessageBinding mBind) {
            super(mBind.getRoot());
            this.mBind = mBind;
        }

        public void onBindData(Message message) {
            // Message message = mBind.getMessage();
            mBind.notify.setVisibility(View.GONE);
            switch (message.msgType) {
                case MessageType.NOTIFY: {
                    mBind.leftLayout.setVisibility(View.GONE);
                    mBind.rightLayout.setVisibility(View.GONE);

                    mBind.notify.setVisibility(View.VISIBLE);
                    mBind.notify.setText(message.body);
                    break;
                }
                case MessageType.TEXT: {
                    if (MessageActivity.userId.equals(message.producerId)) {
                        mBind.leftLayout.setVisibility(View.GONE);
                        mBind.rightLayout.setVisibility(View.VISIBLE);

                        mBind.leftContent.setText(message.body);
                    } else {
                        mBind.leftLayout.setVisibility(View.VISIBLE);
                        mBind.rightLayout.setVisibility(View.GONE);

                        mBind.rightContent.setText(message.body);
                    }
                    break;
                }
                default: {
                    mBind.leftLayout.setVisibility(View.GONE);
                    mBind.rightLayout.setVisibility(View.GONE);

                    mBind.notify.setVisibility(View.VISIBLE);
                    mBind.notify.setText("unsupported type");
                    break;
                }
            }
        }
    }

}
