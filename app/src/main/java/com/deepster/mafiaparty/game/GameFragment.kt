package com.deepster.mafiaparty.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.deepster.mafiaparty.R
import com.deepster.mafiaparty.model.entities.Game
import com.deepster.mafiaparty.model.entities.Role
import com.deepster.mafiaparty.model.itemview.UserItemView
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_game.*

@Suppress("NON_EXHAUSTIVE_WHEN")
class GameFragment : Fragment() {

    private lateinit var viewModel: GameViewModel
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)

        db = FirebaseFirestore.getInstance()

        val currentUser = viewModel.currentUser.value!!

        val adapter = GroupAdapter<ViewHolder>()
        recycler_players.adapter = adapter
        recycler_players.layoutManager = LinearLayoutManager(context)

        viewModel.game.observe(this, Observer { game ->
            viewModel.role.value = game.players[currentUser.username]
            text_period.text = game.period.toString()
            if (game.period.toString().contains("NIGHT")) { // Night time
                //todo Make background dark
                when (viewModel.role.value) {
                    Role.MAFIA -> {
                        text_role_help.text = resources.getString(R.string.mafia_help)
                        recycler_players.visibility = View.VISIBLE
                        button_vote.visibility = View.VISIBLE
                    }
                    Role.COP -> {
                        text_role_help.text = resources.getString(R.string.cop_help)
                        recycler_players.visibility = View.VISIBLE
                        button_vote.visibility = View.VISIBLE
                    }
                    Role.DOCTOR -> {
                        text_role_help.text = resources.getString(R.string.doctor_help)
                        recycler_players.visibility = View.VISIBLE
                        button_vote.visibility = View.VISIBLE
                    }
                    Role.CITIZEN -> {
                        text_role_help.text = resources.getString(R.string.citizen_help)
                        recycler_players.visibility = View.GONE
                        button_vote.visibility = View.GONE
                    }
                }
            } else { // Day time
                //todo Make background light

            }
            adapter.clear()
            adapter.addAll(game.players.keys.map { player -> UserItemView(player) })
        })

        val roomID = viewModel.game.value!!.roomID

        // Update game object
        db.collection("games").document(roomID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val game = snapshot.toObject(Game::class.java)!!
                viewModel.game.value = game
            }
        }


    }

}
