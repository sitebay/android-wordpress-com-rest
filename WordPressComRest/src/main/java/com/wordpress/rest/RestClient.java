package com.wordpress.rest;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class RestClient {
    public static final String TAG = "WordPressREST";
    public static enum REST_CLIENT_VERSIONS {V0, V1, V1_1, V1_2, V1_3, V2}
    public static final String PARAMS_ENCODING = "UTF-8";

    protected static final String REST_API_ENDPOINT_URL_V0 = "https://my.sitebay.org/f/api/v1/mobile/";
    protected static final String REST_API_ENDPOINT_URL_V1 = "https://my.sitebay.org/f/api/v1/mobile/";
    protected static final String REST_API_ENDPOINT_URL_V1_1 = "https://my.sitebay.org/f/api/v1/mobile/";
    protected static final String REST_API_ENDPOINT_URL_V1_2 = "https://my.sitebay.org/f/api/v1/mobile/";
    protected static final String REST_API_ENDPOINT_URL_V1_3 = "https://my.sitebay.org/f/api/v1/mobile/";
    protected static final String REST_API_ENDPOINT_URL_V2 = "https://my.sitebay.org/f/api/v1/mobile/";

    private RequestQueue mQueue;
    private String mAccessToken;
    private String mUserAgent;
    private String mRestApiEndpointURL;
    private RestRequest.OnAuthFailedListener mOnAuthFailedListener;

    public RestClient(RequestQueue queue) {
        this(queue, REST_CLIENT_VERSIONS.V1);
    }

    public RestClient(RequestQueue queue, REST_CLIENT_VERSIONS version) {
        mQueue = queue;

        if (version == REST_CLIENT_VERSIONS.V2) {
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V2;
        } else if (version == REST_CLIENT_VERSIONS.V1_3) {
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V1_3;
        } else if (version == REST_CLIENT_VERSIONS.V1_2) {
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V1_2;
        } else if (version == REST_CLIENT_VERSIONS.V1_1) {
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V1_1;
        } else if (version == REST_CLIENT_VERSIONS.V0) {
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V0;
        } else {
            // Fallback to version 1
            mRestApiEndpointURL = REST_API_ENDPOINT_URL_V1;
        }
    }

    public RestClient(RequestQueue queue, String token, String endpointURL) {
        this(queue);
        mAccessToken = token;
        mRestApiEndpointURL = endpointURL;
    }

    public RestRequest get(String path, Listener<JSONObject> listener, ErrorListener errorListener) {
        return makeRequest(Method.GET, getAbsoluteURL(path), null, listener, errorListener);
    }

    public RestRequest post(String path, Map<String, String> body, Listener<JSONObject> listener,
                            ErrorListener errorListener) {
        return makeRequest(Method.POST, getAbsoluteURL(path), body, listener, errorListener);
    }

    public RestRequest makeRequest(int method, String url, Map<String, String> params, Listener<JSONObject> listener,
                                   ErrorListener errorListener) {
        RestRequest request = new RestRequest(method, url, params, listener, errorListener);
        if (mOnAuthFailedListener != null) {
            request.setOnAuthFailedListener(mOnAuthFailedListener);
        }
        request.setUserAgent(mUserAgent);
        request.setAccessToken(mAccessToken);
        return request;
    }

    public JsonRestRequest makeRequest(String url, JSONObject params, Listener<JSONObject> listener,
                                   ErrorListener errorListener) {
        JsonRestRequest request = new JsonRestRequest(url, params, listener, errorListener);
        if (mOnAuthFailedListener != null) {
            request.setOnAuthFailedListener(mOnAuthFailedListener);
        }
        request.setUserAgent(mUserAgent);
        request.setAccessToken(mAccessToken);
        return request;
    }

    public RestRequest send(RestRequest request) {
        // Volley send the request
        mQueue.add(request);
        return request;
    }

    public void setOnAuthFailedListener(RestRequest.OnAuthFailedListener onAuthFailedListener) {
        mOnAuthFailedListener = onAuthFailedListener;
    }

    public String getAbsoluteURL(String url) {
        // if it already starts with our endpoint, let it pass through
        if (url.indexOf(mRestApiEndpointURL) == 0) {
            return url;
        }
        // if it has a leading slash, remove it
        if (url.indexOf("/") == 0) {
            url = url.substring(1);
        }
        // prepend the endpoint
        return String.format("%s%s", mRestApiEndpointURL, url);
    }

    public String getAbsoluteURL(String path, Map<String, String> params) {
        String url = getAbsoluteURL(path);
        if (params != null) {
            // build a query string
            StringBuilder query = new StringBuilder();
            try {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    query.append(URLEncoder.encode(entry.getKey(), PARAMS_ENCODING));
                    query.append("=");
                    query.append(URLEncoder.encode(entry.getValue(), PARAMS_ENCODING));
                    query.append("&");
                }
                if (query.length() > 0) {
                    query.deleteCharAt(query.length() - 1);
                }
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + PARAMS_ENCODING, uee);
            }

            if (url.contains("?")) {
                url = String.format("%s&%s", new Object[]{url, query});
            } else {
                url = String.format("%s?%s", new Object[]{url, query});
            }
        }
        return url;
    }

    //Sets the User-Agent header to be sent with each future request.
    public void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    // Sets the auth token to be used in the request header
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    public boolean isAuthenticated() {
        return mAccessToken != null;
    }

    public String getEndpointURL() {
        return mRestApiEndpointURL;
    }
}
