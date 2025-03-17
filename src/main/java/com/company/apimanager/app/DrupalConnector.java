package com.company.apimanager.app;

import com.company.apimanager.entity.Product;
import com.company.apimanager.entity.PublishedProduct;
import com.company.apimanager.entity.Site;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.aspectj.weaver.ast.Call;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.persistence.eis.EISException.TIMEOUT;
import static org.eclipse.persistence.eis.EISException.propertiesNotSet;

public class DrupalConnector {
    String drupalEndpoint;
    String adminUser;
    String adminPassword;

    public DrupalConnector() {
    }

    public DrupalConnector(String drupalEndpoint, String adminUser, String adminPassword) {
        this.drupalEndpoint = drupalEndpoint;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
    }

    public int updateProductPage(List<PublishedProduct> productList,  CallDrupalApiError error) {
        int retInt = HttpURLConnection.HTTP_OK;
        if (productList == null) {
            error.setErrorMsg("Product list is NULL");
            return -1;
        }
        if (productList.isEmpty()) {
            error.setErrorMsg("Product list is Empty");
            return -1;
        }
        String pageContent = "<div class=\\\"services\\\">";
        for (int i = 0;i < productList.size(); i++) {
            String productStr = this.GenerateProductStr(productList.get(i));
            if (productStr != null) {
                pageContent = pageContent + productStr;
            }
        }
        pageContent = pageContent + "</div>";

        String patchStr = "{       \n" +
                "\n" +
                "    \"type\": [\n" +
                "\n" +
                "    {\n" +
                "\n" +
                "      \"target_id\": \"page\"\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "  ],\n" +
                "  \"body\": [\n" +
                "\n" +
                "        {\n" +
                "\n" +
                "            \"value\": \"" + pageContent + "\\r\\n\",\n" +
                "\n" +
                "     " +
                "" +
                "       \"format\": \"full_html\"\n" +
                "\n" +
                "        }\n" +
                "\n" +
                "    ]\n" +
                "\n" +
                "}";

