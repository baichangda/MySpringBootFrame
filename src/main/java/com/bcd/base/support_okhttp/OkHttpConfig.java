package com.bcd.base.support_okhttp;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class OkHttpConfig {

    static Logger logger = LoggerFactory.getLogger(OkHttpConfig.class);

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(newHttpLoggingInterceptor())
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }

    private HttpLoggingInterceptor newHttpLoggingInterceptor() {
        final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(s -> {
            try {
                logger.info(s);
            } catch (Exception ex) {
                logger.error("error", ex);
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }

    public static void main(String[] args) throws IOException {
        HttpUrl httpUrl = HttpUrl.get("https://fanyi.baidu.com/v2transapi")
                .newBuilder()
                .addQueryParameter("from", "zh")
                .addQueryParameter("to", "en")
                .build();
        final FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add("from", "zh");
        bodyBuilder.add("to", "en");
        bodyBuilder.add("query", "你好");
        bodyBuilder.add("transtype", "translang");
        bodyBuilder.add("simple_means_flag", "3");
        bodyBuilder.add("sign", "232427.485594");
        bodyBuilder.add("token", "6e3a577552496c051f11504d13d2c9d5");
        bodyBuilder.add("domain", "common");
        final OkHttpClient okHttpClient = new OkHttpConfig().okHttpClient();
        final Request request = new Request.Builder()
                .header("Cookie", "BIDUPSID=36372D03359D23781367EED00C55BF8C; PSTM=1624513480; __yjs_duid=1_9b18d48398b589a299c6339ca21c6c6b1624513484016; BAIDUID=2E1E9EE6E19EB4327FE81BFE224C207B:FG=1; H_WISE_SIDS=110085_127969_180636_188744_189755_190627_194519_194530_196426_197471_197711_199570_204902_207697_208721_208809_209202_209568_210306_210321_212296_212416_212797_212867_213036_213080_213278_213358_214094_214189_214640_214795_214892_215127_215175_215730_215892_216570_216596_216618_216770_216846_216883_216943_217183_217185_217205_217216_217390_217392_217761_218098_218183_218234_218277_218329_218445_218454_218537_218598_218957_218966_218992_219156_219249_219255_219329_219362_219448_219450_219510_219513_219548_219566_219587_219591_219713_219727_219733_219738_219744_219799_219818_219822_219942_220068_220091_8000075_8000105_8000116_8000128_8000137_8000145_8000150_8000156_8000178_8000179_8000183; H_WISE_SIDS_BFESS=110085_127969_180636_188744_189755_190627_194519_194530_196426_197471_197711_199570_204902_207697_208721_208809_209202_209568_210306_210321_212296_212416_212797_212867_213036_213080_213278_213358_214094_214189_214640_214795_214892_215127_215175_215730_215892_216570_216596_216618_216770_216846_216883_216943_217183_217185_217205_217216_217390_217392_217761_218098_218183_218234_218277_218329_218445_218454_218537_218598_218957_218966_218992_219156_219249_219255_219329_219362_219448_219450_219510_219513_219548_219566_219587_219591_219713_219727_219733_219738_219744_219799_219818_219822_219942_220068_220091_8000075_8000105_8000116_8000128_8000137_8000145_8000150_8000156_8000178_8000179_8000183; BAIDUID_BFESS=2E1E9EE6E19EB4327FE81BFE224C207B:FG=1; ZFY=SJKE6DBvY24gfO7yU1YuOCXGJL:BCEwTB32tjwb41ijw:C; H_PS_PSSID=36543_36460_36884_34812_36570_36807_36789_37173_37137_37258_26350_37308_37204; BA_HECTOR=05a5ah24a08g2g20a405jqna1hhj3g817; Hm_lvt_64ecd82404c51e03dc91cb9e8c025574=1662621328; Hm_lpvt_64ecd82404c51e03dc91cb9e8c025574=1662621328; APPGUIDE_10_0_2=1; REALTIME_TRANS_SWITCH=1; FANYI_WORD_SWITCH=1; HISTORY_SWITCH=1; SOUND_SPD_SWITCH=1; SOUND_PREFER_SWITCH=1; ab_sr=1.0.1_ODYwYTcyNDlhZjAxYTE2MTdiNzVlN2IxN2Q4YzRkMjEyMTAwNGNlZGQ5ZGFhNDQzMWRlZmQyOTE1MjA5NzJiMmQ2ZWNmNWY1YWE0YjY4NzMxYjZkMzAyNzZhODE2YmRiM2UzZWMzZDNmNDZkZjhhM2MzZWE3ZTVhMGUzNjJiZTMwZjllYjA5MTMxODIxMTFkYzhlOTY5NDE3MzUwNDcyOA==")
                .url(httpUrl)
                .post(bodyBuilder.build())
                .build();
        final Call call = okHttpClient.newCall(request);
        final Response response = call.execute();
        System.out.println(response.body().string());
    }
}
