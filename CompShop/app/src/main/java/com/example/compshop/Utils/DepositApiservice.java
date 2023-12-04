package com.example.compshop.Utils;

import com.example.compshop.Models.DepositRequest;
import com.example.compshop.Models.DepositResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DepositApiservice {
    @POST("v1/deposit")
    Call<DepositResponse> makeDeposit(
            @Header("Authorization") String authToken,
            @Body DepositRequest depositRequest
    );
}
