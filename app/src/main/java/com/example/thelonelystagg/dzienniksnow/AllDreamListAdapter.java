package com.example.thelonelystagg.dzienniksnow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thelonelystagg.dzienniksnow.database.models.Dream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllDreamListAdapter extends RecyclerView.Adapter<AllDreamListAdapter.DreamViewHolder>
    implements Filterable
{
    class DreamViewHolder extends RecyclerView.ViewHolder {
        private final ImageView dreamAudioIndicator;
        private final TextView dreamTitle;
        private final TextView dreamDate;
        private final TextView dreamDesc;


        private DreamViewHolder(View itemView) {
            super(itemView);
            dreamAudioIndicator = itemView.findViewById(R.id.audioIndicator);
            dreamTitle = itemView.findViewById(R.id.dreamTitle);
            dreamDate = itemView.findViewById(R.id.dreamDate);
            dreamDesc = itemView.findViewById(R.id.desc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDreamSelected(mDreamsFiltered.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onDreamSelectedLong(mDreamsFiltered.get(getAdapterPosition()));
                    return true;
                }
            });
        }
    }


    private final LayoutInflater mInflater;
    private Context mContext;
    List<Dream> mDreams; //Catched copy of dreams
    List<Dream> mDreamsFiltered; //start == mDreams
    private AllDreamListAdapterListener listener;


    AllDreamListAdapter(Context context, AllDreamListAdapterListener _listener) { mInflater = LayoutInflater.from(context); mContext = context; listener = _listener;}



    @Override
    public DreamViewHolder onCreateViewHolder( ViewGroup parent, int i) {
        View itemView = mInflater.inflate(R.layout.dream_row_item, parent, false);
        return new DreamViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DreamViewHolder holder, int position) {
        if (mDreamsFiltered !=null) {
            final Dream current = mDreamsFiltered.get(position);

            if(current.getIfAudioAsBoolean())
                holder.dreamAudioIndicator.setImageResource(R.mipmap.ic_with_sound);
            else
                holder.dreamAudioIndicator.setImageResource(R.mipmap.ic_with_no_sound);
            holder.dreamTitle.setText(current.getTitle());
            if(current.getDate().isEmpty())
                holder.dreamDate.setText("");
            else
            {
                String tmpdate = current.getDate();

                String month = tmpdate.substring(5,7);
                int foo;
                try {
                    foo = Integer.parseInt(month);
                }
                catch (NumberFormatException e)
                {
                    foo = 0;
                }
                foo++;
                foo = foo%12;
                holder.dreamDate.setText(tmpdate.substring(8,10)+ "."+
                                        ((foo<10)?"0":"")+foo+"."+
                                        tmpdate.substring(0,4)+" r.");
            }

            holder.dreamDesc.setText((current.getDescription().length()>50)?
                                                        (current.getDescription().substring(0,50)+"..."):
                                                        current.getDescription());

        }
        else {
            //Covers the case of data not being ready yet.
            holder.dreamAudioIndicator.setImageResource(R.mipmap.ic_with_no_sound);
            holder.dreamTitle.setText("Brak wpisu.");
            holder.dreamDate.setText("");
            holder.dreamDesc.setText("");
        }
    }

    @Override
    public int getItemCount(){
        if (mDreamsFiltered != null)
            return mDreamsFiltered.size();
        else return 0;
    }




    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                mDreamsFiltered = new ArrayList<>();

                if(constraint == null || constraint.length()==0)
                {
                    mDreamsFiltered.addAll(mDreams);
                }
                else
                {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for(Dream item : mDreams)
                    {
                        if(item.getTitle().toLowerCase().contains(filterPattern))
                        {
                            mDreamsFiltered.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = mDreamsFiltered;

                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                notifyDataSetChanged();
            }
        };
    }


    public interface AllDreamListAdapterListener {
        void onDreamSelected(Dream dream);
        void onDreamSelectedLong(Dream dream);
    }

    void setDreams(List<Dream> dreams){
        mDreams = dreams;
        mDreamsFiltered = dreams;
        notifyDataSetChanged();
    }



}
