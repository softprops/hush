package me.lessis

import android.app.Activity
import android.content.{Context, Intent}
import android.location.{Criteria, Location, LocationListener, LocationManager}
import android.media.{AudioManager}
import android.os.Bundle
import android.widget.{Button, TextView}
import android.view.{Gravity, Menu, MenuItem, View}
import android.graphics.Typeface

class MainActivity extends Activity
     with Toasted with Serviced with Async
     with Logged with Played {

  def startPreferences = {
    startActivity(new Intent(getBaseContext, classOf[HushPreferences]))
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater().inflate(R.menu.hush_menu, menu)
    true
  }
     
  override def onOptionsItemSelected(i: MenuItem) = {
    i.getItemId match {
      case R.id.my_quite_places =>
        log.debug("my quite places selected")
        true
      case _ =>
        super.onOptionsItemSelected(i)
    }
  }

  override def onCreate(previous: Bundle) {
    super.onCreate(previous)
    
    setContentView(R.layout.hush)

    findViewById(R.id.hush_txt) match {
      case tv: TextView =>
        try {
          tv.setTypeface(Typeface.createFromAsset(getAssets, "ChunkFive-Roman.ttf"))
        } catch {
          case e => log.error("error assigning typeface %s" format e.getMessage, Some(e))
        }
    }

    val btn = findViewById(R.id.hush_btn) match {
      case btn: Button =>
         btn.setOnClickListener(new View.OnClickListener() {
           def onClick(v: View) {
             if("Quiet Please".equals(btn.getText())) {
               audios { am =>
                 btn.setText("It's okay.\nGo ahead and ring")
                 am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                 toast("ringer set to vibrate")
                 startPreferences
               }
             } else {
               audios { am =>
                 btn.setText("Quiet Please")
                 am.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                 toast("ringer set to normal")
               }
             }
           }
         })
         btn
    }

    locations { lm =>
      val lp = lm.getBestProvider(new Criteria(), true)
      log.debug("best provider is %s" format lp)
      lm.requestLocationUpdates(
        lp, 0, 0, new LocationListener {
          def onLocationChanged(l: Location) {
            async {

              playAudio(R.raw.supermario)

              val ll = (l.getLatitude, l.getLongitude)
              log.info("device location changed %s" format ll)
              audios { am =>
                (am.getRingerMode, DefaultDecider.quiet(ll, MainActivity.this)) match {
                  case (AudioManager.RINGER_MODE_NORMAL, (quiet, msg)) =>
                    if(quiet) {
                      longToast("%s\nringer set to vibrate" format msg.get)
                      am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                      btn.setText("It's okay.\nGo ahead and ring")
                    }
                  case (AudioManager.RINGER_MODE_SILENT, _) =>
                  case (AudioManager.RINGER_MODE_VIBRATE, (quiet, msg)) =>
                    if(!quiet) {
                      longToast("%s\nringer set to normal" format msg.get)
                      am.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
                      btn.setText("Quiet Please")
                    }
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
