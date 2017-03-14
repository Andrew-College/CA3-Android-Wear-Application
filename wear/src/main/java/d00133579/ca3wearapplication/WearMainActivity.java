package d00133579.ca3wearapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class WearMainActivity extends Activity {
   private TextView mSentenceTextView, mCodeTextView, mLetterTextView;
   private long timeout = 0l;
   private final long ITERATION_TIMEOUT = 5000;
   private StringBuilder
      currentLetter = new StringBuilder(0),
      currentWord = new StringBuilder(0),
      sentence = new StringBuilder(0);
   private boolean
      halt = false,
      stop = false,
      letterTyped = false,
      wordTyped = false,
      sentenceTyped = false;
   private String[] codes, letters;
   private HashMap<String, String> vals;
   private WatchViewStub stub;

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);


      //setup morse
      codes = getResources().getStringArray(R.array.morsecodes);
      letters = getResources().getStringArray(R.array.morseletters);
      vals = new HashMap<>();
      for (int i = 0; i < codes.length; i++) {
         vals.put(codes[i], letters[i]);
      }
      //

      stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
      stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
         @Override
         public void onLayoutInflated(WatchViewStub stub) {
            mSentenceTextView = (TextView) stub.findViewById(R.id.sentence);
            mCodeTextView = (TextView) stub.findViewById(R.id.code);
            mLetterTextView = (TextView) stub.findViewById(R.id.letter);
//            System.out.println(getParentActivityIntent().hasExtra("thismessagehere"));

//            if (getIntent().hasExtra("thismessagehere")) {
//               mSentenceTextView.setText(getIntent().getStringExtra("thismessagehere"));
//            }
            final Button morseButton = (Button) stub.findViewById(R.id.morseButton);
            morseButton.setOnTouchListener(new View.OnTouchListener() {
               //source: http://stackoverflow.com/a/9460468/2558205
               boolean mHasPerformedLongPress;
               Runnable mPendingCheckForLongPress;
               long longTimeout = 200;

               @Override
               public boolean onTouch(final View v, MotionEvent event) {

                  switch (event.getAction()) {
                     case MotionEvent.ACTION_UP:
                        Character newCharacter = ' ';
                        if (!mHasPerformedLongPress) {
                           // This is a tap, so remove the longpress check
                           if (mPendingCheckForLongPress != null) {
                              v.removeCallbacks(mPendingCheckForLongPress);
                           }
//                           System.out.println(".");
                           newCharacter = '.';
                        } else {
//                           System.out.println("-");
                           newCharacter = '-';
                        }

                        morseButton.setText(getString(R.string.tapHold));
                        morseButton.setBackgroundColor(Color.parseColor(getString(R.string.colourDefault)));
                        mHasPerformedLongPress = false;

                        morseTyped(newCharacter);

                        break;
                     case MotionEvent.ACTION_DOWN:
                        if (mPendingCheckForLongPress == null) {
                           mPendingCheckForLongPress = new Runnable() {
                              public void run() {
                                 morseButton.setText(getString(R.string.releaseTouch));
                                 morseButton.setBackgroundColor(Color.parseColor(getString(R.string.colourRelease)));
                                 mHasPerformedLongPress = true;
                                 //do your job
                              }
                           };
                        }


                        mHasPerformedLongPress = false;
                        v.postDelayed(mPendingCheckForLongPress, longTimeout);//use own timeout interval

                        break;
                     case MotionEvent.ACTION_MOVE:
                        final int x = (int) event.getX();
                        final int y = (int) event.getY();

                        // Be lenient about moving outside of buttons
                        int slop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
                        if ((x < 0 - slop) || (x >= v.getWidth() + slop) ||
                           (y < 0 - slop) || (y >= v.getHeight() + slop)) {

                           if (mPendingCheckForLongPress != null) {
                              v.removeCallbacks(mPendingCheckForLongPress);
                           }
                        }
                        break;
                     default:
                        return false;
                  }

                  return false;
               }
            });

            //initiate auto-countdown
            new timer().execute();
         }

         private Runnable resetTextRunnable = new Runnable() {
            @Override
            public void run() {
               currentLetter = new StringBuilder();
               currentWord = new StringBuilder();
               mCodeTextView.setText(getString(R.string.code));
               mLetterTextView.setText(getString(R.string.letter));
            }
         };

         private Runnable resetTextViewsRunnable = new Runnable() {
            @Override
            public void run() {

               mCodeTextView.setText(getString(R.string.code));
               mLetterTextView.setText(getString(R.string.letter));
            }
         };

         class timer extends AsyncTask<Void, Void, Void> {
            private long
               letterTimeout = 4000,
               wordTimeout = 3000;

            @Override
            protected Void doInBackground(Void... params) {
               while (true) {
                  if (stop) {
                     return null;
                  } else {
//                     timeout = System.currentTimeMillis() + ITERATION_TIMEOUT;
                     while (!stop && !halt) {

//                        System.out.println(timeout - System.currentTimeMillis());
                        if (wordTyped && (System.currentTimeMillis() + wordTimeout) - timeout >= 0) {//enter a space, currentLetter has finished
                           sentence.append(currentWord.toString() + " ");
                           runOnUiThread(resetTextRunnable);
                           System.out.println("word: " + sentence.toString());
                           wordTyped = false;
                           continue;
                        }

                        if (letterTyped && (System.currentTimeMillis() + letterTimeout) - timeout >= 0) {//enter a space, currentLetter has finished
                        //    System.out.println(currentLetter.toString());
                        //    System.out.println(vals.containsKey(currentLetter.toString()));
                           if (vals.containsKey(currentLetter.toString())) {// convert morse to text, discard otherwise
                              currentWord.append(vals.get(currentLetter.toString()));
                              // System.out.println("letter: " + currentWord.toString());
                              letterTyped = false;
                           }
                           currentLetter = new StringBuilder(0);
                           runOnUiThread(resetTextViewsRunnable);
                           continue;
                        }

                        if (sentenceTyped && System.currentTimeMillis() - timeout >= 0) {// stop waiting
                           halt = true;
                           runOnUiThread(resetTextRunnable);
                           sentenceTyped = false;
                        //    System.out.println(".......SENTENCE........");
                        //    System.out.println(sentence);
                           runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                 mSentenceTextView.setText(sentence.toString());
                              }
                           });
                           continue;
                        }
                     }
                  }
               }
            }
         }
      });

   }

   private void morseTyped(Character newCharacter) {
      timeout = System.currentTimeMillis() + ITERATION_TIMEOUT;
      if (halt) {
         halt = false;
      }
      currentLetter.append(newCharacter);
      String thisLetter = currentLetter.toString();
      if (vals.containsKey(thisLetter)) {
         mCodeTextView.setText(thisLetter);
         mLetterTextView.setText(vals.get(thisLetter));
      }
      letterTyped = true;
      wordTyped = true;
      sentenceTyped = true;
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      stop = true;
   }
}
