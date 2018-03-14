package net.lzzy.networkdisk.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import net.lzzy.networkdisk.R;
import net.lzzy.networkdisk.models.UserFile;

import java.util.List;


public class PicturesAdapter extends PagerAdapter {
    private Context context;
    private List<UserFile> files;

    public PicturesAdapter(Context context, List<UserFile> files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files == null ? 0 : files.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {//必须实现，实例化
        View view = LayoutInflater.from(context).inflate(R.layout.item_pager_image, container, false);
        final ImageView iv = view.findViewById(R.id.item_pager_image);

        Glide.with(context).load(files.get(position).getUrl()).asBitmap().placeholder(R.drawable.ic_default_image).into(iv);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                pb.setVisibility(View.GONE);
//                iv.setVisibility(View.VISIBLE);
//            }
//        }, 2000);


        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object != null) {
            container.removeView((View) object);
        }
    }
}
