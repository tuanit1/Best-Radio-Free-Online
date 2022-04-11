package com.alexnguyen.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.alexnguyen.adapter.AdapterOnDemand;
import com.alexnguyen.asyncTasks.LoadOnDemand;
import com.alexnguyen.interfaces.RadioListListener;
import com.alexnguyen.item.ItemOnDemandCat;
import com.alexnguyen.item.ItemRadio;
import com.alexnguyen.radiofreeonline.R;
import com.alexnguyen.utils.Constants;
import com.alexnguyen.utils.Methods;
import com.alexnguyen.utils.SharedPref;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentOnDemandDetails extends Fragment {

    private RecyclerView recyclerView;
    private AdapterOnDemand adapter;
    private ImageView imageView_ondemand;
    private SearchView searchView;
    private ArrayList<ItemRadio> arraylist;
    private ItemOnDemandCat itemOnDemandCat;
    private CircularProgressBar progressBar;
    private TextView textView_empty;
    public static AppCompatButton button_try;
    private LinearLayout ll_empty;
    private String errr_msg;
    private SharedPref sharedPref;
    private Methods methods;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_ondemand, container, false);

        itemOnDemandCat = (ItemOnDemandCat) getArguments().getSerializable("item");

        methods = new Methods(getActivity());
        sharedPref = new SharedPref(getActivity());

        arraylist = new ArrayList<>();
        imageView_ondemand = rootView.findViewById(R.id.imageView_ondemand);
        progressBar = rootView.findViewById(R.id.progressBar_on);

        ll_empty = rootView.findViewById(R.id.ll_empty);
        textView_empty = rootView.findViewById(R.id.textView_empty_msg);
        button_try = rootView.findViewById(R.id.button_empty_try);
        ViewCompat.setBackgroundTintList(button_try, ColorStateList.valueOf(sharedPref.getFirstColor()));

        Picasso.get()
                .load(itemOnDemandCat.getImage())
                .into(imageView_ondemand);

        GridLayoutManager lLayout = new GridLayoutManager(getActivity(), 2);

        recyclerView = rootView.findViewById(R.id.recyclerView_on);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(lLayout);
        recyclerView.setNestedScrollingEnabled(false);

        loadOnDemand();

        button_try.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadOnDemand();
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    private void loadOnDemand() {
        if (methods.isConnectingToInternet()) {
            LoadOnDemand loadOnDemand = new LoadOnDemand(new RadioListListener() {
                @Override
                public void onStart() {
                    arraylist.clear();
                    ll_empty.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

                @Override
                public void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListRadio) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (!verifyStatus.equals("-1")) {
                                arraylist.addAll(arrayListRadio);
                                errr_msg = getString(R.string.items_not_found);
                                setAdapter();
                            } else {
                                errr_msg = message;
                                methods.getVerifyDialog(getString(R.string.error_unauth_access), message);
                                setEmpty();
                            }
                        } else {
                            errr_msg = getString(R.string.error_server);
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }, methods.getAPIRequest(Constants.METHOD_ONDEMAND, 0, "", "", "", "", itemOnDemandCat.getId(), "", "", "", "", "", "", "", null));
            loadOnDemand.execute();
        } else {
            errr_msg = getString(R.string.internet_not_connected);
            setEmpty();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (arraylist.size() > 0) {
                if (searchView.isIconified()) {
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    public void setAdapter() {
        adapter = new AdapterOnDemand(getActivity(), arraylist);
        recyclerView.setAdapter(adapter);
        setEmpty();
    }

    private void setEmpty() {
        if (arraylist.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        } else {
            textView_empty.setText(errr_msg);
            recyclerView.setVisibility(View.GONE);
            ll_empty.setVisibility(View.VISIBLE);
        }
    }
}