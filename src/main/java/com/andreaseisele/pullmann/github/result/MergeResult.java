package com.andreaseisele.pullmann.github.result;

import com.andreaseisele.pullmann.github.dto.MergeResponse;

public class MergeResult {

    private final MergeResponse response;

    private MergeResult(MergeResponse response) {
        this.response = response;
    }

    public static MergeResult of(MergeResponse response) {
        return new MergeResult(response);
    }

    public static MergeResult failure() {
        return new MergeResult(null);
    }

    public boolean isSuccessful() {
        return response != null && response.merged();
    }

    public MergeResponse getResponse() {
        return response;
    }

}
