package com.example.compshop.Utils;

import static com.example.compshop.Utils.Constants.BASE_URL;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiClient apiClient;
    private static Retrofit retrofit;

    private ApiClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().
                    baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    ).build();
        }
    }

    public static synchronized ApiClient getInstance() {
        if (apiClient == null)
            apiClient = new ApiClient();
        return apiClient;
    }

    public DepositApiservice getDepositApiservice() {
        return retrofit.create(DepositApiservice.class);
    }
}
