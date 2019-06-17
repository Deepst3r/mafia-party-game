package com.deepster.mafiaparty.game


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepster.mafiaparty.R
import com.deepster.mafiaparty.model.entities.Game
import com.deepster.mafiaparty.model.entities.Period
import com.deepster.mafiaparty.model.entities.User
import com.deepster.mafiaparty.model.itemview.UserItemView
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_lobby.*

class LobbyFragment : Fragment() {

    private lateinit var currentUser: User
    private lateinit var db: FirebaseFirestore

    private val args: LobbyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lobby, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        text_room_id.text = args.roomID
        currentUser = args.currentUser

        val adapter = GroupAdapter<ViewHolder>()
        recycler_players.adapter = adapter
        recycler_players.layoutManager = LinearLayoutManager(context)
        db = FirebaseFirestore.getInstance()

        db.collection("games").document(args.roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                adapter.clear()
                val game = snapshot.toObject(Game::class.java)

                if (game!!.period == Period.NIGHT_ONE) {
                    val role = game.players[currentUser.username]!!
                    val gameAction =
                        LobbyFragmentDirections.actionLobbyFragmentToGameFragment(args.roomID, role, currentUser)
                    findNavController().navigate(gameAction)
                }

                adapter.addAll(game.players.keys.map { playerName ->
                    UserItemView(playerName)
                })

            }

        }
    }
}
