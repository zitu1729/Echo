package com.internshala.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.EchoDatabase.EchoDatabase
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.adapters.FavoriteAdapter



class FavoriteFragment : Fragment() {
    var myActivity: Activity? = null
    var noFavorites: RelativeLayout? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null
    var visibleLayout: RelativeLayout? = null
    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null
    var currentSongHelper: CurrentSongHelper? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        activity!!.title = "Favorites"
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitlefavScreen)
        playPauseButton = view.findViewById(R.id.playPauseFavButton)
        visibleLayout = view.findViewById(R.id.favVisibleLayout)
        recyclerView = view.findViewById(R.id.favoriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivity)
        bottomBarSetup()
        display_favorites_by_searching()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentArtist = songCursor.getString(songArtist)
                var currentTitle = songCursor.getString(songTitle)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))

            }
        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()

            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                SongPlayingFragment.Staticated.onSongComplete()
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            })
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                    ?.replace(R.id.details_fragment, songPlayingFragment)
                    ?.addToBackStack("songPlayingFragmentFavorite")
                    ?.commit()
        })
        playPauseButton?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
                currentSongHelper?.isPlaying = false
            } else {
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
                currentSongHelper?.isPlaying = true
            }
        })
    }


     fun display_favorites_by_searching() {
         if (favoriteContent?.checkSize() as Int > 0) {
             refreshList = ArrayList<Songs>()
             getListFromDatabase = favoriteContent?.queryDBList()
            var fetchListfromDevice = getSongsFromPhone()
            if (fetchListfromDevice != null) {
                 for (i in 0..fetchListfromDevice?.size - 1) {
                     for (j in 0..getListFromDatabase?.size as Int - 1) {
                         if ((getListFromDatabase?.get(j)?.songID) == (fetchListfromDevice?.get(i)?.songID)) {
                             refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                         }
                     }
                 }
            }

             if (refreshList == null) {
                 visibleLayout?.visibility = View.INVISIBLE
                 noFavorites?.visibility = View.VISIBLE
             } else {
                 val favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                 val mLayoutManager = LinearLayoutManager(activity)
                 recyclerView?.layoutManager = mLayoutManager
                 recyclerView?.itemAnimator = DefaultItemAnimator()
                 recyclerView?.adapter = favoriteAdapter
                 recyclerView?.setHasFixedSize(true)
             }
         }else {
             visibleLayout?.visibility = View.INVISIBLE
             noFavorites?.visibility = View.VISIBLE
         }
     }
}
