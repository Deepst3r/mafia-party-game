package com.deepster.mafiaparty.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.deepster.mafiaparty.R
import com.deepster.mafiaparty.shared.Game
import com.deepster.mafiaparty.shared.Role
import com.deepster.mafiaparty.shared.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var viewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(activity!!).get(GameViewModel::class.java)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener {
            // Navigate to Login if the user is not logged
            if (auth.currentUser == null) {
                val loginAction = MainFragmentDirections.actionMainFragmentToLoginFragment()
                findNavController().navigate(loginAction)
            } else {
                db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener { data ->
                    if (data.exists()) {
                        viewModel.currentUser.value = data.toObject(User::class.java)!!
                    }
                }
            }
        }


        button_create_lobby.setOnClickListener {
            // Generate a random 4 uppercase letter word as room id
            val roomID = (1..4)
                .map { i -> kotlin.random.Random.nextInt(65, 91).toChar() } // upper letter ascii
                .joinToString("")
            //todo Check if it doesn't exist

            val newGame =
                Game(
                    roomID,
                    mutableMapOf(viewModel.currentUser.value!!.username to Role.OWNER)
                ) // Create game object
            db.collection("games").document(roomID).set(newGame).addOnSuccessListener {
                viewModel.game.value = newGame
                val newGameAction = MainFragmentDirections.actionMainFragmentToLobbyFragment()
                findNavController().navigate(newGameAction) // Move to the lobby fragment
            }
        }

        button_join_lobby.setOnClickListener { button ->
            val roomID = textinput_room_id.editText!!.text.toString()

            db.collection("games").document(roomID).get().addOnSuccessListener { game ->
                // Get the joined game
                if (game.exists()) {
                    val joinedGame =
                        game.toObject(Game::class.java) // Convert the Firebase doc to Game class

                    val currentUser = viewModel.currentUser.value!!

                    // If the player is new to the lobby  - add him
                    if (!joinedGame!!.alivePlayers.containsKey(currentUser.username)) {
                        joinedGame.alivePlayers[currentUser.username] =
                            Role.PLAYER // Add the player joining the game
                        db.collection("games").document(roomID).set(joinedGame)
                    }

                    val joinAction = MainFragmentDirections.actionMainFragmentToLobbyFragment()
                    viewModel.game.value = joinedGame
                    button.findNavController().navigate(joinAction)
                }
            }
        }
    }
}


