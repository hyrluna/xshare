package xh.xshare.net;

import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by G1494458 on 2017/8/22.
 */

public class NetworkClient implements FileDownloadService {

    private FileDownloadService fileDownloadService;

    public NetworkClient() {
        fileDownloadService = new Retrofit.Builder()
                .baseUrl("http://www.zdtco.com")
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(FileDownloadService.class);
    }

    @Override
    public Call<ResponseBody> download() {
        return fileDownloadService.download();
    }

    @NonNull
    private Retrofit getRetrofit(String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }
}
