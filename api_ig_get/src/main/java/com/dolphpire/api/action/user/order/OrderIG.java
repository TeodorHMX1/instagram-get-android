package com.dolphpire.api.action.user.order;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dolphpire.api.initializer.DolphPireApp;
import com.dolphpire.api.interfaces.ApiCallback;
import com.dolphpire.api.interfaces.FailureCallback;
import com.dolphpire.api.interfaces.ZFlowOnCompleteCallback;
import com.dolphpire.api.links.EndPoints;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dolphpire.api.initializer.DolphPireApp.TAG;

public class OrderIG
{

    //class model

    private String userID;
    private String order;
    private String type;
    private ZFlowOnCompleteCallback.OnComplete onCompleteListener;
    private FailureCallback.OnFailureListener onFailureListener;
    private ApiCallback.ApiKeyError mApiKeyError;

    OrderIG(String userID, String order, int type)
    {
        this.userID = String.valueOf(userID);
        this.order = String.valueOf(order);
        this.type = String.valueOf(type);
    }

    public void execute()
    {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                EndPoints.LINK_USER_ORDER, response ->
        {
            Log.e(TAG, "response: " + response);
            try
            {
                JSONObject responseObj = new JSONObject(response);
                // check for error flag
                if (!responseObj.getBoolean("error"))
                {
                    if (onCompleteListener != null)
                    {
                        onCompleteListener.onCompleted();
                    }
                } else
                {
                    JSONObject errorData = responseObj.getJSONObject("errorData");
                    if (errorData.getInt("errorType") == 100)
                    {
                        if (mApiKeyError != null)
                        {
                            mApiKeyError.badApi();
                        }
                    } else
                    {
                        if (onFailureListener != null)
                        {
                            Exception exception = new Exception(errorData.getInt("errorType") + ", " + errorData.getInt("errorMessage"));
                            onFailureListener.onFailure(exception);
                        }
                    }
                }

            } catch (JSONException ignored)
            {
                //empty method
            }
        }, new Response.ErrorListener()
        {

            @Override
            public void onErrorResponse(VolleyError error)
            {
                NetworkResponse networkResponse = error.networkResponse;
                //Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                //Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
        {

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("api_key", DolphPireApp.getInstance().getApiKey());
                params.put("secret_key", DolphPireApp.getInstance().getSecretKey());
                params.put("my_uid", String.valueOf(DolphPireApp.getInstance().getUUID()));

                params.put("userID", userID);
                params.put("order", order);
                params.put("type", type);
                return params;
            }
        };

        //Adding request to request queue
        DolphPireApp.getInstance().addToRequestQueue(strReq);

    }

    public OrderIG addOnCompleteListener(ZFlowOnCompleteCallback.OnComplete onCompleteListener)
    {
        this.onCompleteListener = onCompleteListener;
        return this;
    }

    public OrderIG addOnFailureListener(FailureCallback.OnFailureListener onFailureListener)
    {
        this.onFailureListener = onFailureListener;
        return this;
    }

    public OrderIG addOnFailedListener(ApiCallback.ApiKeyError mApiKeyError)
    {
        this.mApiKeyError = mApiKeyError;
        return this;
    }

}