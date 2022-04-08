package com.alexnguyen.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.alexnguyen.interfaces.InterAdListener;
import com.alexnguyen.item.ItemRadio;
import com.alexnguyen.radiofreeonline.BaseActivity;
import com.alexnguyen.radiofreeonline.R;
import com.alexnguyen.utils.Constants;
import com.alexnguyen.utils.DBHelper;
import com.alexnguyen.utils.Methods;

import java.util.ArrayList;


public class AdapterCityDetails extends RecyclerView.Adapter<AdapterCityDetails.MyViewHolder> {

    private DBHelper dbHelper;
    private Context context;
    private ArrayList<ItemRadio> arraylist;
    private ArrayList<ItemRadio> filteredArrayList;
    private NameFilter filter;
    private Methods methods;

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_title, textView_views, textView_lang;
        private ImageView imageView_fav, imageView;
        private CardView cardView;

        private MyViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.row_layout);
            textView_views = view.findViewById(R.id.textView_view);
            textView_lang = view.findViewById(R.id.textView_list_lang);
            textView_title = view.findViewById(R.id.textView_radio_name);
            imageView = view.findViewById(R.id.row_logo);
            imageView_fav = view.findViewById(R.id.imageView_fav);
        }
    }

    public AdapterCityDetails(Context context, ArrayList<ItemRadio> list) {
        this.context = context;
        this.arraylist = list;
        this.filteredArrayList = list;
        dbHelper = new DBHelper(context);


        methods = new Methods(context, interAdListener);
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Constants.GRID_PADDING, r.getDisplayMetrics());
        Constants.columnWidth = (int) ((methods.getScreenWidth() - ((Constants.NUM_OF_COLUMNS + 1) * padding)) / Constants.NUM_OF_COLUMNS);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_cityradio_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Boolean isFav = checkFav(position);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Constants.columnWidth, Constants.columnWidth);
        params.setMargins(0, 0, 0, 20);
        holder.cardView.setLayoutParams(params);
        if (isFav) {
            holder.imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_hover));
        } else {
            holder.imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav));
        }

        holder.textView_views.setText(Methods.format(Double.parseDouble(arraylist.get(position).getViews())));
        holder.textView_title.setText(arraylist.get(position).getRadioName());
        holder.textView_lang.setText(arraylist.get(position).getLanguage());

        Picasso.get()
                .load(methods.getImageThumbSize(arraylist.get(holder.getAdapterPosition()).getRadioImageurl(),""))
                .into(holder.imageView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methods.showInter(holder.getAdapterPosition(),"");
                methods.showNativeAd();
            }
        });

        holder.imageView_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dbHelper.addORremoveFav(arraylist.get(holder.getAdapterPosition()))) {
                    holder.imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav_hover));
                    methods.showToast(context.getString(R.string.add_to_fav));
                } else {
                    holder.imageView_fav.setImageDrawable(context.getResources().getDrawable(R.mipmap.fav));
                    methods.showToast(context.getString(R.string.remove_from_fav));
                }
            }
        });
    }

    private Boolean checkFav(int pos) {
        return dbHelper.checkFav(arraylist.get(pos));
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new NameFilter();
        }
        return filter;
    }

    private class NameFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            constraint = constraint.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (constraint.toString().length() > 0) {
                ArrayList<ItemRadio> filteredItems = new ArrayList<>();

                for (int i = 0, l = filteredArrayList.size(); i < l; i++) {
                    String nameList = filteredArrayList.get(i).getRadioName();
                    if (nameList.toLowerCase().contains(constraint))
                        filteredItems.add(filteredArrayList.get(i));
                }
                result.count = filteredItems.size();
                result.values = filteredItems;
            } else {
                synchronized (this) {
                    result.values = filteredArrayList;
                    result.count = filteredArrayList.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            arraylist = (ArrayList<ItemRadio>) results.values;
            notifyDataSetChanged();
        }
    }

    private InterAdListener interAdListener = new InterAdListener() {
        @Override
        public void onClick(int position, String type) {
            Constants.arrayList_radio.clear();
            Constants.arrayList_radio.addAll(arraylist);
            ((BaseActivity) context).clickPlay(position, true);
        }
    };
}