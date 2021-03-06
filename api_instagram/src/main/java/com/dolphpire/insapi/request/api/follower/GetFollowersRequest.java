package com.dolphpire.insapi.request.api.follower;

import com.dolphpire.insapi.manager.IGCommonFieldsManager;
import com.dolphpire.insapi.manager.IGConfig;
import com.dolphpire.insapi.manager.utils.IGUtils;
import com.dolphpire.insapi.request.InsBaseGetRequest;
import java.util.HashMap;
import java.util.Map;

public class GetFollowersRequest extends InsBaseGetRequest<FollowersResponseData> {

  private boolean isFirstPage;
  private String userId;
  private String nextMaxId;

  public GetFollowersRequest(boolean isFirstPage, String userId, String nextMaxId) {
    this.isFirstPage = isFirstPage;
    this.userId = userId;
    this.nextMaxId = nextMaxId;
  }

  @Override
  protected Map<String, String> getMapParams() {

    HashMap<String, String> paramsMap = new HashMap<>();
    paramsMap.put("rank_token", String.format("%s_%s", userId, IGUtils.getAndroidId(
        IGCommonFieldsManager.getInstance().getContext())));//rank_token本地拼接的userid_deviceID （
    paramsMap.put("ranked_content", "true");
    if (!isFirstPage) {
      paramsMap.put("max_id", nextMaxId);
    }

    return paramsMap;
  }

  @Override
  protected String getActionUrl() {
    return String.format(IGConfig.ACTION_GET_FOLLOWERS, userId);
  }
}
