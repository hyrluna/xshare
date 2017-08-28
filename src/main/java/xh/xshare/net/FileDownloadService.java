package xh.xshare.net;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;

/**
 * Created by G1494458 on 2017/8/16.
 */

public interface FileDownloadService {
//    static final String UPDATE_URL_UAT = "http://www.zdtco.com/app/UAT/download/android.json";
//    static final String UPDATE_URL_PRO = "http://www.zdtco.com/app/download/android.json";
    //http://www.zdtco.com/app/download/Android/ZDTAPP1_4_7.apk

    @GET("/app/download/Android/ZDTAPP1_4_7.apk")
    @Streaming
    Call<ResponseBody> download();

}
