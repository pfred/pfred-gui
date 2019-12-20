package org.pfred.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.pfred.rest.RestServiceCaller;
import org.pfred.rest.RestServiceResult;
import org.pfred.SSL.SSLFix;

/**
 * @author Dario Cruz
 *
 */

public class RestServiceClient {
    private static String endpoint = "https://os.biogen.com";
    private static String protocol = "https";
    private static String service;
    private static String uri;
    private static String fileuri;
    private static int ntries = 50;

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
                                                 final String PrimaryID, final String oligoLength){
        try {
            String outfile = "siRNAActivityModelResult.csv";
            int max = 2;

            if(protocol == "https"){
                SSLFix.execute();
                RestServiceCaller.setHttpsEnabled(true);
            }
            else{
                RestServiceCaller.setHttpsEnabled(false);
            }
            service = "ActivityModel";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            // Hardcoded

            fileuri = endpoint + "/" + "PFREDRestService/service" + "/" +
                "OffTargetSearch" + "/" + "Check";

            URI newuri = new URI(uri);
            URI newfileuri = new URI(fileuri);
            RestServiceResult restCheckFile;
            String pacoResult = null;

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "PrimaryID=" + PrimaryID);

            if(pathValue == "ASO"){
                newuri = appendUri(newuri.toString(), "OligoLength=" + oligoLength);
                outfile = "ASOActivityModelResult.csv";
            }

            RestServiceResult restResult = RestServiceCaller.post(newuri.toString(), null, 2000);

            // Get response as string
            System.out.println("Response Code: " + restResult.getResponseCode());

            if (restResult.getResponseCode() == 504){
                System.out.println("Got code 504, let us wait and try to get the results...");

                newfileuri = appendUri(fileuri, "File=paco.txt");
                newfileuri = appendUri(newfileuri.toString(), "RunDirectory=" + runDir);

                restCheckFile = RestServiceCaller.get(newfileuri.toString(), 2000);
                pacoResult = restCheckFile.getResultString();

                while (pacoResult == null && max > 0){

                    System.out.println("From paco.txt got " + pacoResult);

                    // Wait 50 seconds
                    try {
                        System.out.println("Sleeping...");
                        Thread.sleep(120000);
                        System.out.println("Woke Up!...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    restCheckFile = RestServiceCaller.get(newfileuri.toString(), 5000);
                    pacoResult = restCheckFile.getResultString();
                    System.out.println("Response Code: " + restCheckFile.getResponseCode());
                    max = max - 1;
                }
                System.out.println("Found Nonempty paco.txt, ready to collect result from Activity");
                newuri = appendUri(fileuri, "File=" + outfile);
                newuri = appendUri(newuri.toString(), "RunDirectory=" + runDir);
                restResult = RestServiceCaller.get(newuri.toString(), 2000);
                System.out.println("Response Code: " + restResult.getResponseCode());
            }

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
            if(protocol == "https"){
                SSLFix.execute();
                RestServiceCaller.setHttpsEnabled(true);
            }
            else{
                RestServiceCaller.setHttpsEnabled(false);
            }
            service = "OffTargetSearch";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            // Hardcoded

            fileuri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + "Check";

            URI newuri = new URI(uri);
            URI newfileuri = new URI(fileuri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "Species=" + species);
            newuri = appendUri(newuri.toString(), "IDs=" + IDs);
            newuri = appendUri(newuri.toString(), "missMatches=" + missMatches);

            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 2000);
            RestServiceResult restCheckFile;
            String pacoResult = null;

            // Get response as string
            System.out.println("Response Code: " + restResult.getResponseCode());

            // If 504, let's cheat... send empty species, REST will understand this as just ask for
            // results

            if (restResult.getResponseCode() == 504){
                System.out.println("Got code 504, let us wait and try to get the results...");

                newfileuri = appendUri(fileuri, "File=paco.txt");
                newfileuri = appendUri(newfileuri.toString(), "RunDirectory=" + runDir);

                newuri = appendUri(uri, "RunDirectory=" + runDir);
                newuri = appendUri(newuri.toString(), "Species=paco");
                newuri = appendUri(newuri.toString(), "IDs=" + IDs);
                newuri = appendUri(newuri.toString(), "missMatches=" + missMatches);

                restCheckFile = RestServiceCaller.get(newfileuri.toString(), 2000);
                pacoResult = restCheckFile.getResultString();

                while (pacoResult == null){

                    System.out.println("From paco.txt got " + pacoResult);

                    // Wait 50 seconds
                    try {
                        System.out.println("Sleeping...");
                        Thread.sleep(50000);
                        System.out.println("Woke Up!...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    restCheckFile = RestServiceCaller.get(newfileuri.toString(), 2000);
                    pacoResult = restCheckFile.getResultString();
                }
                System.out.println("Found Nonempty paco.txt, ready to collect result from OffTarget");
                restResult = RestServiceCaller.get(newuri.toString(), 2000);
            }else{
                while (restResult.getResponseCode() != 200 && ntries > 0){
                    System.out.println("Retrying..." + ntries);
                    restResult = RestServiceCaller.get(newuri.toString(), 2000);
                    System.out.println("Response Code: " + restResult.getResponseCode());
                    ntries = ntries - 1;
                }
                if(restResult.getResponseCode() != 200){
                    System.out.println("Call failed, max tries exceeded, aborting...");
                    return null;
                }
            }
            pacoResult = null;
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
            if(protocol == "https"){
                SSLFix.execute();
                RestServiceCaller.setHttpsEnabled(true);
            }
            else{
                RestServiceCaller.setHttpsEnabled(false);
            }
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
            System.out.println("Response Code: " + restResult.getResponseCode());
            return restResult.getResultString();

        } catch (URISyntaxException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String runAddToFileUtilityService(final String runDir, final String file,
                                                    final String text){
        String pathValue = "appendToFile";
        try {
            if(protocol == "https"){
                SSLFix.execute();
                RestServiceCaller.setHttpsEnabled(true);
            }
            else{
                RestServiceCaller.setHttpsEnabled(false);
            }
            service = "ScriptUtilities";

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue;

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "FileName=" + file);
            newuri = appendUri(newuri.toString(), "Text=" + text);
            newuri = appendUri(newuri.toString(), "RunDirectory=" + runDir);

            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get response as string
            System.out.println("Response Code: " + restResult.getResponseCode());
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
            if(protocol == "https"){
                SSLFix.execute();
                RestServiceCaller.setHttpsEnabled(true);
            }
            else{
                RestServiceCaller.setHttpsEnabled(false);
            }
            service = "ScriptUtilities";

            // Hardcoded

            fileuri = endpoint + "/" + "PFREDRestService/service" + "/" +
                "OffTargetSearch" + "/" + "Check";

            URI newfileuri = new URI(fileuri);

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue + "_first";

            URI newuri = new URI(uri);

            newuri = appendUri(uri, "RunDirectory=" + runDir);
            newuri = appendUri(newuri.toString(), "SecondaryTranscriptIDs=" + secondaryIDs);
            newuri = appendUri(newuri.toString(), "PrimaryTranscriptID=" + primaryID);
            newuri = appendUri(newuri.toString(), "oligoLen=" + oligoLen);


            RestServiceResult restResult = RestServiceCaller.get(newuri.toString(), 2000);
            RestServiceResult restCheckFile;
            String pacoResult = null;

            // Get response as string
            System.out.println("Response Code: " + restResult.getResponseCode());

            // If 504, let's cheat... send empty species, REST will understand this as just ask for
            // results

            if (restResult.getResponseCode() == 504){
                System.out.println("Got code 504, let us wait and try to get the results...");

                newfileuri = appendUri(fileuri, "File=paco.txt");
                newfileuri = appendUri(newfileuri.toString(), "RunDirectory=" + runDir);

                restCheckFile = RestServiceCaller.get(newfileuri.toString(), 2000);
                pacoResult = restCheckFile.getResultString();

                while (pacoResult == null){

                    System.out.println("From paco.txt got " + pacoResult);

                    // Wait 50 seconds
                    try {
                        System.out.println("Sleeping...");
                        Thread.sleep(50000);
                        System.out.println("Woke Up!...");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    restCheckFile = RestServiceCaller.get(newfileuri.toString(), 2000);
                    pacoResult = restCheckFile.getResultString();
                }
                System.out.println("Found Nonempty paco.txt, ready to collect result from Enumeration");
                newuri = appendUri(fileuri, "File=EnumerationResult.csv");
                newuri = appendUri(newuri.toString(), "RunDirectory=" + runDir);
                restResult = RestServiceCaller.get(newuri.toString(), 2000);
                System.out.println("Response Code: " + restResult.getResponseCode());
            }

            results[0] = restResult.getResultString();

            // Get first string from enumerate_first

            uri = endpoint + "/" + "PFREDRestService/service" + "/" +
                service + "/" + pathValue + "_second";

            newuri = appendUri(uri, "RunDirectory=" + runDir);

            restResult = RestServiceCaller.get(newuri.toString(), 1000);

            // Get second string from enumerate_second
            System.out.println("Response Code: " + restResult.getResponseCode());
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
        int iend = endpoint.indexOf(":");
        if (iend != -1) {
            protocol = endpoint.substring(0 , iend);
            System.out.println("Used Protocol: " + protocol);
        }
    }
}
