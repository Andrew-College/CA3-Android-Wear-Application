package d00133579.ca3wearapplication;

import android.inputmethodservice.InputMethodService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends InputMethodService {
   private static final String
      MORSE_TAPPING_CAPABILITY_NAME = "morse_tapping";

   private String transcriptionNodeId = null;
   private GoogleApiClient mGoogleApiClient = null;
   private getNodesTask nodesTask;

   private List<Node> connections;

   @Override
   public View onCreateInputView() {
      return getLayoutInflater().inflate(R.layout.activity_main, null);
   }

   @Override
   public void onBindInput() {
      super.onBindInput();
      // System.out.println(Wearable.API);
      // System.out.println("This has been created!");
      mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
         .addApi(Wearable.API)
         .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
               //Log.d("THISISAVERYLONGMESSAGE", "onConnected: " + connectionHint);
               //System.out.println("THIS IS NOw USED: " + (mGoogleApiClient == null ? "" : ""));
               getCurrentInputConnection().commitText(String.valueOf(new Random().nextFloat()), 5);
               Toast.makeText(getBaseContext(), "Look at your wear device now!", Toast.LENGTH_LONG);

               nodesTask = new getNodesTask();
               nodesTask.execute();
               // Now you can use the Data Layer API
            }

            @Override
            public void onConnectionSuspended(int cause) {
               Log.d("THISISAVERYLONGMESSAGE", "onConnectionSuspended: " + cause);
            }
         })
         .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
               Log.d("THISISAVERYLONGMESSAGE", "onConnectionFailed: " + result);
            }
         })
         .build();

      mGoogleApiClient.connect();

      // System.out.println(" connected? " + mGoogleApiClient.isConnected());
      // System.out.println(" connecting? " + mGoogleApiClient.isConnecting());
      // System.out.println("connecting...");
   }


   private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
      Set<Node> connectedNodes = capabilityInfo.getNodes();

      transcriptionNodeId = pickBestNodeId(connectedNodes);
   }

   private String pickBestNodeId(Set<Node> nodes) {
      String bestNodeId = null;
      // Find a nearby node or pick one arbitrarily
      for (Node node : nodes) {
         if (node.isNearby()) {
            // System.out.println(node.getDisplayName());
            return node.getId();
         }
         bestNodeId = node.getId();
      }
      return bestNodeId;
   }

   private void requestTranscription(byte[] voiceData) {
      if (transcriptionNodeId != null) {
         Wearable.MessageApi.sendMessage(mGoogleApiClient, transcriptionNodeId,
            MORSE_TAPPING_CAPABILITY_NAME, voiceData).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
               if (!sendMessageResult.getStatus().isSuccess()) {
                  // Failed to send message
                  System.out.println("fail tality");
               }
            }
         })
         ;
      } else {
         // Unable to retrieve node with transcription capability
         System.out.println("heck nope");
      }
   }

   private class getNodesTask extends AsyncTask<Void, Void, List<Node>> {

      @Override
      protected List<Node> doInBackground(Void... misc) {
        //  System.out.println("in the background");
         NodeApi.GetConnectedNodesResult nodes =
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        //  System.out.println(nodes.getNodes().get(0).getDisplayName());
         return nodes.getNodes();
      }

      protected void onPostExecute(List<Node> result) {
         connections = result;
         new capabilitygatherer().execute();
         new sendTest().execute(result, null, null);
      }
   }

   private class capabilitygatherer extends AsyncTask<Void, Void, Void> {

      @Override
      protected Void doInBackground(Void... params) {
         System.out.println("gathering data");
         CapabilityApi.GetCapabilityResult resultSet =
            Wearable.CapabilityApi.getCapability(mGoogleApiClient, MORSE_TAPPING_CAPABILITY_NAME, CapabilityApi.FILTER_REACHABLE).await();
         CapabilityApi.CapabilityListener capabilityListener =
            new CapabilityApi.CapabilityListener() {
               @Override
               public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                  updateTranscriptionCapability(capabilityInfo);
               }
            };

         Wearable.CapabilityApi.addCapabilityListener(
            mGoogleApiClient,
            capabilityListener,
            MORSE_TAPPING_CAPABILITY_NAME);
         return null;
      }

      @Override
      protected void onPreExecute() {
         super.onPreExecute();
        //  Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient, CapabilityApi.FILTER_REACHABLE).setResultCallback(new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
        //     @Override
        //     public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
        //        System.out.println("before");
        //        for (Map.Entry<String, CapabilityInfo> part : getAllCapabilitiesResult.getAllCapabilities().entrySet()) {
        //           System.out.println(part.getKey());
        //        }
        //     }
        //  });
      }

      @Override
      protected void onPostExecute(Void aVoid) {
         super.onPostExecute(aVoid);
        //  PendingResult<CapabilityApi.GetAllCapabilitiesResult> idea = Wearable.CapabilityApi.getAllCapabilities(mGoogleApiClient, CapabilityApi.FILTER_REACHABLE);
        //  idea.setResultCallback(new ResultCallback<CapabilityApi.GetAllCapabilitiesResult>() {
        //     @Override
        //     public void onResult(CapabilityApi.GetAllCapabilitiesResult getAllCapabilitiesResult) {
        //        System.out.println("aftuh");
        //        for (Map.Entry<String, CapabilityInfo> part : getAllCapabilitiesResult.getAllCapabilities().entrySet()) {
        //           System.out.println(part.getKey());
        //        }
        //     }
        //  });
      }
   }

   private class sendTest extends AsyncTask<List<Node>, Void, Void> {

      @Override
      protected Void doInBackground(List<Node>... params) {
         for (List<Node> param : params) {
            if (param != null) {
               for (Node node : param) {
                  // System.out.println("have something: " + node.getDisplayName());
                  PendingResult<MessageApi.SendMessageResult> messageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                     "this is a test message from the phone", null);
                  // messageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                  //    @Override
                  //    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                  //       System.out.print("done: ");
                  //       System.out.println(sendMessageResult.getRequestId());
                  //    }
                  // });
               }
            } else {
              //  System.out.println("param is null");
            }
         }
         return null;
      }
   }
}

