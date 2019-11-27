package org.pfred.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.pfred.rest.RestServiceCaller;
import org.pfred.rest.RestServiceResult;

/**
 * @author Dario Cruz
 *
 */

public class RestServiceClient {
    private static String endpoint = "https://os.biogen.com";
    private static String service;
    private static String uri;

    public static URI appendUri(String uri, String appendQuery) throws URISyntaxException {
        URI oldUri = new URI(uri);

        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = appendQuery;
        } else {
            newQuery += "&" + appendQuery;
        }

        URI newUri = new URI(oldUri.getScheme(), oldUri.getAuthority(),
                             oldUri.getPath(), newQuery, oldUri.getFragment());

        return newUri;
    }

    public static String runActivityModelService(String pathValue, final String runDir,
                                                 final String PrimarySeq, final String oligoLength){
        try {
            RestServiceCaller.setHttpsEnabled(true);
            service = "ActivityModel";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "PrimarySequence=" + PrimarySeq);

            if(pathValue == "ASO"){
                newuri = appendUri(newuri.toString(), "OligoLength=" + oligoLength);
            }

            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get response as string
            return restResult.getResultString();

        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String runOffTargetSearchService(String pathValue, final String species, final String runDir,
                                                 final String IDs, final String missMatches){
        try {
            RestServiceCaller.setHttpsEnabled(true);
            service = "OffTargetSearch";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "Species=" + species);
            newuri = appendUri(newuri.toString(), "IDs=" + IDs);
            newuri = appendUri(newuri.toString(), "missMatches=" + missMatches);

            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get response as string
            return restResult.getResultString();

        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String runScriptUtilitiesService(String pathValue, final String runDir,
                                                   final String ensemblID, final String requestedSpecies,
                                                   final String species){
        try {
            RestServiceCaller.setHttpsEnabled(true);
            service = "ScriptUtilities";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);

            if (pathValue == "Orthologs"){
                newuri = appendUri(newuri.toString(), "enseblID=" + ensemblID);
                newuri = appendUri(newuri.toString(), "RequestedSpecies=" + requestedSpecies);
                newuri = appendUri(newuri.toString(), "Species=" + species);
            }

            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get response as string
            return restResult.getResultString();

        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String[] runEnumerateUtilitiesService(String pathValue, final String runDir,
                                                      final String secondaryIDs, final String primaryID,
                                                      final String oligoLen){
        String[] results = new String[2];
        try {
            RestServiceCaller.setHttpsEnabled(true);
            service = "ScriptUtilities";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue + "_first";

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "SecondaryTranscriptIDs=" + secondaryIDs);
            newuri = appendUri(newuri.toString(), "PrimaryTranscriptID=" + primaryID);
            newuri = appendUri(newuri.toString(), "oligoLen=" + oligoLen);


            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get first string from enumerate_first
            results[0] = restResult.getResultString();

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue + "_second";

            newuri = appendUri(uri, "RunDirectory=" + runDir);

            // Get second string from enumerate_second
            results[1] = restResult.getResultString();

            return results;

        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getEndPoint(){
        return endpoint;
    }

    public static void setEndPoint(String newendpoint){
        endpoint = newendpoint;
    }
}
