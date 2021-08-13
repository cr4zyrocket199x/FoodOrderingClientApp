package com.cr4zyrocket.foodorderingapp111.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cr4zyrocket.foodorderingapp111.Database.Database;
import com.cr4zyrocket.foodorderingapp111.Favourite;
import com.cr4zyrocket.foodorderingapp111.FoodDetail;
import com.cr4zyrocket.foodorderingapp111.FoodList;
import com.cr4zyrocket.foodorderingapp111.Interface.ItemClickListener;
import com.cr4zyrocket.foodorderingapp111.Model.FavouriteFood;
import com.cr4zyrocket.foodorderingapp111.R;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
class FavouriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvFavFoodName,tvFavFoodPrice;
    //public ImageView ivFavFoodImage;
    private ItemClickListener itemClickListener;

    public FavouriteViewHolder(@NonNull View itemView) {
        super(itemView);
        tvFavFoodName=itemView.findViewById(R.id.tvFavFoodName);
        tvFavFoodPrice=itemView.findViewById(R.id.tvFavFoodPrice);
        //ivFavFoodImage=itemView.findViewById(R.id.ivFavFoodImage);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAbsoluteAdapterPosition(),false);
    }

}
public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteViewHolder>{
    private List<FavouriteFood>favourites;
    private Context context;

    public FavouriteAdapter() {
    }

    public FavouriteAdapter(List<FavouriteFood> favourites, Context context) {
        this.favourites = favourites;
        this.context = context;
    }

    @NonNull
    @Override
    public FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.favourite_item,parent,false);
        return new FavouriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FavouriteViewHolder holder, final int position) {
        holder.tvFavFoodName.setText(favourites.get(position).getFoodName());
        Locale locale=new Locale("vi","VN");
        NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
        holder.tvFavFoodPrice.setText(fmt.format(Integer.parseInt(favourites.get(position).getFoodPrice())));
//        String foodImage=favourites.get(position).getFoodImage();
//        Picasso.with(context).load(Uri.parse(foodImage)).into(holder.ivFavFoodImage, new Callback() {
//            @Override
//            public void onSuccess() {
//            }
//
//            @Override
//            public void onError() {
//                holder.ivFavFoodImage.setImageResource(R.drawable.image_notfound);
//            }
//        });

        holder.setItemClickListener((view, position1, isLongClick) -> {
            //Start new Activity
            Intent foodDetailIntent=new Intent(context, FoodDetail.class);
            foodDetailIntent.putExtra("FoodID",favourites.get(position1).getFoodID());
            foodDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(foodDetailIntent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            new Database(context.getApplicationContext()).removeFromFavorite(favourites.get(position));
            removeItem(holder.getAbsoluteAdapterPosition());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return favourites.size();
    }

    public void removeItem(int position){
        favourites.remove(position);
        notifyItemRemoved(position);
    }
    public void restoreItem(FavouriteFood favouriteFood,int pos){
        favourites.add(pos,favouriteFood);
        notifyItemInserted(pos);
    }
}