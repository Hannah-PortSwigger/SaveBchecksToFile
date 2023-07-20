import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static burp.api.montoya.http.message.requests.HttpRequest.httpRequestFromUrl;

public class MyBurpExtension implements BurpExtension
{
    private final String bchecksStorageFolder = "/CHANGE/ME/bchecks/";

    private MontoyaApi api;
    private final String githubHost = "https://raw.githubusercontent.com";
    private final String orgPlusRepo = "/Portswigger/BChecks";

    @Override
    public void initialize(MontoyaApi api)
    {
        this.api = api;
        api.extension().setName("Save BChecks to Folder");

        updateBCheckList();
    }

    private void updateBCheckList()
    {
        List<String> allBcheckUrls = new LinkedList<>();

        //TODO recursive parsing of folders to find .bcheck files
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/Javascript"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/files"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/files/configs"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/nacos"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/prometheus"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/springboot"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/other/tokens"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/vulnerabilities-CVEd"));
        allBcheckUrls.addAll(parseFolder("https://github.com/PortSwigger/BChecks/tree/main/vulnerability-classes/injection"));

        allBcheckUrls.forEach(this::parseBcheckUrl);
    }

    private List<String> parseFolder(String url)
    {
        HttpRequestResponse requestResponse = api.http().sendRequest(httpRequestFromUrl(url));

        String responseBody = requestResponse.response().bodyToString();

        Pattern pattern = Pattern.compile("\"name\":\"[a-zA-Z0-9- ()_&]*\\.bcheck\",\"path\":\"([a-zA-Z0-9-/ ()_&]*\\.bcheck)\",\"contentType\":\"file\"");
        Matcher matcher = pattern.matcher(responseBody);

        List<String> bcheckUrlList = new LinkedList<>();

        while (matcher.find())
        {
            String path = matcher.group(1);
            String strippedPath = path.replace(" ", "%20");
            bcheckUrlList.add(githubHost + orgPlusRepo + "/main/" + strippedPath);
        }

        return bcheckUrlList;
    }

    private void parseBcheckUrl(String bcheckUrl)
    {
        HttpRequestResponse requestResponse = api.http().sendRequest(httpRequestFromUrl(bcheckUrl));

        String bcheckContents = requestResponse.response().bodyToString();

        saveBcheckToFile(bcheckContents);
    }

    private void saveBcheckToFile(String bcheckContents)
    {
        Pattern pattern = Pattern.compile("name: \"(.*)\"");
        Matcher matcher = pattern.matcher(bcheckContents);

        if (matcher.find())
        {
            String name = matcher.group(1);

            try
            {
                File bcheckFile = new File(bchecksStorageFolder + name + ".bcheck");

                FileWriter fileWriter = new FileWriter(bcheckFile, false);
                fileWriter.write(bcheckContents);
                fileWriter.close();
                api.logging().logToOutput("BCheck saved: " + name);
            }
            catch (Exception e)
            {
                api.logging().logToError(e);
            }
        }
        else
        {
            api.logging().logToError("BCheck did not specify name.");
        }
    }
}
