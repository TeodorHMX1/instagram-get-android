package com.dolphpire.instamanage.getLikesFragment;

import android.animation.ArgbEvaluator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.dolphpire.api.initializer.DolphPireApp;
import com.dolphpire.instamanage.R;
import com.dolphpire.instamanage.getLikesFragment.adapter.AdapterGetLikes;
import com.dolphpire.instamanage.getLikesFragment.model.ModelGetLikes;
import com.dolphpire.instamanage.igposts.IGPostsActivity;
import com.dolphpire.instamanage.views.DolphPireIS;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dolphpire.api.utils.NumberFormat.numberFormat;

public class GetLikesFragment extends Fragment
{

    @BindView(R.id.rvGetLikes)
    RecyclerView rvGetLikes;
    @BindView(R.id.llBottomPlaceOrder)
    LinearLayout llBottomPlaceOrder;
    @BindView(R.id.llCancelOrder)
    LinearLayout llCancelOrder;
    @BindView(R.id.llPlaceOrder)
    LinearLayout llPlaceOrder;
    @BindView(R.id.llBottomLoading)
    RelativeLayout llBottomLoading;
    @BindView(R.id.txtAmountCoins)
    TextView txtAmountCoins;
    @BindView(R.id.txtAmountLikes)
    TextView txtAmountLikes;
    @BindView(R.id.rlPostHolder)
    RelativeLayout rlPostHolder;
    @BindView(R.id.srlRefreshLikes)
    SwipeRefreshLayout srlRefreshLikes;
    @BindView(R.id.txtNoLikes)
    TextView txtNoLikes;
    @BindView(R.id.imvPostPreview)
    DolphPireIS imvPostPreview;
    private View mView;
    private Context mContext;
    private Activity mActivity;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<ModelGetLikes> mDataList;
    private AdapterGetLikes mAdapter;
    private ModelGetLikes mModelGetLikes;
    private int itemChose = -1;

    public GetLikesFragment()
    {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_get_likes, container, false);
        ButterKnife.bind(this, mView);

        mActivity = getActivity();

        return mView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        mDataList = new ArrayList<>();
        mAdapter = new AdapterGetLikes(mDataList, mActivity);
        mAdapter.setListener(this::showDialogOrder);

        rvGetLikes.setItemAnimator(new DefaultItemAnimator());
        linearLayoutManager = new LinearLayoutManager(mContext);
        rvGetLikes.setLayoutManager(linearLayoutManager);
        rvGetLikes.setHasFixedSize(false);
        rvGetLikes.setAdapter(mAdapter);

        llBottomPlaceOrder.setVisibility(View.GONE);
        llBottomLoading.setVisibility(View.GONE);

        setAnimation();

        populateRecyclerView();

        llCancelOrder.setOnClickListener(v -> llBottomPlaceOrder.setVisibility(View.GONE));

