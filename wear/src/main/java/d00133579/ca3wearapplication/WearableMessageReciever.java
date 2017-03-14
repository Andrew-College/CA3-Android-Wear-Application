package d00133579.ca3wearapplication;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Andmin on 30/04/2015.
 */
public class WearableMessageReciever extends WearableListenerService {
   private static final String START_ACTIVITY = "starter";

   @Override
   public void onMessageReceived(MessageEvent messageEvent) {
      System.out.println("started=======");
      System.out.println(messageEvent.getSourceNodeId());
      System.out.println(messageEvent.getRequestId());
      System.out.println(messageEvent.getPath());
      System.out.println(messageEvent.getData());
      System.out.println("=========================");
//      if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY ) ) {
      Intent intent = new Intent(this, WearMainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//         intent.putExtra("thismessagehere", messageEvent.getPath());
      startActivity(intent);
//      } else {
//         super.onMessageReceived( messageEvent );
//      }
   }
}