        String drupalProductPageUrl = drupalEndpoint + "/" + "node/" + Site.PRODUCT_PAGE_ID +
                "?_format=json";
        retInt = this.doPatch(patchStr, drupalProductPageUrl, error);
        return retInt;
    }

    private String GenerateProductStr(PublishedProduct publishedProduct) {
        if (publishedProduct == null) {
            return null;
        }
        Product product = publishedProduct.getProduct();
        if ((product.getName() == null) || (product.getTitle() == null)) {
            return null;
        }
        String productUrl = drupalEndpoint + "/node/" + publishedProduct.getDrupal_product_id();
        String productStr = "<div class=\\\"service\\\">" + "<h3>";
        productStr = productStr + product.getName() + "</h3>" + "<p>" + product.getTitle() +
        "</p><p><a class=\\\"button-link\\\" href=\\\"" + productUrl + "\\\">read more</a></p>";
        productStr = productStr + "</div>";
        return productStr;
    }

    public int updateAboutPage(String aboutPageContent, String aboutPageImageId,
                               CallDrupalApiError error) {
        int retInt = HttpURLConnection.HTTP_OK;

        if (aboutPageContent == null) {
            error.setErrorMsg("Input page is NULL");
            return -1;
        }
        String patchStr = "{       \n" +
                "\n" +
                "    \"type\": [\n" +
                "\n" +
                "    {\n" +
                "\n" +
                "      \"target_id\": \"page\"\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "  ],\n" +
                "\n" +
                "   \"field_image\": [\n" +
                "\n" +
                "        {\n" +
                "\n" +
                "            \"target_id\":" + aboutPageImageId + ",\n" +
                "\n" +
                "            \"alt\": \""+ "About" + "\"\n" +
                "\n" +
                "        }\n" +
                "\n" +
                "    ],\n"
                +
                "  \"body\": [\n" +
                "\n" +
                "        {\n" +
                "\n" +
                "            \"value\": \"" + aboutPageContent + "\\r\\n\",\n" +
                "\n" +
                "     " +
                "" +
                "       \"format\": \"full_html\"\n" +
                "\n" +
                "        }\n" +
                "\n" +
                "    ]\n" +
                "\n" +
                "}";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBasicAuth(this.adminUser, this.adminPassword);

            HttpEntity<String> entity = new HttpEntity<String>(patchStr, headers);
            RestTemplate restTemplate = new RestTemplate();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);
            restTemplate.setRequestFactory(requestFactory);

            String drupalPutUrl = drupalEndpoint + "/" + "node/" + Site.ABOUT_PAGE_ID +
                    "?_format=json";

            String resultAsJsonStr = restTemplate.exchange(drupalPutUrl, HttpMethod.PATCH, entity,
                    String.class).getBody();

        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            error.setErrorMsg(httpClientErrorException.getMessage());
            error.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            retInt = -1;
            error.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int updateHomePage(String homePageContent, CallDrupalApiError error) {
        int retInt = HttpURLConnection.HTTP_OK;

        if (homePageContent == null) {
            error.setErrorMsg("Input page is NULL");
            return -1;
        }
        String patchStr = "{       \n" +
                "\n" +
                "    \"type\": [\n" +
                "\n" +
                "    {\n" +
                "\n" +
                "      \"target_id\": \"page\"\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "  ],\n" +
                "  \"body\": [\n" +
                "\n" +
                "        {\n" +
                "\n" +
                "            \"value\": \"" + homePageContent + "\\r\\n\",\n" +
                "\n" +
                "     " +
                "" +
                "       \"format\": \"full_html\"\n" +
                "\n" +
                "        }\n" +
                "\n" +
                "    ]\n" +
                "\n" +
                "}";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBasicAuth(this.adminUser, this.adminPassword);

            HttpEntity<String> entity = new HttpEntity<String>(patchStr, headers);
            RestTemplate restTemplate = new RestTemplate();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);
            restTemplate.setRequestFactory(requestFactory);

            String drupalPutUrl = drupalEndpoint + "/" + "node/" + Site.HOME_PAGE_ID +
                    "?_format=json";

            String resultAsJsonStr = restTemplate.exchange(drupalPutUrl, HttpMethod.PATCH, entity,
                    String.class).getBody();

        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            error.setErrorMsg(httpClientErrorException.getMessage());
            error.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            retInt = -1;
            error.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    private int doPatch(String patchStr, String patchUrl, CallDrupalApiError error) {
        int retInt = HttpURLConnection.HTTP_OK;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.setBasicAuth(this.adminUser, this.adminPassword);

            HttpEntity<String> entity = new HttpEntity<String>(patchStr, headers);
            RestTemplate restTemplate = new RestTemplate();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setConnectTimeout(TIMEOUT);
            requestFactory.setReadTimeout(TIMEOUT);
            restTemplate.setRequestFactory(requestFactory);

            /*String drupalPutUrl = drupalEndpoint + "/" + "node/" + Site.HOME_PAGE_ID +
                    "?_format=json";

             */

            String resultAsJsonStr = restTemplate.exchange(patchUrl, HttpMethod.PATCH, entity,
                    String.class).getBody();
        }
        catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            error.setErrorMsg(httpClientErrorException.getMessage());
            error.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        } catch (Exception e) {
            retInt = -1;
            error.setErrorMsg(e.getMessage());
        }
        return retInt;
    }

    public int uploadFile(byte[] bytes, String fileName, DrupalUploadedFile uploadedFile,
                          CallDrupalApiError error) throws IOException {
        int retInt = HttpURLConnection.HTTP_OK;

        if ((bytes == null) || (fileName == null)) {
            error.setErrorMsg("Parameter NULL");
            return  -1;
        }

        URL obj = new URL(drupalEndpoint + "/file/upload/node/page/field_image?_format=json");
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
            httpURLConnection.setRequestProperty("Content-Disposition", "file; filename=\"" + fileName
                    + "\"");
            httpURLConnection.setRequestProperty("Accept", "*/*");
            //httpURLConnection.setRequestProperty("Authorization", "Basic YWRtaW46QWJjZDEyMzRA");
            String basicAuth = "Basic " + Utility.encodeBase64(this.adminUser + ":" +
                    this.adminPassword);
            httpURLConnection.setRequestProperty("Authorization", basicAuth);

            // For POST only - START
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            os.write(bytes);

            os.flush();
            os.close();
            // For POST only - END

            int responseCode = httpURLConnection.getResponseCode();
            //System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject convertedObject = new Gson().fromJson(response.toString(), JsonObject.class);
                String fId = convertedObject.get("fid").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
                String fUrl = convertedObject.get("uri").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
                uploadedFile.setfId(fId);
                uploadedFile.setUrl(fUrl);
            }
            else {
                retInt = responseCode;
                error.setErrorMsg("HTTP response code = " + responseCode);
            }
        } catch (HttpClientErrorException httpClientErrorException) {
            retInt = httpClientErrorException.getRawStatusCode();
            error.setErrorMsg(httpClientErrorException.getMessage());
            error.setHttpResponseCode(httpClientErrorException.getRawStatusCode());
        }
        catch (Exception exception) {
            error.setErrorMsg(exception.getMessage());
            retInt = -1;
        }
        return retInt;
    }
}


