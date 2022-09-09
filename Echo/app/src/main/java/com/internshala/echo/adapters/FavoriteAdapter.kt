package com.internshala.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.activities.MainActivity
import com.internshala.echo.fragments.MainScreenFragment
import com.internshala.echo.fragments.SongPlayingFragment

class FavoriteAdapter(_songDetails: ArrayList<Songs>, _context: Context) : RecyclerView.Adapter<FavoriteAdapter.MyviewHolder>() {
    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        var songTitleUpdate = songObject?.songTitle
        var songArtistUpdate = songObject?.artist
        if (songObject?.songTitle.equals("<unknown>", true)) {
            songTitleUpdate = "Unknown"
        }
        if (songObject?.artist.equals("<unknown>", true)) {
            songArtistUpdate = "Unknown"
        }
        holder.trackTitle?.text = songTitleUpdate
        holder.trackArtist?.text = songArtistUpdate
        holder.contentHolder?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying == true) {
                try {
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
            args.putInt("songId", songObject?.songID?.toInt() as Int)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songData", songDetails)
            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .commit()
        })


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyviewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_fav_adapter, parent, false)
        return MyviewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null) {
            return 0
        } else {

            return (songDetails as ArrayList<Songs>).size
        }

    }

    class MyviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById<TextView>(R.id.favTrackTitle)
            trackArtist = view.findViewById<TextView>(R.id.favTractArtist)
            contentHolder = view.findViewById<RelativeLayout>(R.id.favContentRow)
        }

    }
}