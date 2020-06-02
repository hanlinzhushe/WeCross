package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.restserver.RestResponse;
import io.netty.handler.codec.http.FullHttpRequest;

/** GET/POST /test */
public class TestURIHandler implements URIHandler {

    @Override
    public void handle(FullHttpRequest httpRequest, Callback callback) {
        RestResponse<String> restResponse = new RestResponse<>();
        restResponse.setData("OK!");

        callback.onResponse(restResponse);
    }
}