        llPlaceOrder.setOnClickListener(v ->
        {
            llBottomPlaceOrder.setVisibility(View.GONE);
            llBottomLoading.setVisibility(View.VISIBLE);
            if (DolphPireApp.getInstance().getUser().getCoins() >= mDataList.get(itemChose).getCoins() && DolphPireApp.getInstance().getUser().getIGPostModel() != null)
            {
                DolphPireApp.initializeApi()
                        .user().order()
                        .likes(
                                DolphPireApp.getInstance().getIGAccount().getIGID(),
                                itemChose,
                                DolphPireApp.getInstance().getUser().getIGPostModel().getId(),
                                DolphPireApp.getInstance().getUser().getIGPostModel().getImg480x480()
                        )
                        .addOnCompleteListener(() ->
                        {
                            llBottomLoading.setVisibility(View.GONE);
                            Toast.makeText(mContext, "Purchased " + mDataList.get(itemChose).getLikes() + " likes.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                        {
                            llBottomPlaceOrder.setVisibility(View.VISIBLE);
                            llBottomLoading.setVisibility(View.GONE);
                        })
                        .execute();
                DolphPireApp.getInstance().decreaseCoinsBy(mDataList.get(itemChose).getCoins());
            } else if (DolphPireApp.getInstance().getUser().getIGPostModel() == null) {
                Toast.makeText(mContext, "Please select a post", Toast.LENGTH_SHORT).show();
                llBottomLoading.setVisibility(View.GONE);
            } else
            {
                Toast.makeText(mContext, "Failed to purchase. You don't have enough coins.", Toast.LENGTH_SHORT).show();
                llBottomLoading.setVisibility(View.GONE);
            }
        });

        rlPostHolder.setOnClickListener(v ->
        {
            Intent intent = new Intent(mContext, IGPostsActivity.class);
            mContext.startActivity(intent);
        });

        srlRefreshLikes.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                refreshImageData();
            }
        });

        DolphPireApp.getInstance().syncIGPost()
                .setListener(mIGPostModel ->
                {

                    Glide.with(mActivity)
                            .load(mIGPostModel.getImg240x240())
                            .into(imvPostPreview);

                    txtNoLikes.setText(numberFormat(mIGPostModel.getLikes()));

                }, "IG_POST_LIKES_FRAGMENT");

        if (DolphPireApp.getInstance().getUser().getIGPostModel() != null)
        {
            Glide.with(mActivity)
                    .load(DolphPireApp.getInstance().getUser().getIGPostModel().getImg240x240())
                    .into(imvPostPreview);

            txtNoLikes.setText(numberFormat(DolphPireApp.getInstance().getUser().getIGPostModel().getLikes()));
            refreshImageData();
        } else
        {
            getIGImage();
        }
        DolphPireApp.getInstance().syncIGAccount()
                .setListener(mIGAccount -> getIGImage(), "IG_POST_LIKES_FRAGMENT");

    }

    private void refreshImageData()
    {
        srlRefreshLikes.setRefreshing(true);

        DolphPireApp.initializeApi().igAccount().posts()
                .withUserID(DolphPireApp.getInstance().getIGAccount().getIGID())
                .set()
                .addOnCompleteListener(mIGPostsModel ->
                {

                    boolean found = false;
                    int i = 0;
                    while (!found && i < mIGPostsModel.getPosts().size())
                    {
                        if (mIGPostsModel.getPosts().get(i).getId() == DolphPireApp.getInstance().getUser().getIGPostModel().getId())
                        {
                            found = true;
                        } else
                        {
                            i++;
                        }
                    }
                    if (!found)
                    {
                        i = 0;
                    }

                    Glide.with(mActivity)
                            .load(mIGPostsModel.getPosts().get(i).getImg240x240())
                            .into(imvPostPreview);

                    txtNoLikes.setText(numberFormat(mIGPostsModel.getPosts().get(i).getLikes()));

                    DolphPireApp.getInstance()
                            .setIGPosts(mIGPostsModel);

                    DolphPireApp.getInstance()
                            .setPost(mIGPostsModel.getPosts().get(i));

                    srlRefreshLikes.setRefreshing(false);

                })
                .execute();

    }

    private void getIGImage()
    {
        srlRefreshLikes.setRefreshing(true);
        DolphPireApp.initializeApi().igAccount().posts()
                .withUserID(DolphPireApp.getInstance().getIGAccount().getIGID())
                .set()
                .addOnCompleteListener(mIGPostsModel ->
                {

                    Glide.with(mActivity)
                            .load(mIGPostsModel.getPosts().get(0).getImg240x240())
                            .into(imvPostPreview);

                    txtNoLikes.setText(numberFormat(mIGPostsModel.getPosts().get(0).getLikes()));

                    DolphPireApp.getInstance()
                            .setIGPosts(mIGPostsModel);

                    DolphPireApp.getInstance()
                            .setPost(mIGPostsModel.getPosts().get(0));
                    srlRefreshLikes.setRefreshing(false);

                })
                .execute();
    }

    private void setAnimation()
    {

        final ArgbEvaluator evaluator = new ArgbEvaluator();
        final int start = ContextCompat.getColor(mContext, R.color.colorBgPlaceOrder1);
        final int end = ContextCompat.getColor(mContext, R.color.colorBgPlaceOrder2);
        final GradientDrawable gradientPO = (GradientDrawable) llBottomPlaceOrder.getBackground();
        final GradientDrawable gradientL = (GradientDrawable) llBottomLoading.getBackground();

        ValueAnimator animator = TimeAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(valueAnimator ->
        {
            float fraction = valueAnimator.getAnimatedFraction();
            int newStrat = (int) evaluator.evaluate(fraction, start, end);
            int newEnd = (int) evaluator.evaluate(fraction, end, start);
            int[] newArray = {newStrat, newEnd};
            gradientPO.setColors(newArray);
            gradientL.setColors(newArray);
        });

        animator.start();

    }

    private void showDialogOrder(int pos)
    {

        itemChose = pos;

        txtAmountCoins.setText(String.valueOf(mDataList.get(pos).getCoins()));
        txtAmountLikes.setText(String.valueOf(mDataList.get(pos).getLikes()));

        llBottomPlaceOrder.setVisibility(View.VISIBLE);

    }

    private void populateRecyclerView()
    {

        mModelGetLikes = new ModelGetLikes(20, 10);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(50, 25);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(100, 50);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(200, 100);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(500, 250);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(1000, 500);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(2000, 1000);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(4000, 2000);
        mDataList.add(mModelGetLikes);

        mModelGetLikes = new ModelGetLikes(10000, 5000);
        mDataList.add(mModelGetLikes);

        mAdapter.notifyDataSetChanged();

    }

}