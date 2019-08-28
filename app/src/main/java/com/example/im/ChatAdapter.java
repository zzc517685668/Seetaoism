package com.example.im;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;

import java.util.List;

/**
 * Created by $lzj on 2019/6/11.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<EMMessage> list;
    private String myName;

    public ChatAdapter(Context context, List<EMMessage> list, String myName) {
        this.context = context;
        this.list = list;
        this.myName = myName;
    }

    public void setList(List<EMMessage> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {

        RecyclerView.ViewHolder viewHolder = null;
        if (type == 0){
            View inflate = LayoutInflater.from(context).inflate(R.layout.layout_right, null);
            viewHolder = new MyViewHolder(inflate);
        }else if (type == 1){
            View inflate = LayoutInflater.from(context).inflate(R.layout.layout_left, null);
            viewHolder = new UserViewHolder(inflate);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        final EMMessage emMessage = list.get(position);

        if (viewHolder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) viewHolder;
            myViewHolder.info.setText("Form:" + emMessage.getFrom() +", To: "+ emMessage.getTo());
            myViewHolder.content.setText("Text：" + emMessage.getBody().toString());

        }else if (viewHolder instanceof UserViewHolder){
            UserViewHolder userViewHolder = (UserViewHolder) viewHolder;

            userViewHolder.info.setText("Form:" + emMessage.getFrom() +", To: "+ emMessage.getTo());
            userViewHolder.content.setText("Text：" + emMessage.getBody().toString());

        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String切割，获取本地播放路径
                EMMessageBody body = emMessage.getBody();

                String s = body.toString();
                String localUrl = "";
                String[] split = s.split(",");
                for (int i = 0; i < split.length; i++) {
                    String s1 = split[i];
                    String[] split1 = s1.split(":");
                    for (int j = 0; j < split1.length; j++) {
                        String s2 = split1[j];
                        Log.d("abc", "onClick: "+ s2);
                        if (s2.startsWith("/storage/emulated")){
                            localUrl = s2;
                            break;
                        }
                    }
                }

                Log.d("abc", "localUrl: "+localUrl);

                if (mListener!=null){
                    mListener.onItemClick(localUrl);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getFrom().equals(myName)){
            return 0;
        }else {
            return 1;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView info;
        private TextView content;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            info = itemView.findViewById(R.id.info);
            content = itemView.findViewById(R.id.content);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        private TextView info;
        private TextView content;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            info = itemView.findViewById(R.id.info);
            content = itemView.findViewById(R.id.content);
        }
    }


    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(String localUrl);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }
}
