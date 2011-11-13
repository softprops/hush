package me.lessis

import android.app.Activity
import android.content.Context
import android.location.{Criteria, Location, LocationListener, LocationManager}
import android.media.AudioManager
import android.os.Bundle
import android.widget.{Button, TextView, Toast}
import android.view.{Gravity, View}
import android.graphics.Typeface

class MainActivity extends Activity
     with Toasted with Serviced with Async
     with Logged {

  override def onCreate(previous: Bundle) {
    super.onCreate(previous)
    
    setContentView(R.layout.hush)

    findViewById(R.id.hush_txt) match {
      case tv: TextView =>
        try {
          tv.setTypeface(Typeface.createFromAsset(getAssets, "/ChunkFive.ttf"))
        } catch {
          case e => log.error("error assigning typeface %s" format e.getMessage, Some(e))
        }
    }

    findViewById(R.id.hush_btn) match {
      case btn: Button =>
         btn.setOnClickListener(new View.OnClickListener() {
           def onClick(v: View) {
             if("Quiet Please".equals(btn.getText())) {
               toast("turn it off")
               audios { am =>
                 btn.setText("It's okay, go ahead and ring")
                 am.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
               }
             } else {
               toast("turn it on")
               audios { am =>
                 btn.setText("Quiet Please")
                 am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
               }
             }
           }
         })
    }   

    locations { lm =>
      val lp = lm.getBestProvider(new Criteria(), true)
      log.debug("best provider is %s" format lp)
      lm.requestLocationUpdates(
        lp, 0, 0, new LocationListener {
          def onLocationChanged(l: Location) {
            async {
              val ll = (l.getLatitude, l.getLongitude)
              log.info("device location changed %s" format ll)
              audios { am =>
                (am.getRingerMode, DefaultDecider.quiet(ll, MainActivity.this)) match {
                  case (AudioManager.RINGER_MODE_NORMAL, (quiet, _)) =>
                    if(quiet) am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                  case (AudioManager.RINGER_MODE_SILENT, _) =>
                  case (AudioManager.RINGER_MODE_VIBRATE, (quiet, _)) =>
                    if(!quiet) am.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                }
              }
            }
          }
          def onStatusChanged(provider: String, status: Int, extras: Bundle) = 
            log.debug("%s status changed %s" format(provider, status))
          def onProviderEnabled(provider: String) =
            log.debug("%s provider enabled" format provider)
          def onProviderDisabled(provider: String) =
            log.debug("%s provider disabled" format provider)
        }
      )
    }
  }
}
